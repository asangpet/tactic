package edu.cmu.tactic.services.template;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayTable;

import edu.cmu.tactic.analysis.ResponseAnalysis;
import edu.cmu.tactic.builder.Builder;
import edu.cmu.tactic.data.Response;
import edu.cmu.tactic.model.AnalysisGraph;
import edu.cmu.tactic.model.Component;
import edu.cmu.tactic.model.DiscreteProbDensity;
import edu.cmu.tactic.model.Service;
import edu.cmu.tactic.placement.Cluster;
import edu.cmu.tactic.placement.Host;
import edu.cmu.tactic.placement.ImpactCluster;
import edu.cmu.tactic.placement.VirtualMachine;
import edu.cmu.tactic.services.ResponseDataService;

public class ShardContentWebAnalysis extends AnalysisInstance {
	Cluster cluster;
	Service service;
	AnalysisGraph graph;
	ResponseDataService dataService;
	
	public ShardContentWebAnalysis() {
		cluster = new ImpactCluster("impact");
		((ImpactCluster)cluster).setLog(log);
		
		service = Builder.buildService("drupal", "varnish")
					.pushDist("bench-drupal","bench-drupal-2","bench-drupal-3","bench-drupal-4","bench-drupal-5")
						.dist("bench-memcache","bench-drupal-db","bench-solr")
					.pop()
					.pushDist("bench-nfs")
					.build();
		cluster.add(service).addHost("amdw6");

		service.getComponent("varnish").setIpAddress("10.0.50.1").setProtocol("HTTP");
		service.getComponent("bench-drupal").setIpAddress("10.0.60.1").setProtocol("HTTP");
		service.getComponent("bench-drupal-2").setIpAddress("10.0.60.2").setProtocol("HTTP");
		service.getComponent("bench-drupal-3").setIpAddress("10.0.60.3").setProtocol("HTTP");
		service.getComponent("bench-drupal-4").setIpAddress("10.0.60.4").setProtocol("HTTP");
		service.getComponent("bench-drupal-5").setIpAddress("10.0.60.5").setProtocol("HTTP");
		service.getComponent("bench-memcache").setIpAddress("10.0.90.1").setProtocol("MEMCACHE");
		service.getComponent("bench-drupal-db").setIpAddress("10.0.70.1").setProtocol("MySQL");
		service.getComponent("bench-solr").setIpAddress("10.0.80.1").setProtocol("HTTP");
		service.getComponent("bench-nfs").setIpAddress("10.0.91.1").setProtocol("HTTP");
	}
	
	double findImpact(AnalysisGraph graph, Double relativeShift, String nodeName, String root) {
		Map<String, Double> relativeTransfer = new LinkedHashMap<String, Double>();
		relativeTransfer.put(nodeName, relativeShift);
		graph.predictTransfer(relativeTransfer);
		return graph.getNode(root).getAnalysisResponse().getPdf().average();
	}
	
	void calculateImpact() {
		double origin = graph.getNode("varnish").getAnalysisResponse().getPdf().average();
		
		Map<Component, Double> impact = new LinkedHashMap<Component, Double>();
		for (Component comp:service.getComponents()) {
			impact.put(comp, findImpact(graph, 10d, comp.getName(), "varnish"));
		}
		
		log.info("    origin - {}",origin);
		for (Component comp:impact.keySet()) {
			log.info("{} impact - {}",comp.getName(), impact.get(comp));
			comp.setImpact((impact.get(comp)-origin)/origin);
		}
	}
	
	@Override
	public Map<Host,Collection<VirtualMachine>> calculatePlacement() {
		/*
		for (int id=0;id<10;id++) {
			setup(""+id);
			calculateImpact(""+id);
		}
		*/
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
	
	public void setup() {
		graph = service.getAnalysisGraph();
		graph.setLog(log);
		graph.setMatlab(matlab);
	}
	
	@Override
	public Map<String, double[]> analyze() {
		Map<String, DiscreteProbDensity> densityMap = new LinkedHashMap<String, DiscreteProbDensity>();
		for (Component comp:service.getComponents()) {
			//List<Response> responseList = dataService.getTiming(comp.getIpAddress(), comp.getProtocol(), "multi_vdn12_345ms_run_3");
			List<Response> responseList = dataService.getTiming(comp.getIpAddress(), comp.getProtocol(), "multi_vdns1_2345m_run_3");
			comp.setResponseList(responseList);
			double[] responses = dataService.timingToResponse(responseList);
			DiscreteProbDensity responseDensity = matlab.newDiscreteProbDensity();
			responseDensity.convert(responses);
			densityMap.put(comp.getName(), responseDensity);			
		}
		graph.analyze(densityMap);
		
		Map<String, double[]> result = new LinkedHashMap<String, double[]>();
		for (Component comp:service.getComponents()) {
			String name = comp.getName();
			result.put("o-"+comp.getName(), graph.getNode(name).getServerResponse().getPdf());
			log.info("origin - {} : {}",name, graph.getNode(name).getServerResponse().average());
		}
		
		/* Manual shift (Disable)
		Map<String, DiscreteProbDensity> transferMap = new LinkedHashMap<String, DiscreteProbDensity>();
		//transferMap.put("db", matlab.gaussian(60, 10).setRaw(500));
		transferMap.put("db", matlab.gev(0.2, 100, 300).setRaw(500));
		
		graph.predict(transferMap);
		*/
		
		/*
		findImpact(graph,1d,"varnish","varnish");
		
		log.info("var - {}",graph.getNode("varnish").getAnalysisResponse().getPdf().average());
		log.info("drup- {}",graph.getNode("bench-drupal").getAnalysisResponse().getPdf().average());
		log.info("db  - {}",graph.getNode("bench-drupal-db").getAnalysisResponse().getPdf().average());
		log.info("solr- {}",graph.getNode("bench-solr").getAnalysisResponse().getPdf().average());
		log.info("cache-{}",graph.getNode("bench-memcache").getAnalysisResponse().getPdf().average());
		
		result.put("varnish", graph.getNode("varnish").getAnalysisResponse().getPdf().getPdf());
		result.put("drupal", graph.getNode("bench-drupal").getAnalysisResponse().getPdf().getPdf());
		result.put("db", graph.getNode("bench-drupal-db").getAnalysisResponse().getPdf().getPdf());
		result.put("solr", graph.getNode("bench-solr").getAnalysisResponse().getPdf().getPdf());
		result.put("cache", graph.getNode("bench-memcache").getAnalysisResponse().getPdf().getPdf());
		*/
		for (Component comp:service.getComponents()) {
			String name = comp.getName();
			result.put(comp.getName(), graph.getNode(name).getAnalysisResponse().getPdf().getPdf());
			log.info("{} : {}",name, graph.getNode(name).getAnalysisResponse().getPdf().average());
		}
		
		/** Find coarrival probability **/
		log.info("Co-arrival status");
		ResponseAnalysis analysis = new ResponseAnalysis();
		ArrayTable<Component, Component, Double> coMatrix = ArrayTable.create(service.getComponentsIterable(), service.getComponentsIterable());
		for (Component ref:service.getComponents()) {
			for (Component test:service.getComponents()) {
				coMatrix.put(ref, test, analysis.findCoarrivalProb(ref.getResponseList(), test.getResponseList()));
				log.info("{} co {} = {}",new Object[] { ref.getName(), test.getName(), coMatrix.get(ref, test)});
			}
		}
		
		calculateImpact();
		
		return result;
	}
	
	@Override
	public AnalysisGraph getAnalysisGraph() {
		return graph;
	}
	
	public void setDataService(ResponseDataService dataService) {
		this.dataService = dataService;
	}
}
