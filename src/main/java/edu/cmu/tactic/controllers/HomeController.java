package edu.cmu.tactic.controllers;

import java.text.DateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.cmu.tactic.model.Pet;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	@Inject 
	private Logger logger;
	
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
		
		return "home";
	}
	
	@RequestMapping(value = "/pets", consumes="application/json", method = RequestMethod.POST)
	public @ResponseBody List<Pet> showPet(@RequestBody Pet pet, Model model) {
		logger.info(pet.toString());
		List<Pet> l = new ArrayList<Pet>();
		l.add(pet);
		l.add(pet);
		return l;
	}
}
