package edu.cmu.tactic.client;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
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
import com.ning.http.client.Response;

public class User {
	static Logger log = LoggerFactory.getLogger(User.class);
	
	private String entry;
	AsyncHttpClient client;
	Zipf zipf;
	
	public User(String entranceUri) {
		entry = entranceUri;
	}
	
	public void setClient(AsyncHttpClient client) {
		this.client = client;
	}
	
	List<String> getResources(Document doc) {
		List<String> links = new LinkedList<String>();
		// Extract js/image source url
		Elements srcLinks = doc.select("[src]");
		for (Element elem:srcLinks) {			
			links.add(elem.tagName()+" "+elem.attr("src"));
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
	
	
	TreeSet<String> getLinks(Document doc) {
		TreeSet<String> links = new TreeSet<String>();		
		// Return referenced links
		Elements refs = doc.select("[href]");
		for (Element elem:refs) {
			String target = elem.attr("href");
			if (!target.startsWith("/") || target.endsWith(".xml") || target.endsWith(".ico")) continue;
			links.add(target);
		}
		
		return links;
	}
	
	public Collection<String> run() throws Exception {
		Collection<String> links = 
			client.prepareGet(entry).execute(new AsyncCompletionHandler<Collection<String>>() {
				@Override
				public Collection<String> onCompleted(Response response) throws Exception {
					if (response.getContentType().contains("text/html")) {
						Document doc = Jsoup.parse(response.getResponseBody());						
						
						return getLinks(doc);
					}
					return null;
				}
			}).get();
		return links;
	}
	
	public String nextUri() throws Exception {
		Collection<String> ref = run();
		if (zipf == null || ref.size() > zipf.getSize()) {
			zipf = new Zipf(ref.size(), 1);
		}
		int target = zipf.nextInt() % ref.size();
		Iterator<String> iter = ref.iterator();
		String uri = iter.next();
		for (int i=0;i<target;i++) {
			uri = iter.next();			
		}
		return uri;
	}
	
	public void setEntry(String entry) {
		this.entry = entry;
	}
}
