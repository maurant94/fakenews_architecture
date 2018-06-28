package it.uniroma1.dis.block;

import java.io.Serializable;
import java.util.List;

import it.uniroma1.dis.util.StringUtil;

public class FiveW implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private ItemFiveW who;
	private ItemFiveW what;
	private ItemFiveW where;
	private ItemFiveW when;
	//not using why
	private ItemFiveW why;
	//use dative element
	private ItemFiveW dative;
	
	//why can be null, otherwise refactor
	public FiveW(ItemFiveW who, ItemFiveW what, ItemFiveW where, ItemFiveW when, ItemFiveW why, ItemFiveW dative) {
		this.who = who;
		this.what = what;
		this.where = where;
		this.when = when;
		this.why = why;
		this.dative = dative;
	}
	
	public ItemFiveW getWho() {
		return who;
	}
	public void setWho(ItemFiveW who) {
		this.who = who;
	}
	public ItemFiveW getWhat() {
		return what;
	}
	public void setWhat(ItemFiveW what) {
		this.what = what;
	}
	public ItemFiveW getWhere() {
		return where;
	}
	public void setWhere(ItemFiveW where) {
		this.where = where;
	}
	public ItemFiveW getWhen() {
		return when;
	}
	public void setWhen(ItemFiveW when) {
		this.when = when;
	}
	public ItemFiveW getWhy() {
		return why;
	}
	public void setWhy(ItemFiveW why) {
		this.why = why;
	}
	public ItemFiveW getDative() {
		return dative;
	}
	public void setDative(ItemFiveW dative) {
		this.dative = dative;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dative == null) ? 0 : dative.hashCode());
		result = prime * result + ((what == null) ? 0 : what.hashCode());
		result = prime * result + ((when == null) ? 0 : when.hashCode());
		result = prime * result + ((where == null) ? 0 : where.hashCode());
		result = prime * result + ((who == null) ? 0 : who.hashCode());
		result = prime * result + ((why == null) ? 0 : why.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FiveW other = (FiveW) obj;
		if (dative == null) {
			if (other.dative != null)
				return false;
		} else if (!dative.equals(other.dative))
			return false;
		if (what == null) {
			if (other.what != null)
				return false;
		} else if (!what.equals(other.what))
			return false;
		if (when == null) {
			if (other.when != null)
				return false;
		} else if (!when.equals(other.when))
			return false;
		if (where == null) {
			if (other.where != null)
				return false;
		} else if (!where.equals(other.where))
			return false;
		if (who == null) {
			if (other.who != null)
				return false;
		} else if (!who.equals(other.who))
			return false;
		if (why == null) {
			if (other.why != null)
				return false;
		} else if (!why.equals(other.why))
			return false;
		return true;
	}
	
	public static boolean compareList(List<FiveW> first, List<FiveW> second) {
		if (first == null && second == null)
			return true;
		else if (first == null || second == null)
			return false;
		else if (first.equals(second)) 
			return true;
		else 
			return (first.size() == second.size()) && first.containsAll(second);
	}
	
	public static String calculateListHash(List<FiveW> list) {
		String tempHash = "";
		if (list != null)
			for (FiveW item: list)
				tempHash += item.hashCode();
		return StringUtil.applySha256(tempHash); //FIXME - MAYBE USE SOMETHING LIGHT
	}
	
}
