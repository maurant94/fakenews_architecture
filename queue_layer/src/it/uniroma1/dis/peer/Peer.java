package it.uniroma1.dis.peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import it.uniroma1.dis.block.Block;
import it.uniroma1.dis.block.Data;
import it.uniroma1.dis.block.FiveW;
import it.uniroma1.dis.block.QueueChain;
import it.uniroma1.dis.ethereum.SmartContractManager;
import it.uniroma1.dis.facade.CassandraFacade;
import it.uniroma1.dis.facade.FiveWExtractor;
import it.uniroma1.dis.util.StringUtil;

public class Peer {

	//singleton
	private static Peer instance;
	private JChannel channel;
	
	//key, in our case public = address [public decrypt, private encrypt]
	private PrivateKey privateKey;
	private PublicKey publicKey;
	//let us save leader Address
	private Address leaderAddress;
	
	//constant message
	private static final String ACK = "ACK_MESSAGE";
	private static final String NACK = "NACK_MESSAGE";
	private static final String DUP = "DUPLICATE_MESSAGE";
	private static final String ERR = "BYZANTINE_MISMATCH";
	//set of variables need for consensus
	private int BYZANTINE_QUORUM; // let us suppose pbft
	private int CONSENSUS_QUORUM;
	private int seq_num; //the max one
	private HashMap<Integer, TrackMessage> hystory; //SHARED IN STATE
	private HashMap<Integer, List<TrackMessage>> speculativeResponse;
	private HashMap<Integer, String> decided;
	private HashMap<Integer, Timer> timers;
	private List<Data> inProgressBlock;
	private Timer blockCreatorTimer;
	
	private QueueChain own_chain;
	
	private SmartContractManager manager;
	
