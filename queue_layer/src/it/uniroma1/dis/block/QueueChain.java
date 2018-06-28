package it.uniroma1.dis.block;

import java.util.ArrayList;
import java.util.List;

public class QueueChain {
	
	private List<Block> blockchain;

	public static final int BLOCK_LEN = 3;
	public static final int BLOCK_TIMER_MILLIS = 10000;
	
	public QueueChain() {
		blockchain = new ArrayList<>();
	}
	
	public boolean isChainValid() {
		if (blockchain.isEmpty()) return true;
		
		Block currentBlock; 
		Block previousBlock;
		
		//loop through blockchain to check hashes:
		for(int i=1; i < blockchain.size(); i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//compare registered hash and calculated hash:
			if(!currentBlock.getHash().equals(currentBlock.calculateHash()) ){
				System.out.println("Current Hashes not equal");			
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.getHash().equals(currentBlock.getPrevHash()) ) {
				System.out.println("Previous Hashes not equal");
				return false;
			}
		}
		return true;
	}
	private boolean isBlockInsertable(Block currentBlock) {
		if (blockchain.isEmpty()) return true;
		Block lastBlock = blockchain.get(blockchain.size() -1);

		//compare registered hash and calculated hash:
		if(!currentBlock.getHash().equals(currentBlock.calculateHash()) ){
			System.out.println("Current Hashes not equal");			
			return false;
		}
		//compare previous hash and registered previous hash
		if(!lastBlock.getHash().equals(currentBlock.getPrevHash()) ) {
			System.out.println("Previous Hashes not equal");
			return false;
		}

		return true;
	}

	public boolean addBlock(Block block) {
		if (isBlockInsertable(block)) {
			blockchain.add(block);
			if (isChainValid())
				return true;
			else
				blockchain.remove(blockchain.size() -1);
		}
		return false;
	}
	
	public List<Block> getBlockchain(){
		return blockchain;
	}
	
	
}
