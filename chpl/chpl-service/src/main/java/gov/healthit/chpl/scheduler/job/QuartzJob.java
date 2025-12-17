package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.IOException;

import org.quartz.Job;
import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.scheduler.AuthenticatedUserAwareJob;
import gov.healthit.chpl.util.FileUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class QuartzJob extends AuthenticatedUserAwareJob implements Job {
    public static final String JOB_DATA_KEY_EMAIL = "email";
    public static final String JOB_DATA_KEY_ACB = "acb";
    public static final String JOB_DATA_KEY_SUBMITTED_BY_USER_ID = "submittedByUserId";
    protected static final String TEMP_DIR_NAME = "temp";

    private FileUtils fileUtils;

    @Autowired
    public final void setEnviroment(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    protected File getDownloadFolder() throws IOException {
        return fileUtils.getDownloadFolder();
    }

}
