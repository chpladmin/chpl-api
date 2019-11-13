package gov.healthit.chpl.scheduler.job;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.UploadTemplateVersionDAO;
import gov.healthit.chpl.dto.UploadTemplateVersionDTO;

/**
 * Job that can be scheduled to mark all 2014 upload templates as deleted.
 * @author kekey
 *
 */
public class Remove2014UploadTemplateJob implements Job {
    private static final Logger LOGGER = LogManager.getLogger("remove2014UploadTemplateJobLogger");

    @Autowired
    private UploadTemplateVersionDAO uploadTemplateDao;

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Remove 2014 Upload Template job. *********");
        try {
            List<UploadTemplateVersionDTO> uploadTemplates = uploadTemplateDao.findAll();
            for (UploadTemplateVersionDTO uploadTemplate : uploadTemplates) {
                if (!StringUtils.isEmpty(uploadTemplate.getName()) && uploadTemplate.getName().contains("2014")) {
                    LOGGER.info("Found 2014 template: " + uploadTemplate.getName());
                    uploadTemplateDao.delete(uploadTemplate.getId());
                    LOGGER.info("Marked " + uploadTemplate.getName() + " as deleted.");
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the Remove 2014 Upload Template job. *********");
    }
}
