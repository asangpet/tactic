package edu.cmu.tactic.services.template;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;

import edu.cmu.tactic.model.AnalysisGraph;
import edu.cmu.tactic.model.MatlabUtility;
import edu.cmu.tactic.placement.Host;
import edu.cmu.tactic.placement.VirtualMachine;

public abstract class AnalysisInstance {
	protected Logger log;	
	protected MatlabUtility matlab;
	
	public void setLog(Logger log) {
		this.log = log;
	}
	
	public void setMatlab(MatlabUtility matlab) {
		this.matlab = matlab;
	}
	
	public abstract AnalysisGraph getAnalysisGraph();
	public abstract Map<String, double[]> analyze();
	public abstract Map<Host,Collection<VirtualMachine>> calculatePlacement();
}
