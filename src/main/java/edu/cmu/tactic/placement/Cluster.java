package edu.cmu.tactic.placement;

import org.codehaus.jackson.annotate.JsonProperty;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

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
	ListMultimap<Host, VirtualMachine> mapping;
	
	public Cluster(String name) {
		super(name);
		hosts = new LinkedHashMap<String, Host>();
		services = new LinkedHashMap<String, Service>();
		vms = new LinkedHashMap<String, VirtualMachine>();
		mapping = LinkedListMultimap.create(hosts.size());
	}
	
	public Cluster add(Service service) {
		services.put(service.getName(),service);
		return this;
	}
	
	public Cluster add(Host host) {
		hosts.put(host.getName(), host);
		return this;
	}
	
	public Cluster add(VirtualMachine vm) {
		vms.put(vm.getName(), vm);
		return this;
	}
	
	public ListMultimap<Host, VirtualMachine> getMapping() {
		return mapping;
	}
	
	public abstract void place();
	
}
