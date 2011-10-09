package edu.cmu.tactic.model;

import java.util.ArrayList;
import java.util.List;


/**
 * A component belongs to a service and could be hosted on different virtual machines
 * 
 * @author asangpet
 *
 */
public class Component extends Entity {
	double coarrival;
	double impact;
	List<Dependency> dependencies = new ArrayList<Dependency>();
	
	@Deprecated String[] instances;
	
	public Component(String name) {
		super(name);
	}
	
	@Override
	public String toString() {
		String result = name+" = { "+name;
		
		if (instances.length > 0) { result+= ", instances: "+instances; }
		if (dependencies.size() > 0) { result+= ", dep:"+dependencies; }
		return result+" }";				
	}
	
	public void setCoarrival(double coarrival) {
		this.coarrival = coarrival;
	}
	
	public void setImpact(double impact) {
		this.impact = impact;
	}
	
	public double getCoarrival() {
		return coarrival;
	}
	public double getImpact() {
		return impact;
	}
}
