package it.uniroma1.dis.peer;

import java.util.List;

import it.uniroma1.dis.block.Data;
import it.uniroma1.dis.block.FiveW;
import it.uniroma1.dis.ethereum.NewsBean;
import it.uniroma1.dis.ethereum.SmartContractManager;
import it.uniroma1.dis.facade.CassandraFacade;
import it.uniroma1.dis.facade.FiveWExtractor;
import it.uniroma1.dis.util.StringUtil;

public class TrustedPeer {
	
	private SmartContractManager manager;
	private Integer lastIndexVoted = 0; //FOR VOTING
	private Integer lastIndexRetrieved1L = 0; //for getting from 1L
	private final Integer checkpointNum = 100;
	private final Integer checkpointQueue = 80;
	private Integer checkpointVal = 0;
	
	public TrustedPeer() throws Exception {
		manager = new SmartContractManager();
	}
	
	public void checkpoint() throws Exception { //SUPPOSE 80% QUEUE, 20% SMART CONTRACT, UP TO CURRENT AVAILABILITY
		try {
			//GET DATA FROM QUEUE
			Peer p = new Peer(1); //FIXME - LET'S SIMULATE CALL
			List<Data> queueList =  p.getChainValues();
			for (Data data: queueList) {
				if (checkpointVal == checkpointQueue) break;
				manager.addNewsToCheck(data.getName(), data.getHash(), data.getTrustiness(), data.getClaimAsString());
				checkpointVal++;
			}
			
			//DATA FROM 1L
			Integer len = manager.getNewsLen();
			NewsBean bean = null;
			if (len != null && len != -1) {
				for (int i = lastIndexRetrieved1L; i < len; i++) {
					if (checkpointVal == checkpointQueue) break;
					bean = manager.getNewsAboveTreshold(i);
					manager.addNewsToCheck(bean);
					checkpointVal++;
				}
				lastIndexRetrieved1L = len -1;
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
				for (int i = lastIndexVoted; i < len; i++) {
					String hash = manager.getNewsToCheck2L(i);
					if (hash != null) {
						if (Math.random() < 0.5) //RANDOM VOTE
							manager.vote(SmartContractManager.ACK_VOTE, hash);
						else
							manager.vote(SmartContractManager.NACK_VOTE, hash);
					}
					
				}
				lastIndexVoted = len -1; //UPDATE LAST
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}