package edu.cmu.tactic.model;

import java.util.Formatter;

public class Link {
	Node source;
	Node target;
	Dependency type;
	Node parent;
	Double response;
	
	public Link(Node source, Node target, Dependency type, Node parent) {
		this.source = source;
		this.target = target;
		this.type = type;
		this.parent = parent;
	}
	
	@Override
	public String toString() {
		return new Formatter().format("(%s,%s - %s)", source, target, type.getClass()).toString(); 
	}	
}