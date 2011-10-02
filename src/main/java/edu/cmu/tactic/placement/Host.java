package edu.cmu.tactic.placement;

import java.util.ArrayList;
import java.util.List;

public class Host extends Entity {
	List<VirtualMachine> tenants;
	double load;
	
	public Host(String name) {
		super(name);
		tenants = new ArrayList<VirtualMachine>();
	}
	
	public void add(VirtualMachine vm) {
		tenants.add(vm);
	}
	
	/**
	 * This should return the component's coarrival probability had it been placed on this host
	 * 
	 * @param component
	 * @return
	 */
	public double getCoarrival(Component component) {
		List<Component> components = new ArrayList<Component>();
		for (VirtualMachine vm:tenants) {
			components.addAll(vm.getTenants());
		}
		return Component.getCoarrival(component, components);
	}
}
