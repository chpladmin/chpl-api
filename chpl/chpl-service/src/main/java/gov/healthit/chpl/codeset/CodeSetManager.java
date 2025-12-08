package gov.healthit.chpl.codeset;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationCriterionAttributeDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.scheduler.job.downloadfile.GenerateListingDownloadFile;
import gov.healthit.chpl.scheduler.job.downloadfile.ListingSet;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CodeSetManager {

    private CodeSetValidator codeSetValidator;
    private CodeSetDAO codeSetDAO;
    private CertificationCriterionAttributeDAO certificationCriterionAttributeDAO;
    private ErrorMessageUtil msgUtil;
    private ActivityManager activityManager;

    @Autowired
    public CodeSetManager(CodeSetValidator codeSetValidator,
            CodeSetDAO codeSetDAO,
            CertificationCriterionAttributeDAO certificationCriterionAttributeDAO,
            ErrorMessageUtil msgUtil,
            ActivityManager activityManager) {
        this.codeSetValidator = codeSetValidator;
        this.codeSetDAO = codeSetDAO;
        this.certificationCriterionAttributeDAO = certificationCriterionAttributeDAO;
        this.msgUtil = msgUtil;
        this.activityManager = activityManager;
    }

    @Transactional
    public List<CodeSet> getAll() {
        return codeSetDAO.findAll();
    }

    @Transactional
    public List<CertificationCriterion> getCertificationCriteriaForCodeSets() {
        return certificationCriterionAttributeDAO.getCriteriaForCodeSets();
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CODE_SET, "
            + "T(gov.healthit.chpl.permissions.domains.CodeSetDomainPermissions).UPDATE)")
    @Transactional
    @ListingStoreRemove(removeBy = RemoveBy.ALL)
    @GenerateListingDownloadFile(listingSet = {ListingSet.EDITION_2011, ListingSet.EDITION_2014, ListingSet.INACTIVE})
    public CodeSet update(CodeSet codeSet) throws EntityRetrievalException, ValidationException {
        CodeSet origCodeSet = codeSetDAO.getById(codeSet.getId());
        codeSetValidator.validateForEdit(codeSet);
        codeSetDAO.update(codeSet);
        addNewCriteriaForExistingCodeSet(codeSet, origCodeSet);
        deleteCriteriaRemovedFromCodeSet(codeSet, origCodeSet);

        CodeSet updatedCodeSet = codeSetDAO.getById(codeSet.getId());
        try {
            activityManager.addActivity(ActivityConcept.CODE_SET, origCodeSet.getId(),
                    origCodeSet.getName() + " was updated.",
                    origCodeSet, updatedCodeSet);
        } catch (ActivityException ex) {
            LOGGER.error("Error adding activity about updating code set " + origCodeSet.getName(), ex);
        }

        return updatedCodeSet;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CODE_SET, "
            + "T(gov.healthit.chpl.permissions.domains.CodeSetDomainPermissions).CREATE)")
    @Transactional
    public CodeSet create(CodeSet codeSet) throws EntityRetrievalException, ValidationException {
        codeSetValidator.validateForAdd(codeSet);
        CodeSet newCodeSet = codeSetDAO.add(codeSet);
        if (!CollectionUtils.isEmpty(codeSet.getCriteria())) {
            codeSet.getCriteria().stream()
                    .forEach(crit -> codeSetDAO.addCodeSetCriteriaMap(newCodeSet, crit));
        }
        CodeSet createdCodeSet = codeSetDAO.getById(newCodeSet.getId());

        try {
            activityManager.addActivity(ActivityConcept.CODE_SET, createdCodeSet.getId(),
                    createdCodeSet.getName() + " was created.",
                    null, createdCodeSet);
        } catch (ActivityException ex) {
            LOGGER.error("Error adding activity about creating code set " + createdCodeSet.getName(), ex);
        }
        return createdCodeSet;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CODE_SET, "
            + "T(gov.healthit.chpl.permissions.domains.CodeSetDomainPermissions).DELETE)")
    @Transactional
    public void delete(Long codeSetId) throws EntityRetrievalException, ValidationException {
        CodeSet codeSet = codeSetDAO.getById(codeSetId);
        if (codeSet == null) {
            ValidationException e = new ValidationException(msgUtil.getMessage("codeSet.notFound"));
            throw e;
        }

        codeSetValidator.validateForDelete(codeSet);
        codeSet.getCriteria().forEach(crit -> codeSetDAO.removeCodeSetCriteriaMap(codeSet, crit));
        codeSetDAO.remove(codeSet);

        try {
            activityManager.addActivity(ActivityConcept.CODE_SET, codeSet.getId(),
                    codeSet.getName() + " was deleted.",
                    codeSet, null);
        } catch (ActivityException ex) {
            LOGGER.error("Error adding activity about deleting code set " + codeSet.getName(), ex);
        }
    }

    private void addNewCriteriaForExistingCodeSet(CodeSet codeSet, CodeSet originalCodeSet) {
        getCriteriaAddedToCodeSet(codeSet, originalCodeSet).stream()
                .forEach(crit -> codeSetDAO.addCodeSetCriteriaMap(codeSet, crit));
    }

    private void deleteCriteriaRemovedFromCodeSet(CodeSet codeSet, CodeSet originalCodeSet) {
        getCriteriaRemovedFromCodeSet(codeSet, originalCodeSet).stream()
                .forEach(crit -> codeSetDAO.removeCodeSetCriteriaMap(codeSet, crit));
    }

    private List<CertificationCriterion> getCriteriaAddedToCodeSet(CodeSet updatedCodeSet, CodeSet originalCodeSet) {
        return subtractLists(updatedCodeSet.getCriteria(), originalCodeSet.getCriteria());
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
