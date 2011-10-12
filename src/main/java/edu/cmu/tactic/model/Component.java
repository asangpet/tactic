package edu.cmu.tactic.model;

/**
 * A component belongs to a service and could be hosted on different virtual machines
 * 
 * @author asangpet
 *
 */
public class Component extends Entity {
	double coarrival;
	double impact;
	
	public Component(String name) {
		super(name);
	}
	
	@Override
	public String toString() {
		String result = "{ "+name+" }";
		return result;				
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