	public Peer(int byzantine) throws Exception {
		System.setProperty("java.net.preferIPv4Stack", "true"); // REMEMBER OR IT DOES NOT WORK  !!!!!!!
		//INITIALIZE SMART CONTRACT MANAGER;
		manager = new SmartContractManager();
		BYZANTINE_QUORUM = 3*byzantine; //PBFT
		CONSENSUS_QUORUM = 1; //INITIAL
		own_chain = new QueueChain();
		hystory = new HashMap<>();
		decided = new HashMap<>();
		timers = new HashMap<>();
		blockCreatorTimer = null;
		inProgressBlock = new ArrayList<>();
		speculativeResponse = new HashMap<>();
		//VIEW MANAGED BY JGROUP
		seq_num = 0;
		generateKeyPair();
		channel = new JChannel();//new JChannel("resources/jgroup-config.xml");
		channel.setReceiver(new ReceiverAdapter() {
			public void receive(Message msg) {
				TrackMessage originalMsg = (TrackMessage)msg.getObject();
				TrackMessage message = TrackMessage.decrypt(originalMsg.getSecKeyAES(), originalMsg.getEncrytped_message(), originalMsg.getDecryptKey());
				TrackMessage reply = null;
				/*
				 * SEMPLIFICATION THE LEADER IS THE FIRST, MANAGED BY JGROUP
				 */
				switch (message.getType()) {
					case TrackMessage.REQUEST :
						//YOU ARE THE LEADER IF HERE
						System.out.println("REQUEST RECEIVED from " + msg.getSrc());
						seq_num++; //increase
						Data data = preparData(message.getResource_name(), message.getResurce());
						reply = TrackMessage.getOrderRequest(data, seq_num, hystory, privateKey, publicKey);
						send(reply);
						break;
					case TrackMessage.ORDER_REQUEST :
						System.out.println("ORD REQUEST RECEIVED from " + msg.getSrc());
						if (checkAndStore(originalMsg, message)) {
							String outcome = checkValidity(originalMsg.getData());
							reply = TrackMessage.getSpeculativeResponse(originalMsg.getData(), message.getSequence_number(), 
									hystory, privateKey, publicKey.toString(), outcome, publicKey);
							sendToLeader(reply);
						} else {
							System.out.println("@@@ NOT CHECK NOT STORE --- " + message.getSequence_number());
						}
						break;
					case TrackMessage.SPEC_REQUEST :
						System.out.println("SPEC REQUEST RECEIVED  from " + msg.getSrc());
						if (timers.get(message.getSequence_number()) == null) {
							timers.put(message.getSequence_number(),new Timer());
							TimerTask task = new TimerTask() {
								@Override
								public void run() {
									try {
							            //assuming it takes 3 secs to complete the task
										TrackMessage tempMsg = message; //override
							            Thread.sleep(3*1000);
							            if (timers.get(tempMsg.getSequence_number()) != null) {
							            		// OTHERWISE TIMER ALREADY STOPPED
							            		timers.get(tempMsg.getSequence_number()).cancel();
							            		timers.put(tempMsg.getSequence_number(),null);
							            		System.out.println("timer***" + tempMsg.getSequence_number());
							            		suspect(leaderAddress);
								            
							            		//create block
							            		decided.put(message.getSequence_number(), ERR);
							            		createBlock(message);
							            		//BEING A MISMATCH GO TO 1L
							            		Data dataRow = hystory.get(message.getSequence_number()).getData();
											try {
												manager.start5w(dataRow.getName(), dataRow.getHash(), StringUtil.fromObjects(dataRow.getPayload()), dataRow.getClaimAsString(), dataRow.getContent());
											} catch (Exception e) {
												e.printStackTrace();
											}
							            		TrackMessage reply = TrackMessage.getDecidedResponse(message.getSequence_number(),decided, publicKey.toString(), privateKey, publicKey);
							            		send(reply);
							            }

							        } catch (Exception e) {
							            e.printStackTrace();
							        }
								}							

							};
							timers.get(message.getSequence_number()).schedule(task, 0);
							
						}
						//YOU ARE THE LEADER
						if (speculativeResponse.get(message.getSequence_number()) == null)
							speculativeResponse.put(message.getSequence_number(), new ArrayList<>());
						speculativeResponse.get(message.getSequence_number()).add(originalMsg);
						HashMap<String, Integer> temp = new HashMap<>();
						temp.put(ACK, 0);
						temp.put(NACK, 0);
						temp.put(DUP, 0);
						for(TrackMessage t: speculativeResponse.get(message.getSequence_number()))
							temp.put(t.getOutcome_response(), temp.get(t.getOutcome_response()) +1);
						boolean consensus_reached = false;
						String outcome = null;
						if (temp.get(ACK) > BYZANTINE_QUORUM) {
							System.out.println("CONSENSUS ACK !!!");
							outcome = ACK;
							consensus_reached = true;
						}
						else if (temp.get(NACK) > BYZANTINE_QUORUM) {
							System.out.println("CONSENSUS NACK !!!");
							outcome = NACK;
							consensus_reached = true;
						}
						else if (temp.get(DUP) > BYZANTINE_QUORUM) {
							System.out.println("CONSENSUS DUP !!!");
							outcome = DUP;
							consensus_reached = true;
						}
						else if (speculativeResponse.get(message.getSequence_number()).size() > CONSENSUS_QUORUM) {
							System.out.println("BYZANTINE MISMATCH");
							outcome = ERR;
							consensus_reached = true;
						}
						
						//only leader can insert directly
						if (consensus_reached && !decided.containsKey(message.getSequence_number())) {
							//stop timer
							timers.get(message.getSequence_number()).cancel();
				            	timers.put(message.getSequence_number(),null);
				            //create block
				            	decided.put(message.getSequence_number(), outcome);
							createBlock(message);
							//LEADER ADDS TO CASSANDRA
							CassandraFacade c = new CassandraFacade();
							byte[] res = StringUtil.fromObjects(hystory.get(message.getSequence_number()).getData().getPayload());
							String hash = hystory.get(message.getSequence_number()).getData().getHash();
							if (outcome.equals(ACK))
								c.insertResource(res, hash);
							else if (outcome.equals(NACK)) { //IN THIS CASE GO TO 1L
								Data dataRow = hystory.get(message.getSequence_number()).getData();
								try {
									manager.start5w(dataRow.getName(), dataRow.getHash(), StringUtil.fromObjects(dataRow.getPayload()), dataRow.getClaimAsString(), dataRow.getContent());
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							reply = TrackMessage.getDecidedResponse(message.getSequence_number(),decided, publicKey.toString(), privateKey, publicKey);
							send(reply);
						}
						break;
					case TrackMessage.DECIDE_REQUEST :
						System.out.println("DECIDE REQUEST RECEIVED  from " + msg.getSrc());
						//leader has just created block
						if (!channel.address().equals(leaderAddress) && !decided.containsKey(message.getSequence_number())
								&& checkDecided(decided, message.getDecided())) { 
							System.err.println("HERE");
							decided = message.getDecided(); //UPDATE LOCAL
							createBlock(message);
						}	
						break;
					default: //else case
						break;
				}
			}
			public void viewAccepted(View view) {
				//new view for every peer that join, change only if leader is suspected
				leaderAddress =  view.getCoord();
				CONSENSUS_QUORUM = view.getMembers().size();
			}
			public void getState(OutputStream output) throws Exception {
				synchronized(hystory) {
			        Util.objectToStream(hystory, new DataOutputStream(output));
			    }
			}
			public void setState(InputStream input) throws Exception {
				HashMap<Integer, TrackMessage> list;
			    list=(HashMap<Integer, TrackMessage>)Util.objectFromStream(new DataInputStream(input));
			    synchronized(hystory) {
			        hystory = list;
			    }
			}
		});
		channel.connect("MyCluster");
		try {
			channel.getState(null, 100000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void minePending() {
		//TODO as thread for each res
		try {
			List<byte[]> payload = manager.getPayload("hashhashhash"); //TODO - MANAGE GLOBAL VARIABLE FOR NON ACKED RESOURCES AND PERSONAL
			List<FiveW> fivew = FiveWExtractor.getextractedList(StringUtil.toObjects(payload));
			manager.add5w("hashhashhash", fivew);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized Peer getPeer(int consensus) {
        if (instance == null) {
	        	try {
	        		instance = new Peer(consensus);
	        	}catch(Exception e) {
	        		e.printStackTrace();
	        		return null;
	        	}
        }
        return instance;
    }
	
	//key get only public (BY OTHER FOR CHECK)
	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void send(Object payload) {
		try {
			System.out.println("SENDING MSG...");
			channel.send(new Message(null, payload));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void sendToLeader(Object payload) {
		try {
			System.out.println("SENDING MSG TO LEADER...");
			channel.send(new Message(leaderAddress, payload));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			channel.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<FiveW> get5W(Byte[] resource) {
		//call to python module (FACADE)
		return FiveWExtractor.getextractedList(resource);
	}
	
	//START OPERATION
	public void start(Byte[] resource, String name) {
		TrackMessage t = TrackMessage.getRequest(resource, name, publicKey.toString(), privateKey, publicKey);
		if (t!= null)
			sendToLeader(t);
	}
	
	//PREPARE HASH FUNCT
	public Data preparData(String name, Byte[] resource) {
		List<FiveW> list = get5W(resource);
		List<Data> claim = new ArrayList<>(); //FIXME, queue not present, but used for rule of thumb
		String hash = StringUtil.applySha256(resource.toString());
		String hash5w = FiveW.calculateListHash(list);
		Data d = new Data(name, hash, claim, -1, list, hash5w, resource);
		return d;
	}
	
	private String checkValidity(Data data) {
		//CHECK 5W
		List<FiveW> temp = get5W(data.getPayload());
		if (!FiveW.compareList(data.getContent(), temp)) {
			return NACK;
		}
		
		//CHECK DUPLICATE IN DB (CASSANDRA)
		CassandraFacade cassandra = null;
		try {
			cassandra = new CassandraFacade();
		}catch(Exception e) {
			cassandra = null;
		}
		if(cassandra == null || data.getHash() == null)
			return NACK;
		else if(cassandra.existHash(data.getHash()))
			return DUP;
		
		//else NOT PRESENT ID DB, SO CHECK TRUSTINESS - LONG CHAIN (TODO)
		//String hash5W = data.getHash5W();
		//check if in 2L or 1L, (NO SENSE IN QUEUE) IF PRESENT RETURN ACK
		if (manager.isHashPresent(temp))
			return ACK;
		return NACK;
		
	}
	
	private boolean checkAndStore(TrackMessage encrypted, TrackMessage decrypted) {
		if (encrypted.getData() == null ||
				!StringUtil.applySha256(encrypted.getData().toString()).equals(decrypted.getData_digest()) ||
				decrypted.getSequence_number() != (hystory.keySet().isEmpty()?1:(Collections.max(hystory.keySet()) + 1)) ||
				!decrypted.getHistory_digest().equals(StringUtil.applySha256Map(hystory)))
			return false;
		hystory.put(decrypted.getSequence_number(), encrypted);
		return true;
	}
	
	private void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048);
	        	KeyPair keyPair = keyGen.generateKeyPair();
	        	// Set the public and private keys from the keyPair
	        	privateKey = keyPair.getPrivate();
	        	publicKey = keyPair.getPublic();
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void createBlock(TrackMessage message) {
		Data dataRow = hystory.get(message.getSequence_number()).getData();
		inProgressBlock.add(dataRow);
		Block block;
		if (inProgressBlock.size() == QueueChain.BLOCK_LEN) {
			if (!own_chain.getBlockchain().isEmpty())
				block = new Block(inProgressBlock, own_chain.getBlockchain().get(own_chain.getBlockchain().size() -1).getHash());
			else block = new Block(inProgressBlock, null);
			if (own_chain.addBlock(block))
				System.out.println("***Block created***" + own_chain.getBlockchain().size() + "***" + message.getType());

			if (blockCreatorTimer != null) {
				blockCreatorTimer.cancel();
	            blockCreatorTimer = null;
			}
			inProgressBlock = new ArrayList<>();
			
		} else if (blockCreatorTimer == null) {
			blockCreatorTimer = new Timer();
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					try {
			            Thread.sleep(QueueChain.BLOCK_TIMER_MILLIS);
			            
			            System.out.println("+++timer creation***");
			            //if wake up after block creation
						if (blockCreatorTimer != null) {
							blockCreatorTimer.cancel();
				            blockCreatorTimer = null;
				            //END TIMER SO CREATE BLOCK
				            Block block;
							if (!own_chain.getBlockchain().isEmpty())
								block = new Block(inProgressBlock, own_chain.getBlockchain().get(own_chain.getBlockchain().size() -1).getHash());
							else block = new Block(inProgressBlock, null);
							if (own_chain.addBlock(block))
								System.out.println("***Block created***" + own_chain.getBlockchain().size() + "***" + message.getType());

							inProgressBlock = new ArrayList<>();
							//FIXME SEND A MESSAGE CREATE ? 
						}
			        } catch (Exception e) {
			            e.printStackTrace();
			        }
				}							

			};
			blockCreatorTimer.schedule(task, 0);
		}
		//AFTER CREATING A BLOCK SAVE TO CASSANDRA 
	}
	
	public boolean checkDecided(HashMap<Integer, String> leaderOne, HashMap<Integer, String> peerOne) {
		//JUST ONE ELEMENT DIFFERENT, THE LAST
		int error = 1;
		if (peerOne.isEmpty()) return true;
		for (Integer i : leaderOne.keySet()) {
			if (!peerOne.containsKey(i) && !peerOne.get(i).equals(leaderOne.get(i)))
				error--;
			if (error < 0) return false;
		}
			
		return true;
	}
	
	
	
}
