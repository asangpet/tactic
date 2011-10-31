package edu.cmu.tactic.services;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.slf4j.Logger;
import edu.cmu.tactic.model.MatlabUtility;

@org.springframework.stereotype.Service
public class AnalysisService {
	@Inject	Logger log;	
	@Inject MatlabUtility matlab;

	Map<String, AnalysisInstance> instances = new LinkedHashMap<String, AnalysisInstance>();
	
	@PostConstruct
	public void addDemoAnalysis() {
		DemoWebAnalysis demo = new DemoWebAnalysis();
		demo.setLog(log);
		demo.setMatlab(matlab);
		demo.setup("0");
		instances.put("demo",demo);
	}
	
	public AnalysisInstance getInstance(String name) {
		return instances.get(name);
	}
	
}
