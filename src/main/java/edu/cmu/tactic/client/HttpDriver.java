package edu.cmu.tactic.client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Cookie;
import com.ning.http.client.Response;

public class HttpDriver {
	private static final Logger log = LoggerFactory.getLogger(HttpDriver.class);
	private static final int REQUEST_QUEUE_SIZE = 10000;
	
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
	private AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
		.setAllowPoolingConnection(false)
		.setCompressionEnabled(true)
		.build();
	private AsyncHttpClient client = new AsyncHttpClient(config);
	List<RequestObject> requests = new ArrayList<RequestObject>(REQUEST_QUEUE_SIZE);
	long offsetTime;
	private final ReentrantLock countdownLock = new ReentrantLock();
	int countdown;
	int maxwait;

	List<String> getLinks(String content) {
		List<String> links = new LinkedList<String>();
		Document doc = Jsoup.parse(content);						
		// Extract js/image source url
		Elements srcLinks = doc.select("[src]");
		for (Element elem:srcLinks) {
			links.add(elem.attr("src"));
			//log.info("{}",elem.attr("src"));
		}
		// Extract CSS
		Elements cssLinks = doc.select("style");
		for (Element elem:cssLinks) {
			//Pattern p = Pattern.compile(".*url(\"(http.*)\").*");
			Pattern p = Pattern.compile("url\\(\"(.*)\"\\)");
			Matcher m = p.matcher(elem.html());
			while (m.find()) {
				links.add(m.group(1));
			}
		}
		return links;
	}
	
	class RequestObject {
		String uri;
		long issueTime;
		long replyTime;
		long responseTime;
		long issueDelay;
		boolean isDependent = false;
		
		public RequestObject(long delay, String uri, boolean isDependent) {
			this.uri = uri;
			this.issueDelay = delay;
		}
		
		public void makeRequest() {
			try {
				issueTime = System.currentTimeMillis();
				client.prepareGet(uri).execute(new AsyncCompletionHandler<Response>() {			
					@Override
					public Response onCompleted(Response response) throws Exception {
						replyTime = System.currentTimeMillis();
						responseTime = replyTime-issueTime;
						log.info("{}\t{}\t{}\t{}",new Object[] {issueTime-offsetTime, responseTime, uri, response.getStatusText()});
						
						// Load dependent object for html						
						for (Cookie cookie:response.getCookies()) {
							log.info("Got Cookie! {}",cookie);
						}
						if (response.getContentType().contains("text/html")) {
							for (String link:getLinks(response.getResponseBody())) {
								countdownLock.lock();
								countdown++;
								new RequestObject(0, link, true).issue();
								countdownLock.unlock();
							}
						}
						
						// Reduce counter for downloaded objects
						countdownLock.lock();
						try {
							countdown--;
							if (countdown <= 0) {
								scheduler.shutdown();
								System.exit(0);
							}
						} finally {
							countdownLock.unlock();
						}
						return response;
					}
					
					@Override
					public void onThrowable(Throwable t) {
						log.info("{}\tFAILED\t{}\tFAILED",issueTime-offsetTime, uri);
						log.error("{}:{}:{}",new Object[] {t,t.getMessage(),t.getCause()});
						countdownLock.lock();
						try {
							countdown--;
							if (countdown <= 0) {
								scheduler.shutdown();
								System.exit(0);
							}
						} finally {
							countdownLock.unlock();
						}
					}
				});
			} catch (IOException e) {
				log.error("{}:{}/{}",new Object[] { e, e.getMessage(),e.getCause() });
			}					
		}
		
		public void issue() {		
			scheduler.schedule(new Runnable() {
				@Override
				public void run() {
					makeRequest();				
				}
			}, issueDelay - (System.currentTimeMillis() - offsetTime), TimeUnit.MILLISECONDS);		
		}
	}
	
	public HttpDriver(String tracefile, int waitTime) throws Exception {
		BufferedReader f = new BufferedReader(new FileReader(tracefile));
		String request;
		this.maxwait = waitTime;
		
		while ((request = f.readLine()) != null) {
			StringTokenizer tokens = new StringTokenizer(request);
			if (tokens.hasMoreTokens()) {
				long delay = Long.parseLong(tokens.nextToken());
				String uri = tokens.nextToken();
			
				requests.add(new RequestObject(delay,uri,false));
			}
		}		
	}
	
	public void run() throws Exception {
		log.info("Replaying {} requests",requests.size());
		countdown = requests.size();
		offsetTime = System.currentTimeMillis();
		for (RequestObject req:requests) {
			req.issue();
		}
		log.info("Issue time = {}",(System.currentTimeMillis()-offsetTime));
		scheduler.awaitTermination(30, TimeUnit.MINUTES);
		client.close();
	}
	
	public static void main(String[] args) throws Exception {
		long now = new Date().getTime();
		long schedule = ((now / 10000)+1)*10000;
		log.info("Will start in {} ms",(schedule - now));
		int waitTime = 30;
		if (args.length > 1) waitTime = Integer.parseInt(args[1]);
		final HttpDriver driver = new HttpDriver(args[0],waitTime);
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					driver.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, new Date(schedule));		
	}
}
