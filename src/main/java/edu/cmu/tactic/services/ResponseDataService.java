package edu.cmu.tactic.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.cmu.tactic.analysis.ResponseAnalysis;
import edu.cmu.tactic.data.Response;
import edu.cmu.tactic.data.ResponseRepository;
import edu.cmu.tactic.model.DiscreteProbDensity;
import edu.cmu.tactic.model.MatlabUtility;
import edu.cmu.tactic.model.ParametricDensity;

@Service
public class ResponseDataService {
	@Inject 
	Logger log;
	
	@Inject MongoTemplate mongoTemplate;
	@Inject ResponseRepository responseRepo;
	@Inject	MatlabUtility matlab;
	
	void getResponse() {
		log.trace("Helloooo");
	}
	
	void listResponse() {
		for (String s:mongoTemplate.getCollectionNames()) {
			log.info(s);
		}
		
		log.info("Total record:"+responseRepo.count());
		/*
		for (Response r:responseRepo.findAll()) {
			log.info(r.toString());
		}
		*/
	}
	
	public Page<Response> listServer(String ip) {		
		return responseRepo.findByServerAddress(ip, new PageRequest(0,100));
	}
	
	public List<Response> getTiming(String addr) {
		return getTiming(addr, null);
	}
	public List<Response> getTiming(String addr, String protocol) {
		DBCollection c = mongoTemplate.getCollection("responseTime");
		DBObject query = null;
		if (protocol != null) {
			query = BasicDBObjectBuilder.start()
					.add("server.address", addr)
					.add("protocol", protocol)
					.get();
		} else {
			query = BasicDBObjectBuilder.start()
					.add("server.address", addr)
					.get();			
		}
			
		DBCursor cursor = c.find(query);
		log.info("Load timing {} - {}",addr,cursor.count());
		
		List<Response> result = new ArrayList<Response>(cursor.count());
		Iterator<DBObject> iter = cursor.iterator();
		while (iter.hasNext()) {
			DBObject response = iter.next();
			Response r = new Response();
			r.setRequestTime((Double)response.get("requestTime"));
			r.setResponseTime((Double)response.get("responseTime"));
			result.add(r);
		}		
		cursor.close();
		
		return result;
	}
	
	public double[] getResponseTime(String addr, String protocol) {
		DBCollection c = mongoTemplate.getCollection("responseTime");
		DBObject query = null;
		if (protocol != null) {
			query = BasicDBObjectBuilder.start()
					.add("server.address", addr)
					.add("protocol", protocol)
					.get();
		} else {
			query = BasicDBObjectBuilder.start()
					.add("server.address", addr)
					.get();			
		}
			
		DBCursor cursor = c.find(query);
		log.info("Load timing {} - {}",addr,cursor.count());
		
		double[] result = new double[cursor.count()];
		int idx = 0;
		Iterator<DBObject> iter = cursor.iterator();
		while (iter.hasNext() && idx < result.length) {
			DBObject response = iter.next();
			result[idx++] = (Double)response.get("responseTime");
		}		
		cursor.close();
		
		return result;
	}
	
	public double[] getActionDensity() {
		double[] responseTime = getResponseTime("10.0.50.1","ACTION");
		DiscreteProbDensity responseDensity = matlab.newDiscreteProbDensity();
		responseDensity.convert(responseTime);
		log.info("Density {}",responseDensity);
		return responseDensity.getPdf();
	}
	
	public double[] listRequest() {
		/*
		Page<Response> result = responseRepo.findByProtocol("ACTION", new PageRequest(1,100));
		List<Response> response = responseRepo.findAllByProtocol("ACTION");
		log.info("Total response {} == {}",result.getTotalElements(),response.size());
		
		DiscreteProbDensity density = matlab.newDiscreteProbDensity();
		double[] time = new double[response.size()];
		int counter = 0;
		for (Response r:response) {
			time[counter++] = r.getResponseTime();
		}
		density.convert(time);
		log.info("Density {}",density);
		ParametricDensity param = matlab.getGevParamFit(density);
		log.info("count={} f95%={} r95%={}", new Object[] { density.count(), param.getPdf().percentile(95), density.percentile(95)});
		
		List<Response> webResponse = responseRepo.findAllByServerAddress("10.0.50.1");
		ResponseAnalysis analysis = new ResponseAnalysis();
		log.info("Co-arrival matric {}",analysis.findCoarrivalProb(response, webResponse));
		return density.getPdf();
		*/
		
		LinkedHashMap<String, List<Response>> responseMap = new LinkedHashMap<String, List<Response>>();
		responseMap.put("action", getTiming("10.0.50.1","ACTION"));
		responseMap.put("load", getTiming("10.0.50.1","HTTP"));
		responseMap.put("app", getTiming("10.0.60.1"));
		responseMap.put("db", getTiming("10.0.70.1"));
		responseMap.put("solr", getTiming("10.0.80.1"));
		responseMap.put("memcache", getTiming("10.0.90.1"));
		responseMap.put("image", getTiming("10.0.91.1"));
		ResponseAnalysis analysis = new ResponseAnalysis();
		for (Map.Entry<String, List<Response>> ref:responseMap.entrySet()) {
			for (Map.Entry<String, List<Response>> test:responseMap.entrySet()) {
				log.info("{} co {} = {}",new Object[] { ref.getKey(), test.getKey(), analysis.findCoarrivalProb(ref.getValue(), test.getValue())});
			}
		}
		
		return new double[] {1.0};
	}
}
