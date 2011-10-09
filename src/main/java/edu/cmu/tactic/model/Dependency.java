package edu.cmu.tactic.model;

import java.util.List;


public class Dependency extends Entity {
	@Deprecated
	boolean match = false;	// Indicate whether we should match the list instance with the given dependencies
	List<Component> components;	
	
	public Double distProb;
	
	public Dependency() {
		super("dep");
	}
	
	public String toString() {
		String result = name+" = { ";

		if (match) { result+= ", match"; }
		if (components.size() > 0) { result+= ", instances: ${instances}"; }
		return result+" }";
	}
}
