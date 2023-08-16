package gov.healthit.chpl.criteriaattribute.functionalitytested;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.criteriaattribute.CriteriaAttribute;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeSaveContext;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeService;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeValidationContext;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeValidator;
import gov.healthit.chpl.dao.CertificationCriterionAttributeDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.comparator.CertificationCriterionComparator;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class FunctionalityTestedManager {
    private CriteriaAttributeValidator criteriaAttributeValidator;
    private CriteriaAttributeService criteriaAttributeService;
    private FunctionalityTestedDAO functionalityTestedDAO;
    private CertificationCriterionAttributeDAO certificationCriterionAttributeDAO;
    private ErrorMessageUtil errorMessageUtil;
    private CertificationCriterionComparator criteriaComparator;
    private FunctionalityTestedComparator funcTestedComparator;

    @Autowired
    public FunctionalityTestedManager(FunctionalityTestedDAO functionalityTestedDAO, CriteriaAttributeValidator criteriaAttributeValidator,
            CriteriaAttributeService criteriaAttributeService, CertificationCriterionAttributeDAO certificationCriterionAttributeDAO,
            ErrorMessageUtil errorMessageUtil, CertificationCriterionComparator criteriaComparator) {

        this.functionalityTestedDAO = functionalityTestedDAO;
        this.criteriaAttributeValidator = criteriaAttributeValidator;
        this.criteriaAttributeService = criteriaAttributeService;
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


    //@PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).TEST_TOOL, "
    //        + "T(gov.healthit.chpl.permissions.domains.TestToolDomainPermissions).UPDATE)")
    @Transactional
    @ListingStoreRemove(removeBy = RemoveBy.ALL)
    public FunctionalityTested update(FunctionalityTested functionalityTested) throws EntityRetrievalException, ValidationException {
        criteriaAttributeValidator.validateForEdit(CriteriaAttributeValidationContext.builder()
                .criteriaAttribute(functionalityTested)
                .criteriaAttributeDAO(functionalityTestedDAO)
                .name("Functionality Tested")
                .build());

        criteriaAttributeService.update(CriteriaAttributeSaveContext.builder()
                .criteriaAttribute(functionalityTested)
                .criteriaAttributeDAO(functionalityTestedDAO)
                .name("Functionality Tested")
                .build());
        return functionalityTestedDAO.getById(functionalityTested.getId());
    }

    //@PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).TEST_TOOL, "
    //        + "T(gov.healthit.chpl.permissions.domains.TestToolDomainPermissions).CREATE)")
    @Transactional
    public FunctionalityTested create(FunctionalityTested functionalityTested) throws EntityRetrievalException, ValidationException {
        criteriaAttributeValidator.validateForAdd(CriteriaAttributeValidationContext.builder()
                .criteriaAttribute(functionalityTested)
                .criteriaAttributeDAO(functionalityTestedDAO)
                .name("Functionality Tested")
                .build());

        CriteriaAttribute criteriaAttribute = criteriaAttributeService.add(CriteriaAttributeSaveContext.builder()
                .criteriaAttribute(functionalityTested)
                .criteriaAttributeDAO(functionalityTestedDAO)
                .name("Functionality Tested")
                .build());

        return functionalityTestedDAO.getById(criteriaAttribute.getId());
    }

    //@PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).TEST_TOOL, "
    //        + "T(gov.healthit.chpl.permissions.domains.TestToolDomainPermissions).DELETE)")
    @Transactional
    public void delete(Long testToolId) throws EntityRetrievalException, ValidationException {
        FunctionalityTested functionalityTested = functionalityTestedDAO.getById(testToolId);
        if (functionalityTested == null) {
            ValidationException e = new ValidationException(errorMessageUtil.getMessage("testTool.notFound"));
            throw e;
        }
        criteriaAttributeValidator.validateForDelete(CriteriaAttributeValidationContext.builder()
                .criteriaAttribute(functionalityTested)
                .criteriaAttributeDAO(functionalityTestedDAO)
                .name("Functionality Tested")
                .build());

        criteriaAttributeService.delete(CriteriaAttributeSaveContext.builder()
                .criteriaAttribute(functionalityTested)
                .criteriaAttributeDAO(functionalityTestedDAO)
                .name("Functionality Tested")
                .build());
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
