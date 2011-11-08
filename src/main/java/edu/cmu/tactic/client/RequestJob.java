package edu.cmu.tactic.client;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Cookie;
import com.ning.http.client.Response;

public class RequestJob implements Job {
	String uri;
	int actionId;
	long issueTime;
	long replyTime;
	long responseTime;
	long issueDelay;
	boolean isDependent = false;
	AtomicInteger dependentCounter = null;
	
	static boolean loadDependent = true;
	
	static Collector collector;
	static AsyncHttpClient client;
	static Logger log;
	static AtomicInteger requestCounter = new AtomicInteger(0);
	
	static AtomicInteger countdown;
	static Scheduler scheduler;
	static long offsetTime;
	
	static AtomicInteger actionCounter = new AtomicInteger(0);
	static ConcurrentHashMap<Integer, Long> actionTime = new ConcurrentHashMap<Integer, Long>();
	static ConcurrentHashMap<Integer, RequestJob> actionMap = new ConcurrentHashMap<Integer, RequestJob>();
	
	public RequestJob() { }
	
	public RequestJob(long delay, String uri, boolean isDependent) {
		this.uri = uri;
		this.issueDelay = delay;
		this.isDependent = isDependent;
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		context.getMergedJobDataMap();
		if (!isDependent) {
			log.debug("{} {}",actionId, uri);
			actionMap.put(actionId, this);
		}
		try {
			issueTime = System.currentTimeMillis();
			client.prepareGet(uri).execute(new AsyncCompletionHandler<Response>() {			
				@Override
				public Response onCompleted(Response response) throws Exception {
					replyTime = System.currentTimeMillis();
					responseTime = replyTime-issueTime;
					actionTime.put(actionId, replyTime);
					log.debug("{}\t{}\t{}\t{}",new Object[] {issueTime-offsetTime, responseTime, uri, response.getStatusText()});
					
					// Load dependent object for html						
					for (Cookie cookie:response.getCookies()) {
						log.info("Got Cookie! {}",cookie);
					}
					List<String> resources = null;
					if (loadDependent && response.getContentType().contains("text/html")) {
						resources = getResources(response.getResponseBody());
					}
					if (resources != null) {
						dependentCounter = new AtomicInteger(resources.size());
						for (String link:resources) {
							int priority = 1;
							if (link.contains(".css")) priority = 3;
							else if (link.contains(".js")) priority = 2;
							RequestJob.issue(0, response.getUri().resolve(new URI(link)).toString(), true, priority, actionId);
						}
					}
					
					handleClose(true);
					
					return response;
				}
				
				@Override
				public void onThrowable(Throwable t) {
					log.info("{}\tFAILED\t{}\tFAILED",issueTime-offsetTime, uri);
					log.error("{}:{}:{}",new Object[] {t,t.getMessage(),t.getCause()});
					
					try {
						handleClose(false);
					} catch (Exception e) {
						log.error("Shutdown error {}",e);
					}
				}
				
				void handleClose(boolean success) throws Exception {
					// Reduce counter for downloaded objects
					RequestJob job = RequestJob.actionMap.get(actionId);
					if (job.dependentCounter == null || isDependent && job.dependentCounter.decrementAndGet() <= 0) {
						// Finish loading last request for the page
						collector.record(job, replyTime, success);
						//log.info("{}\t{}\tACTION:{}", new Object[] { parent.issueTime - offsetTime, replyTime - parent.issueTime, parent.uri });
						RequestJob.actionMap.remove(actionId);
						if (countdown.decrementAndGet() <=0) {
							new Thread("Terminator") {
								public void run() {
									try {
										log.info("Completed in {} ms", (System.currentTimeMillis() - offsetTime));
										client.close();
										scheduler.shutdown();										
									} catch (Exception e) {
										e.printStackTrace();
									}
								};
							}.start();
						}					
					}
				}
			});
		} catch (IOException e) {
			log.error("{}:{}/{}",new Object[] { e, e.getMessage(),e.getCause() });
		}					
	}
	
	List<String> getResources(String content) {
		List<String> links = new LinkedList<String>();
		Document doc = Jsoup.parse(content);						
		// Extract CSS
		Elements cssLinks = doc.select("style");
		for (Element elem:cssLinks) {
			//Pattern p = Pattern.compile(".*url(\"(http.*)\").*");
			Pattern p = Pattern.compile("url\\([\"\'](.*)[\"\']\\)");
			Matcher m = p.matcher(elem.html());
			while (m.find()) {
				links.add(m.group(1));
			}
		}
		// Extract js/image source url
		Elements srcLinks = doc.select("[src]");
		for (Element elem:srcLinks) {
			links.add(elem.attr("src"));
			//log.info("{}",elem.attr("src"));
		}
		
		return links;
	}

	public static void issue(long issueDelay, String uri, boolean isDependent, int priority, int actionId) {
		JobDetail job = newJob(RequestJob.class)
				.withIdentity("Request-"+requestCounter.getAndIncrement())
				.usingJobData("uri", uri)
				.usingJobData("issueDelay", issueDelay)
				.usingJobData("isDependent", isDependent)
				.usingJobData("actionId", actionId)
				.build();		
		TriggerBuilder<Trigger> builder = newTrigger().withPriority(priority);
		builder = (issueDelay==0)? builder.startNow() : builder.startAt(new Date(issueDelay + offsetTime));
		Trigger trigger = builder.build();		
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (Exception e) {
			log.error("Scheduling error - {}",e);
		}
	}
	
	public void issue() {
		int actionId = actionCounter.getAndIncrement();
		RequestJob.issue(issueDelay, uri, isDependent, 10, actionId);
	}
	
	public void setIsDependent(boolean isDependent) {
		this.isDependent = isDependent;
	}
	public void setIssueDelay(long issueDelay) {
		this.issueDelay = issueDelay;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public void setActionId(int actionId) {
		this.actionId = actionId;
	}
	
}
