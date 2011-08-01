package edu.cmu.tactic.services;

import static org.junit.Assert.assertNotNull;

import edu.cmu.tactic.config.ComponentConfig;
import edu.cmu.tactic.services.ResponseDataService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader=AnnotationConfigContextLoader.class, classes=ComponentConfig.class)
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
