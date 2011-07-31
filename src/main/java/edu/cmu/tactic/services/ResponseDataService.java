package edu.cmu.tactic.services;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResponseDataService {
	@Autowired Logger log;
	
	void getResponse() {
		log.trace("Helloooo");
	}
}
