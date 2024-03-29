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

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.criteriaattribute.rule.RuleDAO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class CriteriaAttributeValidator {
    private static final int MAX_LISTINGS_IN_DELETE_ERROR_MESSAGE = 25;

    private ErrorMessageUtil errorMessageUtil;
    private RuleDAO ruleDAO;

    @Autowired
    public CriteriaAttributeValidator(ErrorMessageUtil errorMessageUtil, RuleDAO ruleDAO) {
        this.errorMessageUtil = errorMessageUtil;
        this.ruleDAO = ruleDAO;
    }

    public void validateForEdit(CriteriaAttributeValidationContext context) throws ValidationException, EntityRetrievalException {
        Set<String> messages = new HashSet<String>();

        if (StringUtils.isEmpty(context.getCriteriaAttribute().getValue())) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.emptyValue"));
        }

        if (context.getIsRegulatoryTextCitationRequired()
                && StringUtils.isEmpty(context.getCriteriaAttribute().getRegulatoryTextCitation())) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.emptyRegulatoryTextCitation"));
        }

        if (context.getStartDayRequired()
                && context.getCriteriaAttribute().getStartDay() == null) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.emptyStartDay"));
        }

        if (context.getEndDayRequired()
                && context.getCriteriaAttribute().getEndDay() == null) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.emptyEndDay"));
        }

        if (context.getRequiredDayRequired()
                && context.getCriteriaAttribute().getRequiredDay() == null) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.emptyRequiredDay"));
        }

        if (CollectionUtils.isEmpty(context.getCriteriaAttribute().getCriteria())) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.noCriteria"));
        } else {
            if (isCriteriaAttributeDuplicateOnEdit(context)) {
                messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.duplicate", context.getName()));
            }
            messages.addAll(validateCriteriaRemovedFromCriteriaAttribute(context));
        }


        if (context.getRuleRequired()
                && context.getCriteriaAttribute().getRule() == null) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.emptyRule"));
        }

        if (context.getCriteriaAttribute().getRule() != null
                && ruleDAO.getRuleEntityById(context.getCriteriaAttribute().getRule().getId()) == null) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.notFoundRule"));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    public void validateForAdd(CriteriaAttributeValidationContext context) throws ValidationException, EntityRetrievalException {
        Set<String> messages = new HashSet<String>();

        if (StringUtils.isEmpty(context.getCriteriaAttribute().getValue())) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.emptyValue"));
        }

        if (context.getIsRegulatoryTextCitationRequired()
                && StringUtils.isEmpty(context.getCriteriaAttribute().getRegulatoryTextCitation())) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.emptyRegulatoryTextCitation"));
        }

        if (context.getStartDayRequired()
                && context.getCriteriaAttribute().getStartDay() == null) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.emptyStartDay"));
        }

        if (context.getEndDayRequired()
                && context.getCriteriaAttribute().getEndDay() == null) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.emptyEndDay"));
        }

        if (context.getRequiredDayRequired()
                && context.getCriteriaAttribute().getRequiredDay() == null) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.emptyRequiredDay"));
        }

        if (CollectionUtils.isEmpty(context.getCriteriaAttribute().getCriteria())) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.noCriteria"));
        }

        if (isCriteriaAttributeDuplicateOnAdd(context)) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.duplicate", context.getName()));
        }

        if (context.getRuleRequired()
                && context.getCriteriaAttribute().getRule() == null) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.emptyRule"));
        }

        if (context.getCriteriaAttribute().getRule() != null
                && ruleDAO.getRuleEntityById(context.getCriteriaAttribute().getRule().getId()) == null) {
            messages.add(errorMessageUtil.getMessage("criteriaAttribute.edit.notFoundRule"));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }

    }

    public void validateForDelete(CriteriaAttributeValidationContext context) throws ValidationException {
        List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
        try {
            listings = context.getCriteriaAttributeDAO().getCertifiedProductsByCriteriaAttribute(context.getCriteriaAttribute());
        } catch (EntityRetrievalException ex) {
            throw new ValidationException(ex.getMessage());
        }

        if (!CollectionUtils.isEmpty(listings)) {
            String message = errorMessageUtil.getMessage("criteriaAttribute.delete.listingsExist",
                    context.getName(),
                    listings.size(),
                    listings.size() > 1 ? "s" : "");
            if (listings.size() < MAX_LISTINGS_IN_DELETE_ERROR_MESSAGE) {
                message = message + ": "
                        + listings.stream()
                            .map(listing -> listing.getChplProductNumber())
                            .collect(Collectors.joining(", "));
            }

            ValidationException e = new ValidationException(message);
            throw e;
        }
    }

    private Set<String> validateCriteriaRemovedFromCriteriaAttribute(CriteriaAttributeValidationContext context) {
        Set<String> messages = new HashSet<String>();
        CriteriaAttribute origCriteriaAttribute = context.getCriteriaAttributeDAO().getCriteriaAttributeById(context.getCriteriaAttribute().getId());

        getCriteriaRemovedFromCriteriaAttribute(context.getCriteriaAttribute(), origCriteriaAttribute).stream()
                .forEach(crit -> {
                    List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
                    try {
                        listings = context.getCriteriaAttributeDAO().getCertifiedProductsByCriteriaAttributeAndCriteria(origCriteriaAttribute, crit);
                    } catch (EntityRetrievalException ex) {
                        messages.add(ex.getMessage());
                    }

                    if (!CollectionUtils.isEmpty(listings)) {
                        String message = errorMessageUtil.getMessage("criteriaAttribute.edit.deletedCriteria.listingsExist",
                                context.getName(),
                                CertificationCriterionService.formatCriteriaNumber(crit),
                                listings.size(),
                                listings.size() > 1 ? "s" : "");
                        if (listings.size() < MAX_LISTINGS_IN_DELETE_ERROR_MESSAGE) {
                                message = message + ": "
                                        + listings.stream()
                                        .map(listing -> listing.getChplProductNumber())
                                        .collect(Collectors.joining(", "));
                        }
                        messages.add(message);
                    }
                });
        return messages;
    }

    private boolean isCriteriaAttributeDuplicateOnEdit(CriteriaAttributeValidationContext context) throws EntityRetrievalException {
        String updatedCitationText = context.getCriteriaAttribute().getRegulatoryTextCitation() != null ? context.getCriteriaAttribute().getRegulatoryTextCitation() : "";

        return context.getCriteriaAttributeDAO().getAllAssociatedCriteriaMaps().stream()
                .filter(map -> {
                        String origCitationText = map.getCriteriaAttribute().getRegulatoryTextCitation() != null ? map.getCriteriaAttribute().getRegulatoryTextCitation() : "";

                        return map.getCriteriaAttribute().getValue().equalsIgnoreCase(context.getCriteriaAttribute().getValue())
                                && origCitationText.equalsIgnoreCase(updatedCitationText)
                                && !map.getCriteriaAttribute().getId().equals(context.getCriteriaAttribute().getId());
                })
                .findAny()
                .isPresent();
    }

    private boolean isCriteriaAttributeDuplicateOnAdd(CriteriaAttributeValidationContext context) throws EntityRetrievalException {
        String updatedCitationText = context.getCriteriaAttribute().getRegulatoryTextCitation() != null ? context.getCriteriaAttribute().getRegulatoryTextCitation() : "";

        return context.getCriteriaAttributeDAO().getAllAssociatedCriteriaMaps().stream()
                .filter(map -> {
                        String origCitationText = map.getCriteriaAttribute().getRegulatoryTextCitation() != null ? map.getCriteriaAttribute().getRegulatoryTextCitation() : "";

                        return map.getCriteriaAttribute().getValue().equalsIgnoreCase(context.getCriteriaAttribute().getValue())
                                && origCitationText.equalsIgnoreCase(updatedCitationText);
                })
                .findAny()
                .isPresent();
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
