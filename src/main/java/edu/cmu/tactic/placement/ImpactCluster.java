package edu.cmu.tactic.placement;

import java.util.Comparator;
import java.util.PriorityQueue;

import javax.inject.Inject;

import org.slf4j.Logger;

import edu.cmu.tactic.model.Component;

public class ImpactCluster extends Cluster {
	public ImpactCluster(String name) {
		super(name);		
	}
	
	public void place() {
		// Best-fit impact placement
		// reset map
		mapping.clear();
		
		// Find average component co-arrival
		componentMonitor.findCoarrival();
		
		// Find average normalized impact
		componentMonitor.findImpact();
		
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
				vm.score += comp.getImpact() * comp.getCoarrival();
			}
			vmQueue.add(vm);
		}
		
		// Clean up host tenants
		for (Host host:hosts.values()) {
			host.reset();
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
			
			log.debug("Put vm {}/{} on host {}/{}", new Object[] { vm.getName(), vm.score, host.getName(), host.load });

			host.load += vm.score;
			host.add(vm);
			availableHost.add(host);			
			
			mapping.put(host, vm);
		}
	}

	public void placeRandom() {
		mapping.clear();
		
		Host[] hostArray = new Host[hosts.size()];
		hostArray = hosts.values().toArray(hostArray);
		for (Host host:hosts.values()) {
			host.reset();
		}
			
		for (VirtualMachine vm:vms.values()) {
			int pick = (int)Math.round(Math.random()*(hostArray.length-1));
			mapping.put(hostArray[pick], vm);
			hostArray[pick].add(vm);
		}
	}
	
	
}
