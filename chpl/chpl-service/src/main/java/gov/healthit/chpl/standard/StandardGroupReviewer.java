package gov.healthit.chpl.standard;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public abstract class StandardGroupReviewer implements Reviewer {

    private StandardGroupService standardGroupService;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public StandardGroupReviewer(StandardGroupService standardGroupService, ErrorMessageUtil msgUtil) {
        this.standardGroupService = standardGroupService;
        this.msgUtil = msgUtil;
    }

    public abstract boolean allowsExtension();

    public void reviewStandardExistsForEachGroup(CertifiedProductSearchDetails listing, CertificationResult certResult, LocalDate validAsOfDate) {
        standardGroupService.getGroupedStandardsForCriteria(certResult.getCriterion(), validAsOfDate).entrySet().stream()
                .filter(standardGroup -> standardGroup.getValue().size() >= 2)
                .forEach(standardGroup -> {
                    if (!doesAtLeastOneStandardForGroupExistForCriterion(standardGroup.getValue(), certResult)) {
                        //Assume all of the active standards within the group have the same required day,
                        //and further assume that all grouped standards with the same required day will have
                        //the same extension end day. It is possible to set different values for these
                        //things using the UI or API but it is not a realistic scenario at this time.
                        LocalDate extensionEndDay = standardGroup.getValue().get(0).getExtensionEndDay();
                        if (allowsExtension()
                                && extensionEndDay != null
                                && validAsOfDate.isBefore(extensionEndDay)) {
                            listing.addWarningMessage(msgUtil.getMessage("listing.criteria.standardGroupNotSelectedDuringExtensionPeriod",
                                    Util.formatCriteriaNumber(certResult.getCriterion()),
                                    standardGroup.getValue().stream().map(std -> std.getRegulatoryTextCitation()).collect(Collectors.joining(", ")),
                                    DateUtil.format(extensionEndDay)));
                        } else {
                            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.standardGroupNotSelected",
                                Util.formatCriteriaNumber(certResult.getCriterion()),
                                standardGroup.getValue().stream().map(std -> std.getRegulatoryTextCitation()).collect(Collectors.joining(", "))));
                        }
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
