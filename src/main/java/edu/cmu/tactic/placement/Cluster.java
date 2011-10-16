package edu.cmu.tactic.placement;

import org.codehaus.jackson.annotate.JsonProperty;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import edu.cmu.tactic.model.Entity;
import edu.cmu.tactic.model.Service;

import java.util.LinkedHashMap;

/**
 * A cluster consists of multiple hosts
 * 
 * @author asangpet
 *
 */
public abstract class Cluster extends Entity {
	@JsonProperty LinkedHashMap<String,Host> hosts;
	@JsonProperty LinkedHashMap<String,Service> services;
	@JsonProperty LinkedHashMap<String,VirtualMachine> vms;
	ComponentMonitor componentMonitor;
	ListMultimap<Host, VirtualMachine> mapping;
	
	public Cluster(String name) {
		super(name);
		hosts = new LinkedHashMap<String, Host>();
		services = new LinkedHashMap<String, Service>();
		vms = new LinkedHashMap<String, VirtualMachine>();
		mapping = LinkedListMultimap.create(hosts.size());
		
		componentMonitor = new ComponentMonitor(name+"-component-monitor");
	}
	
	public Cluster add(Service service) {
		services.put(service.getName(),service);
		componentMonitor.add(service);
		return this;
	}
	
	public Cluster add(Host host) {
		hosts.put(host.getName(), host);
		return this;
	}
	
	public Cluster addHost(String name) {
		Host host = new Host(name);
		return add(host);
	}
	
	public Cluster add(VirtualMachine vm) {
		vms.put(vm.getName(), vm);
		return this;
	}
	
	public Cluster addVm(String vmName, Service service, String... components) {
		VirtualMachine vm = new VirtualMachine(vmName);
		for (String comp:components) {
			vm.add(service.getComponent(comp));
		}
		return this.add(vm);
	}
	
	public ListMultimap<Host, VirtualMachine> getMapping() {
		return mapping;
	}
	
	public Service getService(String serviceName) {
		return services.get(serviceName);
	}
	
	public abstract void place();
	
}
