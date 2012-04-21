package edu.cmu.tactic.builder;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Stack;

import edu.cmu.tactic.model.Component;
import edu.cmu.tactic.model.CompositionDependency;
import edu.cmu.tactic.model.Dependency;
import edu.cmu.tactic.model.DistributionDependency;
import edu.cmu.tactic.model.Service;

public class ServiceBuilder extends Builder {
	Service servicePrototype;
	LinkedHashSet<Component> currentComponents;
	Stack<LinkedHashSet<Component>> activeStack;
	Class<? extends Dependency> rootDependency;
	
	public ServiceBuilder(String serviceName, String root) {
		servicePrototype = new Service(serviceName);
		Component rootComp = new Component(root);
		servicePrototype.add(rootComp);
		servicePrototype.setRootComponent(rootComp);
		
		currentComponents = new LinkedHashSet<Component>();
		currentComponents.add(rootComp);
		
		rootDependency = CompositionDependency.class;
		activeStack = new Stack<LinkedHashSet<Component>>();
	}
	
	/**
	 * Push current active component to stack and replace the current active set with a distributed dependency
	 * @param name
	 * @return
	 */
	public ServiceBuilder pushDist(String... names) {
		activeStack.push(currentComponents);
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
	/**
	 * Replace the current components with the ones on the stack
	 * @return
	 */
	public ServiceBuilder pop() {
		currentComponents = activeStack.pop();
		return this;
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
	
	/**
	 * Match the upstream tier with the current-tier with one-to-one composition relationship
	 * @param names
	 * @return
	 */
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
