package gov.healthit.chpl.certifiedproduct.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.codeset.CodeSet;
import gov.healthit.chpl.codeset.CodeSetDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.functionalitytested.FunctionalityTested;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedDAO;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.standard.StandardCriteriaMap;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.Util;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CertificationResultUpToDateService {
    private StandardDAO standardDao;
    private CodeSetDAO codeSetDao;
    private FunctionalityTestedDAO functionatlityTestedDao;

    @Autowired
    public CertificationResultUpToDateService(StandardDAO standardDao,
            CodeSetDAO codeSetDao,
            FunctionalityTestedDAO functionatlityTestedDao) {
        this.standardDao = standardDao;
        this.codeSetDao = codeSetDao;
        this.functionatlityTestedDao = functionatlityTestedDao;
    }

    @Transactional
    public boolean isUpToDate(CertificationResult certResult, LocalDate asOfDate) {
        List<StandardCriteriaMap> stdCriteriaMaps = standardDao.getAllStandardCriteriaMaps();
        stdCriteriaMaps.removeIf(map -> !map.getCriterion().getId().equals(certResult.getCriterion().getId()));
        List<Standard> standardsForCriterion = stdCriteriaMaps.stream()
                .map(map -> map.getStandard())
                .collect(Collectors.toList());

        return areNonGroupedStandardsUpToDate(standardsForCriterion, certResult, asOfDate)
                && areGroupedStandardsUpToDate(standardsForCriterion, certResult, asOfDate)
                && areCodeSetsUpToDate(certResult, asOfDate)
                && isFunctionalityTestedUpToDate(certResult, asOfDate);
    }

    @Transactional
    public boolean isUpToDate(CertificationResult certResult) {
        return isUpToDate(certResult, LocalDate.now());
    }

    private boolean areNonGroupedStandardsUpToDate(List<Standard> standardsForCriterion, CertificationResult certResult, LocalDate asOfDate) {
        List<Boolean> baselineStandardsUpToDate = standardsForCriterion.stream()
                .filter(std -> StringUtils.isEmpty(std.getGroupName()))
                .map(std -> areAllStandardsRequiredAndPresent(Stream.of(std).toList(), certResult, asOfDate))
                .map(bool -> Boolean.valueOf(bool))
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(baselineStandardsUpToDate)) {
            return !baselineStandardsUpToDate.stream()
                    .filter(baselineUpToDate -> !baselineUpToDate)
                    .findAny()
                    .isPresent();
        }
        return true;
    }

    private boolean areGroupedStandardsUpToDate(List<Standard> standardsForCriterion, CertificationResult certResult, LocalDate asOfDate) {
       Map<String, List<Standard>> standardGroupsForCriterion = standardsForCriterion.stream()
                .filter(std -> !StringUtils.isEmpty(std.getGroupName()))
                .collect(Collectors.groupingBy(std -> std.getGroupName()));

       List<Boolean> groupsUpToDate = standardGroupsForCriterion.keySet().stream()
           .map(groupName -> standardGroupsForCriterion.get(groupName))
           .map(stdList -> isAnyStandardRequiredAndPresent(stdList, certResult, asOfDate))
           .map(bool -> Boolean.valueOf(bool))
           .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(groupsUpToDate)) {
            return !groupsUpToDate.stream()
                    .filter(groupUpToDate -> !groupUpToDate)
                    .findAny()
                    .isPresent();
        }
        return true;
    }

    private boolean areAllStandardsRequiredAndPresent(List<Standard> standards, CertificationResult certResult, LocalDate asOfDate) {
        List<Standard> requiredStandards = standards.stream()
                .filter(std -> DateUtil.isOnOrBefore(std.getStartDay(), asOfDate)
                        && (std.getRequiredDay() != null && std.getRequiredDay().isBefore(asOfDate))
                        && (std.getEndDay() == null || std.getEndDay().isAfter(asOfDate)))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(requiredStandards)) {
            return !requiredStandards.stream()
                    .filter(requiredStandard -> !isStandardOnCertResult(requiredStandard, certResult))
                    .peek(missingRequiredStandard -> LOGGER.debug("Required standard " + missingRequiredStandard.getRegulatoryTextCitation() + " is missing for criterion " + Util.formatCriteriaNumber(certResult.getCriterion())))
                    .findAny()
                    .isPresent();
        }
        return true;
    }

    private boolean isAnyStandardRequiredAndPresent(List<Standard> standards, CertificationResult certResult, LocalDate asOfDate) {
        List<Standard> requiredStandards = standards.stream()
                .filter(std -> DateUtil.isOnOrBefore(std.getStartDay(), asOfDate)
                        && (std.getRequiredDay() != null && std.getRequiredDay().isBefore(asOfDate))
                        && (std.getEndDay() == null || std.getEndDay().isAfter(asOfDate)))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(requiredStandards)) {
            return requiredStandards.stream()
                    .filter(requiredStandard -> isStandardOnCertResult(requiredStandard, certResult))
                    .findAny()
                    .isPresent();
        }
        return true;
    }

    private boolean isStandardOnCertResult(Standard standard, CertificationResult certResult) {
        return certResult.getStandards().stream()
                .filter(certResultStd -> standard.getId().equals(certResultStd.getStandard().getId()))
                .findAny()
                .isPresent();
    }

    private boolean areCodeSetsUpToDate(CertificationResult certResult, LocalDate asOfDate) {
        List<CodeSet> codeSetsRequiredForCriterion = null;
        Map<Long, List<CodeSet>> codeSetMaps = codeSetDao.getCodeSetCriteriaMaps();
        if (codeSetMaps.containsKey(certResult.getCriterion().getId())) {
            codeSetsRequiredForCriterion = codeSetMaps.get(certResult.getCriterion().getId()).stream()
                    .filter(codeSet -> DateUtil.isOnOrBefore(codeSet.getStartDay(), asOfDate)
                            && codeSet.getRequiredDay().isBefore(asOfDate))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(codeSetsRequiredForCriterion)) {
                return !codeSetsRequiredForCriterion.stream()
                        .filter(requiredCodeSet -> !isCodeSetOnCertResult(requiredCodeSet, certResult))
                        .peek(missingRequiredCodeSet -> LOGGER.debug("Required Code Set " + missingRequiredCodeSet.getName() + " is missing for criterion " + Util.formatCriteriaNumber(certResult.getCriterion())))
                        .findAny()
                        .isPresent();
            }
        }
        return true;
    }

    private Boolean isCodeSetOnCertResult(CodeSet codeSet, CertificationResult certResult) {
        return certResult.getCodeSets().stream()
                .filter(cs -> codeSet.getId().equals(cs.getCodeSet().getId()))
                .findAny()
                .isPresent();
    }

    private boolean isFunctionalityTestedUpToDate(CertificationResult certResult, LocalDate asOfDate) {
        List<FunctionalityTested> functionalityTestedRequiredForCriterion = null;
        Map<Long, List<FunctionalityTested>> ftMaps = functionatlityTestedDao.getFunctionalitiesTestedCriteriaMaps();
        if (ftMaps.containsKey(certResult.getCriterion().getId())) {
            functionalityTestedRequiredForCriterion = ftMaps.get(certResult.getCriterion().getId()).stream()
                    .filter(ft -> DateUtil.isOnOrBefore(ft.getStartDay(), asOfDate)
                            && (ft.getRequiredDay() != null && ft.getRequiredDay().isBefore(asOfDate))
                            && (ft.getEndDay() == null || ft.getEndDay().isAfter(asOfDate)))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(functionalityTestedRequiredForCriterion)) {
                return !functionalityTestedRequiredForCriterion.stream()
                        .filter(requiredFt -> !isFunctionalityTestedOnCertResult(requiredFt, certResult))
                        .peek(missingRequiredFt -> LOGGER.debug("Required Functionality Tested " + missingRequiredFt.getRegulatoryTextCitation() + " is missing for criterion " + Util.formatCriteriaNumber(certResult.getCriterion())))
                        .findAny()
                        .isPresent();
            }
        }
        return true;
    }

    private Boolean isFunctionalityTestedOnCertResult(FunctionalityTested functionalityTested, CertificationResult certResult) {
        return certResult.getFunctionalitiesTested().stream()
                .filter(ft -> functionalityTested.getId().equals(ft.getFunctionalityTested().getId()))
                .findAny()
                .isPresent();
    }
}
