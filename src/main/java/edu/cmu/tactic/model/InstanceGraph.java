package edu.cmu.tactic.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class InstanceGraph {
	int currentIndex = 0;
	HashMap<String, Node> nodes = new HashMap<String, Node>(); // [ see Node.groovy ]
	List<Node> nodeList = new ArrayList<Node>();
	List<Link> links = new ArrayList<Link>(); // [source:,target:,type:,parent:]
	
	// shallow copy of data structure
	@SuppressWarnings("unchecked")
	public AnalysisGraph createNewInstance() {
		AnalysisGraph graph = new AnalysisGraph();
		graph.currentIndex = currentIndex;
		
		graph.nodes = (HashMap<String, Node>) nodes.clone();
		graph.nodeList = (ArrayList<Node>)((ArrayList<Node>) nodeList).clone();
		graph.links = (ArrayList<Link>)((ArrayList<Link>) links).clone();
		return graph;
	}
	
	public Node getNode(String name) {
		return nodes.get(name);
	}
	
	Subgraph getChildren(Node node) {
		Set<Node> childNodes = new LinkedHashSet<Node>();
		Set<Link> childLinks = new LinkedHashSet<Link>();
		for (Link it:links) {
			if (it.parent.equals(node)) {
				childLinks.add(it);
				childNodes.add(it.target);
			}
		}
		return new Subgraph(childNodes, childLinks);
	}
	
	Subgraph getParent(Node node) {
		Set<Node> parentNodes = new LinkedHashSet<Node>();
		Set<Link> parentLinks = new LinkedHashSet<Link>();
		for (Link it:links) {
			if (it.target.equals(node)) {
				parentNodes.add(it.source);
				parentLinks.add(it);
			}
		}
		return new Subgraph(parentNodes, parentLinks);
	}
	
	InstanceGraph bindCoordinate(List<Point> colist) {
		for (Point it:colist) {
			if (nodes.get(it.node)!=null) {
				nodes.get(it.node).x = it.x;
				nodes.get(it.node).y = it.y;
			}
		}
		return this;
	}
				
	Node addNode(String name, Component tier) {
		Node newNode = getNode(name);
		if (newNode == null) {
			newNode = new Node(name, currentIndex, tier, false);
			currentIndex++;
			nodes.put(name, newNode);
			nodeList.add(newNode);
		}
		return newNode;
	}
	
	Node addNode(Component tier) {
		Node newNode = getNode(tier.name);
		if (newNode == null) {
			newNode = new Node(tier.name, currentIndex, tier, false);
			currentIndex++;
			nodes.put(tier.name, newNode);
			nodeList.add(newNode);
		}
		return newNode;
	}
	
	// helper function which automatically create a cloned link when editing values
	// used to create transformed graph and preserve the original link's information 
	void resetLink(Link link, Node source, Node target, Dependency type) {
		links.remove(link);
		links.add(new Link(source, target, type, source));
	}
	
	void addLink(Node source, Node target, Dependency type) {
		links.add(new Link(source,target,type,source));
	}		
	
	void resetMark() {
		for (Node it:nodeList) {
			it.mark = false;
			it.edited = false;
			it.transferEdited = false;
		}	
	}
	
	void mark(Node node) {
		node.mark = true;
	}
	
	public HashMap<String, List<HashMap<String,Object>>> json() {
		HashMap<String, List<HashMap<String,Object>>> jsonGraph = new HashMap<String, List<HashMap<String,Object>>>();
		jsonGraph.put("nodes", new ArrayList<HashMap<String,Object>>(nodes.size()));
		jsonGraph.put("links", new ArrayList<HashMap<String,Object>>(links.size()));
		
		int vid = 0;
		List<HashMap<String,Object>> jnodes = jsonGraph.get("nodes");
		for (Node it:nodeList) {
			HashMap<String,Object> result = new LinkedHashMap<String,Object>();
			result.put("name", it.name);
			result.put("group", 1);
			result.put("mark", it.mark);
			if (it.x != null) result.put("x", it.x);
			if (it.y != null) result.put("y", it.y);
			it.vid = vid++;
			
			jnodes.add(result);
		}
		
		List<HashMap<String,Object>> jlinks = jsonGraph.get("links");
		for (Link it:links) {
			HashMap<String,Object> result = new LinkedHashMap<String,Object>();
			result.put("source", it.source.vid);
			result.put("target", it.target.vid);
			result.put("value", it.type.getClass());
			jlinks.add(result);
		}
		return jsonGraph;
	}
}
