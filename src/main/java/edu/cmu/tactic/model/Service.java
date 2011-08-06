package edu.cmu.tactic.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Service {
	String name;
	String label = "service";
	String[] instances;
	List<Dependency> dependencies = new ArrayList<Dependency>(); 

	UUID uuid = UUID.randomUUID();
	HashMap<String,String> tierMap = new HashMap<String, String>();
	
	AnalysisGraph graph = null;
	AnalysisGraph analysisGraph = null;
	
	public String toString() {
		String result = name+" = { "+label;
		
		if (instances.length > 0) { result+= ", instances: "+instances; }
		if (dependencies.size() > 0) { result+= ", dep:"+dependencies; }
		return result+" }";
	}
	
	// Get the graph which is ready to analyze 
	// (expanded + assign traffic filter for each link)
	AnalysisGraph getAnalysisGraph(boolean reset) {
		if (analysisGraph == null && !reset) {
			analysisGraph = getInstanceGraph().createNewInstance();
			transformGraph();
			
			// update parents
			for (Node it:analysisGraph.nodeList) {
				it.parents = analysisGraph.getParent(it);
			}
		}
		return analysisGraph;
	}
	
	AnalysisGraph getAnalysisGraph() {
		return getAnalysisGraph(false);
	}
		
	private class NodeTemplate {
		String type;
		Node node;
		public NodeTemplate(String type, Node node) {
			this.type = type;
			this.node = node;
		}
	}
	// apply relationship transform based on node type
	// TODO: should extract out as plugin
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
	
	// Get instance graph associated with this service
	AnalysisGraph getInstanceGraph() {
		if (graph == null) {
			graph = new AnalysisGraph();
			createGraph(this ,graph, null, null);
		}
		return graph;
	}
	
	void createGraph(Service tier, InstanceGraph graph, Node root, Integer matchIndex) {
		Node thisNode = null;
		if (root!=null) {
			thisNode = graph.getNode(root.name);
		} else {
			thisNode = graph.addNode(tier.instances[0], tier);
		}
		
		if (thisNode == null) return;
		
		if (root == null) root = thisNode;
		if (thisNode.mark) {
			return;
		}
		graph.mark(thisNode);
		
		for (Dependency rel:tier.dependencies) {
			int idx = 0;
			for (String name:rel.instances) {
				// create a link when we match the instance between tier, 
				// or if the link doesn't have to match between instances
				if (idx == matchIndex || !rel.match) {
					Node subNode = graph.addNode(name, rel);					
					try {
						graph.addLink(root,subNode,rel.getClass().newInstance());
						createGraph(rel,graph,subNode,idx);
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				idx++;
			}
		}
	}	
}