package edu.cmu.tactic.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import edu.cmu.tactic.model.MatlabUtility;

@Configuration
@ComponentScan(basePackages="edu.cmu.tactic", excludeFilters={ @Filter(Configuration.class) })
public class ComponentConfig {
	@Bean
	public Logger log() {
		return LoggerFactory.getLogger("edu.cmu.one.tactic");
	}
	
	@Bean
	public MatlabUtility matlab() {
		return new MatlabUtility();
	}
}
