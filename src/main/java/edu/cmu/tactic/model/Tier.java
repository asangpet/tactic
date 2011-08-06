package edu.cmu.tactic.model;

public class Tier {
	String name;
	
	String[] instances;
	Dependency[] dependencies;
	
	public Tier(String name) {
		this.name = name;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Tier) && ((Tier)obj).name.equals(name);
	}
	
	public String toString() {
		return name;
	}
}
