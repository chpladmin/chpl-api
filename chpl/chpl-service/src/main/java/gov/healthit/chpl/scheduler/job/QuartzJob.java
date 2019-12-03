package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.IOException;

import org.quartz.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 * Base class for Quartz jobs that depend on Spring context.
 * 
 * @author alarned
 *
 */
public abstract class QuartzJob implements Job {
    private Environment env;

    @Autowired
    public final void setEnviroment(final Environment env) {
        this.env = env;
    }

    protected File getDownloadFolder() throws IOException {
        String downloadFolderPath = env.getProperty("downloadFolderPath");
        File downloadFolder = new File(downloadFolderPath);
        if (!downloadFolder.exists()) {
            if (!downloadFolder.mkdirs()) {
                throw new IOException("Can not create download folder directory");
            }
        }
        return downloadFolder;
    }

}
