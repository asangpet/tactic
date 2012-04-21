package edu.cmu.tactic.client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;

public class HttpDriver {
	private static final Logger log = LoggerFactory.getLogger(HttpDriver.class);
	private static final int REQUEST_QUEUE_SIZE = 10000;
	
	Scheduler scheduler = new StdSchedulerFactory().getScheduler();
	
	private AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
		.setIOThreadMultiplier(1)
		.setAllowPoolingConnection(true)
		.setCompressionEnabled(true)
		.setFollowRedirects(true)
		.build();		
	private AsyncHttpClient client = new AsyncHttpClient(config);
	
	List<RequestJob> requests = new ArrayList<RequestJob>(REQUEST_QUEUE_SIZE);	
	
	public HttpDriver(String tracefile) throws Exception {
		try {
			BufferedReader f = new BufferedReader(new FileReader(tracefile));
			String request;
		
			while ((request = f.readLine()) != null) {
				StringTokenizer tokens = new StringTokenizer(request);
				if (tokens.hasMoreTokens()) {
					long delay = Long.parseLong(tokens.nextToken());
					String uri = tokens.nextToken();
			
					requests.add(new RequestJob(delay,uri,false));
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void run() throws Exception {
		log.info("Replaying {} requests",requests.size());
		
		RequestJob.totalRequests = requests.size();
		RequestJob.client = client;
		RequestJob.collector = new Collector();
		RequestJob.scheduler = scheduler;
		RequestJob.log = log;
		RequestJob.offsetTime = System.currentTimeMillis();

		scheduler.start();
		for (RequestJob req:requests) {
			req.issue();
		}
	}
	
	public static void main(String[] args) throws Exception {
		long now = new Date().getTime();
		long schedule = ((now / 10000)+1)*10000;
		System.setProperty("org.terracotta.quartz.skipUpdateCheck", "true");
		log.info("Will start in {} ms",(schedule - now));
		final HttpDriver driver = new HttpDriver(args[0]);
		Thread.sleep(schedule-now);
		driver.run();
	}
}
