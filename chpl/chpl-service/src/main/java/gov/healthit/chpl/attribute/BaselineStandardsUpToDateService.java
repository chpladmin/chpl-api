package gov.healthit.chpl.attribute;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.standard.BaselineStandardService;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.Util;

//For the purposes of this calculation, a "baseline" standard is active (start day before now) and is not grouped.
//Baseline standards that we want to report on are those that have a required day in the past or future.
//We care about required baseline standards that are not present on a criteria.
@Component
public class BaselineStandardsUpToDateService {

    private BaselineStandardService baselineStandardService;
    private StandardDAO standardDao;
    private CertificationResultRules certificationResultRules;

    public BaselineStandardsUpToDateService(BaselineStandardService baselineStandardService,
            StandardDAO standardDao,
            CertificationResultRules certificationResultRules) {
        this.baselineStandardService = baselineStandardService;
        this.standardDao = standardDao;
        this.certificationResultRules = certificationResultRules;
    }

    public List<StandardUpToDate> getAttributeUpToDate(CertificationResult certResult, Logger logger) {
        List<StandardUpToDate> standardUpToDateReports = new ArrayList<StandardUpToDate>();

        Boolean isCriteriaEligible = isCriteriaEligibleForStandards(certResult.getCriterion(), logger);
        if (isCriteriaEligible) {
            List<StandardUpToDate> upToDateReports = getUpToDateReportsForUnattestedActiveBaselineStandards(certResult, logger);
            if (!CollectionUtils.isEmpty(upToDateReports)) {
                standardUpToDateReports.addAll(upToDateReports);
            }
        }
        return standardUpToDateReports;
    }

    private Boolean isCriteriaEligibleForStandards(CertificationCriterion criterion, Logger logger) {
        return certificationResultRules.hasCertOption(criterion.getId(), CertificationResultRules.STANDARD)
                && doesCriterionHaveAnyStandards(criterion, logger);
    }

    private List<StandardUpToDate> getUpToDateReportsForUnattestedActiveBaselineStandards(CertificationResult certResult, Logger logger) {
        logger.info("Checking unattested baseline standards for " + Util.formatCriteriaNumber(certResult.getCriterion()));
        List<Standard> unattestedActiveBaselineStandards = getUnattestedActiveBaselineStandards(certResult, logger);
        logger.info("There are " + unattestedActiveBaselineStandards.size() + " unattested baseline standards for " + Util.formatCriteriaNumber(certResult.getCriterion()));

        List<StandardUpToDate> standardUpToDateReports = new ArrayList<StandardUpToDate>();
        if (CollectionUtils.isNotEmpty(unattestedActiveBaselineStandards)) {
            unattestedActiveBaselineStandards.stream()
                    .peek(std -> logger.info("Checking unattested active baseline standard " + std.getRegulatoryTextCitation()))
                    .filter(std -> !std.isRetired() && std.getRequiredDay() != null)
                    .peek(std -> logger.info("Unattested active baseline standard " + std.getRegulatoryTextCitation() + " is required but not found"))
                    .map(std -> StandardUpToDate.builder()
                            .criterion(certResult.getCriterion())
                            .eligibleForAttribute(true)
                            .expiringButPresent(false)
                            .requiredButNotPresent(true)
                            .standard(std)
                            .updateRequiredBy(std.getRequiredDay())
                            .build())
                    .forEach(stdUpToDate -> standardUpToDateReports.add(stdUpToDate));
        }
        return standardUpToDateReports;
    }

    private List<Standard> getUnattestedActiveBaselineStandards(CertificationResult certResult, Logger logger) {
        return getActiveBaselineStandardsForCriterion(certResult.getCriterion(), logger).stream()
                .filter(std -> !isStandardInList(std, certResult.getStandards().stream().map(crs -> crs.getStandard()).toList()))
                .toList();
    }

    private List<Standard> getActiveBaselineStandardsForCriterion(CertificationCriterion criterion, Logger logger) {
        List<Standard> activeBaselineStandards = baselineStandardService.getActiveBaselineStandardsForCriterion(
                criterion, LocalDate.now());
        logger.info("Found " + activeBaselineStandards.size() + " active baseline standards for " + Util.formatCriteriaNumber(criterion) + ": "
                + Util.joinListGrammatically(activeBaselineStandards.stream().map(std -> std.getRegulatoryTextCitation()).collect(Collectors.toList()), "and"));
        return activeBaselineStandards;
    }

    private Boolean isStandardInList(Standard standardToCheck, List<Standard> standards) {
        return standards.stream()
                .filter(std -> std.getId().equals(standardToCheck.getId()))
                .findAny()
                .isPresent();
    }

    private boolean doesCriterionHaveAnyStandards(CertificationCriterion criterion, Logger logger) {
        try {
            return standardDao.getAllStandardCriteriaMap().stream()
                    .filter(map -> map.getCriterion().getId().equals(criterion.getId()))
                    .findAny()
                    .isPresent();
        } catch (EntityRetrievalException e) {
            logger.error("Could not retrieve Standards for Criterion.", e);
            return false;
        }
    }
}
