package gov.healthit.chpl.scheduler.job.onetime;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.activity.history.explorer.ChplProductNumberChangedActivityExplorer;
import gov.healthit.chpl.dao.CertifiedProductChplProductNumberHistoryDao;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "populateChplProductNumberHistoryLogger")
public class PopulateChplProductNumberHistoryJob implements Job {

    @Autowired
    private ChplProductNumberChangedActivityExplorer activityExplorer;

    @Autowired
    private CertifiedProductsWithNewStyleNumberDao listingDao;

    @Autowired
    private CertifiedProductChplProductNumberHistoryDao chplProductNumberChangeDao;

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Populate CHPL Product Number History job *********");
        try {
            List<CertifiedProduct> allListingsWithNewStyleChplProductNumbers
                = listingDao.findWithNewStyleChplProductNumber();
            LOGGER.info("There are " + allListingsWithNewStyleChplProductNumbers.size() + " listings with new-style CHPL Product Numbers.");
            //TODO loop through each



        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
        }
        LOGGER.info("********* Completed the Populate CHPL Product Number History job *********");
    }

    @Component
    @NoArgsConstructor
    public class CertifiedProductsWithNewStyleNumberDao extends BaseDAOImpl {

        @Transactional
        public List<CertifiedProduct> findWithNewStyleChplProductNumber() {
            List<CertifiedProductDetailsEntity> entities = entityManager.createQuery(
                    "SELECT DISTINCT cp "
                    + "FROM CertifiedProductDetailsEntity cp "
                    + "WHERE chplProductNumber NOT LIKE 'CHP-%'",
                    CertifiedProductDetailsEntity.class).getResultList();

            List<CertifiedProduct> results = new ArrayList<CertifiedProduct>();
            for (CertifiedProductDetailsEntity entity : entities) {
                CertifiedProduct cp = CertifiedProduct.builder()
                        .id(entity.getId())
                        .chplProductNumber(entity.getChplProductNumber())
                        .certificationDate(entity.getCertificationDate().getTime())
                        .certificationStatus(entity.getCertificationStatusName())
                        .curesUpdate(entity.getCuresUpdate())
                        .edition(entity.getYear())
                        .lastModifiedDate(entity.getLastModifiedDate().getTime())
                        .build();
                results.add(cp);
            }
            return results;
        }
    }
}
