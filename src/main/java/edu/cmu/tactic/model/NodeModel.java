package edu.cmu.tactic.model;

public class NodeModel {
	TransferFunction transfer;
	double cutoff;
	
	DiscreteProbDensity pdf;
	ParametricDensity outputResponse;
	double[] param;
	double rawCount;
	
	public NodeModel() {}
	
	public NodeModel(DiscreteProbDensity pdf, double[] param, double rawCount) {
		this.pdf = pdf;
		this.param = param;
		this.rawCount = rawCount;
	}
	
	public NodeModel(ParametricDensity outputResponse) {
		this.outputResponse = outputResponse;
	}
}
