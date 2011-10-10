package edu.cmu.tactic.model;

public class AsynchronousDependency extends Dependency {
	public AsynchronousDependency(Component initiator, Component upstream) {
		super("async",initiator,upstream);
	}
}
