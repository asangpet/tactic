package edu.cmu.tactic.config;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.document.mongodb.MongoDbFactory;
import org.springframework.data.document.mongodb.MongoFactoryBean;
import org.springframework.data.document.mongodb.MongoTemplate;
import org.springframework.data.document.mongodb.SimpleMongoDbFactory;

import com.mongodb.Mongo;

@Configuration
@ImportResource("classpath:/edu/cmu/tactic/data/repo.xml")
public class DataConfig {
	
	@Inject Mongo mongo;
	
	public @Bean MongoFactoryBean mongo() {
		MongoFactoryBean mongo = new MongoFactoryBean();
		mongo.setHost("10.1.3.1");
		return mongo;
	}
	
	public @Bean MongoDbFactory mongoDbFactory() {
		return new SimpleMongoDbFactory(mongo, "collector_k");
	}
	
	public @Bean MongoTemplate mongoTemplate() {
		return new MongoTemplate(mongoDbFactory());
	}
}
