package it.uniroma1.dis.ethereum;

import java.math.BigInteger;

public class NewsBean {
	
	private String hash;
	private String claims; //as concatenated strings
	private String name;
	private BigInteger trustiness;
	
	public NewsBean(String hash, String claims, String name, BigInteger trustiness) {
		this.hash = hash;
		this.claims = claims;
		this.name = name;
		this.trustiness = trustiness;
	}
	public NewsBean() {}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getClaims() {
		return claims;
	}
	public void setClaims(String claims) {
		this.claims = claims;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public BigInteger getTrustiness() {
		return trustiness;
	}
	public void setTrustiness(BigInteger trustiness) {
		this.trustiness = trustiness;
	}
	
	public boolean isEmpty() { //CLAIM MAY BE NULL
		if (this.hash == null ||
				this.name == null ||
				this.trustiness == null)
			return true;
		return false;
	}

}
