package gov.healthit.chpl.ucdProcess;

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
public class UcdProcessManager {
    private static final int MAX_LISTINGS_IN_DELETE_ERROR_MESSAGE = 25;

    private UcdProcessDAO ucdProcessDao;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public UcdProcessManager(UcdProcessDAO ucdProcessDao,
            ErrorMessageUtil errorMessageUtil) {
        this.ucdProcessDao = ucdProcessDao;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Transactional
    public List<UcdProcess> getAllUcdProcesses() {
        return ucdProcessDao.getAll();
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).UCD_PROCESS, "
            + "T(gov.healthit.chpl.permissions.domains.UcdProcessDomainPermissions).UPDATE)")
    @Transactional
    @ListingStoreRemove(removeBy = RemoveBy.ALL)
    public UcdProcess update(UcdProcess ucdProcess) throws EntityRetrievalException, ValidationException {
        ucdProcess.setName(StringUtils.trim(ucdProcess.getName()));
        UcdProcess originalUcdProcess = ucdProcessDao.getById(ucdProcess.getId());
        if (!originalUcdProcess.equals(ucdProcess)) {
            validateForEdit(ucdProcess);
            ucdProcessDao.update(ucdProcess);
        }
        return ucdProcessDao.getById(ucdProcess.getId());
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).UCD_PROCESS, "
            + "T(gov.healthit.chpl.permissions.domains.UcdProcessDomainPermissions).CREATE)")
    @Transactional
    public UcdProcess create(UcdProcess ucdProcess) throws EntityCreationException, ValidationException {
        ucdProcess.setName(StringUtils.trim(ucdProcess.getName()));
        validateForAdd(ucdProcess);
        return ucdProcessDao.create(ucdProcess);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).UCD_PROCESS, "
            + "T(gov.healthit.chpl.permissions.domains.UcdProcessDomainPermissions).DELETE)")
    @Transactional
    public void delete(Long ucdProcessId) throws EntityRetrievalException, ValidationException {
        UcdProcess originalUcdProcess = ucdProcessDao.getById(ucdProcessId);
        validateForDelete(originalUcdProcess);
        ucdProcessDao.delete(ucdProcessId);
    }

    private void validateForDelete(UcdProcess ucdProcess) throws ValidationException {
        List<CertifiedProduct> listings = ucdProcessDao.getCertifiedProductsByUcdProcess(ucdProcess);

        if (!CollectionUtils.isEmpty(listings)) {
            String message = errorMessageUtil.getMessage("ucdProcess.delete.listingsExist",
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

    private void validateForEdit(UcdProcess updatedUcdProcess) throws ValidationException {
        Set<String> messages = new HashSet<String>();

        if (StringUtils.isBlank(updatedUcdProcess.getName())) {
            messages.add(errorMessageUtil.getMessage("ucdProcess.emptyName"));
        } else if (isUcdProcessDuplicate(updatedUcdProcess)) {
            messages.add(errorMessageUtil.getMessage("ucdProcess.duplicate", updatedUcdProcess.getName()));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    private void validateForAdd(UcdProcess ucdProcess) throws ValidationException {
        Set<String> messages = new HashSet<String>();

        if (StringUtils.isBlank(ucdProcess.getName())) {
            messages.add(errorMessageUtil.getMessage("ucdProcess.emptyName"));
        } else if (isUcdProcessDuplicate(ucdProcess)) {
            messages.add(errorMessageUtil.getMessage("ucdProcess.duplicate", ucdProcess.getName()));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    private boolean isUcdProcessDuplicate(UcdProcess ucdProcess) {
        return ucdProcessDao.getAll().stream()
                .filter(existingUcd -> existingUcd.getName().equalsIgnoreCase(ucdProcess.getName()))
                .findAny()
                .isPresent();
    }
}
