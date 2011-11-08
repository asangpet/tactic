package edu.cmu.tactic.client;

import java.net.URI;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class Collector {
	Logger log = LoggerFactory.getLogger(Collector.class);
	
	Mongo m;
	DBCollection c;
	
	public Collector() throws UnknownHostException, MongoException {
		m = new Mongo("10.1.3.1");
		DB db = m.getDB("collector_b");
		c = db.getCollection("responseTime");
	}
	
	public void record(RequestJob job, long replyTime, boolean success) {
		try {
			log.info("{}\t{}\t{}", new Object[] { job.issueTime - RequestJob.offsetTime, replyTime - job.issueTime, job.uri });
			URI uri = new URI(job.uri);
			String statusText = (success)?"COMPLETED":"FAILED";
			DBObject data = BasicDBObjectBuilder.start().add("timestamp", System.currentTimeMillis())
								.push("server")
									.add("address", uri.getHost())
									.add("port", 0)
								.pop().push("client")
									.add("address", "localhost")
									.add("port", 0)
								.pop()
								.add("requestTime", 1.0*job.issueTime)
								.add("responseTime", 1.0*(replyTime - job.issueTime))
								.add("request", job.uri)
								.add("response", statusText)
								.add("protocol", "ACTION")
								.get();
			c.insert(data);
			//log.info("{}", data);
		} catch (Exception e) {
			log.error("Error recording : {} - {}",job,e);
		}
	}
}
