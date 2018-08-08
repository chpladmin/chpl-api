package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class SedG3Reviewer implements Reviewer {
    private static final String G3_2014 = "170.314 (g)(3)";
    private static final String G3_2015 = "170.315 (g)(3)";
    @Autowired private ErrorMessageUtil msgUtil;
    
    @Override
    public void review(CertifiedProductSearchDetails listing) {
        boolean foundSedCriteria = false;
        boolean attestsToSed = false;

        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.isSuccess() != null && cert.isSuccess().booleanValue()) {
                if (cert.isSed() != null && cert.isSed()) {
                    foundSedCriteria = true;
                }
                if (cert.getNumber().equalsIgnoreCase(G3_2014)
                        || cert.getNumber().equalsIgnoreCase(G3_2015)) {
                    attestsToSed = true;
                }
            }
        }
        if (foundSedCriteria && !attestsToSed) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.foundSedCriteriaWithoutAttestingSed"));
        }
        if (!foundSedCriteria && attestsToSed) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.foundNoSedCriteriaButAttestingSed"));
        }
    }
}
