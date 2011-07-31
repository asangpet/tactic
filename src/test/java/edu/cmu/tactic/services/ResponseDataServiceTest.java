package edu.cmu.tactic.services;

import static org.junit.Assert.assertNotNull;

import edu.cmu.tactic.services.ResponseDataService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration("test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ResponseDataServiceTest {
	
	@Autowired private Logger log;
	
	@Autowired
	private ResponseDataService service;

	@Test
	public void testSimpleProperties() throws Exception {
		assertNotNull(service);		
		log.info(service.toString());
		service.getResponse();
	}
	
}
