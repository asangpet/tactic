package edu.cmu.tactic.scenario;

import org.springframework.stereotype.Component;

import edu.cmu.tactic.placement.ImpactCluster;

@Component
public class SimpleImpactCluster extends ImpactCluster {
	public SimpleImpactCluster() {
		super("impact");
	}
}
