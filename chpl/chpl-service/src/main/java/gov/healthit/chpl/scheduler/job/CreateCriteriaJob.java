package gov.healthit.chpl.scheduler.job;

import java.util.Date;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import net.sf.ehcache.CacheManager;

/**
 * This job creates criteria.
 */
public class CreateCriteriaJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("createCriteriaJobLogger");

    @Autowired
    private CertificationCriterionDAO certCriteriaDao;

    private HashMap<String, String> criteriaToAdd;

    private static final long EDITION_2015_ID = 3L;

    public CreateCriteriaJob() throws Exception {
        super();
        criteriaToAdd = new HashMap<String, String>();
        criteriaToAdd.put("170.315 (b)(10)", "Clinical Information Export");
        criteriaToAdd.put("170.315 (d)(12)", "Encrypt Authentication Credentials");
        criteriaToAdd.put("170.315 (d)(13)", "Multi-Factor Authentication");
        criteriaToAdd.put("170.315 (g)(10)", "Standardized API for Patient and Population Services");
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Create Criteria job. *********");
        for (String number : criteriaToAdd.keySet()) {
            CertificationCriterionDTO existingCert = certCriteriaDao.getByName(number);
            if (existingCert == null) {
                CertificationCriterionDTO toCreate = new CertificationCriterionDTO();
                toCreate.setNumber(number);
                toCreate.setTitle(criteriaToAdd.get(number));
                toCreate.setCertificationEditionId(EDITION_2015_ID);
                toCreate.setCreationDate(new Date());
                toCreate.setDeleted(false);
                toCreate.setRemoved(false);
                try {
                    certCriteriaDao.create(toCreate);
                } catch (final Exception ex) {
                    LOGGER.error("Exception creating criteria " + number, ex);
                }
            } else {
                LOGGER.error("Exception creating criteria " + number + "; already exists");
            }
        }
        //TODO: figure out how to add g1/g2 mappings for new criteria
        //TODO: Loop through all 2015 existing Listings and add these new criteria as success=false
        CacheManager.getInstance().clearAll();
        LOGGER.info("********* Completed the Create Criteria job. *********");
    }
}
