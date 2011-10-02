package edu.cmu.tactic.placement;

import javax.inject.Inject;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@ContextConfiguration(loader=AnnotationConfigContextLoader.class, classes={ PlacementTest.class })
public class PlacementTest {
	
	public @Bean Builder testBuilder() {
		return new Builder();
	}
	
	public @Bean Logger log() {
		return LoggerFactory.getLogger("edu.cmu.one.tactic");
	}
	
	public @Bean ImpactCluster impactCluster() {
		return new ImpactCluster("impact");
	}
	public @Bean RandomCluster randomCluster() {
		return new RandomCluster("random");
	}
	
	public @Bean ObjectMapper mapper() {
		ObjectMapper mapper = new ObjectMapper();		
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);		
		mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
		SimpleModule testModule = new SimpleModule("ClusterModule", new Version(1,0,0,null));
		testModule.addSerializer(new ClusterSerializer());
		mapper.registerModule(testModule);
		return mapper;
	}
	
	@Inject Builder builder; 
	@Inject ObjectMapper mapper;
	@Inject Logger log;
	@Inject ApplicationContext context;
	
	@Test
	public void testRandomCluster() throws Exception {
		Cluster cluster = context.getBean(RandomCluster.class);
		cluster = builder.clusterBuilder(cluster);
		ObjectWriter writer = mapper.defaultPrettyPrintingWriter();
		cluster.place();
		log.info("Test random cluster");
		System.out.println(writer.writeValueAsString(cluster));		
	}

	@Test
	public void testImpactCluster() throws Exception {		
		Cluster cluster = context.getBean(ImpactCluster.class);
		cluster = builder.clusterBuilder(cluster);
		log.info("Test "+cluster.name);
		ObjectWriter writer = mapper.defaultPrettyPrintingWriter();
		cluster.place();
		System.out.println(writer.writeValueAsString(cluster));		
	}
	
	/*
	public static void main(String[] args) throws Exception {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(PlacementTest.class);
		context.getBean(PlacementTest.class).testRun();
	}
	*/
}
