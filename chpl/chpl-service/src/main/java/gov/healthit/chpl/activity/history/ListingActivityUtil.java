package gov.healthit.chpl.activity.history;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.certifiedproduct.service.CertificationStatusEventsService;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ListingActivityUtil {
    private CertificationCriterionService certificationCriterionService;
    private CertificationStatusEventsService certificationStatusEventsService;

    private ObjectMapper jsonMapper;

    public ListingActivityUtil(CertificationCriterionService certificationCriterionService, CertificationStatusEventsService certificationStatusEventsService) {
        this.certificationCriterionService = certificationCriterionService;
        this.certificationStatusEventsService=  certificationStatusEventsService;

        jsonMapper = new ObjectMapper();
    }

    public CertifiedProductSearchDetails getListing(String listingJson) {
        return getListing(listingJson, false);
    }

    public CertifiedProductSearchDetails getListing(String listingJson, boolean normalize) {
        CertifiedProductSearchDetails listing = null;
        if (!StringUtils.isEmpty(listingJson)) {
            try {
                listing =
                    jsonMapper.readValue(listingJson, CertifiedProductSearchDetails.class);

                if (normalize) {
                    listing.getCertificationResults().stream()
                            .forEach(cr -> normalizeCertificationResult(cr));

                    normalizeCertificationStatusHistory(listing);

                }
            } catch (Exception ex) {
                LOGGER.error("Could not parse activity JSON " + listingJson, ex);
            }
        }
        return listing;
    }

    private void normalizeCertificationResult(CertificationResult result) {
        //This method assumes that if the Criterion does not exist, the criterion is not the Cures version
        if (result.getCriterion() == null) {
            List<CertificationCriterion> criteria = certificationCriterionService.getByNumber(result.getNumber());
            //Get the first non-Cures criterion
            result.setCriterion(criteria.stream()
                    .filter(c -> !Util.isCures(c))
                    .findFirst()
                    .get());

        }
    }

    private void normalizeCertificationStatusHistory(CertifiedProductSearchDetails listing) {
        try {
            listing.setCertificationEvents(certificationStatusEventsService.getCertificationStatusEvents(listing.getId()));
        } catch (EntityRetrievalException e) {
            LOGGER.catching(e);
            listing.setCertificationEvents(new ArrayList<CertificationStatusEvent>());
        }
    }

}
