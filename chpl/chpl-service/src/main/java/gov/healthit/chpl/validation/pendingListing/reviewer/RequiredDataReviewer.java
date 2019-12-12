package gov.healthit.chpl.validation.pendingListing.reviewer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("pendingRequiredDataReviewer")
public class RequiredDataReviewer implements Reviewer {
    protected ErrorMessageUtil msgUtil;
    protected CertificationResultRules certRules;

    @Autowired
    public RequiredDataReviewer(final ErrorMessageUtil msgUtil, final CertificationResultRules certRules) {
        this.msgUtil = msgUtil;
        this.certRules = certRules;
    }

    @Override
    public void review(final PendingCertifiedProductDTO listing) {
        if (listing.getCertificationEditionId() == null && StringUtils.isEmpty(listing.getCertificationEdition())) {
            listing.getErrorMessages().add("Certification edition is required but was not found.");
        }
        if (listing.getCertificationDate() == null) {
            listing.getErrorMessages().add("Certification date was not found.");
        }
        if (listing.getCertificationBodyId() == null) {
            listing.getErrorMessages().add("ONC-ACB is required but was not found.");
        }
        if (StringUtils.isEmpty(listing.getUniqueId())) {
            listing.getErrorMessages().add("The product unique id is required.");
        }
        if (StringUtils.isEmpty(listing.getDeveloperName())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.nameRequired"));
        }
        if (StringUtils.isEmpty(listing.getProductName())) {
            listing.getErrorMessages().add("A product name is required.");
        }
        if (StringUtils.isEmpty(listing.getProductVersion())) {
            listing.getErrorMessages().add("A product version is required.");
        }
        if (listing.getDeveloperAddress() != null) {
            if (StringUtils.isEmpty(listing.getDeveloperAddress().getStreetLineOne())) {
                listing.getErrorMessages().add(msgUtil.getMessage("developer.address.streetRequired"));
            }
            if (StringUtils.isEmpty(listing.getDeveloperAddress().getCity())) {
                listing.getErrorMessages().add(msgUtil.getMessage("developer.address.cityRequired"));
            }
            if (StringUtils.isEmpty(listing.getDeveloperAddress().getState())) {
                listing.getErrorMessages().add(msgUtil.getMessage("developer.address.stateRequired"));
            }
            if (StringUtils.isEmpty(listing.getDeveloperAddress().getZipcode())) {
                listing.getErrorMessages().add(msgUtil.getMessage("developer.address.zipRequired"));
            }
        } else {
            if (StringUtils.isEmpty(listing.getDeveloperStreetAddress())) {
                listing.getErrorMessages().add(msgUtil.getMessage("developer.address.streetRequired"));
            }
            if (StringUtils.isEmpty(listing.getDeveloperCity())) {
                listing.getErrorMessages().add(msgUtil.getMessage("developer.address.cityRequired"));
            }
            if (StringUtils.isEmpty(listing.getDeveloperState())) {
                listing.getErrorMessages().add(msgUtil.getMessage("developer.address.stateRequired"));
            }
            if (StringUtils.isEmpty(listing.getDeveloperZipCode())) {
                listing.getErrorMessages().add(msgUtil.getMessage("developer.address.zipRequired"));
            }
        }
        if (StringUtils.isEmpty(listing.getDeveloperWebsite())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.websiteRequired"));
        }
        if (StringUtils.isEmpty(listing.getDeveloperEmail())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.contact.emailRequired"));
        }
        if (StringUtils.isEmpty(listing.getDeveloperPhoneNumber())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.contact.phoneRequired"));
        }
        if (StringUtils.isEmpty(listing.getDeveloperContactName())) {
            listing.getErrorMessages().add(msgUtil.getMessage("developer.contact.nameRequired"));
        }

        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getMeetsCriteria() == null) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.criteria.metInvalid", cert.getCriterion().getNumber()));
            } else if (cert.getMeetsCriteria().booleanValue()) {
                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.GAP)
                        && cert.getGap() == null) {
                    listing.getErrorMessages().add(
                            msgUtil.getMessage("listing.criteria.missingGap", cert.getCriterion().getNumber()));
                }

                boolean gapEligibleAndTrue = false;
                if (certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.GAP)
                        && cert.getGap() != null
                        && cert.getGap().booleanValue()) {
                    gapEligibleAndTrue = true;
                }

                if (!gapEligibleAndTrue
                        && certRules.hasCertOption(cert.getCriterion().getNumber(), CertificationResultRules.TEST_PROCEDURE)
                        && (cert.getTestProcedures() == null || cert.getTestProcedures().size() == 0)) {
                    listing.getErrorMessages()
                            .add(msgUtil.getMessage("listing.criteria.missingTestProcedure", cert.getCriterion().getNumber()));
                }
            }

            //macra measures are saved regardless of whether the criteria has been met
            //so check for duplicates no matter what
            if (cert.getG1MacraMeasures() != null && cert.getG1MacraMeasures().size() > 1) {
                List<String> g1Warnings =
                        validateMacraMeasuresAreUniqueForCertificationResult(
                                cert.getG1MacraMeasures(), cert.getCriterion().getNumber(), "listing.criteria.duplicateG1MacraMeasure");
                if (g1Warnings.size() > 0) {
                    listing.getWarningMessages().addAll(g1Warnings);
                    cert.setG1MacraMeasures(removeDuplicateMacraMeasures(cert.getG1MacraMeasures()));
                }
            }

            if (cert.getG2MacraMeasures() != null && cert.getG2MacraMeasures().size() > 1) {
                List<String> g2Warnings =
                        validateMacraMeasuresAreUniqueForCertificationResult(
                                cert.getG2MacraMeasures(), cert.getCriterion().getNumber(), "listing.criteria.duplicateG2MacraMeasure");
                if (g2Warnings.size() > 0) {
                    listing.getWarningMessages().addAll(g2Warnings);
                    cert.setG2MacraMeasures(removeDuplicateMacraMeasures(cert.getG2MacraMeasures()));
                }
            }
        }
    }

    private List<String> validateMacraMeasuresAreUniqueForCertificationResult(List<PendingCertificationResultMacraMeasureDTO> macraMeasures, String certNumber, String messageCode) {
        List<String> messages = new ArrayList<String>();
        Set<String> uniqueMacras = new HashSet<String>();
        for (PendingCertificationResultMacraMeasureDTO macraMeasure : macraMeasures) {
            if (macraMeasure.getEnteredValue() != null
                    && uniqueMacras.contains(macraMeasure.getEnteredValue())) { // Duplicate
                messages.add(msgUtil.getMessage(messageCode, certNumber, macraMeasure.getEnteredValue()));
            } else {
                uniqueMacras.add(macraMeasure.getEnteredValue());
            }
        }
        return messages;
    }

    private List<PendingCertificationResultMacraMeasureDTO> removeDuplicateMacraMeasures(List<PendingCertificationResultMacraMeasureDTO> macraMeasures) {
        List<PendingCertificationResultMacraMeasureDTO> dedupedMacraMeasures = new ArrayList<PendingCertificationResultMacraMeasureDTO>();
        Set<String> uniqueMacras = new HashSet<String>();
        for (PendingCertificationResultMacraMeasureDTO macraMeasure : macraMeasures) {
            if (macraMeasure.getEnteredValue() != null
                    && !uniqueMacras.contains(macraMeasure.getEnteredValue())) {
                dedupedMacraMeasures.add(macraMeasure);
                uniqueMacras.add(macraMeasure.getEnteredValue());
            }
        }
        return dedupedMacraMeasures;
    }
}
