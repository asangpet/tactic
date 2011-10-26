package edu.cmu.tactic.config;

import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class TacticWebAppInitializer implements WebApplicationInitializer {
	Logger log = LoggerFactory.getLogger(TacticWebAppInitializer.class);
	
	@Override
	public void onStartup(ServletContext sc) throws ServletException {
		log.info("----TACTIC Application Server Started----");
		
		AnnotationConfigWebApplicationContext root = new AnnotationConfigWebApplicationContext();
		root.scan("edu.cmu.tactic.config");
		
		sc.addListener(new ContextLoaderListener(root));
		
		ServletRegistration.Dynamic appServlet = 
				sc.addServlet("appServlet", new DispatcherServlet(new GenericWebApplicationContext()));
		appServlet.setLoadOnStartup(1);
		Set<String> mappingConflicts = appServlet.addMapping("/");
		if (!mappingConflicts.isEmpty()) {
			throw new IllegalStateException("appServlet binded to existing mapping, update your tomcat");
		}
	}	
}
