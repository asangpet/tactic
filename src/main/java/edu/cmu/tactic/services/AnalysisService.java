package edu.cmu.tactic.services;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;

import edu.cmu.tactic.builder.Builder;
import edu.cmu.tactic.model.AnalysisGraph;
import edu.cmu.tactic.model.DiscreteProbDensity;
import edu.cmu.tactic.model.MatlabUtility;
import edu.cmu.tactic.model.Service;
import edu.cmu.tactic.placement.Cluster;
import edu.cmu.tactic.placement.ImpactCluster;

@org.springframework.stereotype.Service
public class AnalysisService {
	@Inject	Logger log;
	
	@Inject MatlabUtility matlab;

	Cluster cluster;
	Service service;
	
	public Service getDefaultService() {
		if (cluster == null) {
			cluster = new ImpactCluster("impact");
			((ImpactCluster)cluster).setLog(log);
		
			Builder builder = new Builder();
			cluster = builder.simpleClusterBuilder(cluster);
		}
		
		if (service == null) {
			service = cluster.getService("simple");
		}
		return service;
	}
	
	public Map<String, double[]> analyze() {
		AnalysisGraph graph = getDefaultService().getAnalysisGraph();
		graph.setLog(log);
		graph.setMatlab(matlab);
		
		Map<String, DiscreteProbDensity> densityMap = new LinkedHashMap<String, DiscreteProbDensity>();
		densityMap.put("web", matlab.gaussian(300, 20).setRaw(100));
		densityMap.put("app", matlab.gaussian(200, 20).setRaw(100));
		densityMap.put("db", matlab.gaussian(100, 20).setRaw(100));
		log.info("--pre-web - {}",densityMap.get("web").average());
		log.info("--pre-app - {}",densityMap.get("app").average());
		log.info("--pre-db  - {}",densityMap.get("db").average());
		
		graph.analyze(densityMap);
		log.info("web - {}",graph.getNode("web").getAnalysisResponse().getPdf().mode());
		log.info("app - {}",graph.getNode("app").getAnalysisResponse().getPdf().mode());
		log.info("db  - {}",graph.getNode("db").getAnalysisResponse().getPdf().mode());
		
		Map<String, DiscreteProbDensity> transferMap = new LinkedHashMap<String, DiscreteProbDensity>();
		//transferMap.put("app", matlab.gaussian(210, 20).setRaw(100));
		transferMap.put("db", matlab.gaussian(110, 20).setRaw(100));
		graph.predict(transferMap);
		log.info("web - {}",graph.getNode("web").getAnalysisResponse().getPdf().mode());
		log.info("app - {}",graph.getNode("app").getAnalysisResponse().getPdf().mode());
		log.info("db  - {}",graph.getNode("db").getAnalysisResponse().getPdf().mode());
		
		Map<String, double[]> result = new LinkedHashMap<String, double[]>();
		result.put("web", graph.getNode("web").getAnalysisResponse().getPdf().getPdf());
		result.put("app", graph.getNode("app").getAnalysisResponse().getPdf().getPdf());
		result.put("db", graph.getNode("db").getAnalysisResponse().getPdf().getPdf());
		
		return result;
	}
}
