package gov.healthit.chpl.criteriaattribute.functionalitytested;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.criteriaattribute.CriteriaAttribute;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeSaveContext;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeService;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeValidationContext;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeValidator;
import gov.healthit.chpl.dao.CertificationCriterionAttributeDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.functionalityTested.FunctionalityTested;
import gov.healthit.chpl.functionalityTested.FunctionalityTestedDAO;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class FunctionalityTestedManager {
    private CriteriaAttributeValidator criteriaAttributeValidator;
    private CriteriaAttributeService criteriaAttributeService;
    private FunctionalityTestedDAO functionalityTestedDAO;
    private CertificationCriterionAttributeDAO certificationCriterionAttributeDAO;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public FunctionalityTestedManager(FunctionalityTestedDAO functionalityTestedDAO, CriteriaAttributeValidator criteriaAttributeValidator,
            CriteriaAttributeService criteriaAttributeService, CertificationCriterionAttributeDAO certificationCriterionAttributeDAO,
            ErrorMessageUtil errorMessageUtil) {

        this.functionalityTestedDAO = functionalityTestedDAO;
        this.criteriaAttributeValidator = criteriaAttributeValidator;
        this.criteriaAttributeService = criteriaAttributeService;
        this.certificationCriterionAttributeDAO = certificationCriterionAttributeDAO;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Transactional
    public List<FunctionalityTested> getAll() {
        return functionalityTestedDAO.findAll();
    }

    @Transactional
    public List<CertificationCriterion> getCertificationCriteriaForTestTools() {
        return certificationCriterionAttributeDAO.getCriteriaForTestTools();
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

}
