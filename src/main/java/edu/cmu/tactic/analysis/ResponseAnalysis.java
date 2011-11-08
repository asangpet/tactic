package edu.cmu.tactic.analysis;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.tactic.data.Response;

public class ResponseAnalysis {
	// Find co-arrival of b with respsect to a (e.g how many request as a percentage of a overlap with b)
	public double findCoarrivalProb(List<Response> a, List<Response> b) {
		double co = 0;
		for (Response ra:a) {
			for (Response rb:b) {
				if (ra.isOverlap(rb)) {
					co++; break;
				}
			}
		}
		return co/a.size();
	}
	
	static List<Response> mockResponse(double[] timepair) {
		List<Response> list = new ArrayList<Response>(timepair.length/2);
		for (int i=0;i<timepair.length;i+=2) {
			Response r = new Response();
			r.setRequestTime(timepair[i]);
			r.setResponseTime(timepair[i+1]-timepair[i]);
			list.add(r);
		}
		return list;
	}
	
	public static void main(String[] args) {
		ResponseAnalysis a = new ResponseAnalysis();
		List<Response> r1 = mockResponse(new double[] {0,1, 2,3, 4,5, 6,7});
		List<Response> r2 = mockResponse(new double[] {0,1, 3.1,6.1});
		System.out.println(a.findCoarrivalProb(r1, r2));
	}
}
