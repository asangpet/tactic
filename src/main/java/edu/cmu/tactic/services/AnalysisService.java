package edu.cmu.tactic.services;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Inject;
import org.slf4j.Logger;
import edu.cmu.tactic.builder.Builder;
import edu.cmu.tactic.model.AnalysisGraph;
import edu.cmu.tactic.model.Component;
import edu.cmu.tactic.model.DiscreteProbDensity;
import edu.cmu.tactic.model.MatlabUtility;
import edu.cmu.tactic.model.Service;
import edu.cmu.tactic.placement.Cluster;
import edu.cmu.tactic.placement.Host;
import edu.cmu.tactic.placement.ImpactCluster;
import edu.cmu.tactic.placement.VirtualMachine;

@org.springframework.stereotype.Service
public class AnalysisService {
	@Inject	Logger log;
	
	@Inject MatlabUtility matlab;

	Cluster cluster;
	Service service;
	AnalysisGraph graph;
	
	public Service getDefaultService() {
		cluster = new ImpactCluster("impact");
		((ImpactCluster)cluster).setLog(log);
		
		Builder builder = new Builder();
		cluster = builder.multipleSimpleClusterBuilder(cluster);
		
		service = cluster.getService("simple");
		return service;
	}
	
	double findImpact(AnalysisGraph graph, Double relativeShift, String nodeName, String root) {
		Map<String, Double> relativeTransfer = new LinkedHashMap<String, Double>();
		relativeTransfer.put(nodeName, relativeShift);
		graph.predictTransfer(relativeTransfer);
		return graph.getNode(root).getAnalysisResponse().getPdf().mode();
	}
	
	void calculateImpact(String id) {
		double origin = graph.getNode("web"+id).getAnalysisResponse().getPdf().mode();
		
		Map<Component, Double> impact = new LinkedHashMap<Component, Double>();
		for (Component comp:service.getComponents()) {
			impact.put(comp, findImpact(graph, 1d, comp.getName(), "web"+id));
		}
		
		log.info("    origin - {}",origin);
		for (Component comp:impact.keySet()) {
			log.info("{} impact - {}",comp.getName(), impact.get(comp));
			comp.setImpact((impact.get(comp)-origin)/origin);
		}
	}
	
	public Map<Host,Collection<VirtualMachine>> calculatePlacement() {
		getDefaultService();
		for (int id=0;id<10;id++) {
			setup(""+id);
			calculateImpact(""+id);
		}
		
		for (int i=0;i<5;i++) {
			log.debug("Place iteration {}",i);
			((ImpactCluster)cluster).placeRandom();
			Map<Service,Double> impact = cluster.evaluate();
		
			for (Service svc:impact.keySet()) {
				log.info("Impact {} = {}",svc.getName(), impact.get(svc));
			}
		}
		return cluster.getMapping().asMap();
	}
	
	void setup(String id) {
		service = cluster.getService("simple"+id);
		log.debug("{} - {}","simple"+id, service);
		graph = service.getAnalysisGraph();
		graph.setLog(log);
		graph.setMatlab(matlab);
		
		Map<String, DiscreteProbDensity> densityMap = new LinkedHashMap<String, DiscreteProbDensity>();
		//densityMap.put("web", matlab.gaussian(500, 10).setRaw(100));
		//densityMap.put("app", matlab.gaussian(400, 10).setRaw(100));
		//densityMap.put("db", matlab.gaussian(30, 10).setRaw(1000));
		
		densityMap.put("web"+id, matlab.gev(0.2, 100, 1200).setRaw(100));
		densityMap.put("app"+id, matlab.gev(0.2, 100, 1100).setRaw(100));
		densityMap.put("db"+id, matlab.gev(0.2, 100, 200).setRaw(500));
		/*
		log.info("--pre-web - {}",densityMap.get("web").average());
		log.info("--pre-app - {}",densityMap.get("app").average());
		log.info("--pre-db  - {}",densityMap.get("db").average());
		*/
		graph.analyze(densityMap);
	}
	
	public Map<String, double[]> analyze() {
		Map<String, double[]> result = new LinkedHashMap<String, double[]>();
		setup("");
		
		result.put("oweb", graph.getNode("web").getServerResponse().getPdf());
		result.put("oapp", graph.getNode("app").getServerResponse().getPdf());
		result.put("odb", graph.getNode("db").getServerResponse().getPdf());		
		
		log.info("web - {}",graph.getNode("web").getAnalysisResponse().getPdf().average());
		log.info("app - {}",graph.getNode("app").getAnalysisResponse().getPdf().average());
		log.info("db  - {}",graph.getNode("db").getAnalysisResponse().getPdf().average());

		/*
		result.put("web", graph.getNode("web").getAnalysisResponse().getPdf().getPdf());
		result.put("app", graph.getNode("app").getAnalysisResponse().getPdf().getPdf());
		result.put("db", graph.getNode("db").getAnalysisResponse().getPdf().getPdf());
		*/
		
		/* Manual shift
		Map<String, DiscreteProbDensity> transferMap = new LinkedHashMap<String, DiscreteProbDensity>();
		//transferMap.put("db", matlab.gaussian(60, 10).setRaw(500));
		transferMap.put("db", matlab.gev(0.2, 100, 300).setRaw(500));
		
		graph.predict(transferMap);
		*/
		
		findImpact(graph,1d,"web","web");
		
		log.info("web - {}",graph.getNode("web").getAnalysisResponse().getPdf().average());
		log.info("app - {}",graph.getNode("app").getAnalysisResponse().getPdf().average());
		log.info("db  - {}",graph.getNode("db").getAnalysisResponse().getPdf().average());
		
		result.put("web", graph.getNode("web").getAnalysisResponse().getPdf().getPdf());
		result.put("app", graph.getNode("app").getAnalysisResponse().getPdf().getPdf());
		result.put("db", graph.getNode("db").getAnalysisResponse().getPdf().getPdf());

		/*
		result.put("web", graph.getNode("web").getAnalysisResponse().getPdf().getPdf());
		result.put("app", graph.getNode("app").getAnalysisResponse().getPdf().getPdf());
		result.put("db", graph.getNode("db").getAnalysisResponse().getPdf().getPdf());
		*/
		
		//result.put("web", graph.getNode("web").getModel().getTransfer().getPdf().getPdf());		
		//result.put("app", graph.getNode("app").getModel().getTransfer().getPdf().getPdf());
		//result.put("db", graph.getNode("db").getModel().getTransfer().getPdf().getPdf());
		
		return result;
	}
}
