package edu.cmu.tactic.controllers;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
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
		model.addAttribute("response", responseData.listServer("192.168.0.106"));
		return "home";
	}

	@RequestMapping(value = "/servers", produces="application/json")
	public @ResponseBody Page<Response> showRequest(@RequestParam("address") String address, Model model) {
		logger.info(address);
		Page<Response> response = responseData.listServer(address);
		logger.info("page records:"+response.getContent().size());
		return response;
	}
	
	@RequestMapping(value = "/graph", produces="application/json")
	public @ResponseBody HashMap<String, List<HashMap<String,Object>>> showGraph(Model model) {
		return analyzer.getDefaultService().getAnalysisGraph().json();
	}
	
	@RequestMapping(value = "/analyze", produces="application/json") 
	public @ResponseBody Map<String, double[]> analyzeResponse(Model model) {
		return analyzer.analyze();
	}
}
