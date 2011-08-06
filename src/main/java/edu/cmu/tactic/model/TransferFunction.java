package edu.cmu.tactic.model;

import java.util.List;

public class TransferFunction {
	DiscreteProbDensity inputPdf;
	DiscreteProbDensity outputPdf;
	
	DiscreteProbDensity nonparamPdf;
	DiscreteProbDensity editedNonparamPdf;
	
	DiscreteProbDensity pdf;
	
	double[] param;
	
	List<double[]> linkparam;
	
	public TransferFunction(double[] param, DiscreteProbDensity nonparamPdf) {
		this.param = param;
		this.nonparamPdf = nonparamPdf;
	}
	
	public TransferFunction(double[] param) {
		this(param,null);
	}
	
	public TransferFunction(double[] param, List<double[]> linkparam, DiscreteProbDensity nonparamPdf) {
		this.param = param;
		this.linkparam = linkparam;
		this.nonparamPdf = nonparamPdf;
	}
}
