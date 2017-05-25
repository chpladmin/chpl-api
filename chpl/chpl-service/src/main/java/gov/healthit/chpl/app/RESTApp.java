package gov.healthit.chpl.app;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import gov.healthit.chpl.manager.SearchMenuManager;

public class RESTApp extends App {
	private static final Logger logger = LogManager.getLogger(RESTApp.class);
	private SearchMenuManager searchMenuManager;
	
	public static void main(String[] args) throws Exception {
		// setup application
		RESTApp restApp = new RESTApp();
		Properties props = restApp.getProperties();
		restApp.setLocalContext(props);
		AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		restApp.initiateSpringBeans(context, props);

		// Get authentication token for REST call to API
		Token token = new Token(props);
				
		// Get data as needed
		String url = props.getProperty("chplUrlBegin") + props.getProperty("basePath") + props.getProperty("insertAPIPathHere");
		HttpUtil.getRequest(url, null, props, token); // sample get request
		
		// Do stuff below...
	}
	
	@Override
	protected void initiateSpringBeans(AbstractApplicationContext context, Properties props) {
		this.setSearchMenuManager((SearchMenuManager)context.getBean("searchMenuManager")); // sample bean initiation
		// add additional beans to context here as needed
	}
	
	public SearchMenuManager getSearchMenuManager() {
		return searchMenuManager;
	}
	
	public void setSearchMenuManager(SearchMenuManager searchMenuManager) {
		this.searchMenuManager = searchMenuManager;
	}

}
