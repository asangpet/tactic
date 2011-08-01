package edu.cmu.tactic.services;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class ResponseDataService {
	@Inject 
	Logger log;
	
	void getResponse() {
		log.trace("Helloooo");
	}
}
