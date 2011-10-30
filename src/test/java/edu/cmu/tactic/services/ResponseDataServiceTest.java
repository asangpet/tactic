package edu.cmu.tactic.services;

import static org.junit.Assert.assertNotNull;
import javax.inject.Inject;

import edu.cmu.tactic.config.ComponentConfig;
import edu.cmu.tactic.config.DataConfig;
import edu.cmu.tactic.services.ResponseDataService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader=AnnotationConfigContextLoader.class, classes={ ComponentConfig.class, DataConfig.class })
public class ResponseDataServiceTest {
	
	@Inject private Logger log;
	
	@Inject private ResponseDataService service;

	@Test
	public void testSimpleProperties() throws Exception {
		assertNotNull(service);		
		log.info(service.toString());
		service.getResponse();
	}
	
	@Test
	public void testMongoRepo() throws Exception {
		service.listResponse();
		//service.listServer("10.0.50.1");
		
		//service.listResponse();
	}
	
}
