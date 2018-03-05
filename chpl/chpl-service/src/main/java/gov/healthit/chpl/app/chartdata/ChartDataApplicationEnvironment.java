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


/**
 * Defines and loads the environment for populating the statistics data.  Provides access to the Spring managed objects.
 * @author TYoung
 *
 */
public class ChartDataApplicationEnvironment {
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
    private static final Logger LOGGER = LogManager.getLogger(ChartDataApplicationEnvironment.class);
    private Properties properties;
    private AbstractApplicationContext applicationContext;
    private LocalContext localContext;

    /**
     * Default constructor.  Then the object is instantiated:
     * 1) the properties are loaded
     * 2) the datasource is created
     * 3) the Spring context is created
     * @throws Exception any exception
     */
    public ChartDataApplicationEnvironment() throws Exception {
        loadProperties();
        loadLocalContext();
        loadApplicationContext();
    }

    /**
     * Retrieves a Spring managed bean from the Spring context.
     * @param name this is the name of the Spring managed object
     * @return an Object from the Spring Context
     */
    public Object getSpringManagedObject(final String name) {
        LOGGER.info(applicationContext.getClassLoader());
        return applicationContext.getBean(name);
    }

    private void loadApplicationContext() {
        applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
    }

    private void loadLocalContext() throws Exception {
        localContext = LocalContextFactory.createLocalContext(properties.getProperty("dbDriverClass"));
        localContext.addDataSource(properties.getProperty("dataSourceName"),
                properties.getProperty("dataSourceConnection"), properties.getProperty("dataSourceUsername"),
                properties.getProperty("dataSourcePassword"));
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
