package gov.healthit.chpl.codeset;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class CodeSetValidator {
    private static final int MAX_LISTINGS_IN_DELETE_ERROR_MESSAGE = 25;

    private ErrorMessageUtil errorMessageUtil;
    private CodeSetDAO codeSetDAO;

    @Autowired
    public CodeSetValidator(ErrorMessageUtil errorMessageUtil, CodeSetDAO codeSetDAO) {
        this.errorMessageUtil = errorMessageUtil;
        this.codeSetDAO = codeSetDAO;
    }

    public void validateForEdit(CodeSet codeSet) throws ValidationException, EntityRetrievalException {
        Set<String> messages = new HashSet<String>();

        if (codeSet.getRequiredDay() == null) {
            messages.add(errorMessageUtil.getMessage("codeSet.edit.emptyRequiredDay"));
        }

        if (CollectionUtils.isEmpty(codeSet.getCriteria())) {
            messages.add(errorMessageUtil.getMessage("codeSet.edit.noCriteria"));
        } else {
            if (isCodeSetDuplicateOnEdit(codeSet)) {
                messages.add(errorMessageUtil.getMessage("codeSet.edit.duplicate", "Code Set"));
            }
            messages.addAll(validateCriteriaRemovedFromCodeSet(codeSet));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    public void validateForAdd(CodeSet codeSet) throws ValidationException, EntityRetrievalException {
        Set<String> messages = new HashSet<String>();

        if (codeSet.getRequiredDay() == null) {
            messages.add(errorMessageUtil.getMessage("codeSet.edit.emptyRequiredDay"));
        }

        if (CollectionUtils.isEmpty(codeSet.getCriteria())) {
            messages.add(errorMessageUtil.getMessage("codeSet.edit.noCriteria"));
        }

        if (isCodeSetdDuplicateOnAdd(codeSet)) {
            messages.add(errorMessageUtil.getMessage("codeSet.edit.duplicate", "Code Set"));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    public void validateForDelete(CodeSet codeSet) throws ValidationException {
        List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
        try {
            listings = codeSetDAO.getCertifiedProductsByCodeSet(codeSet);
        } catch (EntityRetrievalException ex) {
            throw new ValidationException(ex.getMessage());
        }

        if (!CollectionUtils.isEmpty(listings)) {
            String message = errorMessageUtil.getMessage("codeSet.delete.listingsExist",
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

    private Set<String> validateCriteriaRemovedFromCodeSet(CodeSet codeSet) {
        Set<String> messages = new HashSet<String>();
        CodeSet origCodeSet = codeSetDAO.getById(codeSet.getId());

        getCriteriaRemovedFromCodeSet(codeSet, origCodeSet).stream()
                .forEach(crit -> {
                    List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
                    try {
                        listings = codeSetDAO.getCertifiedProductsByCodeSetAndCriteria(origCodeSet, crit);
                    } catch (EntityRetrievalException ex) {
                        messages.add(ex.getMessage());
                    }

                    if (!CollectionUtils.isEmpty(listings)) {
                        String message = errorMessageUtil.getMessage("codeSet.edit.deletedCriteria.listingsExist",
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

    private boolean isCodeSetDuplicateOnEdit(CodeSet codeSet) throws EntityRetrievalException {
        LocalDate updatedRequiredDay = codeSet.getRequiredDay() != null ? codeSet.getRequiredDay() : LocalDate.MAX;

        return codeSetDAO.getAllCodeSetCriteriaMap().stream()
                .filter(map -> {
                        LocalDate origRequiredDay = map.getCodeSet().getRequiredDay() != null ? map.getCodeSet().getRequiredDay() : LocalDate.MAX;

                        return map.getCodeSet().getRequiredDay().equals(codeSet.getRequiredDay())
                                && origRequiredDay.equals(updatedRequiredDay)
                                && !map.getCodeSet().getId().equals(codeSet.getId());
                })
                .findAny()
                .isPresent();
    }

    private boolean isCodeSetdDuplicateOnAdd(CodeSet codeSet) throws EntityRetrievalException {
        LocalDate updatedRequiredDay = codeSet.getRequiredDay() != null ? codeSet.getRequiredDay() : LocalDate.MAX;

        return codeSetDAO.getAllCodeSetCriteriaMap().stream()
                .filter(map -> {
                    LocalDate origRequiredDay = map.getCodeSet().getRequiredDay() != null ? map.getCodeSet().getRequiredDay() : LocalDate.MAX;

                        return map.getCodeSet().getRequiredDay().equals(codeSet.getRequiredDay())
                                && origRequiredDay.equals(updatedRequiredDay);
                })
                .findAny()
                .isPresent();
    }

    private List<CertificationCriterion> getCriteriaRemovedFromCodeSet(CodeSet updatedCodeSet, CodeSet originalCodeSet) {
        return  subtractLists(originalCodeSet.getCriteria(), updatedCodeSet.getCriteria());
    }

    private List<CertificationCriterion> subtractLists(List<CertificationCriterion> listA, List<CertificationCriterion> listB) {
        Predicate<CertificationCriterion> notInListB = certFromA -> !listB.stream()
                .anyMatch(cert -> certFromA.getId().equals(cert.getId()));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }
}
