package edu.cmu.tactic.model;

public class Node {
	String name;
	int id,vid;
	Service tier;
	boolean mark,edited,transferEdited;
	
	Double x = null;
	Double y = null;
	
	DiscreteProbDensity serverResponse = null;
	ParametricDensity analysisResponse = null;
	
	NodeModel model = null;
	DiscreteProbDensity modelpdf = null;
	Object modelinput = null;
	Object modeloutput = null;
	int requestCount = 1;
	Object transferFunction = null;
	Double shiftValue = null;
	Subgraph parents = null;
	
	public Node(String name, int id, Service tier, boolean mark) {
		this.name = name;
		this.id = id;
		this.tier = tier;
		this.mark = mark;
	}
	
	@Override
	public boolean equals(Object another) {
		return (another instanceof Node) && (name.equals(((Node)another).name));
	}
	
	@Override
	public String toString() {
		return name;
	}
}
