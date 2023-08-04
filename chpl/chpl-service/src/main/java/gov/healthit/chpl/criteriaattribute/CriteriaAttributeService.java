package gov.healthit.chpl.criteriaattribute;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;

@Component
public class CriteriaAttributeService {

    public void update(CriteriaAttributeSaveContext context) throws EntityRetrievalException {
        CriteriaAttribute originalCriteriaAttribute = context.getCriteriaAttributeDAO().getCriteriaAttributeById(context.getCriteriaAttribute().getId());

        context.getCriteriaAttributeDAO().update(context.getCriteriaAttribute());
        addNewCriteriaForExistingCriteriaAttribute(context, originalCriteriaAttribute);
        deleteCriteriaRemovedFromCriteriaAttribute(context, originalCriteriaAttribute);
    }

    public CriteriaAttribute add(CriteriaAttributeSaveContext context) {
        CriteriaAttribute criteriaAttribute = context.getCriteriaAttributeDAO().add(context.getCriteriaAttribute());

        if (!CollectionUtils.isEmpty(context.getCriteriaAttribute().getCriteria())) {
            context.getCriteriaAttribute().getCriteria().stream()
                    .forEach(crit -> context.getCriteriaAttributeDAO().addCriteriaAttributeCriteriaMap(criteriaAttribute, crit));
        }

        return context.getCriteriaAttributeDAO().getCriteriaAttributeById(criteriaAttribute.getId());
    }

    public void delete(CriteriaAttributeSaveContext context) throws EntityRetrievalException, ValidationException {
        context.getCriteriaAttribute().getCriteria()
                .forEach(crit -> context.getCriteriaAttributeDAO().removeCriteriaAttributeCriteriaMap(context.getCriteriaAttribute(), crit));

        context.getCriteriaAttributeDAO().remove(context.getCriteriaAttribute());
    }

    private void addNewCriteriaForExistingCriteriaAttribute(CriteriaAttributeSaveContext context, CriteriaAttribute originalCriteriaAttribute) {
        getCriteriaAddedToCriteriaAttribute(context.getCriteriaAttribute(), originalCriteriaAttribute).stream()
                .forEach(crit -> context.getCriteriaAttributeDAO().addCriteriaAttributeCriteriaMap(context.getCriteriaAttribute(), crit));
    }

    private void deleteCriteriaRemovedFromCriteriaAttribute(CriteriaAttributeSaveContext context, CriteriaAttribute originalCriteriaAttribute) {
        getCriteriaRemovedFromCriteriaAttribute(context.getCriteriaAttribute(), originalCriteriaAttribute).stream()
                .forEach(crit -> context.getCriteriaAttributeDAO().removeCriteriaAttributeCriteriaMap(context.getCriteriaAttribute(), crit));
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
