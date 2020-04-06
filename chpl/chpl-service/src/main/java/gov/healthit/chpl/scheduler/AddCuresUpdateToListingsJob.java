package gov.healthit.chpl.scheduler;

import java.util.Date;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.SpecialProperties;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.CuresUpdateEventDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.CuresUpdateEventDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.service.CuresUpdateService;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class AddCuresUpdateToListingsJob extends QuartzJob {

    @Autowired
    private CuresUpdateService curesUpdateService;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private SpecialProperties specialProperties;

    @Autowired
    private CuresUpdateEventDAO curesUpdateDao;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Update Listing To Cures Updated job. *********");

        getAll2015Listings().stream()
                .forEach(dto -> processListing(dto));

        LOGGER.info("********* Completed the Update Listing To Cures Updated  job. *********");

    }

    private void processListing(CertifiedProductDetailsDTO cp) {
        try {
            LOGGER.info("Retrieving listing information for  (" + cp.getId() + ") " + cp.getChplProductNumber());
            CertifiedProductSearchDetails listing = certifiedProductDetailsManager.getCertifiedProductDetails(cp.getId());
            if (curesUpdateService.isCuresUpdate(listing)) {
                updateListingAsCuresUpdated(listing.getId());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    private void updateListingAsCuresUpdated(Long certifiedProductId) throws EntityCreationException, EntityRetrievalException {
        addCuresUpdateEvent(certifiedProductId, true, specialProperties.getEffectiveRuleDate());
        addCuresUpdateActivity();
    }

    private void addCuresUpdateActivity() {

    }

    private void addCuresUpdateEvent(Long certifiedProductId, Boolean curesUpdate, Date eventDate)
            throws EntityCreationException, EntityRetrievalException {
        CuresUpdateEventDTO dto = new CuresUpdateEventDTO();
        dto.setCertifiedProductId(certifiedProductId);
        dto.setCuresUpdate(curesUpdate);
        dto.setEventDate(eventDate);
        curesUpdateDao.create(dto);
    }

    private List<CertifiedProductDetailsDTO> getAll2015Listings() {
        return certifiedProductDAO.findByEdition("2015");
    }

}
