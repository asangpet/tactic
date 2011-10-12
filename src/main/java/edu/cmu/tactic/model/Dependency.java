package edu.cmu.tactic.model;

public class Dependency extends Entity {
	Component upstream;
	Component initiator;
	
	public Double distProb;
	
	public Dependency(String type,Component initiator, Component upstream) {
		super(type+"-"+initiator.getName()+"-"+upstream.getName());
		this.upstream = upstream;
		this.initiator = initiator;
	}
	
	public String toString() {
		String result = name+" = { "+initiator+"->"+upstream+" }";
		return result;
	}
}
