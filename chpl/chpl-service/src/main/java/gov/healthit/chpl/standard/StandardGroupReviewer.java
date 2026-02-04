package gov.healthit.chpl.standard;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.collections.impl.block.factory.Comparators;
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

    public void reviewStandardExistsForEachGroup(CertifiedProductSearchDetails listing, CertificationResult certResult,
            LocalDate validAsOfDateRangeStart, LocalDate validAsOfDateRangeEnd) {
        standardGroupService.getGroupedStandardsForCriteria(certResult.getCriterion(), validAsOfDateRangeStart, validAsOfDateRangeEnd).entrySet().stream()
                .filter(standardGroup -> standardGroup.getValue().size() >= 2 && groupHasSomeUnexpiredStandards(standardGroup.getValue()))
                .forEach(standardGroup -> {
                    List<Standard> attestedStandardsFromGroup = getAttestedStandardsFromGroup(standardGroup.getValue(), certResult);
                    if (CollectionUtils.isEmpty(attestedStandardsFromGroup)) {
                        // There MUST be at least one standard per group on the listing
                        listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.standardGroupNotSelected",
                                Util.formatCriteriaNumber(certResult.getCriterion()),
                                standardGroup.getValue().stream().map(std -> std.getRegulatoryTextCitation()).collect(Collectors.joining(", "))));
                    } else {
                        // At least one standard in the group is attested.
                        // Determine if any of the unattested standards in this group have a required date that is more recent than the attested standards from the group
                        // If the required date has passed but not the extension end date, give a warning.
                        // If the extension end date has also passed, give an error.
                        List<Standard> unattestedRequiredStandardsInGroup = getUnattestedStandardsWithRequiredDateForCriterion(
                                standardGroup.getValue(), certResult, validAsOfDateRangeEnd);
                        if (!CollectionUtils.isEmpty(unattestedRequiredStandardsInGroup)) {
                            //Calculate extension end day - not all of the standards will necessarily have one and they won't all necessarily be the same
                            //but we should use the earliest non-null day for our message.
                            LocalDate extensionEndDay = getEarliestExtensionEndDay(unattestedRequiredStandardsInGroup);
                            if (allowsExtension()
                                    && extensionEndDay != null
                                    && validAsOfDateRangeEnd.isBefore(extensionEndDay)) {
                                // Give a grammatically different message if it's multiple standards that will be required vs just one
                                if (unattestedRequiredStandardsInGroup.size() > 1) {
                                    listing.addWarningMessage(msgUtil.getMessage("listing.criteria.standardGroupNotSelectedDuringExtensionPeriod",
                                            Util.formatCriteriaNumber(certResult.getCriterion()),
                                            unattestedRequiredStandardsInGroup.stream().map(std -> std.getRegulatoryTextCitation()).collect(Collectors.joining(", ")),
                                            DateUtil.format(extensionEndDay.plusDays(1))));
                                } else {
                                    listing.addWarningMessage(msgUtil.getMessage("listing.criteria.standardNotSelectedDuringExtensionPeriod",
                                            Util.formatCriteriaNumber(certResult.getCriterion()),
                                            unattestedRequiredStandardsInGroup.get(0).getRegulatoryTextCitation(),
                                            DateUtil.format(extensionEndDay.plusDays(1))));
                                }
                            } else {
                                // Give a grammatically different message if it's multiple standards that are required vs just one
                                if (unattestedRequiredStandardsInGroup.size() > 1) {
                                    listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.standardGroupNotSelected",
                                            Util.formatCriteriaNumber(certResult.getCriterion()),
                                            unattestedRequiredStandardsInGroup.stream().map(std -> std.getRegulatoryTextCitation()).collect(Collectors.joining(", "))));
                                } else {
                                    listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.standardNotSelected",
                                            Util.formatCriteriaNumber(certResult.getCriterion()),
                                            unattestedRequiredStandardsInGroup.get(0).getRegulatoryTextCitation()));
                                }
                            }
                        }
                    }
                });
    }

    private boolean groupHasSomeUnexpiredStandards(List<Standard> standards) {
        return standards.stream()
                .filter(std -> std.getEndDay() == null || std.getEndDay().isAfter(LocalDate.now()))
                .findAny()
                .isPresent();
    }

    private List<Standard> getAttestedStandardsFromGroup(List<Standard> groupedStandards, CertificationResult certResult) {
        return groupedStandards.stream()
                .filter(standardFromGroup -> isStandardInList(standardFromGroup, certResult.getStandards().stream().map(certResultStd -> certResultStd.getStandard()).toList()))
                .collect(Collectors.toList());
    }

    private List<Standard> getUnattestedStandardsWithRequiredDateForCriterion(List<Standard> groupedStandards,
            CertificationResult certResult, LocalDate requiredAsOfDate) {
        List<Standard> attestedStandardsFromGroup = getAttestedStandardsFromGroup(groupedStandards, certResult);
        return groupedStandards.stream()
                .filter(stdFromGroup -> stdFromGroup.getRequiredDay() != null
                        && stdFromGroup.getRequiredDay().isBefore(requiredAsOfDate)
                        && stdFromGroup.getRequiredDay().isAfter(getLatestRequiredDateFromStandardList(attestedStandardsFromGroup)))
                .filter(reqStandardFromGroup -> !isStandardInList(reqStandardFromGroup, certResult.getStandards().stream().map(certResultStd -> certResultStd.getStandard()).toList()))
                .collect(Collectors.toList());
    }

    private LocalDate getEarliestExtensionEndDay(List<Standard> groupedStandards) {
        return groupedStandards.stream()
                .filter(std -> std.getExtensionEndDay() != null)
                .map(std -> std.getExtensionEndDay())
                .min(Comparators.naturalOrder())
                .orElse(LocalDate.MAX);
    }

    private LocalDate getLatestRequiredDateFromStandardList(List<Standard> groupedStandards) {
        return groupedStandards.stream()
                .filter(std -> std.getRequiredDay() != null)
                .map(std -> std.getRequiredDay())
                .max(Comparators.naturalOrder())
                .orElse(LocalDate.MIN);
    }

    private boolean isStandardInList(Standard standardToFind, List<Standard> standard) {
        return standard.stream()
                .filter(std -> std.getId().equals(standardToFind.getId()))
                .findAny()
                .isPresent();
    }
}
