package edu.cmu.tactic.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@ContextConfiguration(loader=AnnotationConfigContextLoader.class, classes={ UserTest.class })
public class UserTest {	
	Logger log = LoggerFactory.getLogger(UserTest.class);
	
	@Test
	public void testUser() throws Exception {
		log.debug("Test user");
		User u = new User("http://varnish/");
		u.run();
	}
}
