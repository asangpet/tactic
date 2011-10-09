package edu.cmu.tactic.placement;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.tactic.model.Entity;

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
}
