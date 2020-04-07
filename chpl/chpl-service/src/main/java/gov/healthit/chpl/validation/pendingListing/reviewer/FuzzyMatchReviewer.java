package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.dao.UcdProcessDAO;
import gov.healthit.chpl.dto.AccessibilityStandardDTO;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.manager.FuzzyChoicesManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("pendingFuzzyMatchReviewer")
public class FuzzyMatchReviewer implements Reviewer {
    private static final Logger LOGGER = LogManager.getLogger(FuzzyMatchReviewer.class);
    @Autowired private FuzzyChoicesManager fuzzyChoicesManager;
    @Autowired private UcdProcessDAO ucdDao;
    @Autowired private QmsStandardDAO qmsDao;
    @Autowired private AccessibilityStandardDAO accStdDao;
    @Autowired private ErrorMessageUtil msgUtil;

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        if (listing.getCertificationCriterion() != null && !listing.getCertificationCriterion().isEmpty()) {
            for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
                if (cert.getUcdProcesses() != null && !cert.getUcdProcesses().isEmpty()) {
                    for (PendingCertificationResultUcdProcessDTO ucd : cert.getUcdProcesses()) {
                        String origUcdProcessName = ucd.getUcdProcessName();
                        String topChoice = fuzzyChoicesManager
                                .getTopFuzzyChoice(origUcdProcessName, FuzzyType.UCD_PROCESS);
                        if (topChoice != null && !origUcdProcessName.equals(topChoice)) {
                            UcdProcessDTO fuzzyMatchedUcd = null;
                            try {
                                fuzzyMatchedUcd = ucdDao.findOrCreate(null, topChoice);
                            } catch (EntityCreationException ex) {
                                LOGGER.error("Could not insert ucd process " + topChoice, ex);
                            }

                            if (fuzzyMatchedUcd != null) {
                                ucd.setUcdProcessId(fuzzyMatchedUcd.getId());
                                ucd.setUcdProcessName(fuzzyMatchedUcd.getName());
                                String warningMsg = msgUtil.getMessage("listing.criteria.fuzzyMatch",
                                        FuzzyType.UCD_PROCESS.fuzzyType(),
                                        Util.formatCriteriaNumber(cert.getCriterion()),
                                        origUcdProcessName, topChoice);
                                listing.getWarningMessages().add(warningMsg);

                            }
                        }
                    }
                }
            }
        }

        for (PendingCertifiedProductQmsStandardDTO qms : listing.getQmsStandards()) {
            String origQmsName = qms.getName();
            String topChoice = fuzzyChoicesManager.getTopFuzzyChoice(origQmsName, FuzzyType.QMS_STANDARD);
            if (topChoice != null && !origQmsName.equals(topChoice)) {
                QmsStandardDTO fuzzyMatchedQms = null;
                try {
                    fuzzyMatchedQms = qmsDao.findOrCreate(null, topChoice);
                } catch (EntityCreationException ex) {
                    LOGGER.error("Could not insert qms standard " + topChoice, ex);
                }

                if (fuzzyMatchedQms != null) {
                    qms.setQmsStandardId(fuzzyMatchedQms.getId());
                    qms.setName(fuzzyMatchedQms.getName());

                    String warningMsg = msgUtil.getMessage("listing.fuzzyMatch",
                                    FuzzyType.QMS_STANDARD.fuzzyType(), origQmsName, topChoice);
                    listing.getWarningMessages().add(warningMsg);
                }
            }
        }

        for (PendingCertifiedProductAccessibilityStandardDTO access : listing.getAccessibilityStandards()) {
            String origAccStd = access.getName();
            String topChoice = fuzzyChoicesManager.getTopFuzzyChoice(origAccStd, FuzzyType.ACCESSIBILITY_STANDARD);
            if (topChoice != null && !origAccStd.equals(topChoice)) {
                AccessibilityStandardDTO fuzzyMatchedAccStd = null;
                try {
                    fuzzyMatchedAccStd = accStdDao.findOrCreate(null, topChoice);
                } catch (EntityCreationException ex) {
                    LOGGER.error("Could not insert accessibility standard " + topChoice, ex);
                }

                if (fuzzyMatchedAccStd != null) {
                    access.setAccessibilityStandardId(fuzzyMatchedAccStd.getId());
                    access.setName(fuzzyMatchedAccStd.getName());

                    String warningMsg = msgUtil.getMessage("listing.fuzzyMatch",
                            FuzzyType.ACCESSIBILITY_STANDARD.fuzzyType(),
                            origAccStd, topChoice);
                    listing.getWarningMessages().add(warningMsg);
                }
            }
        }
    }
}
