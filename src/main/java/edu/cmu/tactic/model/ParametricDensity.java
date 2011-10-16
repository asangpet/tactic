package edu.cmu.tactic.model;

public class ParametricDensity {
	DiscreteProbDensity pdf;
	double[] param;
	double[] inputparam;
	double rawCount;
	
	public ParametricDensity() {	
	}
	
	public ParametricDensity(NodeModel model) {
		this.pdf = new DiscreteProbDensity(model.pdf);
		this.param = model.param.clone();
		this.rawCount = model.rawCount;		
	}
	
	public ParametricDensity(DiscreteProbDensity density, double[] param) {
		this(density,param, 0);
	}
	
	public ParametricDensity(DiscreteProbDensity density, double[] param, double rawCount) {
		this.pdf = density;
		this.param = param;
		this.rawCount = rawCount;
	}
	
	public ParametricDensity(DiscreteProbDensity pdf, double[] param, double[] inputparam) {
		this.pdf = pdf;
		this.param = param;
		this.inputparam = inputparam;
	}
	
	public ParametricDensity(DiscreteProbDensity density, double rawCount) {
		this(density,null,rawCount);
	}
	
	public ParametricDensity(DiscreteProbDensity pdf) {
		this(pdf, null, 0);
	}
	
	public ParametricDensity(ParametricDensity d) {
		this.pdf = new DiscreteProbDensity(d.pdf);
		this.param = d.param;
	}
	
	public DiscreteProbDensity getPdf() {
		return pdf;
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (param != null) {
			for (int i=0;i<param.length;i++) {
				buf.append(param[i]+",");
			}
		}
		StringBuffer inbuf = new StringBuffer();
		if (inputparam!= null) {
			for (int i=0;i<inputparam.length;i++) {
				inbuf.append(inputparam[i]+",");
			}
		}
		return "ParametricDensity - count {"+rawCount+"} - param {"+buf.toString()+"} - input - {"+inbuf+"}";		
	}
}
