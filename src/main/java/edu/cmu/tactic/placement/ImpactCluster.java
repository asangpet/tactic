package edu.cmu.tactic.placement;

import java.util.Comparator;
import java.util.PriorityQueue;

import javax.inject.Inject;

import org.slf4j.Logger;

public class ImpactCluster extends Cluster {
	@Inject Logger log;
	
	public ImpactCluster(String name) {
		super(name);		
	}
	
	public void place() {
		// Best-fit impact placement
		
		// Find average component co-arrival
		for (Service service:services.values()) {
			for (Component comp:service.components.values()) {
				comp.coarrival = 0.5;
			}
		}
		
		// Find average normalized impact
		double testImpact = 0.1;
		for (Service service:services.values()) {
			for (Component comp:service.components.values()) {
				comp.impact = testImpact;
				testImpact += 0.1;
			}
		}		
		
		// Sort by, for each VM, sum (co * impact) for all component hosted inside
		Comparator<VirtualMachine> comparator = new Comparator<VirtualMachine>() {
			@Override
			public int compare(VirtualMachine o1, VirtualMachine o2) {
				if (o2.score < o1.score) return -1; else return 1; 
			}
		};
		
		// Calculate each VM score
		PriorityQueue<VirtualMachine> vmQueue = new PriorityQueue<VirtualMachine>(vms.size(), comparator);
		for (VirtualMachine vm:vms.values()) {
			vm.score = 0;
			for (Component comp:vm.tenants.values()) {
				vm.score += comp.impact * comp.coarrival;
			}
			vmQueue.add(vm);
		}
		
		// Clean up host tenants
		for (Host host:hosts.values()) {
			host.load = 0;
		}
		
		// Greedily load balancing the host based on vm score
		Comparator<Host> hostComparator = new Comparator<Host>() {
			@Override
			public int compare(Host o1, Host o2) {
				if (o1.load < o2.load) return -1; else return 1;
			}
		};
		
		PriorityQueue<Host> availableHost = new PriorityQueue<Host>(hosts.size(),hostComparator);	
		for (Host host:hosts.values()) {
			availableHost.add(host);
		}

		while (vmQueue.peek() != null) {
			VirtualMachine vm = vmQueue.poll();
			Host host = availableHost.poll();
			
			log.debug("Put vm {}/{} on host {}/{}", new Object[] { vm.name, vm.score, host.name, host.load });

			host.load += vm.score;
			host.add(vm);
			availableHost.add(host);			
			
			mapping.put(host, vm);
		}
	}
	
}
