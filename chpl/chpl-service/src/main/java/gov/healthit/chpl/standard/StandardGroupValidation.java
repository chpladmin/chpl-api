package gov.healthit.chpl.standard;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public abstract class StandardGroupValidation extends PermissionBasedReviewer{

    private StandardDAO standardDAO;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public StandardGroupValidation(StandardDAO standardDAO, ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);

        this.standardDAO = standardDAO;
        this.msgUtil = msgUtil;
    }

    public void reviewStandardExistForEachGroup(CertifiedProductSearchDetails listing, CertificationResult certResult, LocalDate validAsOfDate) {
        getGroupedStandardsForCriteria(certResult.getCriterion(), validAsOfDate).entrySet().stream()
                .forEach(standardGroup -> {
                    if (!doesAtLeastOneStandardForGroupExistForCriterion(standardGroup.getValue(), certResult)) {
                        listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.standardGroupNotSelected",
                                Util.formatCriteriaNumber(certResult.getCriterion()),
                                standardGroup.getValue().stream().map(std -> std.getRegulatoryTextCitation()).collect(Collectors.joining(", "))));
                    }
                });
    }

    private boolean doesAtLeastOneStandardForGroupExistForCriterion(List<Standard> groupedStandards, CertificationResult certResult) {
        return groupedStandards.stream()
                .filter(standardFromGroup -> isStandardInList(standardFromGroup, certResult.getStandards().stream().map(certResultStd -> certResultStd.getStandard()).toList()))
                .count() >= 1;
    }

    private boolean isStandardInList(Standard standardToFind, List<Standard> standard) {
        return standard.stream()
                .filter(std -> std.getId().equals(standardToFind.getId()))
                .findAny()
                .isPresent();
    }

    private Map<String, List<Standard>> getGroupedStandardsForCriteria(CertificationCriterion criterion, LocalDate validAsOfDate) {
        try {
            return standardDAO.getAllStandardCriteriaMap().stream()
                    .filter(stdCriteriaMap -> stdCriteriaMap.getCriterion().getId().equals(criterion.getId())
                            && stdCriteriaMap.getStandard().getGroupName() != null
                            && DateUtil.isDateBetweenInclusive(Pair.of(stdCriteriaMap.getStandard().getStartDay(), stdCriteriaMap.getStandard().getEndDay()), validAsOfDate))
                    .collect(Collectors.groupingBy(value -> value.getStandard().getGroupName(), Collectors.mapping(value -> value.getStandard(), Collectors.toList())));
        } catch (EntityRetrievalException e) {
            LOGGER.error("Error retrieving all StandardCriteriaMaps: {}", e.getStackTrace(), e);
            throw new RuntimeException(e);
        }
    }
}
