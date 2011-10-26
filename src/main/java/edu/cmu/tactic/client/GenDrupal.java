package edu.cmu.tactic.client;

import java.io.File;
import java.io.Writer;
import java.nio.charset.Charset;

import com.google.common.io.Files;

public class GenDrupal {
	static final int nodeCount = 9000;
	static final int nodeOffset = 100;
	static final int numRequest = 10000;
	static final int requestRate = 10; // Avg # requests per sec
	static final String frontendURL = "http://10.0.50.1/";
	
	static double expRand(double lambda) {
		return Math.log(1-Math.random()) / (-lambda);
	}
	
	public static void main(String[] args) throws Exception {
		Writer w = Files.newWriter(new File("trace/drupal.trace"), Charset.forName("US-ASCII"));
		Zipf zipf = new Zipf(nodeCount,1);
		double ctime = 0;
		for (int i=0;i<numRequest;i++) {
			String url = frontendURL;
			int page = zipf.nextInt();
			String suffix = "?q=node/"+(page+nodeOffset);
			ctime += expRand(requestRate);
			long curtime = Math.round(ctime*1000);
			if (page > 0) {
				w.write(curtime+" "+url+suffix+"\n");
			} else {
				w.write(curtime+" "+url+"\n");
			}
		}
		w.close();
	}
}
