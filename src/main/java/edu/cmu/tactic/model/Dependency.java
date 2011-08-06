package edu.cmu.tactic.model;

public class Dependency extends Service {
	boolean match = false;
	public Double distProb;
	
	public String toString() {
		String result = name+" = { "+label;

		if (match) { result+= ", match"; }
		if (instances.length > 0) { result+= ", instances: ${instances}"; }
		if (dependencies.size() > 0) { result+= ", dep:${dependencies}"; }
		return result+" }";
	}
}
