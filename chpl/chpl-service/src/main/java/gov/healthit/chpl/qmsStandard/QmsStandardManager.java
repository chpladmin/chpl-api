package gov.healthit.chpl.qmsStandard;

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

@Component
public class QmsStandardManager {
    private static final int MAX_LISTINGS_IN_DELETE_ERROR_MESSAGE = 25;

    private QmsStandardDAO qmsStandardDao;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public QmsStandardManager(QmsStandardDAO qmsStandardDao,
            ErrorMessageUtil errorMessageUtil) {
        this.qmsStandardDao = qmsStandardDao;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Transactional
    public List<QmsStandard> getAllQmsStandards() {
        return qmsStandardDao.getAll();
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).QMS_STANDARD, "
            + "T(gov.healthit.chpl.permissions.domains.QmsStandardDomainPermissions).UPDATE)")
    @Transactional
    @ListingStoreRemove(removeBy = RemoveBy.ALL)
    public QmsStandard update(QmsStandard qmsStandard) throws EntityRetrievalException, ValidationException {
        qmsStandard.setName(StringUtils.trim(qmsStandard.getName()));
        QmsStandard originalQmsStandard = qmsStandardDao.getById(qmsStandard.getId());
        if (originalQmsStandard == null) {
            throw new EntityRetrievalException(errorMessageUtil.getMessage("qmsStandard.doesNotExist"));
        }

        if (!originalQmsStandard.equals(qmsStandard)) {
            validateForEdit(qmsStandard);
            qmsStandardDao.update(qmsStandard);
        }
        return qmsStandardDao.getById(qmsStandard.getId());
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).QMS_STANDARD, "
            + "T(gov.healthit.chpl.permissions.domains.QmsStandardDomainPermissions).CREATE)")
    @Transactional
    public QmsStandard create(QmsStandard qmsStandard) throws EntityCreationException, ValidationException {
        qmsStandard.setName(StringUtils.trim(qmsStandard.getName()));
        validateForAdd(qmsStandard);
        return qmsStandardDao.create(qmsStandard);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).QMS_STANDARD, "
            + "T(gov.healthit.chpl.permissions.domains.QmsStandardDomainPermissions).DELETE)")
    @Transactional
    public void delete(Long qmsStandardId) throws EntityRetrievalException, ValidationException {
        QmsStandard originalQmsStandard = qmsStandardDao.getById(qmsStandardId);
        if (originalQmsStandard == null) {
            throw new EntityRetrievalException(errorMessageUtil.getMessage("qmsStandard.doesNotExist"));
        }
        validateForDelete(originalQmsStandard);
        qmsStandardDao.delete(qmsStandardId);
    }

    private void validateForDelete(QmsStandard qmsStandard) throws ValidationException {
        List<CertifiedProduct> listings = qmsStandardDao.getCertifiedProductsByQmsStandard(qmsStandard);

        if (!CollectionUtils.isEmpty(listings)) {
            String message = errorMessageUtil.getMessage("qmsStandard.delete.listingsExist",
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

    private void validateForEdit(QmsStandard updatedQmsStandard) throws ValidationException {
        Set<String> messages = new HashSet<String>();

        if (StringUtils.isBlank(updatedQmsStandard.getName())) {
            messages.add(errorMessageUtil.getMessage("qmsStandard.emptyName"));
        } else if (isQmsStandardNameDuplicate(updatedQmsStandard)) {
            messages.add(errorMessageUtil.getMessage("qmsStandard.duplicate", updatedQmsStandard.getName()));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    private void validateForAdd(QmsStandard qmsStandard) throws ValidationException {
        Set<String> messages = new HashSet<String>();

        if (StringUtils.isBlank(qmsStandard.getName())) {
            messages.add(errorMessageUtil.getMessage("qmsStandard.emptyName"));
        } else if (isQmsStandardNameDuplicate(qmsStandard)) {
            messages.add(errorMessageUtil.getMessage("qmsStandard.duplicate", qmsStandard.getName()));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    private boolean isQmsStandardNameDuplicate(QmsStandard qmsStandard) {
        return qmsStandardDao.getAll().stream()
                .filter(existingQmsStd -> existingQmsStd.getName().equalsIgnoreCase(qmsStandard.getName()))
                .findAny()
                .isPresent();
    }
}
