package gov.healthit.chpl.functionalitytested;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;
import gov.healthit.chpl.dao.CertificationCriterionAttributeDAO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class FunctionalityTestedManager {
    private FunctionalityTestedValidator functionalityTestedValidator;
    private FunctionalityTestedService functionalityTestedService;
    private FunctionalityTestedDAO functionalityTestedDAO;
    private CertificationCriterionAttributeDAO certificationCriterionAttributeDAO;
    private ErrorMessageUtil errorMessageUtil;
    private CertificationCriterionComparator criteriaComparator;
    private FunctionalityTestedComparator funcTestedComparator;

    @Autowired
    public FunctionalityTestedManager(FunctionalityTestedDAO functionalityTestedDAO, FunctionalityTestedValidator functionalityTestedValidator,
            FunctionalityTestedService functionalityTestedService, CertificationCriterionAttributeDAO certificationCriterionAttributeDAO,
            ErrorMessageUtil errorMessageUtil, CertificationCriterionComparator criteriaComparator) {

        this.functionalityTestedDAO = functionalityTestedDAO;
        this.functionalityTestedValidator = functionalityTestedValidator;
        this.functionalityTestedService = functionalityTestedService;
        this.certificationCriterionAttributeDAO = certificationCriterionAttributeDAO;
        this.errorMessageUtil = errorMessageUtil;
        this.criteriaComparator = criteriaComparator;
        this.funcTestedComparator = new FunctionalityTestedComparator();
    }

    @Transactional
    public List<FunctionalityTested> getAll() {
        return functionalityTestedDAO.findAll();
    }

    @Transactional
    public List<CertificationCriterion> getCertificationCriteriaForFunctionalitiesTested() {
        return certificationCriterionAttributeDAO.getCriteriaForFunctionalitiesTested();
    }


    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FUNCTIONALITY_TESTED, "
            + "T(gov.healthit.chpl.permissions.domains.FunctionalityTestedDomainPermissions).UPDATE)")
    @Transactional
    @ListingStoreRemove(removeBy = RemoveBy.ALL)
    public FunctionalityTested update(FunctionalityTested functionalityTested) throws EntityRetrievalException, ValidationException {
        functionalityTestedValidator.validateForEdit(functionalityTested);
        functionalityTestedService.update(functionalityTested);
        return functionalityTestedDAO.getById(functionalityTested.getId());
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FUNCTIONALITY_TESTED, "
            + "T(gov.healthit.chpl.permissions.domains.FunctionalityTestedDomainPermissions).CREATE)")
    @Transactional
    public FunctionalityTested create(FunctionalityTested functionalityTested) throws EntityRetrievalException, ValidationException {
        functionalityTestedValidator.validateForAdd(functionalityTested);
        return functionalityTestedService.add(functionalityTested);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FUNCTIONALITY_TESTED, "
            + "T(gov.healthit.chpl.permissions.domains.FunctionalityTestedDomainPermissions).DELETE)")
    @Transactional
    public void delete(Long functionalityTestedId) throws EntityRetrievalException, ValidationException {
        FunctionalityTested functionalityTested = functionalityTestedDAO.getById(functionalityTestedId);
        if (functionalityTested == null) {
            ValidationException e = new ValidationException(errorMessageUtil.getMessage("testTool.notFound"));
            throw e;
        }

        functionalityTestedValidator.validateForDelete(functionalityTested);
        functionalityTestedService.delete(functionalityTested);
    }

    public List<FunctionalityTested> getFunctionalitiesTested(Long criteriaId, Long practiceTypeId) {
        List<FunctionalityTested> functionalitiesTestedForCriterion = new ArrayList<FunctionalityTested>();
        Map<Long, List<FunctionalityTested>> functionalitiesTestedByCriteria = functionalityTestedDAO.getFunctionalitiesTestedCriteriaMaps();
        if (functionalitiesTestedByCriteria.containsKey(criteriaId)) {
            functionalitiesTestedForCriterion = functionalitiesTestedByCriteria.get(criteriaId);
            if (practiceTypeId != null) {
                functionalitiesTestedForCriterion = functionalitiesTestedForCriterion.stream()
                        .filter(funcTest -> funcTest.getPracticeType() == null || funcTest.getPracticeType().getId().equals(practiceTypeId))
                        .toList();
            }
        }
        functionalitiesTestedForCriterion.stream()
            .forEach(funcTested -> funcTested.setCriteria(funcTested.getCriteria().stream()
                .sorted(criteriaComparator)
                .toList()));
        return functionalitiesTestedForCriterion.stream()
                .sorted(funcTestedComparator)
                .toList();
    }
}
