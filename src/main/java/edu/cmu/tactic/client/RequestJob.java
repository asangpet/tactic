package edu.cmu.tactic.client;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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
import org.slf4j.Logger;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Cookie;
import com.ning.http.client.Response;

public class RequestJob implements Job {
	String uri;
	long issueTime;
	long replyTime;
	long responseTime;
	long issueDelay;
	boolean isDependent = false;
	
	static boolean loadDependent = true;
	
	static AsyncHttpClient client;
	static Logger log;
	static AtomicInteger requestCounter = new AtomicInteger(0);
	
	static AtomicInteger countdown;
	static Scheduler scheduler;
	static long offsetTime;
	
	public RequestJob() { }
	
	public RequestJob(long delay, String uri, boolean isDependent) {
		this.uri = uri;
		this.issueDelay = delay;
		this.isDependent = isDependent;
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		context.getMergedJobDataMap();
		
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
					List<String> resources = null;
					if (loadDependent && response.getContentType().contains("text/html")) {
						resources = getLinks(response.getResponseBody());
					} /* 
					else if (response.getContentType().contains("text/css")) {
						resources = getResources(response.getResponseBody());
					} */
					if (resources != null) {
						// Sort resources
						List<String> styles, js, images;
						styles = new LinkedList<String>();
						js = new LinkedList<String>();
						images = new LinkedList<String>();
						for (String link:resources) {
							if (link.contains(".css")) {
								styles.add(link);
							} else if (link.contains(".js")) {
								js.add(link);
							} else
								images.add(link);								
						}
						LinkedList<String> links = new LinkedList<String>();
						links.addAll(styles);
						links.addAll(js);
						links.addAll(images);
						
						for (String link:links) {
							countdown.incrementAndGet();
							RequestJob.issue(0, response.getUri().resolve(new URI(link)).toString(), true);
						}
					}
					
					// Reduce counter for downloaded objects
					if (countdown.decrementAndGet() <=0) {
						scheduler.shutdown();
						System.exit(0);
					}
					return response;
				}
				
				@Override
				public void onThrowable(Throwable t) {
					log.info("{}\tFAILED\t{}\tFAILED",issueTime-offsetTime, uri);
					log.error("{}:{}:{}",new Object[] {t,t.getMessage(),t.getCause()});
					try {
						if (countdown.decrementAndGet() <=0) {
							scheduler.shutdown();
							client.close();
							System.exit(0);
						}
					} catch (Exception e) {
						log.error("Shutdown error {}",e);
					}
				}
			});
		} catch (IOException e) {
			log.error("{}:{}/{}",new Object[] { e, e.getMessage(),e.getCause() });
		}					
	}
	
	List<String> getLinks(String content) {
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
	
	List<String> getResources(String content) {
		//log.info("CSS:{}",content);
		List<String> links = new LinkedList<String>();
		Pattern p = Pattern.compile("url\\((.*)\\)");
		Matcher m = p.matcher(content);
		while (m.find()) {
			links.add(m.group(1));
		}
		return links;
	}
	
	public static void issue(long issueDelay, String uri, boolean isDependent) {
		JobDetail job = newJob(RequestJob.class)
				.withIdentity("Request-"+requestCounter.getAndIncrement())
				.usingJobData("uri", uri)
				.usingJobData("issueDelay", issueDelay)
				.usingJobData("isDependent", isDependent)
				.build();
		Trigger trigger = newTrigger()
				.startAt(new Date(issueDelay + offsetTime))
				.build();
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (Exception e) {
			log.error("Scheduling error - {}",e);
		}
	}
	
	public void issue() {
		RequestJob.issue(issueDelay, uri, isDependent);
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
	
}
