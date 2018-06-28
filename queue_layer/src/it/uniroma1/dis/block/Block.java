package it.uniroma1.dis.block;

import java.util.Date;
import java.util.List;

import it.uniroma1.dis.util.StringUtil;

public class Block {

	private List<Data> data;
	private String hash;
	private String prevHash;
	private long timestamp;

	public Block(List<Data> data, String prevhash) {
		this.data = data;
		this.prevHash = prevhash;
		//current time
		this.timestamp = new Date().getTime();
		//calculate hash
		this.hash = calculateHash();
	}

	public List<Data> getData() {
		return data;
	}

	public void setData(List<Data> data) {
		this.data = data;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getPrevHash() {
		return prevHash;
	}

	public void setPrevHash(String prevHash) {
		this.prevHash = prevHash;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String calculateHash() {
		String calculatedhash = StringUtil.applySha256( 
			prevHash==null?"":prevHash +
			Long.toString(timestamp) +
			data 
		);
		return calculatedhash;
	}
	
}
