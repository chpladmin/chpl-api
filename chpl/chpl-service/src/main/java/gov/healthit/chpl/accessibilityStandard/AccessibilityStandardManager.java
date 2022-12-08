package gov.healthit.chpl.accessibilityStandard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class AccessibilityStandardManager {
    private static final int MAX_LISTINGS_IN_DELETE_ERROR_MESSAGE = 25;

    private AccessibilityStandardDAO accessibilityStandardDao;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public AccessibilityStandardManager(AccessibilityStandardDAO accessibilityStandardDao,
            ErrorMessageUtil errorMessageUtil) {
        this.accessibilityStandardDao = accessibilityStandardDao;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Transactional
    public List<AccessibilityStandard> getAllAccessibilityStandards() {
        return accessibilityStandardDao.getAll();
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACCESSIBILITY_STANDARD, "
            + "T(gov.healthit.chpl.permissions.domains.AccessibilityStandardDomainPermissions).UPDATE)")
    @Transactional
    @ListingStoreRemove(removeBy = RemoveBy.ALL)
    public AccessibilityStandard update(AccessibilityStandard accessibilityStandard) throws EntityRetrievalException, ValidationException {
        accessibilityStandard.setName(StringUtils.trim(accessibilityStandard.getName()));
        AccessibilityStandard originalAccessibilityStandard = accessibilityStandardDao.getById(accessibilityStandard.getId());
        if (!originalAccessibilityStandard.equals(accessibilityStandard)) {
            validateForEdit(accessibilityStandard);
            accessibilityStandardDao.update(accessibilityStandard);
        }
        return accessibilityStandardDao.getById(accessibilityStandard.getId());
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACCESSIBILITY_STANDARD, "
            + "T(gov.healthit.chpl.permissions.domains.AccessibilityStandardDomainPermissions).CREATE)")
    @Transactional
    public AccessibilityStandard create(AccessibilityStandard accessibilityStandard) throws EntityCreationException, ValidationException {
        accessibilityStandard.setName(StringUtils.trim(accessibilityStandard.getName()));
        validateForAdd(accessibilityStandard);
        return accessibilityStandardDao.create(accessibilityStandard);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACCESSIBILITY_STANDARD, "
            + "T(gov.healthit.chpl.permissions.domains.AccessibilityStandardDomainPermissions).DELETE)")
    @Transactional
    public void delete(Long accessibilityStandardId) throws EntityRetrievalException, ValidationException {
        AccessibilityStandard originalAccessibilityStandard = accessibilityStandardDao.getById(accessibilityStandardId);
        validateForDelete(originalAccessibilityStandard);
        accessibilityStandardDao.delete(accessibilityStandardId);
    }

    private void validateForDelete(AccessibilityStandard accessibilityStandard) throws ValidationException {
        List<CertifiedProduct> listings = accessibilityStandardDao.getCertifiedProductsByAccessibilityStandard(accessibilityStandard);

        if (!CollectionUtils.isEmpty(listings)) {
            String message = errorMessageUtil.getMessage("accessibilityStandard.delete.listingsExist",
                    listings.size(),
                    listings.size() > 1 ? "s" : "");
            if (listings.size() < MAX_LISTINGS_IN_DELETE_ERROR_MESSAGE) {
                message = message + " "
                        + listings.stream()
                            .map(listing -> listing.getChplProductNumber())
                            .collect(Collectors.joining(", "));
            }
            ValidationException e = new ValidationException(message);
            throw e;
        }
    }

    private void validateForEdit(AccessibilityStandard updatedAccessibilityStandard) throws ValidationException {
        Set<String> messages = new HashSet<String>();

        if (StringUtils.isBlank(updatedAccessibilityStandard.getName())) {
            messages.add(errorMessageUtil.getMessage("accessibilityStandard.emptyName"));
        } else if (isAccessibilityStandardNameDuplicate(updatedAccessibilityStandard)) {
            messages.add(errorMessageUtil.getMessage("accessibilityStandard.duplicate", updatedAccessibilityStandard.getName()));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    private void validateForAdd(AccessibilityStandard accessibilityStandard) throws ValidationException {
        Set<String> messages = new HashSet<String>();

        if (StringUtils.isBlank(accessibilityStandard.getName())) {
            messages.add(errorMessageUtil.getMessage("accessibilityStandard.emptyName"));
        } else if (isAccessibilityStandardNameDuplicate(accessibilityStandard)) {
            messages.add(errorMessageUtil.getMessage("accessibilityStandard.duplicate", accessibilityStandard.getName()));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    private boolean isAccessibilityStandardNameDuplicate(AccessibilityStandard accessibilityStandard) {
        return accessibilityStandardDao.getAll().stream()
                .filter(existingAccStd -> existingAccStd.getName().equalsIgnoreCase(accessibilityStandard.getName()))
                .findAny()
                .isPresent();
    }
}
