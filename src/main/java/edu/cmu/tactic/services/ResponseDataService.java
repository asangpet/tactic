package edu.cmu.tactic.services;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import edu.cmu.tactic.data.Response;
import edu.cmu.tactic.data.ResponseRepository;

@Service
public class ResponseDataService {
	@Inject 
	Logger log;
	
	@Inject MongoTemplate mongoTemplate;
	@Inject ResponseRepository responseRepo;
	
	void getResponse() {
		log.trace("Helloooo");
	}
	
	void listResponse() {
		for (String s:mongoTemplate.getCollectionNames()) {
			log.info(s);
		}
		
		log.info("Total record:"+responseRepo.count());
		for (Response r:responseRepo.findAll()) {
			log.info(r.toString());
		}
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

}
