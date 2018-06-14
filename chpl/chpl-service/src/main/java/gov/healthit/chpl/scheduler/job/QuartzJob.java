package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.quartz.Job;
import org.springframework.context.support.AbstractApplicationContext;

import gov.healthit.chpl.scheduler.QuartzLocalContext;
import gov.healthit.chpl.scheduler.QuartzLocalContextFactory;

/**
 * Base class for Quartz jobs that depend on Spring context.
 * @author alarned
 *
 */
public abstract class QuartzJob implements Job {
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
    private Properties properties;

    protected abstract void initiateSpringBeans(AbstractApplicationContext context) throws IOException;

    protected void setLocalContext() throws Exception {
        QuartzLocalContext ctx = QuartzLocalContextFactory.createLocalContext(
                getProperties().getProperty("dbDriverClass"));
        ctx.addDataSource(getProperties().getProperty("dataSourceName"),
                getProperties().getProperty("dataSourceConnection"),
                getProperties().getProperty("dataSourceUsername"),
                getProperties().getProperty("dataSourcePassword"));
    }

    protected File getDownloadFolder() throws IOException {
        String downloadFolderPath = getProperties().getProperty("downloadFolderPath");
        File downloadFolder = new File(downloadFolderPath);
        if (!downloadFolder.exists()) {
            if (!downloadFolder.mkdirs()) {
                throw new IOException("Can not create download folder directory");
            }
        }
        return downloadFolder;
    }

    protected Properties getProperties() throws IOException {
        if (properties == null || properties.isEmpty()) {
            InputStream in = QuartzJob.class.getClassLoader()
                    .getResourceAsStream(DEFAULT_PROPERTIES_FILE);
            if (in == null) {
                properties = null;
                throw new FileNotFoundException("Environment Properties File not found in class path.");
            } else {
                properties = new Properties();
                properties.load(in);
                in.close();
            }
        }
        return properties;
    }
}
