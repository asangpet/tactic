package edu.cmu.tactic.services;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.springframework.data.document.mongodb.MongoTemplate;
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
	
	void listServer(String ip) {
		for (Response r:responseRepo.findByServerAddress(ip)) {
			log.info(r.toString());
		}
	}
}
