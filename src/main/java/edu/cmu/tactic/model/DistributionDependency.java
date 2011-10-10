package edu.cmu.tactic.model;

public class DistributionDependency extends Dependency{
	DiscreteProbDensity distProb = null; 
	
	public DistributionDependency(Component initiator, Component upstream) {
		super("dist",initiator,upstream);
	}
}
