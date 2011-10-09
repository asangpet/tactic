package edu.cmu.tactic.placement;

import java.util.LinkedHashMap;

import edu.cmu.tactic.model.Component;
import edu.cmu.tactic.model.Entity;
import edu.cmu.tactic.model.Service;

/**
 * This class monitors the running components in the system for co-arrival requests
 * as well as the component's lifecycle (status / start / stop / heartbeat)
 * @author asangpet
 *
 */
public class ComponentMonitor extends Entity {
	LinkedHashMap<String,Component> components;
	
	public ComponentMonitor(String name) {
		super(name);
		components = new LinkedHashMap<String, Component>();
	}
	
	public ComponentMonitor add(Component component) {
		components.put(component.getName(), component);
		return this;
	}
	
	public ComponentMonitor add(Service service) {
		for (Component comp:service.getComponents()) {
			components.put(comp.getName(), comp);
		}
		return this;
	}
	
	/**
	 * This method should estimate the co-arrival probability of the primary component
	 * with respect to all the given components
	 * 
	 * @param primary
	 * @param components
	 * @return
	 */
	public double getCoarrival(Component component) {
		return 0.5;
	}
	
	public double getImpact(Component component) {
		return 0.5;
	}
	
	public void findCoarrival() {
		for (Component comp:components.values()) {
			comp.setCoarrival(0.5);
		}
	}
	
	public void findImpact() {
		for (Component comp:components.values()) {
			comp.setImpact(0.1);
		}		
	}
}
