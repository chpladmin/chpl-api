package gov.healthit.chpl.codesetdate;

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
public class CodeSetDateValidator {
    private static final int MAX_LISTINGS_IN_DELETE_ERROR_MESSAGE = 25;

    private ErrorMessageUtil errorMessageUtil;
    private CodeSetDateDAO codeSetDateDAO;

    @Autowired
    public CodeSetDateValidator(ErrorMessageUtil errorMessageUtil, CodeSetDateDAO codeSetDateDAO) {
        this.errorMessageUtil = errorMessageUtil;
        this.codeSetDateDAO = codeSetDateDAO;
    }

    public void validateForEdit(CodeSetDate codeSetDate) throws ValidationException, EntityRetrievalException {
        Set<String> messages = new HashSet<String>();

        if (codeSetDate.getRequiredDay() == null) {
            messages.add(errorMessageUtil.getMessage("codeSetDate.edit.emptyRequiredDay"));
        }

        if (CollectionUtils.isEmpty(codeSetDate.getCriteria())) {
            messages.add(errorMessageUtil.getMessage("codeSetDate.edit.noCriteria"));
        } else {
            if (isCodeSetDateDuplicateOnEdit(codeSetDate)) {
                messages.add(errorMessageUtil.getMessage("codeSetDate.edit.duplicate", "Code Set Date"));
            }
            messages.addAll(validateCriteriaRemovedFromCodeSetDate(codeSetDate));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    public void validateForAdd(CodeSetDate codeSetDate) throws ValidationException, EntityRetrievalException {
        Set<String> messages = new HashSet<String>();

        if (codeSetDate.getRequiredDay() == null) {
            messages.add(errorMessageUtil.getMessage("codeSetDate.edit.emptyRequiredDay"));
        }

        if (CollectionUtils.isEmpty(codeSetDate.getCriteria())) {
            messages.add(errorMessageUtil.getMessage("codeSetDate.edit.noCriteria"));
        }

        if (isCodeSetDatedDuplicateOnAdd(codeSetDate)) {
            messages.add(errorMessageUtil.getMessage("codeSetDate.edit.duplicate", "Code Set Date"));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    public void validateForDelete(CodeSetDate codeSetDate) throws ValidationException {
        List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
        try {
            listings = codeSetDateDAO.getCertifiedProductsByCodeSetDate(codeSetDate);
        } catch (EntityRetrievalException ex) {
            throw new ValidationException(ex.getMessage());
        }

        if (!CollectionUtils.isEmpty(listings)) {
            String message = errorMessageUtil.getMessage("codeSetDate.delete.listingsExist",
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

    private Set<String> validateCriteriaRemovedFromCodeSetDate(CodeSetDate codeSetDate) {
        Set<String> messages = new HashSet<String>();
        CodeSetDate origCodeSetDate = codeSetDateDAO.getById(codeSetDate.getId());

        getCriteriaRemovedFromCodeSetDate(codeSetDate, origCodeSetDate).stream()
                .forEach(crit -> {
                    List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
                    try {
                        listings = codeSetDateDAO.getCertifiedProductsByCodeSetDateAndCriteria(origCodeSetDate, crit);
                    } catch (EntityRetrievalException ex) {
                        messages.add(ex.getMessage());
                    }

                    if (!CollectionUtils.isEmpty(listings)) {
                        String message = errorMessageUtil.getMessage("codeSetDate.edit.deletedCriteria.listingsExist",
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

    private boolean isCodeSetDateDuplicateOnEdit(CodeSetDate codeSetDate) throws EntityRetrievalException {
        LocalDate updatedRequiredDay = codeSetDate.getRequiredDay() != null ? codeSetDate.getRequiredDay() : LocalDate.MAX;

        return codeSetDateDAO.getAllCodeSetDateCriteriaMap().stream()
                .filter(map -> {
                        LocalDate origRequiredDay = map.getCodeSetDate().getRequiredDay() != null ? map.getCodeSetDate().getRequiredDay() : LocalDate.MAX;

                        return map.getCodeSetDate().getRequiredDay().equals(codeSetDate.getRequiredDay())
                                && origRequiredDay.equals(updatedRequiredDay)
                                && !map.getCodeSetDate().getId().equals(codeSetDate.getId());
                })
                .findAny()
                .isPresent();
    }

    private boolean isCodeSetDatedDuplicateOnAdd(CodeSetDate codeSetDate) throws EntityRetrievalException {
        LocalDate updatedRequiredDay = codeSetDate.getRequiredDay() != null ? codeSetDate.getRequiredDay() : LocalDate.MAX;

        return codeSetDateDAO.getAllCodeSetDateCriteriaMap().stream()
                .filter(map -> {
                    LocalDate origRequiredDay = map.getCodeSetDate().getRequiredDay() != null ? map.getCodeSetDate().getRequiredDay() : LocalDate.MAX;

                        return map.getCodeSetDate().getRequiredDay().equals(codeSetDate.getRequiredDay())
                                && origRequiredDay.equals(updatedRequiredDay);
                })
                .findAny()
                .isPresent();
    }

    private List<CertificationCriterion> getCriteriaRemovedFromCodeSetDate(CodeSetDate updatedCodeSetDate, CodeSetDate originalCodeSetDate) {
        return  subtractLists(originalCodeSetDate.getCriteria(), updatedCodeSetDate.getCriteria());
    }

    private List<CertificationCriterion> subtractLists(List<CertificationCriterion> listA, List<CertificationCriterion> listB) {
        Predicate<CertificationCriterion> notInListB = certFromA -> !listB.stream()
                .anyMatch(cert -> certFromA.getId().equals(cert.getId()));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }
}
