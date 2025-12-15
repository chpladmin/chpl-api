package gov.healthit.chpl.attribute;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
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

    public List<StandardGroupUpToDate> getAttributeUpToDate(CertifiedProductSearchDetails listing, CertificationResult certResult, Logger logger) {
        List<StandardGroupUpToDate> standardGroupUpToDateReports = new ArrayList<StandardGroupUpToDate>();

        Boolean isCriteriaEligible = isCriteriaEligibleForStandards(certResult.getCriterion(), logger);
        if (isCriteriaEligible) {
            List<StandardGroupUpToDate> upToDateReportsForUnattestedStandardGroups = getUpToDateReportsForUnattestedStandardGroups(
                    listing, certResult, logger);
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

    private List<StandardGroupUpToDate> getUpToDateReportsForUnattestedStandardGroups(CertifiedProductSearchDetails listing,
            CertificationResult certResult, Logger logger) {
        logger.info("Checking unattested standard groups for " + Util.formatCriteriaNumber(certResult.getCriterion()));
        Map<String, List<Standard>> standardGroupsNoneAttested = getUnattestedToRequiredStandardGroups(listing, certResult, logger);
        logger.info("There are " + standardGroupsNoneAttested.keySet().size() + " unattested standard groups for " + Util.formatCriteriaNumber(certResult.getCriterion()));

        List<StandardGroupUpToDate> standardGroupUpToDateReports = new ArrayList<StandardGroupUpToDate>();
        if (CollectionUtils.isNotEmpty(standardGroupsNoneAttested.keySet())) {
            standardGroupsNoneAttested.keySet().stream()
                    .peek(groupName -> logger.info("Standard from group " + groupName + " is required but not found"))
                    .map(groupName -> StandardGroupUpToDate.builder()
                            .criterion(certResult.getCriterion())
                            .eligibleForAttribute(true)
                            .expiringButPresent(false)
                            .requiredButNotPresent(true)
                            .standardGroupName(groupName)
                            .updateRequiredBy(getMinimumStartDateFromGroup(standardGroupsNoneAttested.get(groupName)))
                            .build())
                    .forEach(groupUpToDate -> standardGroupUpToDateReports.add(groupUpToDate));
        }

        Map<String, List<Standard>> standardGroupsExpiringAttestedWithoutReplacement = getAttestedToButExpiringRequiredStandardGroups(listing, certResult, logger);
        logger.info("There are " + standardGroupsExpiringAttestedWithoutReplacement.keySet().size() + " standard groups with an expiring standard attested and not replaced for " + Util.formatCriteriaNumber(certResult.getCriterion()));
        if (CollectionUtils.isNotEmpty(standardGroupsExpiringAttestedWithoutReplacement.keySet())) {
            standardGroupsExpiringAttestedWithoutReplacement.keySet().stream()
                    .peek(groupName -> logger.info("Standard from group " + groupName + " is expiring and replacement was not found"))
                    .map(groupName -> StandardGroupUpToDate.builder()
                            .criterion(certResult.getCriterion())
                            .eligibleForAttribute(true)
                            .expiringButPresent(false)
                            .requiredButNotPresent(true)
                            .standardGroupName(groupName)
                            .updateRequiredBy(getMinimumRequiredDateFromGroup(standardGroupsExpiringAttestedWithoutReplacement.get(groupName)))
                            .build())
                    .forEach(groupUpToDate -> standardGroupUpToDateReports.add(groupUpToDate));
        }
        return standardGroupUpToDateReports;
    }

    //standard group that has 0 standards from the group on the cert results
    private Map<String, List<Standard>> getUnattestedToRequiredStandardGroups(CertifiedProductSearchDetails listing, CertificationResult certResult, Logger logger) {
        Map<String, List<Standard>> activeGroupedStandards = getActiveStandardGroupsForCriterion(listing, certResult.getCriterion(), logger);
        List<Standard> standardGroupsNoneAttested = activeGroupedStandards.entrySet().stream()
                .filter(standardGroup -> CollectionUtils.isEmpty(getAttestedStandardsFromGroup(standardGroup.getValue(), certResult)))
                .flatMap(standardGroup -> standardGroup.getValue().stream())
                .collect(Collectors.toList());
        return standardGroupsNoneAttested.stream()
                .collect(Collectors.groupingBy(Standard::getGroupName));
    }

    private List<Standard> getAttestedStandardsFromGroup(List<Standard> groupedStandards, CertificationResult certResult) {
        return groupedStandards.stream()
                .filter(standardFromGroup -> isStandardInList(standardFromGroup, certResult.getStandards().stream().map(certResultStd -> certResultStd.getStandard()).toList()))
                .collect(Collectors.toList());
    }

    private LocalDate getMinimumStartDateFromGroup(List<Standard> standards) {
        return standards.stream()
                .map(std -> std.getStartDay())
                .filter(startDay -> startDay != null)
                .min(Comparator.naturalOrder())
                .orElse(LocalDate.now());
    }

    private LocalDate getMinimumRequiredDateFromGroup(List<Standard> standards) {
        return standards.stream()
                .map(std -> std.getRequiredDay())
                .filter(reqDay -> reqDay != null)
                .min(Comparator.naturalOrder())
                .orElse(LocalDate.now());
    }

    //standard group that has >= 1 standard from the group on the cert results
    //if the standard(s) from the group have an end date, then we have to check to see if there is a newer one
    private Map<String, List<Standard>> getAttestedToButExpiringRequiredStandardGroups(CertifiedProductSearchDetails listing, CertificationResult certResult, Logger logger) {
        Map<String, List<Standard>> expiringGroupToRequiredStandardMap = new LinkedHashMap<String, List<Standard>>();
        Map<String, List<Standard>> activeGroupedStandards = getActiveStandardGroupsForCriterion(listing, certResult.getCriterion(), logger);
        List<String> stdGroupNames = activeGroupedStandards.keySet().stream().collect(Collectors.toList());
        stdGroupNames.stream()
            .map(stdGroupName -> getExpiringStandardsInGroup(activeGroupedStandards.get(stdGroupName)))
            .filter(expiringStdsInThisGroup -> isAnyStandardFromGroupOnCertResult(expiringStdsInThisGroup, certResult))
            .forEach(expiringStdsInThisGroupOnCertResult -> {
                String groupName = expiringStdsInThisGroupOnCertResult.get(0).getGroupName();
                Set<LocalDate> expiringStandardsInGroupEndDates = expiringStdsInThisGroupOnCertResult.stream()
                        .filter(std -> std.getEndDay() != null)
                        .map(std -> std.getEndDay())
                        .collect(Collectors.toSet());
                //are there other standards in the group that have a required date equal to or after any of the expiring standard end dates?
                List<Standard> allStandardsInGroup = activeGroupedStandards.get(groupName);
                List<Standard> standardsToReplace = allStandardsInGroup.stream()
                    .filter(stdInGroup -> expiringStandardsInGroupEndDates.contains(stdInGroup.getRequiredDay()))
                    .collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(standardsToReplace)
                        && !isAnyStandardFromGroupOnCertResult(standardsToReplace, certResult)) {
                    expiringGroupToRequiredStandardMap.put(groupName, standardsToReplace);
                }
            });
        return expiringGroupToRequiredStandardMap;
    }


    private List<Standard> getExpiringStandardsInGroup(List<Standard> standards) {
        return standards.stream()
                .filter(std -> std.getEndDay() != null)
                .collect(Collectors.toList());
    }

    private boolean isAnyStandardFromGroupOnCertResult(List<Standard> standardsInGroup, CertificationResult certResult) {
        return certResult.getStandards().stream()
            .filter(crStd -> isStandardInList(crStd.getStandard(), standardsInGroup))
            .findAny()
            .isPresent();
    }

    private  Map<String, List<Standard>> getActiveStandardGroupsForCriterion(CertifiedProductSearchDetails listing, CertificationCriterion criterion, Logger logger) {
        Map<String, List<Standard>> activeGroupedStandards = groupedStandardService.getGroupedStandardsForCriteria(criterion,
                listing.getCertificationDay(), LocalDate.now());
        logger.info("Found " + activeGroupedStandards.keySet().size() + " active standard group(s) for " + Util.formatCriteriaNumber(criterion) + ": "
                + Util.joinListGrammatically(activeGroupedStandards.keySet().stream().collect(Collectors.toList()), "and"));
        return activeGroupedStandards;
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
