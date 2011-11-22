package edu.cmu.tactic.controllers;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

import edu.cmu.tactic.data.Response;
import edu.cmu.tactic.placement.Host;
import edu.cmu.tactic.placement.VirtualMachine;
import edu.cmu.tactic.services.AnalysisService;
import edu.cmu.tactic.services.ResponseDataService;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	@Inject private Logger logger;
	@Inject ResponseDataService responseData;
	@Inject AnalysisService analyzer;
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! the client locale is "+ locale.toString());
		
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		String formattedDate = dateFormat.format(date);
		
		model.addAttribute("serverTime", formattedDate );
		model.addAttribute("response", responseData.listServer("10.0.50.1"));
		return "home";
	}

	@RequestMapping(value = "/servers", produces="application/json")
	public @ResponseBody Page<Response> showRequest(@RequestParam("address") String address, Model model) {
		logger.info(address);
		Page<Response> response = responseData.listServer(address);
		logger.info("page records:"+response.getContent().size());
		return response;
	}
	@RequestMapping(value = "/requests", produces="application/json")
	public @ResponseBody double[] showRequest(Model model) {
		//Page<Response> response = responseData.listRequest();
		return responseData.listRequest();
		//logger.info("page records:"+response.getContent().size());
		//return response;
	}
	@RequestMapping(value = "/responseTime", produces="application/json")
	public @ResponseBody Map<String, double[]> showActionResponseTime(Model model) {
		Map<String, double[]> responseMap = new HashMap<String,double[]>();
		//responseMap.put("raw20",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_20"), 900));
		//responseMap.put("raw18",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_18"), 900));
		//responseMap.put("raw15",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15"), 900));
		//responseMap.put("app15",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_app"), 900));
		//responseMap.put("solr15",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_solr"), 900));
		//responseMap.put("as15",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_app_solr"), 900));
		//responseMap.put("af15",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_app_img"), 900));
		//responseMap.put("current",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "responseTime"), 900));
		//responseMap.put("raw12",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_12"), 900));
		
		/*
		responseMap.put("app-0",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "app_0"), 900));
		responseMap.put("app-1",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "app_1"), 900));
		responseMap.put("app-solr-0",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "app_solr_0"), 900));
		responseMap.put("app-solr-1",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "app_solr_1"), 900));
		responseMap.put("current",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "responseTime"), 900));
		*/
		responseMap.put("current",responseData.getResponseTime("10.0.50.1", "ACTION", "responseTime"));
		
		return responseMap;
	}
	
	@RequestMapping(value = "/scatterResponseTime", produces="application/json")
	public @ResponseBody Map<String, double[][]> showScatterResponseTime(Model model) {
		Map<String, double[][]> responseMap = new HashMap<String,double[][]>();		
		responseMap.put("current",responseData.sampling2d(responseData.getRequestResponseTime("10.0.50.1", "ACTION", "responseTime"),900));		
		responseMap.put("app-bundle",responseData.sampling2d(responseData.getRequestResponseTime("10.0.50.1", "ACTION", "multi_app_12345_1"),900));
		responseMap.put("app-12-345",responseData.sampling2d(responseData.getRequestResponseTime("10.0.50.1", "ACTION", "multi_app_345_1"),900));
		return responseMap;
	}
	
	@RequestMapping(value = "/cdf", produces="application/json")
	public @ResponseBody Map<String, double[]> showActionCdf(Model model) {
		Map<String, double[]> responseMap = new HashMap<String,double[]>();
		/*
		responseMap.put("raw20",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_20")));
		responseMap.put("raw18",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_18")));
		responseMap.put("raw15",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15")));
		responseMap.put("raw12",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_12")));
		*/
		//responseMap.put("raw15",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15")));
		//responseMap.put("app15",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_app")));
		//responseMap.put("solr15",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_solr")));
		
		/*** App with other servers
		responseMap.put("app",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_app_2")));
		responseMap.put("app-solr",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_app_solr")));
		responseMap.put("app-img",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_app_img_localimg")));
		responseMap.put("app-mem",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_app_mem")));
		responseMap.put("app-db",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_app_db")));
		**/
		
		/** App without cache effect
		responseMap.put("app-0",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "app_0")));
		responseMap.put("app-solr-0",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "app_solr_0")));
		responseMap.put("app-db-0",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "app_db_0")));
		responseMap.put("app-img-0",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "app_img_0")));
		responseMap.put("app-var-0",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "app_varnish_0")));
		responseMap.put("app-mem-0",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "app_mem_0")));

		responseMap.put("app-1",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "app_1")));
		responseMap.put("app-solr-1",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "app_solr_1")));
		responseMap.put("app-db-1",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "app_db_1")));
		responseMap.put("app-img-1",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "app_img_1")));
		responseMap.put("app-var-1",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "app_varnish_1")));
		responseMap.put("app-mem-1",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "app_mem_1")));
		**/
		double[] currentCdf = responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "responseTime"));
		responseMap.put("current",currentCdf);		
		//responseMap.put("app-vdms1-2345n",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "multi_vdms1_2345n")));
		
		/* selected scenarios */
		responseMap.put("vmns1-2345d4",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "multi_vmns1_2345d_run_4")));		
		responseMap.put("vmn12-345ds5",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "multi_vmn12_345ds_run_5")));		
		responseMap.put("vdns1-2345m3",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "multi_vdns1_2345m_run_3")));
		responseMap.put("vdm12-345sn4",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "multi_vdm12_345sn_run_4")));
		responseMap.put("vdms1-2345n5",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "multi_vdms1_2345n_run_5")));
		responseMap.put("vdn12-345ms3",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "multi_vdn12_345ms_run_3")));
		/*
		v 
		  n	d 1 2	ok
		  n d m 1	x
		  n d s 1 	ok
		  n m s 1 	ok
		  n m 1 2 	ok
		  n s 1 2	x
			
		  d 1 2 3	x
		  d m 1 2	ok
		  d m s 1	ok
		  d s 1 2	x
		  m 1 2 3	x
		  m s 1 2	x
		  s 1 2 3	x
		*/
		
		/* Candidates */
		
		
		/*
		responseMap.put("app-bundle",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "multi_app_12345_1")));
		responseMap.put("app-1-2345",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "multi_app_2345_1")));
		responseMap.put("app-12-345",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "multi_app_345_1")));
		responseMap.put("app-12-v345r2",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "multi_app12_v345_2")));
		responseMap.put("app-12-v345r3",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "multi_app12_v345_3")));
		*/
		
		double[] target = new double[currentCdf.length];
		for (int i=0;i<target.length;i++) target[i] = 0.95;
		responseMap.put("target",target);
		return responseMap;
	}
	
	@RequestMapping(value = "/graph", produces="application/json")
	public @ResponseBody HashMap<String, List<HashMap<String,Object>>> showGraph(Model model) {
		return analyzer.getInstance("shardcms").getAnalysisGraph().json();
	}
	
	@RequestMapping(value = "/analyze", produces="application/json") 
	public @ResponseBody Map<String, double[]> analyzeResponse(Model model) {
		return analyzer.getInstance("shardcms").analyze();
	}
	
	@RequestMapping(value = "/place", produces="application/json") 
	public @ResponseBody Map<String, List<String>> placeVm(Model model) {
		Map<Host,Collection<VirtualMachine>> result = analyzer.getInstance("demo").calculatePlacement();
		Map<String,List<String>> output = new LinkedHashMap<String, List<String>>();
		for (Host host:result.keySet()) {
			List<String> vmList = new LinkedList<String>();
			for (VirtualMachine vm:result.get(host)) {
				vmList.add(vm.getName());
			}
			output.put(host.getName(), vmList);
		}
		return output;
	}
	
}
