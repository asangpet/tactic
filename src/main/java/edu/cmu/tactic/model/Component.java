package edu.cmu.tactic.model;

import java.util.Collection;
import java.util.List;

import edu.cmu.tactic.data.Response;

/**
 * A component belongs to a service and could be hosted on different virtual machines
 * 
 * @author asangpet
 *
 */
public class Component extends Entity {
	double coarrival;
	double impact;
	
	String ipAddress;
	String protocol = "HTTP";
	
	List<Response> responseList;
	double[] density;
	
	public Component(String name) {
		super(name);
	}
	
	@Override
	public String toString() {
		String result = "{ "+name+" }";
		return result;				
	}
	
	public void setCoarrival(double coarrival) {
		this.coarrival = coarrival;
	}
	
	public void setImpact(double impact) {
		this.impact = impact;
	}
	
	public double getCoarrival() {
		return coarrival;
	}
	
	// TODO: Placeholder, return high co-arrival component probability for same-service components 
	public double getCoarrival(Service primary, Collection<Component> components) {
		double mean = 0;
		int count = 0;
		for (Component comp:components) {
			if (!comp.equals(this)) {
				count++;
				Component test = primary.getComponent(comp.getName()); 				
				if (test!=null && test.equals(comp)) {
					mean += 0.8 + 0.1*(components.size()-1);
				} else {
					mean += 0.3 + 0.1*(components.size()-1);
				}
			}
		}
		if (count < 1) count = 1;
		return mean / count;
	}
	
	public double getImpact() {
		return impact;
	}
	
	public Component setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
		return this;
	}
	
	public Component setProtocol(String protocol) {
		this.protocol = protocol;
		return this;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	
	public String getProtocol() {
		return protocol;
	}
	
	public void setDensity(double[] density) {
		this.density = density;
	}
	
	public void setResponseList(List<Response> responseList) {
		this.responseList = responseList;
	}
	
	public List<Response> getResponseList() {
		return responseList;
	}
	
	public double[] getDensity() {
		return density;
	}
}
