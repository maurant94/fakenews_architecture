package it.uniroma1.dis.peer;

import java.util.List;

import it.uniroma1.dis.block.Data;
import it.uniroma1.dis.block.FiveW;
import it.uniroma1.dis.ethereum.SmartContractManager;
import it.uniroma1.dis.facade.CassandraFacade;
import it.uniroma1.dis.facade.FiveWExtractor;
import it.uniroma1.dis.util.StringUtil;

public class TrustedPeer {
	
	private SmartContractManager manager;
	private Integer lastIndex = 0;
	
	public TrustedPeer() throws Exception {
		manager = new SmartContractManager();
	}
	
	public void checkpoint() throws Exception {
		try {
			//GET DATA FROM QUEUE
			Peer p = new Peer(1); //LET'S SIMULATE CALL
			List<Data> queueList =  p.getChainValues();
			//TODO DATA FROM 1L DONE DIRECTLY IN SOLIDITY ?
			for (Data data: queueList) {
				manager.addNewsToCheck(data.getName(), data.getHash(), data.getTrustiness(), data.getClaimAsString(), data.getContent());
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	public void validate() throws Exception {
		//SIMULATE AS A RANDOM BLACK BOX, VALIDATING RETIEVED NEWS
		try {
			Integer len = manager.getNewsLen();
			if (len != null && len != -1) {
				for (int i = lastIndex; i < len; i++) {
					String hash = manager.getNewsToCheck2L(i);
					if (hash != null) {
						if (Math.random() < 0.5) //RANDOM VOTE
							manager.vote(SmartContractManager.ACK_VOTE, hash);
						else
							manager.vote(SmartContractManager.NACK_VOTE, hash);
					}
					
				}
				lastIndex = len -1; //UPDATE LAST
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}