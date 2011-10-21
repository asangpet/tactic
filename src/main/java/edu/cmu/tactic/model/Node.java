package edu.cmu.tactic.model;

public class Node {
	String name;
	int id,vid;
	Component tier;
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
	
	public Node(String name, int id, Component tier, boolean mark) {
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
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public ParametricDensity getAnalysisResponse() {
		return analysisResponse;
	}
	
	public DiscreteProbDensity getServerResponse() {
		return serverResponse;
	}
	
	public NodeModel getModel() {
		return model;
	}
	
	public String getName() {
		return name;
	}
}
