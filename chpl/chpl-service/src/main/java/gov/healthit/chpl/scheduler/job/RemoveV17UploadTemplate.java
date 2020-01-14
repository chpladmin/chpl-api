package gov.healthit.chpl.scheduler.job;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ff4j.FF4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.UploadTemplateVersionDAO;
import gov.healthit.chpl.dto.UploadTemplateVersionDTO;
import net.sf.ehcache.CacheManager;

/**
 * Marks version 17 of the upload template as deleted.
 */
public class RemoveV17UploadTemplate extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("removeV17UploadTemplateJobLogger");
    private static final String TEMPLATE_NAME = "2015 CHPL Upload Template v12";

    @Autowired
    private UploadTemplateVersionDAO uploadTemplateDao;

    @Autowired
    private FF4j ff4j;

    public RemoveV17UploadTemplate() throws Exception {
        super();
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Remove Upload Template v17 job. *********");
        List<UploadTemplateVersionDTO> uploadTemplates = uploadTemplateDao.findAll();
        boolean foundTemplate = false;
        for (UploadTemplateVersionDTO uploadTemplate : uploadTemplates) {
            if (uploadTemplate.getName().equals(TEMPLATE_NAME)) {
                foundTemplate = true;
                try {
                    uploadTemplateDao.delete(uploadTemplate.getId());
                    LOGGER.info("Deleted the upload template " + uploadTemplate.getName());
                } catch (Exception ex) {
                    LOGGER.error("Error deleting the upload template " + uploadTemplate.getName());
                }
            }
        }
        if (!foundTemplate) {
            LOGGER.info("No existing template found with name " + TEMPLATE_NAME);
        }
        CacheManager.getInstance().clearAll();
        LOGGER.info("********* Completed the Remove Upload Template v17 job. *********");
    }
}
