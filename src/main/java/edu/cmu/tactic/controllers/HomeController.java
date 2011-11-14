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
		responseMap.put("raw15",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15"), 900));
		responseMap.put("app15",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_app"), 900));
		responseMap.put("solr15",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_solr"), 900));
		responseMap.put("as15",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_app_solr"), 900));
		responseMap.put("af15",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_app_img"), 900));
		//responseMap.put("raw12",responseData.sampling(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_12"), 900));
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
		responseMap.put("app-solr",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_app_solr")));
		responseMap.put("app-nfs",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_app_img")));
		responseMap.put("app-mem-15",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_app_mem")));
		responseMap.put("app-db",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_app_db")));
		//responseMap.put("app-solr-mem15",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "drupal_rate_15_separate_app_solr_mem")));
		responseMap.put("current",responseData.getCdf(responseData.getResponseTime("10.0.50.1", "ACTION", "responseTime")));
		return responseMap;
	}
	
	@RequestMapping(value = "/graph", produces="application/json")
	public @ResponseBody HashMap<String, List<HashMap<String,Object>>> showGraph(Model model) {
		return analyzer.getInstance("cms").getAnalysisGraph().json();
	}
	
	@RequestMapping(value = "/analyze", produces="application/json") 
	public @ResponseBody Map<String, double[]> analyzeResponse(Model model) {
		return analyzer.getInstance("cms").analyze();
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
