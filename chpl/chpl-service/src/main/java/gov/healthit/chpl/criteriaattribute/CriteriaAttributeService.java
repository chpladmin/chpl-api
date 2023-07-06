package gov.healthit.chpl.criteriaattribute;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Component
public class CriteriaAttributeService {

    public void updateCriteriaAttribute(CriteriaAttributeSaveContext context) throws EntityRetrievalException{
        CriteriaAttribute originalCriteriaAttribute = context.getCriteriaAttributeDAO().getCriteriaAttributeById(context.getCriteriaAttribute().getId());

        context.getCriteriaAttributeDAO().update(context.getCriteriaAttribute());
        addNewCriteriaForExistingCriteriaAttribute(context, originalCriteriaAttribute);
        deleteCriteriaRemovedFromCriteriaAttribute(context, originalCriteriaAttribute);
    }

    private void addNewCriteriaForExistingCriteriaAttribute(CriteriaAttributeSaveContext context, CriteriaAttribute originalCriteriaAttribute) {
        getCriteriaAddedToCriteriaAttribute(context.getCriteriaAttribute(), originalCriteriaAttribute).stream()
                .forEach(crit -> context.getCriteriaAttributeDAO().addCriteriaAttributeCriteriaMap(context.getCriteriaAttribute(), crit));
    }

    private void deleteCriteriaRemovedFromCriteriaAttribute(CriteriaAttributeSaveContext context, CriteriaAttribute originalCriteriaAttribute) {
        getCriteriaRemovedFromCriteriaAttribute(context.getCriteriaAttribute(), originalCriteriaAttribute).stream()
                .forEach(crit -> context.getCriteriaAttributeDAO().removeTestToolCriteriaMap(context.getCriteriaAttribute(), crit));
    }

    private List<CertificationCriterion> getCriteriaAddedToCriteriaAttribute(CriteriaAttribute updatedCriteriaAttribute, CriteriaAttribute originalCriteriaAttribute) {
        return subtractLists(updatedCriteriaAttribute.getCriteria(), originalCriteriaAttribute.getCriteria());
    }

    private List<CertificationCriterion> getCriteriaRemovedFromCriteriaAttribute(CriteriaAttribute updatedCriteriaAttribute, CriteriaAttribute originalCriteriaAttribute) {
        return  subtractLists(originalCriteriaAttribute.getCriteria(), updatedCriteriaAttribute.getCriteria());
    }

    private List<CertificationCriterion> subtractLists(List<CertificationCriterion> listA, List<CertificationCriterion> listB) {
        Predicate<CertificationCriterion> notInListB = certFromA -> !listB.stream()
                .anyMatch(cert -> certFromA.equals(cert));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

}
