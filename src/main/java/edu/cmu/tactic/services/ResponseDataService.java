package edu.cmu.tactic.services;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

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
	
	public double[] listRequest() {
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
		return density.getPdf();
	}
}
