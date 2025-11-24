package gov.healthit.chpl.attribute;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.standard.StandardGroupService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.Util;

@Component
public class GroupedStandardsUpToDateService {

    private StandardGroupService groupedStandardService;
    private StandardDAO standardDao;
    private CertificationResultRules certificationResultRules;

    public GroupedStandardsUpToDateService(StandardGroupService groupedStandardService,
            StandardDAO standardDao,
            CertificationResultRules certificationResultRules) {
        this.groupedStandardService = groupedStandardService;
        this.standardDao = standardDao;
        this.certificationResultRules = certificationResultRules;
    }

    public List<StandardGroupUpToDate> getAttributeUpToDate(CertificationResult certResult, Logger logger) {
        List<StandardGroupUpToDate> standardGroupUpToDateReports = new ArrayList<StandardGroupUpToDate>();

        Boolean isCriteriaEligible = isCriteriaEligibleForStandards(certResult.getCriterion(), logger);
        if (isCriteriaEligible) {
            List<StandardGroupUpToDate> upToDateReportsForUnattestedStandardGroups = getUpToDateReportsForUnattestedStandardGroups(certResult, logger);
            if (!CollectionUtils.isEmpty(upToDateReportsForUnattestedStandardGroups)) {
                standardGroupUpToDateReports.addAll(upToDateReportsForUnattestedStandardGroups);
            }
        }

        return standardGroupUpToDateReports;
    }

    private Boolean isCriteriaEligibleForStandards(CertificationCriterion criterion, Logger logger) {
        List<Standard> standardsForCriterion = getAllStandardsForCriterion(criterion, logger);
        return certificationResultRules.hasCertOption(criterion.getId(), CertificationResultRules.STANDARD)
                && !CollectionUtils.isEmpty(standardsForCriterion);
    }

    private List<StandardGroupUpToDate> getUpToDateReportsForUnattestedStandardGroups(CertificationResult certResult, Logger logger) {
        logger.info("Checking unattested standard groups for " + Util.formatCriteriaNumber(certResult.getCriterion()));
        List<String> unattestedStandardGroups = getUnattestedToRequiredStandardGroups(certResult, logger);
        logger.info("There are " + unattestedStandardGroups.size() + " unattested standard groups for " + Util.formatCriteriaNumber(certResult.getCriterion()));

        List<StandardGroupUpToDate> standardGroupUpToDateReports = new ArrayList<StandardGroupUpToDate>();
        if (CollectionUtils.isNotEmpty(unattestedStandardGroups)) {
            unattestedStandardGroups.stream()
                    .peek(groupName -> logger.info("Standard from group " + groupName + " is required but not found"))
                    .map(groupName -> StandardGroupUpToDate.builder()
                            .criterion(certResult.getCriterion())
                            .eligibleForAttribute(true)
                            .expiringButPresent(false)
                            .requiredButNotPresent(true)
                            .standardGroupName(groupName)
                            .build())
                    .forEach(groupUpToDate -> standardGroupUpToDateReports.add(groupUpToDate));
        }
        return standardGroupUpToDateReports;
    }

    private List<String> getUnattestedToRequiredStandardGroups(CertificationResult certResult, Logger logger) {
        Map<String, List<Standard>> activeGroupedStandards = getActiveStandardGroupsForCriterion(certResult.getCriterion(), logger);
        return activeGroupedStandards.keySet().stream()
                .filter(groupName -> !isAnyStandardFromGroupOnCertResult(activeGroupedStandards.get(groupName), certResult)
                        && areStandardsInGroupRequired(activeGroupedStandards.get(groupName)))
                .collect(Collectors.toList());
    }

    private boolean isAnyStandardFromGroupOnCertResult(List<Standard> standardsInGroup, CertificationResult certResult) {
        //TODO
        return false;
    }

    private boolean areStandardsInGroupRequired(List<Standard> standardsInGroup) {
        //TODO
        return true;
    }

    private  Map<String, List<Standard>> getActiveStandardGroupsForCriterion(CertificationCriterion criterion, Logger logger) {
        Map<String, List<Standard>> activeGroupedStandards = groupedStandardService.getGroupedStandardsForCriteria(criterion, LocalDate.now());
        logger.info("Found " + activeGroupedStandards.keySet().size() + " active standard group(s) for " + Util.formatCriteriaNumber(criterion) + ": "
                + Util.joinListGrammatically(activeGroupedStandards.keySet().stream().collect(Collectors.toList()), "and"));
        return activeGroupedStandards;
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

    private Boolean isStandardInList(Standard standardToCheck, List<Standard> standards) {
        return standards.stream()
                .filter(std -> std.getId().equals(standardToCheck.getId()))
                .findAny()
                .isPresent();
    }

    private List<Standard> getAllStandardsForCriterion(CertificationCriterion criterion, Logger logger) {
        try {
            return standardDao.getAllStandardCriteriaMap().stream()
                    .filter(map -> map.getCriterion().getId().equals(criterion.getId()))
                    .map(map -> map.getStandard())
                    .toList();
        } catch (EntityRetrievalException e) {
            logger.error("Could not retrieve Standards for Criterion.", e);
            return List.of();
        }
    }
}
