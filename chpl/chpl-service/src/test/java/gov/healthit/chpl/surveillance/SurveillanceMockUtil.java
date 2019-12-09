package gov.healthit.chpl.surveillance;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.listing.ListingMockUtil;

@Component
public class SurveillanceMockUtil {
    private static final long WEEK_IN_MILLIS = 7*24*60*60*1000;

    @Autowired
    private ListingMockUtil listingMockUtil;

    public Surveillance createOpenSurveillance() {
        Surveillance surv = new Surveillance();
        surv.setStartDate(new Date(System.currentTimeMillis() - WEEK_IN_MILLIS));
        surv.setFriendlyId("SURV01");
        surv.setRandomizedSitesUsed(10);
        SurveillanceType type = new SurveillanceType();
        type.setId(1L);
        type.setName(SurveillanceType.RANDOMIZED);
        surv.setType(type);
        CertifiedProductSearchDetails mockListing = listingMockUtil.createValid2015Listing();
        CertifiedProduct listing = new CertifiedProduct();
        listing.setCertificationDate(mockListing.getCertificationDate());
        listing.setChplProductNumber(mockListing.getChplProductNumber());
        listing.setEdition(mockListing.getCertificationEdition().get("name").toString());
        listing.setId(mockListing.getId());
        surv.setCertifiedProduct(listing);
        surv.getRequirements().add(createSurveillanceRequirement(
                1L, "170.315 (a)(6)", SurveillanceResultType.NO_NON_CONFORMITY,
                SurveillanceRequirementType.CERTIFIED_CAPABILITY));
        return surv;
    }

    private SurveillanceRequirement createSurveillanceRequirement(
            Long id, String requirement, String resultName, String typeName) {
        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setId(id);
        req.setRequirement(requirement);
        SurveillanceResultType result = new SurveillanceResultType();
        result.setId(1L);
        result.setName(resultName);
        req.setResult(result);
        SurveillanceRequirementType type = new SurveillanceRequirementType();
        type.setId(1L);
        type.setName(typeName);
        req.setType(type);
        return req;
    }
}
