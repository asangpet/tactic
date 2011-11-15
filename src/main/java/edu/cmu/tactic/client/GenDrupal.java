package edu.cmu.tactic.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;

public class GenDrupal {
	int numRequest = 10000;
	int requestRate = 20; // Avg # requests per sec
	// Request rate 20 - too high
	// Request rate 15 - 31-65k (drupal2)
	// Request rate 12 - 32-60k (drupal_12)
	double searchPortion = 0.25;	// Probability of making a search request
	String prefix = "http://10.0.50.1";
	Logger log = LoggerFactory.getLogger(GenDrupal.class);
	String traceFile = "trace/drupal_progressive.trace";
	String srcFile = "trace/drupal_url.trace";
	
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
		BufferedWriter w = Files.newWriter(new File(traceFile), Charset.forName("US-ASCII"));
		
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
			w.write(curtime+" "+url);
			w.newLine();
			user.setEntry(url);
		}
		client.close();
		w.close();		
	}
	
	void transform() {
		try {
			BufferedWriter writer = Files.newWriter(new File(traceFile), Charset.forName("US-ASCII"));
			BufferedReader reader = Files.newReader(new File(srcFile), Charset.forName("US-ASCII"));
			double ctime = 0;
			double[] rate     = new double[] {1,     5,     10,    15,     25,    10,     15,    10,     20,    10,    15};
			double[] duration = new double[] {30000, 30000, 60000, 60000,  15000, 30000,  60000, 30000,  15000, 30000, 0 };
			double[] timeMark = new double[duration.length];
			timeMark[0] = duration[0];
			for (int i=1;i<timeMark.length;i++) timeMark[i] = duration[i]+timeMark[i-1];
			
			int mode = 0;
			double currentRate = rate[0];
			for (int i=0;i<numRequest;i++) {
				String url = reader.readLine().split(" ")[1];
				ctime += expRand(currentRate);
				long curtime = Math.round(ctime*1000);
				if (curtime > timeMark[mode] && mode < timeMark.length-1) {
					mode++;
					currentRate = rate[mode];
					System.out.println("Rate switch at "+curtime+" to "+currentRate);
				}
				writer.write(curtime+" "+url);
				writer.newLine();
			}
			reader.close();
			writer.close();
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		//new GenDrupal().generate();
		new GenDrupal().transform();
	}
}
