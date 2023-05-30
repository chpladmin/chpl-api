package gov.healthit.chpl.validation.listing.reviewer.edition2015;

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

import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalityTested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.functionalityTested.FunctionalityTested;
import gov.healthit.chpl.functionalityTested.FunctionalityTestedDAO;
import gov.healthit.chpl.functionalityTested.FunctionalityTestedManager;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;

@Component("functionalityTestedAllowedByCriteriaReviewer")
@Transactional
@DependsOn("certificationEditionDAO")
public class FunctionalityTestedAllowedByCriteriaReviewer extends PermissionBasedReviewer {
    private FunctionalityTestedDAO functionalityTestedDao;
    private FunctionalityTestedManager functionalityTestedManager;
    private DimensionalDataManager dimensionalDataManager;

    @Autowired
    public FunctionalityTestedAllowedByCriteriaReviewer(FunctionalityTestedManager functionalityTestedManager,
            FunctionalityTestedDAO functionalityTestedDao,
            CertificationEditionDAO editionDAO,
            DimensionalDataManager dimensionalDataManager,
            ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        this.functionalityTestedManager = functionalityTestedManager;
        this.functionalityTestedDao = functionalityTestedDao;
        this.dimensionalDataManager = dimensionalDataManager;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() != null) {
            for (CertificationResult cr : listing.getCertificationResults()) {
                if (BooleanUtils.isTrue(cr.isSuccess()) && cr.getFunctionalitiesTested() != null) {
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
        if (crft.getFunctionalityTestedId() != null) {
            functionalityTested = getFunctionalityTested(crft.getFunctionalityTestedId(), cr.getCriterion().getId());
            if (functionalityTested == null) {
                listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.invalidFunctionalityTestedId", Util.formatCriteriaNumber(cr.getCriterion()), crft.getFunctionalityTestedId()));
            }
        } else if (!StringUtils.isEmpty(crft.getName())) {
            functionalityTested = getFunctionalityTested(crft.getName(), cr.getCriterion().getId());
            if (!isFunctionalityTestedCritierionValid(cr.getCriterion().getId(), functionalityTested)) {
                addFunctionalitiesTestedCriterionErrorMessage(crft, cr, listing);
            }
        }
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

        FunctionalityTested functionalityTested = getFunctionalityTested(crft.getFunctionalityTestedId(), cr.getCriterion().getId());
        if (functionalityTested == null || functionalityTested.getId() == null) {
            cp.addDataErrorMessage(msgUtil.getMessage("listing.criteria.invalidFunctionalityTested", Util.formatCriteriaNumber(cr.getCriterion()), crft.getName()));

        } else {
            cp.addBusinessErrorMessage(getFunctionalityTestedCriterionErrorMessage(
                    Util.formatCriteriaNumber(cr.getCriterion()),
                    crft.getName(),
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
                .filter(funcTested -> funcTested.getName().equalsIgnoreCase(functionalityTestedNumber))
                .findAny();
        return funcTestedOpt.isPresent() ? funcTestedOpt.get() : null;
    }

    private String getDelimitedListOfValidCriteriaNumbers(FunctionalityTested functionalityTested) {
        List<String> criteriaNumbers = functionalityTested.getCriteria().stream()
                .map(criterion -> Util.formatCriteriaNumber(criterion))
                .collect(Collectors.toList());
        return Util.joinListGrammatically(criteriaNumbers);
    }
}
