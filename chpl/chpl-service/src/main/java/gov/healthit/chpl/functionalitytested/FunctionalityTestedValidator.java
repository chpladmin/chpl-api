package gov.healthit.chpl.functionalitytested;

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
public class FunctionalityTestedValidator {
    private static final int MAX_LISTINGS_IN_DELETE_ERROR_MESSAGE = 25;

    private ErrorMessageUtil errorMessageUtil;
    private RuleDAO ruleDAO;
    private FunctionalityTestedDAO functionalityTestedDAO;

    @Autowired
    public FunctionalityTestedValidator(ErrorMessageUtil errorMessageUtil, RuleDAO ruleDAO, FunctionalityTestedDAO functionalityTestedDAO) {
        this.errorMessageUtil = errorMessageUtil;
        this.ruleDAO = ruleDAO;
        this.functionalityTestedDAO = functionalityTestedDAO;
    }

    public void validateForEdit(FunctionalityTested functionalityTested) throws ValidationException, EntityRetrievalException {
        Set<String> messages = new HashSet<String>();

        if (StringUtils.isEmpty(functionalityTested.getValue())) {
            messages.add(errorMessageUtil.getMessage("functionalityTested.edit.emptyValue"));
        }

        if (StringUtils.isEmpty(functionalityTested.getRegulatoryTextCitation())) {
            messages.add(errorMessageUtil.getMessage("functionalityTested.edit.emptyRegulatoryTextCitation"));
        }


        if (CollectionUtils.isEmpty(functionalityTested.getCriteria())) {
            messages.add(errorMessageUtil.getMessage("functionalityTested.edit.noCriteria"));
        } else {
            if (isFunctionalityTestedDuplicateOnEdit(functionalityTested)) {
                messages.add(errorMessageUtil.getMessage("functionalityTested.edit.duplicate"));
            }
            messages.addAll(validateCriteriaRemovedFromFunctionalityTested(functionalityTested));
        }


        if (functionalityTested.getRule() != null
                && ruleDAO.getRuleEntityById(functionalityTested.getRule().getId()) == null) {
            messages.add(errorMessageUtil.getMessage("functionalityTested.edit.notFoundRule"));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    public void validateForAdd(FunctionalityTested functionalityTested) throws ValidationException, EntityRetrievalException {
        Set<String> messages = new HashSet<String>();

        if (StringUtils.isEmpty(functionalityTested.getValue())) {
            messages.add(errorMessageUtil.getMessage("functionalityTested.edit.emptyValue"));
        }

        if (StringUtils.isEmpty(functionalityTested.getRegulatoryTextCitation())) {
            messages.add(errorMessageUtil.getMessage("functionalityTested.edit.emptyRegulatoryTextCitation"));
        }

        if (CollectionUtils.isEmpty(functionalityTested.getCriteria())) {
            messages.add(errorMessageUtil.getMessage("functionalityTested.edit.noCriteria"));
        }

        if (isFunctionalityTestedDuplicateOnAdd(functionalityTested)) {
            messages.add(errorMessageUtil.getMessage("functionalityTested.edit.duplicate"));
        }

        if (functionalityTested.getRule() != null
                && ruleDAO.getRuleEntityById(functionalityTested.getRule().getId()) == null) {
            messages.add(errorMessageUtil.getMessage("functionalityTested.edit.notFoundRule"));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    public void validateForDelete(FunctionalityTested functionalityTested) throws ValidationException {
        List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
        try {
            listings = functionalityTestedDAO.getCertifiedProductsByFunctionalityTested(functionalityTested);
        } catch (EntityRetrievalException ex) {
            throw new ValidationException(ex.getMessage());
        }

        if (!CollectionUtils.isEmpty(listings)) {
            String message = errorMessageUtil.getMessage("functionalityTested.delete.listingsExist",
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

    private Set<String> validateCriteriaRemovedFromFunctionalityTested(FunctionalityTested functionalityTested) {
        Set<String> messages = new HashSet<String>();
        FunctionalityTested origFunctionalityTested = functionalityTestedDAO.getById(functionalityTested.getId());

        getCriteriaRemovedFromFunctionalityTested(functionalityTested, origFunctionalityTested).stream()
                .forEach(crit -> {
                    List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
                    try {
                        listings = functionalityTestedDAO.getCertifiedProductsByFunctionalityTestedAndCriteria(origFunctionalityTested, crit);
                    } catch (EntityRetrievalException ex) {
                        messages.add(ex.getMessage());
                    }

                    if (!CollectionUtils.isEmpty(listings)) {
                        String message = errorMessageUtil.getMessage("functionalityTested.edit.deletedCriteria.listingsExist",
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

    private boolean isFunctionalityTestedDuplicateOnEdit(FunctionalityTested functionalityTested) throws EntityRetrievalException {
        String updatedCitationText = functionalityTested.getRegulatoryTextCitation() != null ? functionalityTested.getRegulatoryTextCitation() : "";

        return functionalityTestedDAO.getAllFunctionalityTestedCriteriaMap().stream()
                .filter(map -> {
                        String origCitationText = map.getFunctionalityTested().getRegulatoryTextCitation() != null ? map.getFunctionalityTested().getRegulatoryTextCitation() : "";

                        return map.getFunctionalityTested().getValue().equalsIgnoreCase(functionalityTested.getValue())
                                && origCitationText.equalsIgnoreCase(updatedCitationText)
                                && !map.getFunctionalityTested().getId().equals(functionalityTested.getId());
                })
                .findAny()
                .isPresent();
    }

    private boolean isFunctionalityTestedDuplicateOnAdd(FunctionalityTested functionalityTested) throws EntityRetrievalException {
        String updatedCitationText = functionalityTested.getRegulatoryTextCitation() != null ? functionalityTested.getRegulatoryTextCitation() : "";

        return functionalityTestedDAO.getAllFunctionalityTestedCriteriaMap().stream()
                .filter(map -> {
                        String origCitationText = map.getFunctionalityTested().getRegulatoryTextCitation() != null ? map.getFunctionalityTested().getRegulatoryTextCitation() : "";

                        return map.getFunctionalityTested().getValue().equalsIgnoreCase(functionalityTested.getValue())
                                && origCitationText.equalsIgnoreCase(updatedCitationText);
                })
                .findAny()
                .isPresent();
    }

    private List<CertificationCriterion> getCriteriaRemovedFromFunctionalityTested(FunctionalityTested updatedFunctionalityTested, FunctionalityTested originalFunctionalityTested) {
        return  subtractLists(originalFunctionalityTested.getCriteria(), updatedFunctionalityTested.getCriteria());
    }

    private List<CertificationCriterion> subtractLists(List<CertificationCriterion> listA, List<CertificationCriterion> listB) {
        Predicate<CertificationCriterion> notInListB = certFromA -> !listB.stream()
                .anyMatch(cert -> certFromA.equals(cert));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

}
