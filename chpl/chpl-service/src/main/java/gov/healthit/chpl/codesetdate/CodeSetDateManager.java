package gov.healthit.chpl.codesetdate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.downloadfile.GenerateListingDownloadFile;
import gov.healthit.chpl.scheduler.job.downloadfile.ListingSet;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class CodeSetDateManager {

    private CodeSetDateDAO codeSetDateDAO;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public CodeSetDateManager(CodeSetDateDAO codeSetDateDAO, ErrorMessageUtil errorMessageUtil) {
        this.codeSetDateDAO = codeSetDateDAO;
        this.errorMessageUtil = errorMessageUtil;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FUNCTIONALITY_TESTED, "
            + "T(gov.healthit.chpl.permissions.domains.FunctionalityTestedDomainPermissions).UPDATE)")
    @Transactional
    @ListingStoreRemove(removeBy = RemoveBy.ALL)
    @GenerateListingDownloadFile(listingSet = {ListingSet.EDITION_2011, ListingSet.EDITION_2014, ListingSet.INACTIVE})
    public CodeSetDate update(CodeSetDate codeSetDate) throws EntityRetrievalException {
        codeSetDateDAO.update(codeSetDate);
        return codeSetDateDAO.getById(codeSetDate.getId());
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FUNCTIONALITY_TESTED, "
            + "T(gov.healthit.chpl.permissions.domains.FunctionalityTestedDomainPermissions).CREATE)")
    @Transactional
    public CodeSetDate create(CodeSetDate codeSetDate) throws EntityRetrievalException {
        return codeSetDateDAO.add(codeSetDate);
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FUNCTIONALITY_TESTED, "
            + "T(gov.healthit.chpl.permissions.domains.FunctionalityTestedDomainPermissions).DELETE)")
    @Transactional
    public void delete(Long codeSetDateId) throws EntityRetrievalException{
        CodeSetDate codeSetDate = codeSetDateDAO.getById(codeSetDateId);
        codeSetDateDAO.remove(codeSetDate);
    }

}
