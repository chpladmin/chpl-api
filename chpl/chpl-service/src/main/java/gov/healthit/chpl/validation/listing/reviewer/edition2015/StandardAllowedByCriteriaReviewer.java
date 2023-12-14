package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.standard.CertificationResultStandard;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.standard.StandardManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class StandardAllowedByCriteriaReviewer extends PermissionBasedReviewer {

    private StandardDAO standardDAO;
    private StandardManager standardManager;

    @Autowired
    public StandardAllowedByCriteriaReviewer(StandardManager standardManager,
            StandardDAO standardDAO,
            ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        this.standardManager = standardManager;
        this.standardDAO = standardDAO;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null) {
            for (CertificationResult cr : listing.getCertificationResults()) {
                if (BooleanUtils.isTrue(cr.isSuccess()) && cr.getStandards() != null) {
                    reviewStandardExistForEachGroup(listing, cr);
                    for (CertificationResultStandard crs : cr.getStandards()) {
                        addStandardErrorMessages(crs, cr, listing);
                    }
                }
            }
        }
    }

    private void addStandardErrorMessages(CertificationResultStandard crs, CertificationResult cr, CertifiedProductSearchDetails listing) {
        Standard standard = null;
        if (crs.getStandard().getId() != null) {
            standard = getStandard(crs.getStandard().getId(), cr.getCriterion().getId());
            if (standard == null) {
                listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.invalidStandardId", Util.formatCriteriaNumber(cr.getCriterion()), crs.getStandard().getId()));
            }
        } else if (!StringUtils.isEmpty(crs.getStandard().getRegulatoryTextCitation())) {
            standard = getStandard(crs.getStandard().getRegulatoryTextCitation(), cr.getCriterion().getId());
            if (!isStandardCritierionValid(cr.getCriterion().getId(), standard)) {
                addStandardCriterionErrorMessage(crs, cr, listing);
            }
        }
        reviewStandardRetiredBeforeListingActiveDates(listing, cr, crs);
        reviewStandardAvailabilityAfterListingActiveDates(listing, cr, crs);
    }

    private Boolean isStandardCritierionValid(Long criteriaId, Standard standard) {
        List<Standard> validStandardsForCriteria = standardManager.getStandardsByCriteria(criteriaId);

        if (validStandardsForCriteria == null) {
            return false;
        } else {
            // Is the standard in the valid list (relies on the Standard.equals()
            return validStandardsForCriteria.contains(standard);
        }
    }

    private void addStandardCriterionErrorMessage(CertificationResultStandard crs, CertificationResult cr, CertifiedProductSearchDetails cp) {
        Standard standard = getStandard(crs.getStandard().getId(), cr.getCriterion().getId());
        if (standard == null || standard.getId() == null) {
            cp.addDataErrorMessage(msgUtil.getMessage("listing.criteria.invalidStandard", Util.formatCriteriaNumber(cr.getCriterion()), crs.getStandard().getRegulatoryTextCitation()));
        } else {
            cp.addBusinessErrorMessage(getStandardCriterionErrorMessage(
                    Util.formatCriteriaNumber(cr.getCriterion()),
                    crs.getStandard().getRegulatoryTextCitation(),
                    getDelimitedListOfValidCriteriaNumbers(standard),
                    Util.formatCriteriaNumber(cr.getCriterion())));
        }
    }

    private String getStandardCriterionErrorMessage(String criteriaNumber,
            String regulatoryTextCitation, String listOfValidCriteria, String currentCriterion) {

        return msgUtil.getMessage("listing.criteria.standardCriterionMismatch",
                criteriaNumber, regulatoryTextCitation, listOfValidCriteria, currentCriterion);
    }

    private Standard getStandard(Long standardId, Long criterionId) {
        Map<Long, List<Standard>> standardMappings = standardDAO.getStandardCriteriaMaps();
        if (!standardMappings.containsKey(criterionId)) {
            return null;
        }
        List<Standard> standardForCriterion = standardMappings.get(criterionId);
        Optional<Standard> standardOpt = standardForCriterion.stream()
                .filter(standard -> standard.getId().equals(standardId))
                .findAny();
        return standardOpt.isPresent() ? standardOpt.get() : null;
    }

    private Standard getStandard(String regulatoryTextCitation, Long criterionId) {
        Map<Long, List<Standard>> standardMappings = standardDAO.getStandardCriteriaMaps();
        if (!standardMappings.containsKey(criterionId)) {
            return null;
        }
        List<Standard> standardForCriterion = standardMappings.get(criterionId);
        Optional<Standard> standardOpt = standardForCriterion.stream()
                .filter(standard -> standard.getRegulatoryTextCitation().equalsIgnoreCase(regulatoryTextCitation))
                .findAny();
        return standardOpt.isPresent() ? standardOpt.get() : null;
    }

    private String getDelimitedListOfValidCriteriaNumbers(Standard standard) {
        List<String> criteriaNumbers = standard.getCriteria().stream()
                .map(criterion -> Util.formatCriteriaNumber(criterion))
                .collect(Collectors.toList());
        return Util.joinListGrammatically(criteriaNumbers);
    }

    private void reviewStandardRetiredBeforeListingActiveDates(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultStandard standard) {
        if (isStandardRetiredBeforeListingActiveDates(listing, standard.getStandard())) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.standardUnavailable",
                    standard.getStandard().getValue(),
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewStandardAvailabilityAfterListingActiveDates(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultStandard standard) {
        if (isStandardActiveAfterListingActiveDates(listing, standard.getStandard())) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.standardUnavailable",
                    standard.getStandard().getValue(),
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private boolean isStandardRetiredBeforeListingActiveDates(CertifiedProductSearchDetails listing, Standard standard) {
        LocalDate listingStartDay = listing.getCertificationDay();
        LocalDate funcTestedEndDay = standard.getEndDay() == null ? LocalDate.MAX : standard.getEndDay();
        return funcTestedEndDay.isBefore(listingStartDay);
    }

    private boolean isStandardActiveAfterListingActiveDates(CertifiedProductSearchDetails listing, Standard standard) {
        LocalDate listingEndDay = listing.getDecertificationDay() == null ? LocalDate.now() : listing.getDecertificationDay();
        LocalDate funcTestedStartDay = standard.getStartDay() == null ? LocalDate.MIN : standard.getStartDay();
        return funcTestedStartDay.isAfter(listingEndDay);
    }

    private void reviewStandardExistForEachGroup(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        getGroupedStandardsForCriteria(certResult.getCriterion()).entrySet().stream()
                .forEach(standardGroup -> {
                    if (!doesOneStandardForGroupExistForCriterion(standardGroup.getValue(), certResult)) {
                        listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.standardGroupNotSelected",
                                Util.formatCriteriaNumber(certResult.getCriterion()),
                                standardGroup.getValue().stream().map(std -> std.getRegulatoryTextCitation()).collect(Collectors.joining(", "))));
                    }
                });
    }

    private boolean doesOneStandardForGroupExistForCriterion(List<Standard> groupedStandards, CertificationResult certResult) {
        return groupedStandards.stream()
                .filter(standardFromGroup -> isStandardInList(standardFromGroup, certResult.getStandards().stream().map(certResultStd -> certResultStd.getStandard()).toList()))
                .count() == 1;
    }

    private boolean isStandardInList(Standard standardToFind, List<Standard> standard) {
        return standard.stream()
                .filter(std -> std.getId().equals(standardToFind.getId()))
                .findAny()
                .isPresent();
    }
    private Map<String, List<Standard>> getGroupedStandardsForCriteria(CertificationCriterion criterion) {
        try {
            return standardDAO.getAllStandardCriteriaMap().stream()
                    .filter(stdCriteriaMap -> stdCriteriaMap.getCriterion().getId().equals(criterion.getId())
                            && stdCriteriaMap.getStandard().getGroupName() != null)
                    .collect(Collectors.groupingBy(value -> value.getStandard().getGroupName(), Collectors.mapping(value -> value.getStandard(), Collectors.toList())));
        } catch (EntityRetrievalException e) {
            LOGGER.error("Error retrieving all StandardCriteriaMaps: {}", e.getStackTrace(), e);
            throw new RuntimeException(e);
        }
    }

}
