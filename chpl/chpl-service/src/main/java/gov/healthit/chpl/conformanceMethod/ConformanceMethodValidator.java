package gov.healthit.chpl.conformanceMethod;

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
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class ConformanceMethodValidator {
    private static final int MAX_LISTINGS_IN_DELETE_ERROR_MESSAGE = 25;

    private ErrorMessageUtil errorMessageUtil;
    private ConformanceMethodDAO cmDao;

    @Autowired
    public ConformanceMethodValidator(ErrorMessageUtil errorMessageUtil, ConformanceMethodDAO cmDao) {
        this.errorMessageUtil = errorMessageUtil;
        this.cmDao = cmDao;
    }

    public void validateForEdit(ConformanceMethod conformanceMethod) throws ValidationException, EntityRetrievalException {
        Set<String> messages = new HashSet<String>();

        if (StringUtils.isEmpty(conformanceMethod.getName())) {
            messages.add(errorMessageUtil.getMessage("conformanceMethod.emptyName"));
        }

        if (CollectionUtils.isEmpty(conformanceMethod.getCriteria())) {
            messages.add(errorMessageUtil.getMessage("conformanceMethod.noCriteria"));
        } else {
            if (isConformanceMethodDuplicateOnEdit(conformanceMethod)) {
                messages.add(errorMessageUtil.getMessage("conformanceMethod.duplicate", conformanceMethod.getName()));
            }
            messages.addAll(validateCriteriaRemovedFromConformanceMethod(conformanceMethod));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    public void validateForAdd(ConformanceMethod conformanceMethod) throws ValidationException, EntityRetrievalException {
        Set<String> messages = new HashSet<String>();

        if (StringUtils.isEmpty(conformanceMethod.getName())) {
            messages.add(errorMessageUtil.getMessage("conformanceMethod.emptyName"));
        }

        if (CollectionUtils.isEmpty(conformanceMethod.getCriteria())) {
            messages.add(errorMessageUtil.getMessage("conformanceMethod.noCriteria"));
        }

        if (isConformanceMethodDuplicateOnAdd(conformanceMethod)) {
            messages.add(errorMessageUtil.getMessage("conformanceMethod.duplicate", conformanceMethod.getName()));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    public void validateForDelete(ConformanceMethod conformanceMethod) throws ValidationException {
        List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
        try {
            listings = cmDao.getCertifiedProductsByConformanceMethod(conformanceMethod);
        } catch (EntityRetrievalException ex) {
            throw new ValidationException(ex.getMessage());
        }

        if (!CollectionUtils.isEmpty(listings)) {
            String message = errorMessageUtil.getMessage("conformanceMethod.delete.listingsExist",
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

    private Set<String> validateCriteriaRemovedFromConformanceMethod(ConformanceMethod conformanceMethod) {
        Set<String> messages = new HashSet<String>();
        ConformanceMethod origConformanceMethod = cmDao.getById(conformanceMethod.getId());

        getCriteriaRemovedFromConformanceMethod(conformanceMethod, origConformanceMethod).stream()
                .forEach(crit -> {
                    List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
                    try {
                        listings = cmDao.getCertifiedProductsByConformanceMethodAndCriteria(origConformanceMethod, crit);
                    } catch (EntityRetrievalException ex) {
                        messages.add(ex.getMessage());
                    }

                    if (!CollectionUtils.isEmpty(listings)) {
                        String message = errorMessageUtil.getMessage("conformanceMethod.deletedCriteria.listingsExist",
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

    private boolean isConformanceMethodDuplicateOnEdit(ConformanceMethod conformanceMethod) throws EntityRetrievalException {
        String cmToEditName = conformanceMethod.getName() != null ? conformanceMethod.getName() : "";

        return cmDao.getAllConformanceMethodCriteriaMap().stream()
                .filter(map -> {
                        String origName = map.getConformanceMethod().getName() != null ? map.getConformanceMethod().getName() : "";
                        return origName.equalsIgnoreCase(cmToEditName)
                                && !map.getConformanceMethod().getId().equals(conformanceMethod.getId());
                })
                .findAny()
                .isPresent();
    }

    private boolean isConformanceMethodDuplicateOnAdd(ConformanceMethod conformanceMethod) throws EntityRetrievalException {
        String cmToAddName = conformanceMethod.getName() != null ? conformanceMethod.getName() : "";

        return cmDao.getAllConformanceMethodCriteriaMap().stream()
                .filter(map -> {
                        String existingName = map.getConformanceMethod().getName() != null ? map.getConformanceMethod().getName() : "";
                        return existingName.equalsIgnoreCase(cmToAddName);
                })
                .findAny()
                .isPresent();
    }

    private List<CertificationCriterion> getCriteriaRemovedFromConformanceMethod(ConformanceMethod updatedConformanceMethod,
            ConformanceMethod originalConformanceMethod) {
        return  subtractLists(originalConformanceMethod.getCriteria(), updatedConformanceMethod.getCriteria());
    }

    private List<CertificationCriterion> subtractLists(List<CertificationCriterion> listA, List<CertificationCriterion> listB) {
        Predicate<CertificationCriterion> notInListB = certFromA -> !listB.stream()
                .anyMatch(cert -> certFromA.equals(cert));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

}
