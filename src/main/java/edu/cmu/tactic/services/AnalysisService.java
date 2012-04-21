package edu.cmu.tactic.services;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import edu.cmu.tactic.model.MatlabUtility;

@org.springframework.stereotype.Service
public class AnalysisService {
	@Inject	Logger log;	
	@Inject MatlabUtility matlab;
	@Inject ResponseDataService dataService;

	@PostConstruct
	public void addDemoAnalysis() {
		DemoWebAnalysis demo = new DemoWebAnalysis();
		demo.setLog(log);
		demo.setMatlab(matlab);
		demo.setup("0");
		instances.put("demo",demo);
	}
	
	@PostConstruct
	public void addContentWebAnalysis() {
		ContentWebAnalysis cms = new ContentWebAnalysis();
		cms.setLog(log);
		cms.setMatlab(matlab);
		cms.setup();
		instances.put("cms", cms);
	}
	
	@PostConstruct
	public void addShardContentWebAnalysis() {
		ShardContentWebAnalysis cms = new ShardContentWebAnalysis();
		cms.setLog(log);
		cms.setMatlab(matlab);
		cms.setDataService(dataService);
		cms.setup();
		instances.put("shardcms", cms);
	}
	
	public AnalysisInstance getInstance(String name) {
		return instances.get(name);
	}
	
}
