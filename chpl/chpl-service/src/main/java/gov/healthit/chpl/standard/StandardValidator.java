package gov.healthit.chpl.standard;

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
public class StandardValidator {
    private static final int MAX_LISTINGS_IN_DELETE_ERROR_MESSAGE = 25;

    private ErrorMessageUtil errorMessageUtil;
    private RuleDAO ruleDAO;
    private StandardDAO standardDAO;

    @Autowired
    public StandardValidator(ErrorMessageUtil errorMessageUtil, RuleDAO ruleDAO, StandardDAO standardDAO) {
        this.errorMessageUtil = errorMessageUtil;
        this.ruleDAO = ruleDAO;
        this.standardDAO = standardDAO;
    }

    public void validateForEdit(Standard standard) throws ValidationException, EntityRetrievalException {
        Set<String> messages = new HashSet<String>();

        if (StringUtils.isEmpty(standard.getValue())) {
            messages.add(errorMessageUtil.getMessage("standard.edit.emptyValue"));
        }

        if (StringUtils.isEmpty(standard.getRegulatoryTextCitation())) {
            messages.add(errorMessageUtil.getMessage("standard.edit.emptyRegulatoryTextCitation"));
        }


        if (CollectionUtils.isEmpty(standard.getCriteria())) {
            messages.add(errorMessageUtil.getMessage("standard.edit.noCriteria"));
        } else {
            if (isStandardDuplicateOnEdit(standard)) {
                messages.add(errorMessageUtil.getMessage("standard.edit.duplicate"));
            }
            messages.addAll(validateCriteriaRemovedFromStandard(standard));
        }


        if (standard.getRule() != null
                && ruleDAO.getRuleEntityById(standard.getRule().getId()) == null) {
            messages.add(errorMessageUtil.getMessage("standard.edit.notFoundRule"));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    public void validateForAdd(Standard standard) throws ValidationException, EntityRetrievalException {
        Set<String> messages = new HashSet<String>();

        if (StringUtils.isEmpty(standard.getValue())) {
            messages.add(errorMessageUtil.getMessage("standard.edit.emptyValue"));
        }

        if (StringUtils.isEmpty(standard.getRegulatoryTextCitation())) {
            messages.add(errorMessageUtil.getMessage("standard.edit.emptyRegulatoryTextCitation"));
        }

        if (CollectionUtils.isEmpty(standard.getCriteria())) {
            messages.add(errorMessageUtil.getMessage("standard.edit.noCriteria"));
        }

        if (isStandardDuplicateOnAdd(standard)) {
            messages.add(errorMessageUtil.getMessage("standard.edit.duplicate"));
        }

        if (standard.getRule() != null
                && ruleDAO.getRuleEntityById(standard.getRule().getId()) == null) {
            messages.add(errorMessageUtil.getMessage("standard.edit.notFoundRule"));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    public void validateForDelete(Standard standard) throws ValidationException {
        List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
        try {
            listings = standardDAO.getCertifiedProductsByStandard(standard);
        } catch (EntityRetrievalException ex) {
            throw new ValidationException(ex.getMessage());
        }

        if (!CollectionUtils.isEmpty(listings)) {
            String message = errorMessageUtil.getMessage("standard.delete.listingsExist",
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

    private Set<String> validateCriteriaRemovedFromStandard(Standard standard) {
        Set<String> messages = new HashSet<String>();
        Standard origStandard = standardDAO.getById(standard.getId());

        getCriteriaRemovedFromStandard(standard, origStandard).stream()
                .forEach(crit -> {
                    List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
                    try {
                        listings = standardDAO.getCertifiedProductsByStandardAndCriteria(origStandard, crit);
                    } catch (EntityRetrievalException ex) {
                        messages.add(ex.getMessage());
                    }

                    if (!CollectionUtils.isEmpty(listings)) {
                        String message = errorMessageUtil.getMessage("standard.edit.deletedCriteria.listingsExist",
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

    private boolean isStandardDuplicateOnEdit(Standard standard) throws EntityRetrievalException {
        String updatedCitationText = standard.getRegulatoryTextCitation() != null ? standard.getRegulatoryTextCitation() : "";

        return standardDAO.getAllStandardCriteriaMap().stream()
                .filter(map -> {
                        String origCitationText = map.getStandard().getRegulatoryTextCitation() != null ? map.getStandard().getRegulatoryTextCitation() : "";

                        return map.getStandard().getValue().equalsIgnoreCase(standard.getValue())
                                && origCitationText.equalsIgnoreCase(updatedCitationText)
                                && !map.getStandard().getId().equals(standard.getId());
                })
                .findAny()
                .isPresent();
    }

    private boolean isStandardDuplicateOnAdd(Standard standard) throws EntityRetrievalException {
        String updatedCitationText = standard.getRegulatoryTextCitation() != null ? standard.getRegulatoryTextCitation() : "";

        return standardDAO.getAllStandardCriteriaMap().stream()
                .filter(map -> {
                        String origCitationText = map.getStandard().getRegulatoryTextCitation() != null ? map.getStandard().getRegulatoryTextCitation() : "";

                        return map.getStandard().getValue().equalsIgnoreCase(standard.getValue())
                                && origCitationText.equalsIgnoreCase(updatedCitationText);
                })
                .findAny()
                .isPresent();
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
