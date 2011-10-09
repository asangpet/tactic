package edu.cmu.tactic.placement;

import edu.cmu.tactic.model.Component;
import edu.cmu.tactic.model.Service;

public class Builder {
	public Service webServiceBuilder() {
		Service webService = new Service("webservice");
		webService.add(new Component("proxy"))
				.add(new Component("app"))
				.add(new Component("db"))
				.add(new Component("cache"))
				.add(new Component("storage"));
		
		return webService;
	}
	
	public Cluster clusterBuilder(Cluster main) {
		Service webService = webServiceBuilder();
		
		main.add(new Host("host1")).add(new Host("host2")).add(new Host("host3"));
		main.add(webService);
		
		VirtualMachine vm1 = new VirtualMachine("vm1").add(webService.getComponent("proxy"));
		VirtualMachine vm2 = new VirtualMachine("vm2").add(webService.getComponent("app"));
		VirtualMachine vm3 = new VirtualMachine("vm3").add(webService.getComponent("db"));
		VirtualMachine vm4 = new VirtualMachine("vm4").add(webService.getComponent("cache"));
		VirtualMachine vm5 = new VirtualMachine("vm5").add(webService.getComponent("storage"));
		
		main.add(vm1).add(vm2).add(vm3).add(vm4).add(vm5);
		
		return main;
	}
	
}
