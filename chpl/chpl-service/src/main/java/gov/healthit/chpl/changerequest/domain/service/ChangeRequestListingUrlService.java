package gov.healthit.chpl.changerequest.domain.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestListingUrlDAO;
import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestListingUrl;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public abstract class ChangeRequestListingUrlService extends ChangeRequestDetailsService<ChangeRequestListingUrl> {
    private ChangeRequestDAO crDAO;
    private ChangeRequestListingUrlDAO crListingUrlDAO;
    private CertifiedProductDetailsManager certifiedProductDetailsManager;
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDAO;

    @Autowired
    public ChangeRequestListingUrlService(ChangeRequestDAO crDAO,
            ChangeRequestListingUrlDAO crListingUrlDAO,
            CertifiedProductDetailsManager certifiedProductDetailsManager,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDAO) {
        super();
        this.crDAO = crDAO;
        this.crListingUrlDAO = crListingUrlDAO;
        this.certifiedProductDetailsManager = certifiedProductDetailsManager;
        this.developerCertificationBodyMapDAO = developerCertificationBodyMapDAO;
    }

    @Override
    public ChangeRequestListingUrl getByChangeRequestId(Long changeRequestId, Long developerId) throws EntityRetrievalException {
        return crListingUrlDAO.getByChangeRequestId(changeRequestId);
    }

    @Override
    public Long create(Long changeRequestId, Object changeRequestDetails) {
        try {
            ChangeRequestListingUrl details = (ChangeRequestListingUrl) changeRequestDetails;
            // If CR details match the values from the existing listing, just return
            if (getAffectedUrl(certifiedProductDetailsManager.getCertifiedProductDetails(details.getListing().getId())).equals(details.getUrl())) {
                return null;
            }

            System.out.println("Creating new listing url change request");
            Long newCrId = crListingUrlDAO.create(changeRequestId, details);
            System.out.println("Created listing url change request with id " + newCrId);
            return newCrId;
        } catch (EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(ChangeRequest cr) throws InvalidArgumentsException {
        try {
            // Get the current cr to determine if the request details changed
            ChangeRequest crFromDb = crDAO.get(cr.getId());
            // Convert the map of key/value pairs to a ChangeRequestListingUrl object
            ChangeRequestListingUrl crListingUrl = (ChangeRequestListingUrl) cr.getDetails();
            // Use the id from the DB, not the object. Client could have changed the id.
            crListingUrl.setId(((ChangeRequestListingUrl) crFromDb.getDetails()).getId());
            cr.setDetails(crListingUrl);

            if (!((ChangeRequestListingUrl) cr.getDetails()).equals((crFromDb.getDetails()))) {
                crListingUrlDAO.update((ChangeRequestListingUrl) cr.getDetails());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CertificationBody> getAssociatedCertificationBodies(ChangeRequest cr) {
        return developerCertificationBodyMapDAO.getCertificationBodiesForDeveloper(cr.getDeveloper().getId());
    }

    protected String getChplProductNumber(ChangeRequest cr) {
        String chplProductNumber = "";
        if (cr.getDetails() != null && ((ChangeRequestListingUrl) cr.getDetails()).getListing().getId() != null) {
            try {
                CertifiedProductSearchDetails listing = certifiedProductDetailsManager.getCertifiedProductDetails(((ChangeRequestListingUrl) cr.getDetails()).getListing().getId());
                chplProductNumber = listing.getChplProductNumber();
            } catch (EntityRetrievalException e) {
                LOGGER.error("Could not locate listing with id {}", ((ChangeRequestListingUrl) cr.getDetails()).getListing().getId(), e);
            }
        }
        return chplProductNumber;
    }

    protected abstract String getAffectedUrl(CertifiedProductSearchDetails listing);
}
