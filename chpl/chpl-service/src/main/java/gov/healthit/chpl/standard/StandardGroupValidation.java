package gov.healthit.chpl.standard;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public abstract class StandardGroupValidation extends PermissionBasedReviewer{

    private StandardGroupService standardGroupService;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public StandardGroupValidation(StandardGroupService standardGroupService, ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);

        this.standardGroupService = standardGroupService;
        this.msgUtil = msgUtil;
    }

    public void reviewStandardExistForEachGroup(CertifiedProductSearchDetails listing, CertificationResult certResult, LocalDate validAsOfDate) {
        standardGroupService.getGroupedStandardsForCriteria(certResult.getCriterion(), validAsOfDate).entrySet().stream()
                .filter(standardGroup -> standardGroup.getValue().size() >= 2)
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
}
