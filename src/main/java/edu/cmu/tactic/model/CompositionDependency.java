package edu.cmu.tactic.model;

public class CompositionDependency extends Dependency{
	public CompositionDependency(Component initiator, Component upstream) {
		super("comp",initiator,upstream);
	}
}
