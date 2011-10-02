package edu.cmu.tactic.placement;

import java.util.Collection;

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
	
	/**
	 * This method should estimate the co-arrival probability of the primary component
	 * with respect to all the given components
	 * 
	 * @param primary
	 * @param components
	 * @return
	 */
	public static double getCoarrival(Component primary, Collection<Component> components) {
		return 0.5;
	}
}
