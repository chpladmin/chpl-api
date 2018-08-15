package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.util.ChplProductNumberUtil;

@Component("pendingTestToolReviewer")
public class TestToolReviewer implements Reviewer {
    @Autowired TestToolDAO testToolDao;
    @Autowired private ChplProductNumberUtil productNumUtil;

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        Integer icsCodeInteger = productNumUtil.getIcsCode(listing.getUniqueId());
        
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria() == Boolean.TRUE) {
                if (cert.getTestTools() != null && cert.getTestTools().size() > 0) {
                    for (PendingCertificationResultTestToolDTO testTool : cert.getTestTools()) {
                        if (StringUtils.isEmpty(testTool.getName())) {
                            listing.getErrorMessages().add(
                                    "There was no test tool name found for certification " + cert.getNumber() + ".");
                        } else {
                            TestToolDTO tt = testToolDao.getByName(testTool.getName());
                            if (tt != null && tt.isRetired() && icsCodeInteger != null
                                    && icsCodeInteger.intValue() == 0) {
                                if (listing.hasIcsConflict()) {
                                    listing.getWarningMessages().add("Test Tool '" + testTool.getName()
                                    + "' can not be used for criteria '" + cert.getNumber()
                                    + "', as it is a retired tool, and this Certified Product does not carry ICS.");
                                } else {
                                    listing.getErrorMessages().add("Test Tool '" + testTool.getName()
                                    + "' can not be used for criteria '" + cert.getNumber()
                                    + "', as it is a retired tool, and this Certified Product does not carry ICS.");
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
