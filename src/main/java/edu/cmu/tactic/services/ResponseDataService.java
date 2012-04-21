package edu.cmu.tactic.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.cmu.tactic.analysis.ResponseAnalysis;
import edu.cmu.tactic.data.Response;
import edu.cmu.tactic.data.ResponseRepository;
import edu.cmu.tactic.model.DiscreteCumuDensity;
import edu.cmu.tactic.model.DiscreteProbDensity;
import edu.cmu.tactic.model.MatlabUtility;

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
	
	public List<Double> listServerResponse(String ip) {
		List<Double> result = new ArrayList<Double>();
		for (Response response : responseRepo.findByServerAddress(ip, new PageRequest(0,1000))) {
			result.add(response.getResponseTime());
		}
		return result;
	}
	
	public List<Response> getTiming(String addr) {
		return getTiming(addr, null, "responseTime");
	}
	
	public List<Response> getTiming(String addr, String protocol, String collection) {
		DBCollection c = mongoTemplate.getCollection(collection);
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
	public double[] timingToResponse(List<Response> timing) {
		double[] result = new double[timing.size()];
		int idx = 0;
		for (Response r:timing) {
			result[idx++] = r.getResponseTime();
		}
		return result;
	}
	
	public double[] sampling(double[] source, int size) {
		double[] result = new double[size];
		//double prob = size/source.length;
		int skip = source.length/size;
		int idx = 0;
		for (int i=0;i<source.length;) {			
			//if (Math.random() <= prob || (size-idx >= source.length-i)) {
			result[idx++] = source[i];
			if (size-idx >= source.length-i) i++;
			else i+=skip;
		}
		return result;
	}
	public double[][] sampling2d(double[][] source, int size) {
		double[][] result = new double[size][];
		//double prob = size/source.length;
		int skip = source.length/size;
		int idx = 0;
		for (int i=0;i<source.length;) {			
			//if (Math.random() <= prob || (size-idx >= source.length-i)) {
			if (idx == size) break;
			result[idx++] = source[i];
			if (size-idx >= source.length-i) i++;
			else i+=skip;
		}
		return result;
	}
	
	public double[] getResponseTime(String addr, String protocol) {
		return getResponseTime(addr,protocol,"responseTime");
	}
	public double[] getCdf(double[] data) {
		DiscreteProbDensity responseDensity = matlab.newDiscreteProbDensity();
		responseDensity.convert(data);
		log.info("Density {}",responseDensity);
		
		//return responseDensity.getPdf();
		return new DiscreteCumuDensity(responseDensity).getPdf();		
	}
	
	interface CursorOperator {
		public void init(int length);
		public void iterate(int index, double requestTime, double responseTime);
		public Object getResult();
	}
	Object doQueryResponse(String addr, String protocol, String collection, boolean timeSort, CursorOperator op) {
		DBCollection c = mongoTemplate.getCollection(collection);
		BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add("server.address", addr);
		if (protocol != null) {
			builder.add("protocol", protocol);
		}
		DBObject query = builder.get();
			
		DBCursor cursor = c.find(query, 
				BasicDBObjectBuilder.start().add("requestTime", 1).add("responseTime", 1).get());
		if (timeSort) cursor = cursor.sort(BasicDBObjectBuilder.start().add("requestTime", 1).get());
		log.info("Load timing {} - {}",addr,cursor.count());
		
		int idx = 0, limit=cursor.count();
		op.init(limit);
		Iterator<DBObject> iter = cursor.iterator();
		while (iter.hasNext() && idx < limit) {
			DBObject response = iter.next();
			op.iterate(idx, (Double)response.get("requestTime"), (Double)response.get("responseTime"));
			++idx;
		}		
		cursor.close();
		
		return op.getResult();		
	}
	/**
	 * Return <requestTime, responseTime> as 2 arrays
	 * @param addr
	 * @param protocol
	 * @param collection
	 * @return
	 */
	public double[][] getRequestResponseTime(String addr, String protocol, String collection) {
		return (double[][])doQueryResponse(addr, protocol, collection, true, new CursorOperator() {
			double[][] result;
			double min = Double.MAX_VALUE;
			
			public void init(int length) {
				result = new double[length][];				
			}
			public Object getResult() {
				for (int i=0;i<result.length;i++) {
					result[i][0] = result[i][0] - min;
				}
				return result;
			}
			public void iterate(int index, double requestTime, double responseTime) {
				if (requestTime < min) min = requestTime;
				result[index] = new double[] { requestTime, responseTime };
			}
		});
	}
	public double[] getResponseTime(String addr, String protocol, String collection) {
		return (double[])doQueryResponse(addr, protocol, collection, true, new CursorOperator() {
			double[] result;
			
			public void init(int length) {
				result = new double[length];
				
			}
			public Object getResult() {
				return result;
			}
			public void iterate(int index, double requestTime, double responseTime) {
				result[index] = responseTime;
			}
		});
	}
	public double[] getUnorderedResponseTime(String addr, String protocol, String collection) {
		return (double[])doQueryResponse(addr, protocol, collection, false, new CursorOperator() {
			double[] result;
			
			public void init(int length) {
				result = new double[length];
				
			}
			public Object getResult() {
				return result;
			}
			public void iterate(int index, double requestTime, double responseTime) {
				result[index] = responseTime;
			}
		});
	}
	
	public double[] getActionDensity() {
		double[] responseTime = getResponseTime("10.0.50.1","ACTION");
		DiscreteProbDensity responseDensity = matlab.newDiscreteProbDensity();
		responseDensity.convert(responseTime);
		log.info("Density {}",responseDensity);
		
		//return responseDensity.getPdf();
		return new DiscreteCumuDensity(responseDensity).getPdf();
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
		responseMap.put("action", getTiming("10.0.50.1","ACTION","responseTime"));
		responseMap.put("load", getTiming("10.0.50.1","HTTP","responseTime"));
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
