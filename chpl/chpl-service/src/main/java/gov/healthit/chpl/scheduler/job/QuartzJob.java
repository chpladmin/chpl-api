package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.IOException;

import org.quartz.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public abstract class QuartzJob implements Job {
    public static final String JOB_DATA_KEY_EMAIL = "email";
    public static final String JOB_DATA_KEY_ACB = "acb";
    public static final String JOB_DATA_KEY_SUBMITTED_BY_USER_ID = "submittedByUserId";
    protected static final String TEMP_DIR_NAME = "temp";

    private Environment env;

    @Autowired
    public final void setEnviroment(Environment environment) {
        this.env = environment;
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
