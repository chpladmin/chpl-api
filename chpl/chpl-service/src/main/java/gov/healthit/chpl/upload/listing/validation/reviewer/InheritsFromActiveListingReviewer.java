package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certifiedproduct.service.CertificationStatusEventsService;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.CertificationStatusUtil;
import gov.healthit.chpl.util.CertifiedProductUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("inheritsFromActiveListingReviewer")
@Log4j2
public class InheritsFromActiveListingReviewer {
    private CertifiedProductUtil cpUtil;
    private CertificationStatusEventsService certStatusEventService;
    private ErrorMessageUtil msgUtil;
    private Long daysAllowedSinceParentListingInactive;

    @Autowired
    public InheritsFromActiveListingReviewer(CertifiedProductUtil cpUtil,
            CertificationStatusEventsService certStatusEventService,
            ErrorMessageUtil msgUtil,
            @Value("${daysAllowedSinceParentListingInactive}") Long daysAllowedSinceParentListingInactive) {
        this.cpUtil = cpUtil;
        this.certStatusEventService = certStatusEventService;
        this.msgUtil = msgUtil;
        this.daysAllowedSinceParentListingInactive = daysAllowedSinceParentListingInactive;
    }

    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getIcs() == null) {
            return;
        }

        if (listing.getIcs().getParents() != null && listing.getIcs().getParents().size() > 0) {
            listing.getIcs().getParents().stream()
                .forEach(cpParent -> lookupListingIdAndStatus(cpParent));
            reviewListingParentsNotInactiveTooLong(listing.getIcs().getParents(), listing);
        }
    }

    private void lookupListingIdAndStatus(CertifiedProduct certifiedProduct) {
        if (certifiedProduct.getId() == null
                || StringUtils.isEmpty(certifiedProduct.getCertificationStatus())) {
            try {
                CertifiedProduct foundListing = cpUtil.getListing(certifiedProduct.getChplProductNumber());
                if (foundListing != null) {
                    certifiedProduct.setId(foundListing.getId());
                    certifiedProduct.setCertificationDate(foundListing.getCertificationDate());
                    certifiedProduct.setCertificationStatus(foundListing.getCertificationStatus());
                    certifiedProduct.setCuresUpdate(foundListing.getCuresUpdate());
                    certifiedProduct.setEdition(foundListing.getEdition());
                }
            } catch (Exception ex) {
                LOGGER.catching(ex);
            }
        }
    }

    private void reviewListingParentsNotInactiveTooLong(List<CertifiedProduct> parents, CertifiedProductSearchDetails listing) {
        parents.stream()
            .filter(parent -> CertificationStatusUtil.isInactive(parent.getCertificationStatus()))
            .forEach(inactiveParent -> {
                try {
                    CertificationStatusEvent currStatusEvent = certStatusEventService.getCurrentCertificationStatusEvent(inactiveParent.getId());
                    long numDaysInactive = ChronoUnit.DAYS.between(currStatusEvent.getEventDay(), LocalDate.now());
                    if (numDaysInactive > daysAllowedSinceParentListingInactive) {
                        listing.addBusinessErrorMessage(msgUtil.getMessage("listing.ics.parentListingInactive", inactiveParent.getChplProductNumber()));
                    }
                } catch (Exception ex) {
                    LOGGER.error("Unable to determine if listing " + listing.getChplProductNumber()
                        + " has an inactive ICS parent " + inactiveParent.getChplProductNumber(), ex);
                }
            });
    }
}
