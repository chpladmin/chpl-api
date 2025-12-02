package gov.healthit.chpl.codeset;

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
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2
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
            List<CertificationCriterion> criteriaHavingDuplicates = getCriteriaHavingCodeSetOnEdit(codeSet);
            if (!CollectionUtils.isEmpty(criteriaHavingDuplicates)) {
                messages.add(errorMessageUtil.getMessage("codeSet.edit.duplicate",
                        codeSet.getName(),
                        criteriaHavingDuplicates.size() == 1 ? "on" : "a",
                        Util.joinListGrammatically(criteriaHavingDuplicates.stream()
                                .map(crit -> Util.formatCriteriaNumber(crit))
                                .collect(Collectors.toList()))));
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

        List<CertificationCriterion> criteriaHavingDuplicates = getCriteriaHavingCodeSetOnAdd(codeSet);
        if (!CollectionUtils.isEmpty(criteriaHavingDuplicates)) {
            messages.add(errorMessageUtil.getMessage("codeSet.edit.duplicate",
                    codeSet.getName(),
                    criteriaHavingDuplicates.size() == 1 ? "on" : "a",
                    Util.joinListGrammatically(criteriaHavingDuplicates.stream()
                            .map(crit -> Util.formatCriteriaNumber(crit))
                            .collect(Collectors.toList()))));
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

    private List<CertificationCriterion> getCriteriaHavingCodeSetOnEdit(CodeSet codeSet) throws EntityRetrievalException {
        String updatedCodeSetName = codeSet.getName();
        List<CertificationCriterion> updatedCodeSetCriteria = codeSet.getCriteria();

        return updatedCodeSetCriteria.stream()
                .filter(criterion -> criterionWithCodeSetNameExists(criterion, updatedCodeSetName, codeSet.getId()))
                .collect(Collectors.toList());
    }

    private List<CertificationCriterion> getCriteriaHavingCodeSetOnAdd(CodeSet codeSet) throws EntityRetrievalException {
        String updatedCodeSetName = codeSet.getName();
        List<CertificationCriterion> updatedCodeSetCriteria = codeSet.getCriteria();

        //ensure that there are no other code sets with the same name for any of the criteria in the updated code set
        return updatedCodeSetCriteria.stream()
            .filter(criterion -> criterionWithCodeSetNameExists(criterion, updatedCodeSetName))
            .collect(Collectors.toList());
    }

    private boolean criterionWithCodeSetNameExists(CertificationCriterion criterion, String updatedCodeSetName) {
        List<CodeSet> existingCodeSetsForCriterion = null;
        try {
            existingCodeSetsForCriterion = codeSetDAO.getAllCodeSetCriteriaMap().stream()
                .filter(map -> map.getCriterion().getId().equals(criterion.getId()))
                .map(map -> map.getCodeSet())
                .collect(Collectors.toList());
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Unable to get all code set criteria maps", ex);
            //return true so we don't accidentally allow something that we shouldn't
            return true;
        }

        return existingCodeSetsForCriterion.stream()
                .filter(codeSet -> codeSet.getName().equalsIgnoreCase(updatedCodeSetName))
                .findAny()
                .isPresent();
    }

    private boolean criterionWithCodeSetNameExists(CertificationCriterion criterion, String updatedCodeSetName, Long updatedCodeSetId) {
        List<CodeSet> existingCodeSetsForCriterion = null;
        try {
            existingCodeSetsForCriterion = codeSetDAO.getAllCodeSetCriteriaMap().stream()
                .filter(map -> map.getCriterion().getId().equals(criterion.getId()))
                .map(map -> map.getCodeSet())
                .collect(Collectors.toList());
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Unable to get all code set criteria maps", ex);
            //return true so we don't accidentally allow something that we shouldn't
            return true;
        }

        return existingCodeSetsForCriterion.stream()
                .filter(codeSet -> codeSet.getName().equalsIgnoreCase(updatedCodeSetName)
                        && !codeSet.getId().equals(updatedCodeSetId))
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
