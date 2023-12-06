package gov.healthit.chpl.standard;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.criteriaattribute.rule.RuleDAO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class StandardService {
    private StandardDAO standardDAO;

    @Autowired
    public StandardService(ErrorMessageUtil errorMessageUtil, RuleDAO ruleDAO, StandardDAO standardDAO) {
        this.standardDAO = standardDAO;
    }


    public void update(Standard standard) throws EntityRetrievalException {
        Standard originalStandard = standardDAO.getById(standard.getId());

        standardDAO.update(standard);
        addNewCriteriaForExistingStandard(standard, originalStandard);
        deleteCriteriaRemovedFromStandard(standard, originalStandard);
    }

    public Standard add(Standard standard) {
        Standard newStandard = standardDAO.add(standard);

        if (!CollectionUtils.isEmpty(standard.getCriteria())) {
            standard.getCriteria().stream()
                    .forEach(crit -> standardDAO.addStandardCriteriaMap(newStandard, crit));
        }

        return standardDAO.getById(standard.getId());
    }

    public void delete(Standard standard) throws EntityRetrievalException, ValidationException {
        standard.getCriteria()
                .forEach(crit -> standardDAO.removeStandardCriteriaMap(standard, crit));

        standardDAO.remove(standard);
    }

    private void addNewCriteriaForExistingStandard(Standard standard, Standard originalStandard) {
        getCriteriaAddedToStandard(standard, originalStandard).stream()
                .forEach(crit -> standardDAO.addStandardCriteriaMap(standard, crit));
    }

    private void deleteCriteriaRemovedFromStandard(Standard standard, Standard originalStandard) {
        getCriteriaRemovedFromStandard(standard, originalStandard).stream()
                .forEach(crit -> standardDAO.removeStandardCriteriaMap(standard, crit));
    }

    private List<CertificationCriterion> getCriteriaAddedToStandard(Standard updatedStandard, Standard originalStandard) {
        return subtractLists(updatedStandard.getCriteria(), originalStandard.getCriteria());
    }

    private List<CertificationCriterion> getCriteriaRemovedFromStandard(Standard updatedStandard, Standard originalStandard) {
        return  subtractLists(originalStandard.getCriteria(), updatedStandard.getCriteria());
    }

    private List<CertificationCriterion> subtractLists(List<CertificationCriterion> listA, List<CertificationCriterion> listB) {
        Predicate<CertificationCriterion> notInListB = certFromA -> !listB.stream()
                .anyMatch(cert -> certFromA.equals(cert));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

}
