package edu.cmu.tactic.builder;

import edu.cmu.tactic.model.AsynchronousDependency;
import edu.cmu.tactic.model.Component;
import edu.cmu.tactic.model.Service;
import edu.cmu.tactic.placement.Cluster;
import edu.cmu.tactic.placement.Host;
import edu.cmu.tactic.placement.VirtualMachine;

public class Builder {
	public static ServiceBuilder buildService(String name, String rootName) {
		ServiceBuilder builder = new ServiceBuilder(name, rootName);
		return builder;
	}
	
	public static ServiceBuilder comp(String name) {
		ServiceBuilder builder = new ServiceBuilder(name, name);
		return builder;		
	}
	
	public static ServiceBuilder async(String name) {
		ServiceBuilder builder = new ServiceBuilder(name, name);
		builder.rootDependency = AsynchronousDependency.class;
		return builder;		
	}
	
	public Service webServiceBuilder() {
		/*
		Service webService = new Service("webservice");
		webService.add(new Component("proxy"))
				.add(new Component("app"))
				.add(new Component("db"))
				.add(new Component("cache"))
				.add(new Component("storage"));
		*/
		
		Service webService = Builder.buildService("webservice","root")
			.dist("lb1","lb2")
			.dist("proxy1","proxy2","proxy3")
			.match("app1","app2","app3").each(
				comp("db1"),
				comp("searcher").dist("search1","search2","search3"),
				async("log1")
			);
		
		return webService;
	}
	
	public Cluster clusterBuilder(Cluster main) {
		Service webService = webServiceBuilder();
		
		main.add(new Host("host1")).add(new Host("host2")).add(new Host("host3")).add(new Host("host4"));
		main.add(webService);
		
		VirtualMachine vm1 = new VirtualMachine("vm1").add(webService.getComponent("lb1"));
		VirtualMachine vm2 = new VirtualMachine("vm2").add(webService.getComponent("lb2"));
		VirtualMachine vm3 = new VirtualMachine("vm3").add(webService.getComponent("proxy1"));
		VirtualMachine vm4 = new VirtualMachine("vm4").add(webService.getComponent("proxy2"));
		VirtualMachine vm5 = new VirtualMachine("vm5").add(webService.getComponent("proxy3"));
		VirtualMachine vm6 = new VirtualMachine("vm6").add(webService.getComponent("app1"));
		VirtualMachine vm7 = new VirtualMachine("vm7").add(webService.getComponent("app2"));
		VirtualMachine vm8 = new VirtualMachine("vm8").add(webService.getComponent("app3"));
		VirtualMachine vm9 = new VirtualMachine("vm9").add(webService.getComponent("db1"));
		VirtualMachine vm10 = new VirtualMachine("vm10").add(webService.getComponent("searcher"));
		VirtualMachine vm11 = new VirtualMachine("vm11").add(webService.getComponent("search1"));
		VirtualMachine vm12 = new VirtualMachine("vm11").add(webService.getComponent("search2"));
		VirtualMachine vm13 = new VirtualMachine("vm11").add(webService.getComponent("search3"));
		VirtualMachine vm14 = new VirtualMachine("vm11").add(webService.getComponent("log1"));
		
		main.add(vm1).add(vm2).add(vm3).add(vm4).add(vm5).add(vm6).add(vm7).add(vm8).add(vm9).add(vm10)
			.add(vm11).add(vm12).add(vm13).add(vm14);
		
		return main;
	}
	
}
