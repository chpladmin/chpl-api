package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.functionalitytested.FunctionalityTested;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedDAO;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedManager;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;

@Component("functionalityTestedAllowedByCriteriaReviewer")
@Transactional
@DependsOn("certificationEditionDAO")
public class FunctionalityTestedAllowedByCriteriaReviewer extends PermissionBasedReviewer {
    private FunctionalityTestedDAO functionalityTestedDao;
    private FunctionalityTestedManager functionalityTestedManager;

    @Autowired
    public FunctionalityTestedAllowedByCriteriaReviewer(FunctionalityTestedManager functionalityTestedManager,
            FunctionalityTestedDAO functionalityTestedDao,
            ErrorMessageUtil msgUtil, ResourcePermissionsFactory resourcePermissionsFactory) {
        super(msgUtil, resourcePermissionsFactory);
        this.functionalityTestedManager = functionalityTestedManager;
        this.functionalityTestedDao = functionalityTestedDao;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null) {
            for (CertificationResult cr : listing.getCertificationResults()) {
                if (BooleanUtils.isTrue(cr.getSuccess()) && cr.getFunctionalitiesTested() != null) {
                    for (CertificationResultFunctionalityTested crft : cr.getFunctionalitiesTested()) {
                        addFunctionalitiesTestedErrorMessages(crft, cr, listing);
                    }
                }
            }
        }
    }

    private void addFunctionalitiesTestedErrorMessages(CertificationResultFunctionalityTested crft,
            CertificationResult cr, CertifiedProductSearchDetails listing) {
        FunctionalityTested functionalityTested = null;
        if (crft.getFunctionalityTested().getId() != null) {
            functionalityTested = getFunctionalityTested(crft.getFunctionalityTested().getId(), cr.getCriterion().getId());
            if (functionalityTested == null) {
                listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.invalidFunctionalityTestedId", Util.formatCriteriaNumber(cr.getCriterion()), crft.getFunctionalityTested().getId()));
            }
        } else if (!StringUtils.isEmpty(crft.getFunctionalityTested().getValue())) {
            functionalityTested = getFunctionalityTested(crft.getFunctionalityTested().getValue(), cr.getCriterion().getId());
            if (!isFunctionalityTestedCritierionValid(cr.getCriterion().getId(), functionalityTested)) {
                addFunctionalitiesTestedCriterionErrorMessage(crft, cr, listing);
            }
        }
        reviewFunctionalityTestedRetiredBeforeListingActiveDates(listing, cr, crft);
        reviewFunctionalityTestedAvailabilityAfterListingActiveDates(listing, cr, crft);
    }

    private Boolean isFunctionalityTestedCritierionValid(Long criteriaId, FunctionalityTested functionalityTested) {
        List<FunctionalityTested> validFunctionalityTestedForCriteria = functionalityTestedManager.getFunctionalitiesTested(criteriaId, null);

        if (validFunctionalityTestedForCriteria == null) {
            return false;
        } else {
            // Is the functionality tested in the valid list (relies on the FunctionalityTested.equals()
            return validFunctionalityTestedForCriteria.contains(functionalityTested);
        }
    }

    private void addFunctionalitiesTestedCriterionErrorMessage(CertificationResultFunctionalityTested crft,
            CertificationResult cr, CertifiedProductSearchDetails cp) {

        FunctionalityTested functionalityTested = getFunctionalityTested(crft.getFunctionalityTested().getId(), cr.getCriterion().getId());
        if (functionalityTested == null || functionalityTested.getId() == null) {
            cp.addDataErrorMessage(msgUtil.getMessage("listing.criteria.invalidFunctionalityTested", Util.formatCriteriaNumber(cr.getCriterion()), crft.getFunctionalityTested().getValue()));

        } else {
            cp.addBusinessErrorMessage(getFunctionalityTestedCriterionErrorMessage(
                    Util.formatCriteriaNumber(cr.getCriterion()),
                    crft.getFunctionalityTested().getValue(),
                    getDelimitedListOfValidCriteriaNumbers(functionalityTested),
                    Util.formatCriteriaNumber(cr.getCriterion())));
        }
    }

    private String getFunctionalityTestedCriterionErrorMessage(String criteriaNumber,
            String functionalityTestedNumber, String listOfValidCriteria, String currentCriterion) {

        return msgUtil.getMessage("listing.criteria.functionalityTestedCriterionMismatch",
                criteriaNumber, functionalityTestedNumber, listOfValidCriteria, currentCriterion);
    }

    private FunctionalityTested getFunctionalityTested(Long functionalityTestedId, Long criterionId) {
        Map<Long, List<FunctionalityTested>> funcTestedMappings = functionalityTestedDao.getFunctionalitiesTestedCriteriaMaps();
        if (!funcTestedMappings.containsKey(criterionId)) {
            return null;
        }
        List<FunctionalityTested> functionalityTestedForCriterion = funcTestedMappings.get(criterionId);
        Optional<FunctionalityTested> funcTestedOpt = functionalityTestedForCriterion.stream()
                .filter(funcTested -> funcTested.getId().equals(functionalityTestedId))
                .findAny();
        return funcTestedOpt.isPresent() ? funcTestedOpt.get() : null;
    }

    private FunctionalityTested getFunctionalityTested(String functionalityTestedNumber, Long criterionId) {
        Map<Long, List<FunctionalityTested>> funcTestedMappings = functionalityTestedDao.getFunctionalitiesTestedCriteriaMaps();
        if (!funcTestedMappings.containsKey(criterionId)) {
            return null;
        }
        List<FunctionalityTested> functionalityTestedForCriterion = funcTestedMappings.get(criterionId);
        Optional<FunctionalityTested> funcTestedOpt = functionalityTestedForCriterion.stream()
                .filter(funcTested -> funcTested.getValue().equalsIgnoreCase(functionalityTestedNumber))
                .findAny();
        return funcTestedOpt.isPresent() ? funcTestedOpt.get() : null;
    }

    private String getDelimitedListOfValidCriteriaNumbers(FunctionalityTested functionalityTested) {
        List<String> criteriaNumbers = functionalityTested.getCriteria().stream()
                .map(criterion -> Util.formatCriteriaNumber(criterion))
                .collect(Collectors.toList());
        return Util.joinListGrammatically(criteriaNumbers);
    }

    private void reviewFunctionalityTestedRetiredBeforeListingActiveDates(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultFunctionalityTested functionalityTested) {
        if (isFunctionalityTestedRetiredBeforeListingActiveDates(listing, functionalityTested.getFunctionalityTested())) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.functionalityTestedUnavailable",
                    functionalityTested.getFunctionalityTested().getValue(),
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewFunctionalityTestedAvailabilityAfterListingActiveDates(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultFunctionalityTested functionalityTested) {
        if (isFunctionalityTestedActiveAfterListingActiveDates(listing, functionalityTested.getFunctionalityTested())) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.criteria.functionalityTestedUnavailable",
                    functionalityTested.getFunctionalityTested().getValue(),
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private boolean isFunctionalityTestedRetiredBeforeListingActiveDates(CertifiedProductSearchDetails listing, FunctionalityTested functionalityTested) {
        LocalDate listingStartDay = listing.getCertificationDay();
        LocalDate funcTestedEndDay = functionalityTested.getEndDay() == null ? LocalDate.MAX : functionalityTested.getEndDay();
        return funcTestedEndDay.isBefore(listingStartDay);
    }

    private boolean isFunctionalityTestedActiveAfterListingActiveDates(CertifiedProductSearchDetails listing, FunctionalityTested functionalityTested) {
        LocalDate listingEndDay = listing.getDecertificationDay() == null ? LocalDate.now() : listing.getDecertificationDay();
        LocalDate funcTestedStartDay = functionalityTested.getStartDay() == null ? LocalDate.MIN : functionalityTested.getStartDay();
        return funcTestedStartDay.isAfter(listingEndDay);
    }
}
