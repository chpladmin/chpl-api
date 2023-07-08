package gov.healthit.chpl.criteriaattribute.testtool;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.criteriaattribute.CriteriaAttribute;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeSaveContext;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeService;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeValidationContext;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeValidator;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class TestToolManager {

    private CriteriaAttributeValidator criteriaAttributeValidator;
    private CriteriaAttributeService criteriaAttributeService;
    private TestToolDAO testToolDAO;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestToolManager(TestToolDAO testToolDAO, CriteriaAttributeValidator criteriaAttributeValidator,
            CriteriaAttributeService criteriaAttributeService, ErrorMessageUtil errorMessageUtil) {

        this.testToolDAO = testToolDAO;
        this.criteriaAttributeValidator = criteriaAttributeValidator;
        this.criteriaAttributeService = criteriaAttributeService;
        this.errorMessageUtil = errorMessageUtil;
    }

//    public List<TestToolCriteriaMap> getAllTestToolCriteriaMaps() throws EntityRetrievalException {
//        return svapDao.getAllSvapCriteriaMap();
//    }

//    @Transactional
//    public List<CertificationCriterion> getCertificationCriteriaForTestTool() {
//        return certificationCriterionAttributeDAO.getCriteriaForSvap();
//    }

    @Transactional
    public List<TestTool> getAll() {
        return testToolDAO.getAll();
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).TEST_TOOL, "
            + "T(gov.healthit.chpl.permissions.domains.TestToolDomainPermissions).UPDATE)")
    @Transactional
    @ListingStoreRemove(removeBy = RemoveBy.ALL)
    public TestTool update(TestTool testTool) throws EntityRetrievalException, ValidationException {
        criteriaAttributeValidator.validateForEdit(CriteriaAttributeValidationContext.builder()
                .criteriaAttribe(testTool)
                .criteriaAttributeDAO(testToolDAO)
                .name("Test Tool")
                .build());

        criteriaAttributeService.update(CriteriaAttributeSaveContext.builder()
                .criteriaAttribute(testTool)
                .criteriaAttributeDAO(testToolDAO)
                .name("Test Tool")
                .build());
        return testToolDAO.getById(testTool.getId());
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).TEST_TOOL, "
            + "T(gov.healthit.chpl.permissions.domains.TestToolDomainPermissions).CREATE)")
    @Transactional
    public TestTool create(TestTool testTool) throws EntityRetrievalException, ValidationException {
        criteriaAttributeValidator.validateForAdd(CriteriaAttributeValidationContext.builder()
                .criteriaAttribe(testTool)
                .criteriaAttributeDAO(testToolDAO)
                .name("Test Tool")
                .build());

        CriteriaAttribute criteriaAttribute = criteriaAttributeService.add(CriteriaAttributeSaveContext.builder()
                .criteriaAttribute(testTool)
                .criteriaAttributeDAO(testToolDAO)
                .name("Test Tool")
                .build());

        return testToolDAO.getById(criteriaAttribute.getId());
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).TEST_TOOL, "
            + "T(gov.healthit.chpl.permissions.domains.TestToolDomainPermissions).DELETE)")
    @Transactional
    public void delete(Long testToolId) throws EntityRetrievalException, ValidationException {
        TestTool testTool = testToolDAO.getById(testToolId);
        if (testTool == null) {
            ValidationException e = new ValidationException(errorMessageUtil.getMessage("testTool.notFound"));
            throw e;
        }
        criteriaAttributeValidator.validateForDelete(CriteriaAttributeValidationContext.builder()
                .criteriaAttribe(testTool)
                .criteriaAttributeDAO(testToolDAO)
                .name("Test Tool")
                .build());

        criteriaAttributeService.delete(CriteriaAttributeSaveContext.builder()
                .criteriaAttribute(testTool)
                .criteriaAttributeDAO(testToolDAO)
                .name("Test Tool")
                .build());
    }
}
