package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.util.ChplProductNumberUtil;

@Component
public class TestToolReviewer implements Reviewer {
    @Autowired TestToolDAO testToolDao;
    @Autowired private ChplProductNumberUtil productNumUtil;

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        Integer icsCodeInteger = productNumUtil.getIcsCode(listing.getChplProductNumber());

        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.getTestToolsUsed() != null && cert.getTestToolsUsed().size() > 0) {
                for (CertificationResultTestTool testTool : cert.getTestToolsUsed()) {
                    if (StringUtils.isEmpty(testTool.getTestToolName())) {
                        listing.getErrorMessages()
                        .add("There was no test tool name found for certification " + cert.getNumber() + ".");
                    } else {
                        TestToolDTO tt = testToolDao.getByName(testTool.getTestToolName());
                        if (tt == null) {
                            listing.getErrorMessages().add("No test tool with " + testTool.getTestToolName()
                            + " was found for criteria " + cert.getNumber() + ".");
                        } else if (tt.isRetired() && icsCodeInteger != null && icsCodeInteger.intValue() == 0) {
                            if (listing.hasIcsConflict()) {
                                listing.getWarningMessages().add("Test Tool '" + testTool.getTestToolName()
                                + "' can not be used for criteria '" + cert.getNumber()
                                + "', as it is a retired tool, and this Certified Product does not carry ICS.");
                            } else {
                                listing.getErrorMessages().add("Test Tool '" + testTool.getTestToolName()
                                + "' can not be used for criteria '" + cert.getNumber()
                                + "', as it is a retired tool, and this Certified Product does not carry ICS.");
                            }
                        } else if (tt.isRetired() && (listing.getIcs() == null || listing.getIcs().getInherits() == null
                                || listing.getIcs().getInherits().equals(Boolean.FALSE))) {
                            listing.getErrorMessages().add("Test Tool '" + testTool.getTestToolName()
                            + "' can not be used for criteria '" + cert.getNumber()
                            + "', as it is a retired tool, and this Certified Product does not carry ICS.");
                        }
                    }
                }
            }
        }
    }
}
