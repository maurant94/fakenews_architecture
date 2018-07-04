package it.uniroma1.dis.block;

import java.io.Serializable;

public class ItemFiveW implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	private Double accuracy;
	
	public ItemFiveW(String name, double accuracy) {
		this.name = name;
		this.accuracy = accuracy;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(Double accuracy) {
		this.accuracy = accuracy;
	}

	@Override
	public String toString() {
		return "ItemFiveW [name=" + name + ", accuracy=" + accuracy + "]";
	}
	
	

}
