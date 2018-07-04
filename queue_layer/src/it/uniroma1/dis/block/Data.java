package it.uniroma1.dis.block;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Data implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String name;
	private String hash;
	private List<Data> claim;
	private double trustiness;
	private List<FiveW> content;
	private String hash5W;
	
	//optional just before saving resource
	private Byte[] payload;
	
	//some parameters have to be diìynamiccaly checked
	public Data(String name, String hash, List<Data> claim, double trustiness, List<FiveW> content, String hash5w, Byte[] resource) {
		this.name = name;
		this.hash = hash;
		this.claim = claim;
		this.trustiness = trustiness;
		this.content = content;
		this.hash5W = hash5w;
		this.payload = resource;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public List<Data> getClaim() {
		return claim;
	}

	public void setClaim(List<Data> claim) {
		this.claim = claim;
	}

	public double getTrustiness() {
		return trustiness;
	}

	public void setTrustiness(double trustiness) {
		this.trustiness = trustiness;
	}

	public List<FiveW> getContent() {
		return content;
	}

	public void setContent(List<FiveW> content) {
		this.content = content;
	}

	public String getHash5W() {
		return hash5W;
	}

	public void setHash5W(String hash5w) {
		hash5W = hash5w;
	}

	public Byte[] getPayload() {
		return payload;
	}

	public void setPayload(Byte[] payload) {
		this.payload = payload;
	}
	
	public String getClaimAsString() {
		String claims = "";
		for (Data d : claim) {
			claims += d.getHash() + "-";
		}
		return claims;
	}

	@Override
	public String toString() {
		return "Data [name=" + name + ", hash=" + hash + ", claim=" + claim + ", trustiness=" + trustiness
				+ ", content=" + Arrays.toString(payload) + ", hash5W=" + hash5W + ", payload=" + Arrays.toString(payload) + "]";
	}
	
	

	
}
