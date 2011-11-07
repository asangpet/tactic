package edu.cmu.tactic.services;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.cmu.tactic.builder.Builder;
import edu.cmu.tactic.model.AnalysisGraph;
import edu.cmu.tactic.model.Component;
import edu.cmu.tactic.model.DiscreteProbDensity;
import edu.cmu.tactic.model.Service;
import edu.cmu.tactic.placement.Cluster;
import edu.cmu.tactic.placement.Host;
import edu.cmu.tactic.placement.ImpactCluster;
import edu.cmu.tactic.placement.VirtualMachine;

public class DemoWebAnalysis extends AnalysisInstance {
	Cluster cluster;
	Service service;
	AnalysisGraph graph;
	
	public DemoWebAnalysis() {
		cluster = new ImpactCluster("impact");
		((ImpactCluster)cluster).setLog(log);
		
		Builder builder = new Builder();
		cluster = builder.multipleSimpleClusterBuilder(cluster);
		
		service = cluster.getService("simple");
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
		for (int id=0;id<10;id++) {
			setup(""+id);
			calculateImpact(""+id);
		}
		
		for (int i=0;i<1;i++) {
			log.debug("Place iteration {}",i);
			((ImpactCluster)cluster).place();
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
		densityMap.put("web"+id, matlab.gev(0.2, 100, 1200).setRawCount(100));
		densityMap.put("app"+id, matlab.gev(0.2, 100, 1100).setRawCount(100));
		densityMap.put("db"+id, matlab.gev(0.2, 100, 200).setRawCount(500));
		graph.analyze(densityMap);
	}
	
	public Map<String, double[]> analyze() {
		Map<String, double[]> result = new LinkedHashMap<String, double[]>();
		
		result.put("oweb", graph.getNode("web0").getServerResponse().getPdf());
		result.put("oapp", graph.getNode("app0").getServerResponse().getPdf());
		result.put("odb", graph.getNode("db0").getServerResponse().getPdf());		
		
		log.info("web - {}",graph.getNode("web0").getAnalysisResponse().getPdf().average());
		log.info("app - {}",graph.getNode("app0").getAnalysisResponse().getPdf().average());
		log.info("db  - {}",graph.getNode("db0").getAnalysisResponse().getPdf().average());

		/* Manual shift
		Map<String, DiscreteProbDensity> transferMap = new LinkedHashMap<String, DiscreteProbDensity>();
		//transferMap.put("db", matlab.gaussian(60, 10).setRaw(500));
		transferMap.put("db", matlab.gev(0.2, 100, 300).setRaw(500));
		
		graph.predict(transferMap);
		*/
		
		findImpact(graph,1d,"web0","web0");
		
		log.info("web - {}",graph.getNode("web0").getAnalysisResponse().getPdf().average());
		log.info("app - {}",graph.getNode("app0").getAnalysisResponse().getPdf().average());
		log.info("db  - {}",graph.getNode("db0").getAnalysisResponse().getPdf().average());
		
		result.put("web", graph.getNode("web0").getAnalysisResponse().getPdf().getPdf());
		result.put("app", graph.getNode("app0").getAnalysisResponse().getPdf().getPdf());
		result.put("db", graph.getNode("db0").getAnalysisResponse().getPdf().getPdf());
		
		return result;
	}
	
	@Override
	public AnalysisGraph getAnalysisGraph() {
		return graph;
	}
}
