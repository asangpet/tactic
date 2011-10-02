package edu.cmu.tactic.placement;

import java.util.LinkedHashMap;

/**
 * Service contains a set of dependent components
 * 
 * @author asangpet
 *
 */
public class Service extends Entity {
	LinkedHashMap<String,Component> components;
	
	public Service(String name) {
		super(name);
		components = new LinkedHashMap<String,Component>();
	}
	
	public Service add(Component component) {
		components.put(component.getName(), component);
		return this;
	}
	
	public Component getComponent(String name) {
		return components.get(name);
	}
	
	/**
	 * This should return the given component impact to the service
	 * 
	 * @param component
	 * @return
	 */
	public double getImpact(Component component) {
		return 0.5;
	}
}
