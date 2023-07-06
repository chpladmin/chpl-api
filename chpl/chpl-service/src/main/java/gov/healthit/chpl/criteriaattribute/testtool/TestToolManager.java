package gov.healthit.chpl.criteriaattribute.testtool;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.criteriaattribute.CriteriaAttributeSaveContext;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeService;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeValidationContext;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeValidator;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;

@Component
public class TestToolManager {

    private CriteriaAttributeValidator criteriaAttributeValidator;
    private CriteriaAttributeService criteriaAttributeService;
    private TestToolDAO testToolDAO;

    @Autowired
    public TestToolManager(TestToolDAO testToolDAO, CriteriaAttributeValidator criteriaAttributeValidator, CriteriaAttributeService criteriaAttributeService) {
        this.testToolDAO = testToolDAO;
        this.criteriaAttributeValidator = criteriaAttributeValidator;
        this.criteriaAttributeService = criteriaAttributeService;
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

//    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SVAP, "
//            + "T(gov.healthit.chpl.permissions.domains.SvapDomainPermissions).UPDATE)")
    @Transactional
    @ListingStoreRemove(removeBy = RemoveBy.ALL)
    public TestTool update(TestTool testTool) throws EntityRetrievalException, ValidationException {
        criteriaAttributeValidator.validateForEdit(CriteriaAttributeValidationContext.builder()
                .criteriaAttribe(testTool)
                .criteriaAttributeDAO(testToolDAO)
                .name("Test Tool")
                .build());

        criteriaAttributeService.updateCriteriaAttribute(CriteriaAttributeSaveContext.builder()
                .criteriaAttribute(testTool)
                .criteriaAttributeDAO(testToolDAO)
                .name("Test Tool")
                .build());
        return testToolDAO.getById(testTool.getId());
    }

//    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SVAP, "
//            + "T(gov.healthit.chpl.permissions.domains.SvapDomainPermissions).CREATE)")
//    @Transactional
//    public TestTool create(TestTool testTool) throws EntityRetrievalException, ValidationException {
//        validateForAdd(svap);
//        Svap newSvap = addSvap(svap);
//        addNewCriteriaForNewSvap(newSvap, svap.getCriteria());
//        return getSvap(newSvap.getSvapId());
//    }

//    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SVAP, "
//            + "T(gov.healthit.chpl.permissions.domains.SvapDomainPermissions).DELETE)")
//    @Transactional
//    public void delete(Long testToolId) throws EntityRetrievalException, ValidationException {
//        Svap originalSvap = getSvap(svapId);
//        validateForDelete(originalSvap);
//        deleteAllCriteriaFromSvap(originalSvap);
//        deleteSvap(originalSvap);
//    }
}
