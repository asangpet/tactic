package edu.cmu.tactic.placement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import edu.cmu.tactic.model.Component;
import edu.cmu.tactic.model.Entity;
import edu.cmu.tactic.model.Service;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
	@Inject Logger log;

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
	
	public void setLog(Logger log) {
		this.log = log;
	}

	public abstract void place();
	
	public Map<Service, Double> evaluate() {
		Map<Component, Service> serviceMap = new LinkedHashMap<Component,Service>();
		Map<Component, Double> expectedImpact = new LinkedHashMap<Component, Double>();
		Map<Service, Double> serviceImpact = new LinkedHashMap<Service, Double>();
		for (Service svc:services.values()) {
			for (Component comp:svc.getComponents()) {
				serviceMap.put(comp, svc);
			}
		}
		
		for (Host host:hosts.values()) {
			List<Component> components = new LinkedList<Component>();
			for (VirtualMachine vm:host.tenants) {
				components.addAll(vm.getTenants());
			}
			
			for (Component comp:components) {
				log.debug("serviceMap {} ",serviceMap.get(comp));
				double compCoArrival = comp.getCoarrival(serviceMap.get(comp), components);
				expectedImpact.put(comp,comp.getImpact() * compCoArrival);
			}
		}
		
		for (Service svc:services.values()) {
			log.debug("eval service {}",svc.getName());
			int count = 0;
			double svcImpact = 0;
			for (Component comp:svc.getComponents()) {
				Double expected = expectedImpact.get(comp);
				log.debug("- comp {} {}",comp.getName(), expected);
				if (expected != null) {
					count++;
					svcImpact += expected;
				}
			}
			if (count > 0) {
				serviceImpact.put(svc, svcImpact / count);
			}
		}
		return serviceImpact;
	}
}
