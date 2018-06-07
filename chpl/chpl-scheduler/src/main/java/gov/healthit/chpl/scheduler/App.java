package gov.healthit.chpl.scheduler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.context.support.AbstractApplicationContext;

import gov.healthit.chpl.scheduler.LocalContext;
import gov.healthit.chpl.scheduler.LocalContextFactory;
import gov.healthit.chpl.scheduler.job.CacheStatusAgeJob;

public abstract class App {
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
    protected Properties properties;

    protected abstract void initiateSpringBeans(AbstractApplicationContext context) throws IOException;

    protected void setLocalContext() throws Exception {
        LocalContext ctx = LocalContextFactory.createLocalContext(getProperties().getProperty("dbDriverClass"));
        ctx.addDataSource(getProperties().getProperty("dataSourceName"),
                getProperties().getProperty("dataSourceConnection"), getProperties().getProperty("dataSourceUsername"),
                getProperties().getProperty("dataSourcePassword"));
    }

    protected File getDownloadFolder() throws IOException {
        String downloadFolderPath = getProperties().getProperty("downloadFolderPath");
        File downloadFolder = new File(downloadFolderPath);
        if (!downloadFolder.exists()) {
            downloadFolder.mkdirs();
        }
        return downloadFolder;
    }

    protected Properties getProperties() throws IOException {
        if (properties == null || properties.isEmpty()) {
            InputStream in = CacheStatusAgeJob.class.getClassLoader()
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
