package gov.healthit.chpl.criteriaattribute.testtool;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.criteriaattribute.CriteriaAttributeValidationContext;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeValidator;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;

@Component
public class TestToolManager {

    private CriteriaAttributeValidator criteriaAttributeValidator;
    private TestToolDAO testToolDAO;

    @Autowired
    public TestToolManager(TestToolDAO testToolDAO, CriteriaAttributeValidator criteriaAttributeValidator) {
        this.testToolDAO = testToolDAO;
        this.criteriaAttributeValidator = criteriaAttributeValidator;
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
        TestTool originalTestTool = testToolDAO.getById(testTool.getId());
        criteriaAttributeValidator.validateForEdit(CriteriaAttributeValidationContext.builder()
                .criteriaAttribe(testTool)
                .criteriaAttributeDAO(testToolDAO)
                .name("Test Tool")
                .build());
        testToolDAO.update(testTool);
        addNewCriteriaForExistingTestTool(testTool, originalTestTool);
        deleteCriteriaRemovedFromTestTool(testTool, originalTestTool);
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

    private List<CertificationCriterion> getCriteriaAddedToTestTool(TestTool updatedTestTool, TestTool originalTestTool) {
        return subtractLists(updatedTestTool.getCriteria(), originalTestTool.getCriteria());
    }

    private List<CertificationCriterion> getCriteriaRemovedFromTestTool(TestTool updatedTestTool, TestTool originalTestTool) {
        return  subtractLists(originalTestTool.getCriteria(), updatedTestTool.getCriteria());
    }

    private List<CertificationCriterion> subtractLists(List<CertificationCriterion> listA, List<CertificationCriterion> listB) {
        Predicate<CertificationCriterion> notInListB = certFromA -> !listB.stream()
                .anyMatch(cert -> certFromA.equals(cert));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private void addNewCriteriaForExistingTestTool(TestTool updatedTestTool, TestTool originalTestTool) {
        getCriteriaAddedToTestTool(updatedTestTool, originalTestTool).stream()
                .forEach(crit -> testToolDAO.addTestToolCriteriMap(updatedTestTool, crit));
    }

    private void deleteCriteriaRemovedFromTestTool(TestTool updatedTestTool, TestTool originalTestTool) {
        getCriteriaRemovedFromTestTool(updatedTestTool, originalTestTool).stream()
                .forEach(crit -> testToolDAO.removeTestToolCriteriaMap(updatedTestTool, crit));
    }

}
