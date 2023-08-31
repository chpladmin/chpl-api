package gov.healthit.chpl.functionalitytested;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.criteriaattribute.rule.RuleDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class FunctionalityTestedService {

    private FunctionalityTestedDAO functionalityTestedDAO;

    @Autowired
    public FunctionalityTestedService(ErrorMessageUtil errorMessageUtil, RuleDAO ruleDAO, FunctionalityTestedDAO functionalityTestedDAO) {
        this.functionalityTestedDAO = functionalityTestedDAO;
    }


    public void update(FunctionalityTested functionalityTested) throws EntityRetrievalException {
        FunctionalityTested originalFunctionalityTested = functionalityTestedDAO.getById(functionalityTested.getId());

        functionalityTestedDAO.update(functionalityTested);
        addNewCriteriaForExistingFunctionalityTested(functionalityTested, originalFunctionalityTested);
        deleteCriteriaRemovedFromFunctionalityTested(functionalityTested, originalFunctionalityTested);
    }

    public FunctionalityTested add(FunctionalityTested functionalityTested) {
        FunctionalityTested newfunctionalityTested = functionalityTestedDAO.add(functionalityTested);

        if (!CollectionUtils.isEmpty(functionalityTested.getCriteria())) {
            functionalityTested.getCriteria().stream()
                    .forEach(crit -> functionalityTestedDAO.addFunctionalityTestedCriteriaMap(newfunctionalityTested, crit));
        }

        return functionalityTestedDAO.getById(functionalityTested.getId());
    }

    public void delete(FunctionalityTested functionalityTested) throws EntityRetrievalException, ValidationException {
        functionalityTested.getCriteria()
                .forEach(crit -> functionalityTestedDAO.removeFunctionalityTestedCriteriaMap(functionalityTested, crit));

        functionalityTestedDAO.remove(functionalityTested);
    }

    private void addNewCriteriaForExistingFunctionalityTested(FunctionalityTested functionalityTested, FunctionalityTested originalFunctionalityTested) {
        getCriteriaAddedToFunctionalityTested(functionalityTested, originalFunctionalityTested).stream()
                .forEach(crit -> functionalityTestedDAO.addFunctionalityTestedCriteriaMap(functionalityTested, crit));
    }

    private void deleteCriteriaRemovedFromFunctionalityTested(FunctionalityTested functionalityTested, FunctionalityTested originalFunctionalityTested) {
        getCriteriaRemovedFromFunctionalityTested(functionalityTested, originalFunctionalityTested).stream()
                .forEach(crit -> functionalityTestedDAO.removeFunctionalityTestedCriteriaMap(functionalityTested, crit));
    }

    private List<CertificationCriterion> getCriteriaAddedToFunctionalityTested(FunctionalityTested updatedFunctionalityTested, FunctionalityTested originalFunctionalityTested) {
        return subtractLists(updatedFunctionalityTested.getCriteria(), originalFunctionalityTested.getCriteria());
    }

    private List<CertificationCriterion> getCriteriaRemovedFromFunctionalityTested(FunctionalityTested updatedfunctionalityTested, FunctionalityTested originalFunctionalityTested) {
        return  subtractLists(originalFunctionalityTested.getCriteria(), updatedfunctionalityTested.getCriteria());
    }

    private List<CertificationCriterion> subtractLists(List<CertificationCriterion> listA, List<CertificationCriterion> listB) {
        Predicate<CertificationCriterion> notInListB = certFromA -> !listB.stream()
                .anyMatch(cert -> certFromA.equals(cert));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

}
