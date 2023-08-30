package gov.healthit.chpl.testtool;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.criteriaattribute.CriteriaAttributeService;
import gov.healthit.chpl.dao.CertificationCriterionAttributeDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class TestToolManager {

    private TestToolValidator testToolValidator;
    private CriteriaAttributeService criteriaAttributeService;
    private TestToolDAO testToolDAO;
    private CertificationCriterionAttributeDAO certificationCriterionAttributeDAO;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestToolManager(TestToolDAO testToolDAO, TestToolValidator testToolValidator,
            CriteriaAttributeService criteriaAttributeService, CertificationCriterionAttributeDAO certificationCriterionAttributeDAO,
            ErrorMessageUtil errorMessageUtil) {

        this.testToolDAO = testToolDAO;
        this.testToolValidator = testToolValidator;
        this.criteriaAttributeService = criteriaAttributeService;
        this.certificationCriterionAttributeDAO = certificationCriterionAttributeDAO;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Transactional
    public List<TestTool> getAll() {
        return testToolDAO.getAll();
    }

    @Transactional
    public List<CertificationCriterion> getCertificationCriteriaForTestTools() {
        return certificationCriterionAttributeDAO.getCriteriaForTestTools();
    }


    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).TEST_TOOL, "
            + "T(gov.healthit.chpl.permissions.domains.TestToolDomainPermissions).UPDATE)")
    @Transactional
    @ListingStoreRemove(removeBy = RemoveBy.ALL)
    public TestTool update(TestTool testTool) throws EntityRetrievalException, ValidationException {
        testToolValidator.validateForEdit(testTool);
        updateTestTool(testTool);
        return testToolDAO.getById(testTool.getId());
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).TEST_TOOL, "
            + "T(gov.healthit.chpl.permissions.domains.TestToolDomainPermissions).CREATE)")
    @Transactional
    public TestTool create(TestTool testTool) throws EntityRetrievalException, ValidationException {
        testToolValidator.validateForAdd(testTool);
        return addTestTool(testTool);
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
        testToolValidator.validateForDelete(testTool);
        deleteTestTool(testTool);
    }

    private void updateTestTool(TestTool updatedTestTool) throws EntityRetrievalException {
        TestTool originalTestTool = testToolDAO.getById(updatedTestTool.getId());

        testToolDAO.update(updatedTestTool);
        addNewCriteriaForExistingTestTool(updatedTestTool, originalTestTool);
        deleteCriteriaRemovedFromTestTool(updatedTestTool, originalTestTool);
    }

    private TestTool addTestTool(TestTool newTestTool) {
        TestTool testTool = testToolDAO.add(newTestTool);

        if (!CollectionUtils.isEmpty(newTestTool.getCriteria())) {
            newTestTool.getCriteria().stream()
                    .forEach(crit -> testToolDAO.addTestToolCriteriaMap(testTool, crit));
        }

        return testToolDAO.getById(testTool.getId());
    }

    private void deleteTestTool(TestTool testTool) throws EntityRetrievalException, ValidationException {
        testTool.getCriteria()
                .forEach(crit -> testToolDAO.removeCriteriaAttributeCriteriaMap(testTool, crit));

        testToolDAO.remove(testTool);
    }

    private void addNewCriteriaForExistingTestTool(TestTool updatedTestTool, TestTool originalTestTool) {
        getCriteriaAddedToTestTool(updatedTestTool, originalTestTool).stream()
                .forEach(crit -> testToolDAO.addTestToolCriteriaMap(updatedTestTool, crit));
    }

    private void deleteCriteriaRemovedFromTestTool(TestTool updatedTestTool, TestTool originalTestTool) {
        getCriteriaRemovedFromTestTool(updatedTestTool, originalTestTool).stream()
                .forEach(crit -> testToolDAO.removeCriteriaAttributeCriteriaMap(updatedTestTool, crit));
    }

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

}
