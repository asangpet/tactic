package edu.cmu.tactic.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.UUID;

import com.google.common.collect.LinkedListMultimap;


/**
 * Service is an entity for macro-management
 * Users are allowed to set individual response time SLA for each service
 * 
 * The goal of the service is to generate a mapping from service description to a full dependency graph
 * service = collection of < components > + a root component
 * component = entity (has a name) + a collection of < dependencies >
 * dependency = relationship type + a collection of < components >
 * 
 * TODO: Down-grade the dependencies from being a full service to become a simple relationship between
 * the components.
 * 
 * @author asangpet
 *
 */
public class Service extends Entity {
	String label = "service";
	LinkedHashMap<String,Component> components;
	LinkedListMultimap<Component,Dependency> dependencies;
	
	UUID uuid = UUID.randomUUID();
	HashMap<String,String> tierMap = new HashMap<String, String>();
	
	AnalysisGraph graph = null;
	AnalysisGraph analysisGraph = null;
	
	Component rootComponent;
	
	public String toString() {
		String result = name+" = { \"root\":"+rootComponent + ",";
		result += "components:"+components+",";
		result += "dependencies:"+dependencies+" }";
		return result;
	}
	
	// Get the graph which is ready to analyze 
	// (expanded + assign traffic filter for each link)
	public AnalysisGraph getAnalysisGraph(boolean reset) {
		if (analysisGraph == null && !reset) {
			analysisGraph = getInstanceGraph().createNewInstance();
			// TODO: Implement graph transformation template
			//transformGraph();
			
			// update parents
			for (Node it:analysisGraph.nodeList) {
				it.parents = analysisGraph.getParent(it);
			}
		}
		return analysisGraph;
	}
	
	public AnalysisGraph getAnalysisGraph() {
		return getAnalysisGraph(false);
	}
		
	// apply relationship transform based on node type
	// TODO: should extract out as plugin
	/*
	void transformGraph() {
		AnalysisGraph graph = analysisGraph;
		List<NodeTemplate> modNodes = new LinkedList<NodeTemplate>();
		// for now, only find proxy and remap to dist with cache/nocache
		for (Node it:graph.nodeList) {
			// detect nodes that should be expanded
			if (it.tier.name.toLowerCase().contains("proxy")) {
				modNodes.add(new NodeTemplate("proxy",it));
			}
		}
		
		// perform tree modification
		for (NodeTemplate modInfo:modNodes) {
			Node node = modInfo.node;
			
			if (modInfo.type.equals("proxy")) {
				Subgraph childInfo = graph.getChildren(node); 
				
				// generate a cache node
				Node cacheNode = graph.addNode(node.name+"-cache",node.tier);
				graph.addLink(node,cacheNode,new DistributionDependency());

				// generate non-cache node
				Node noCacheNode = graph.addNode(node.name+"-nocache",node.tier);
				graph.addLink(node,noCacheNode,new DistributionDependency());
				
				// reattach child nodes / links to non-caching node				
				for (Link it:childInfo.links) {
					graph.resetLink(it,noCacheNode,it.target,it.type);
				}
			}
		}
	}
	*/
	
	// Get instance graph associated with this service
	AnalysisGraph getInstanceGraph() {
		if (graph == null) {
			graph = new AnalysisGraph();
			createGraph(this.rootComponent ,graph, null);
		}
		return graph;
	}
	
	void createGraph(Component tier, InstanceGraph graph, Node root) {
		Node thisNode = null;
		if (root!=null) {
			thisNode = graph.getNode(root.name);
		} else {
			thisNode = graph.addNode(tier.getName(), tier);
		}
		
		if (thisNode == null) return;
		
		if (root == null) root = thisNode;
		if (thisNode.mark) {
			return;
		}
		graph.mark(thisNode);
		
		for (Dependency rel:dependencies.get(tier)) {
			Node initiator = graph.addNode(rel.initiator);
			Node upstream = graph.addNode(rel.upstream);
			graph.addLink(initiator, upstream, rel);
			createGraph(rel.upstream,graph,upstream);
		}
	}
	
	public Service(String name) {
		super(name);
		components = new LinkedHashMap<String,Component>();
		dependencies = LinkedListMultimap.create();
	}
	
	public void setRootComponent(Component component) {
		this.rootComponent = component;
	}
	
	public Component getRootComponent() {
		return rootComponent;
	}
	
	public Service add(Component component) {
		components.put(component.getName(), component);
		return this;
	}
	
	public Service add(Dependency dep) {
		dependencies.put(dep.initiator, dep);
		return this;
	}
	
	public Component getComponent(String name) {
		return components.get(name);
	}
	
	public Collection<Dependency> getDependencies() {
		return dependencies.values();
	}
	
	public Component addComponent(String name) {
		Component result = components.get(name);
		if (result == null) {
			result = new Component(name);
			add(result);
		}
		return result;
	}
	
	/**
	 * This should return the given component impact to the service
	 * 
	 * @param component
	 * @return
	 */
	public double getImpact(Component component) {
		return 0.5;
	}
	
	public Collection<Component> getComponents() {
		return components.values();
	}
	
	public Iterable<Component> getComponentsIterable() {
		Iterable<Component> iter = new Iterable<Component>() {
			
			@Override
			public Iterator<Component> iterator() {
				return components.values().iterator();
			}
		};
		return iter;
	}
}