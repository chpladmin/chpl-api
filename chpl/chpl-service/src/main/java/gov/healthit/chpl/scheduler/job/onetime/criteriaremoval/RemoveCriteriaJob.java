package gov.healthit.chpl.scheduler.job.onetime.criteriaremoval;

import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.sharedstore.listing.SharedListingStoreProvider;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "removeCriteriaJobLogger")
public class RemoveCriteriaJob extends QuartzJob {

    @Autowired
    private CertificationCriterionDAO certCriteriaDao;

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private SharedListingStoreProvider sharedListingStoreProvider;

    @Autowired
    private CacheManager cacheManager;

    private static final String[] CRITERIA_TO_REMOVE = {
            "170.315 (b)(6):Data Export"};

    public RemoveCriteriaJob() throws Exception {
        super();
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Remove Criteria job. *********");
        for (String criteria : CRITERIA_TO_REMOVE) {
            try {
                String[] criterionElements = criteria.split(":");
                CertificationCriterion criterion = certCriteriaDao.getByNumberAndTitle(criterionElements[0], criterionElements[1]);
                criterion.setRemoved(true);
                certCriteriaDao.update(criterion);
                LOGGER.info("Updated criteria " + criteria);
            } catch (final Exception ex) {
                LOGGER.error("Exception updating criteria " + criteria, ex);
            }
        }
        cacheManager.getCacheNames().stream()
                .forEach(name -> cacheManager.getCache(name).invalidate());

        LOGGER.info("Retrieving all 2015 listings");
        List<CertifiedProductDetailsDTO> listings = certifiedProductDAO.findByEdition(
                CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        LOGGER.info("Completed retreiving all 2015 listings");
        listings.parallelStream()
            .forEach(dto -> sharedListingStoreProvider.remove(dto.getId()));
        LOGGER.info("********* Completed the Remove Criteria job. *********");
    }
}
