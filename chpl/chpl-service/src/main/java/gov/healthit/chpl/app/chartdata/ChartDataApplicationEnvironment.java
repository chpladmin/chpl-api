package gov.healthit.chpl.app.chartdata;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import gov.healthit.chpl.app.AppConfig;
import gov.healthit.chpl.app.LocalContext;
import gov.healthit.chpl.app.LocalContextFactory;

public class ChartDataApplicationEnvironment {
	private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
    private static final Logger LOGGER = LogManager.getLogger(ChartDataApplicationEnvironment.class);
    private Properties properties;
    private AbstractApplicationContext applicationContext;
    private LocalContext localContext;
    
	public ChartDataApplicationEnvironment() throws Exception {
        loadProperties();
        loadLocalContext();
        loadApplicationContext();
	}
	
	
	private void loadApplicationContext() {
		applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
	}
	
	private void loadLocalContext() throws Exception {
		localContext = LocalContextFactory.createLocalContext(properties.getProperty("dbDriverClass"));
		localContext.addDataSource(properties.getProperty("dataSourceName"),
                properties.getProperty("dataSourceConnection"),
                properties.getProperty("dataSourceUsername"),
                properties.getProperty("dataSourcePassword"));
	}
	
	public Object getSpringManagedObject(String name) {
        LOGGER.info(applicationContext.getClassLoader());
        return applicationContext.getBean(name);
    }
	
	private void loadProperties() throws IOException {
		InputStream in = ChartData.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
        if (in == null) {
            properties = null;
            throw new FileNotFoundException("Environment Properties File not found in class path.");
        } else {
            properties = new Properties();
            properties.load(in);
            in.close();
        }
    }

}
