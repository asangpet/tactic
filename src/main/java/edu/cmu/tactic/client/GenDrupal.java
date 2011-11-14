package edu.cmu.tactic.client;

import java.io.File;
import java.io.Writer;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;

public class GenDrupal {
	int numRequest = 9000;
	int requestRate = 20; // Avg # requests per sec
	// Request rate 20 - too high
	// Request rate 15 - 31-65k (drupal2)
	// Request rate 12 - 32-60k (drupal_12)
	double searchPortion = 0.25;	// Probability of making a search request
	String prefix = "http://10.0.50.1";
	Logger log = LoggerFactory.getLogger(GenDrupal.class);
	String traceFile = "trace/drupal_20.trace";
	
	private AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
				.setFollowRedirects(true)
				.build();
	private AsyncHttpClient client = new AsyncHttpClient(config);
	
	double expRand(double lambda) {
		return Math.log(1-Math.random()) / (-lambda);
	}
	
	String nextWord() {
		String[] words = {"illa", "posted", "photo", "vogue", "abbas", "bene", "gilvus", "haero", "iaeceo", "pagus", "praesent", "proprius", "esca", "enim", "gemino", "mauris", "similis", "sudo", "venio", "virtus", "obruo", "decet", "posted", "photo", "conventio", "Neque", "nunc", "uxor", "Abdo", "causa", "haero", "zelus", "Dolore", "quadrum", "roto", "volutpat", "interdico", "ullamcorper"};
		return words[(int)(Math.round(Math.random()*words.length)) % words.length];
	}
	
	void generate() throws Exception {
		Writer w = Files.newWriter(new File(traceFile), Charset.forName("US-ASCII"));
		
		User user = new User(prefix+"/");		
		user.setClient(client);
		
		double ctime = 0;
		for (int i=0;i<numRequest;i++) {
			String url = prefix+user.nextUri();			
			if (Math.random()<searchPortion && !url.contains("search")) {
				url = prefix+"/search/site/"+nextWord();
			}
			ctime += expRand(requestRate);
			long curtime = Math.round(ctime*1000);
			log.info("{} {}",curtime,url);
			w.write(curtime+" "+url+"\n");
			user.setEntry(url);
		}
		client.close();
		w.close();		
	}
	
	public static void main(String[] args) throws Exception {
		new GenDrupal().generate();
	}
}
