package gov.healthit.chpl.criteriaattribute;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class CriteriaAttributeValidator {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public CriteriaAttributeValidator(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void validateForEdit(CriteriaAttributeValidationContext context) throws ValidationException, EntityRetrievalException {
        Set<String> messages = new HashSet<String>();

        if (StringUtils.isEmpty(context.getCriteriaAttribe().getValue())) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.emptyValue"));
        }

        if (StringUtils.isEmpty(context.getCriteriaAttribe().getRegulatoryTextCitation())) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.emtptyRegulatoryTextCitation"));
        }

        if (CollectionUtils.isEmpty(context.getCriteriaAttribe().getCriteria())) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.noCriteria"));
        }

        if (isCriteriaAttributeDuplicateOnEdit(context)) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.duplicate", context.getName()));
        }

        messages.addAll(validateCriteriaRemovedFromCriteriaAttribute(context));

        //TODO OCD-4242 - Need to validate Rule

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    private Set<String> validateCriteriaRemovedFromCriteriaAttribute(CriteriaAttributeValidationContext context) {
        Set<String> messages = new HashSet<String>();
        CriteriaAttribute origCriteriaAttribute = context.getCriteriaAttributeDAO().getCriteriaAttributeById(context.getCriteriaAttribe().getId());

        getCriteriaRemovedFromCriteriaAttribute(context.getCriteriaAttribe(), origCriteriaAttribute).stream()
                .forEach(crit -> {
                    List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
                    try {
                        listings = context.getCriteriaAttributeDAO().getCertifiedProductsByCriteriaAttributeAndCriteria(origCriteriaAttribute, crit);
                    } catch (EntityRetrievalException ex) {
                        messages.add(ex.getMessage());
                    }

                    if (!CollectionUtils.isEmpty(listings)) {
                        messages.add(errorMessageUtil.getMessage("testTool.edit.deletedCriteria.listingsExist",
                                CertificationCriterionService.formatCriteriaNumber(crit),
                                listings.size(),
                                listings.size() > 1 ? "s" : "",
                                listings.stream()
                                        .map(listing -> listing.getChplProductNumber())
                                        .collect(Collectors.joining(", "))));
                    }
                });
        return messages;
    }

    private boolean isCriteriaAttributeDuplicateOnEdit(CriteriaAttributeValidationContext context) throws EntityRetrievalException {
        return context.getCriteriaAttributeDAO().getAllAssociatedCriteriaMaps().stream()
                .filter(map -> map.getCriteriaAttribute().getValue().equalsIgnoreCase(context.getCriteriaAttribe().getValue())
                        && map.getCriteriaAttribute().getRegulatoryTextCitation().equalsIgnoreCase(context.getCriteriaAttribe().getRegulatoryTextCitation())
                        && !map.getCriteriaAttribute().getId().equals(context.getCriteriaAttribe().getId()))
                .findAny()
                .isPresent();
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
