package edu.cmu.tactic.builder;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.LinkedHashSet;

import edu.cmu.tactic.model.Component;
import edu.cmu.tactic.model.CompositionDependency;
import edu.cmu.tactic.model.Dependency;
import edu.cmu.tactic.model.DistributionDependency;
import edu.cmu.tactic.model.Service;

public class ServiceBuilder extends Builder {
	Service servicePrototype;
	LinkedHashSet<Component> currentComponents;
	Class<? extends Dependency> rootDependency;
	
	public ServiceBuilder(String serviceName, String root) {
		servicePrototype = new Service(serviceName);
		Component rootComp = new Component(root);
		servicePrototype.add(rootComp);
		servicePrototype.setRootComponent(rootComp);
		
		currentComponents = new LinkedHashSet<Component>();
		currentComponents.add(rootComp);
		
		rootDependency = CompositionDependency.class; 
	}
	
	public ServiceBuilder dist(String... names) {
		LinkedHashSet<Component> activeComponents = new LinkedHashSet<Component>();
		for (String name:names) {
			for (Component comp:currentComponents) {
				Component upstream = servicePrototype.addComponent(name);
				servicePrototype.add(new DistributionDependency(comp,upstream));
				activeComponents.add(upstream);
			}
		}
		currentComponents = activeComponents;
		return this;
	}
	
	public ServiceBuilder comp(String... names) {
		LinkedHashSet<Component> activeComponents = new LinkedHashSet<Component>();
		for (String name:names) {
			for (Component comp:currentComponents) {
				Component upstream = servicePrototype.addComponent(name);
				servicePrototype.add(new CompositionDependency(comp,upstream));
				activeComponents.add(upstream);
			}
		}
		currentComponents = activeComponents;
		return this;
	}
	
	public ServiceBuilder match(String... names) {
		LinkedHashSet<Component> activeComponents = new LinkedHashSet<Component>();
		int idx = 0;
		for (Component comp:currentComponents) {
			Component upstream = servicePrototype.addComponent(names[idx++]);
			servicePrototype.add(new CompositionDependency(comp,upstream));
			activeComponents.add(upstream);
		}
		currentComponents = activeComponents;
		return this;
	}
	
	public Service each(ServiceBuilder... serviceTrees) {
		for (ServiceBuilder builder:serviceTrees) {
			addBuilder(builder);
		}
		return servicePrototype;		
	}
	
	public ServiceBuilder each(Collection<ServiceBuilder>... serviceTrees) {
		for (Collection<ServiceBuilder> builders:serviceTrees) {
			for (ServiceBuilder builder:builders) {
				addBuilder(builder);
			}
		}
		return this;
	}
	
	public Service build() {
		return servicePrototype;
	}
	
	private void addBuilder(ServiceBuilder builder) {
		for (Component current:currentComponents) {	
			for (Component comp:builder.servicePrototype.getComponents()) {
				servicePrototype.add(comp);
			}
		
			for (Dependency dep:builder.servicePrototype.getDependencies()) {
				servicePrototype.add(dep);
			}
			
			try {
				Class<?> parameterTypes[] = new Class[2];
				parameterTypes[0] = Component.class;
				parameterTypes[1] = Component.class;
				Constructor<? extends Dependency> con = builder.rootDependency.getConstructor(parameterTypes);
				Dependency dep = con.newInstance(current, builder.servicePrototype.getRootComponent());
			
				servicePrototype.add(dep);
			} catch (Exception e) {}
		}
	}			
}
