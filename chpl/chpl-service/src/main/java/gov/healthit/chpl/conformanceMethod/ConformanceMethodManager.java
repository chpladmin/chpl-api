package gov.healthit.chpl.conformanceMethod;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
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
@Component("conformanceMethodManager")
public class ConformanceMethodManager {

    private ConformanceMethodDAO conformanceMethodDao;
    private CertificationCriterionAttributeDAO certificationCriterionAttributeDao;
    private ConformanceMethodValidator cmValidator;
    private ActivityManager activityManager;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ConformanceMethodManager(ConformanceMethodDAO conformanceMethodDao,
            CertificationCriterionAttributeDAO certificationCriterionAttributeDao,
            ConformanceMethodValidator cmValidator,
            ActivityManager activityManager,
            ErrorMessageUtil msgUtil) {
        this.conformanceMethodDao = conformanceMethodDao;
        this.certificationCriterionAttributeDao = certificationCriterionAttributeDao;
        this.cmValidator = cmValidator;
        this.activityManager = activityManager;
        this.msgUtil = msgUtil;
    }

    @Transactional
    @Cacheable(value = CacheNames.CONFORMANCE_METHODS)
    public List<ConformanceMethod> getAll() {
        return conformanceMethodDao.getAllWithCriteria();
    }

    @Transactional
    public List<CertificationCriterion> getCertificationCriteriaForConformanceMethods() {
        return certificationCriterionAttributeDao.getCriteriaForConformanceMethods();
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CONFORMANCE_METHOD, "
            + "T(gov.healthit.chpl.permissions.domains.ConformanceMethodDomainPermissions).CREATE)")
    @Transactional
    public ConformanceMethod create(ConformanceMethod conformanceMethod) throws EntityRetrievalException, ValidationException {
        normalize(conformanceMethod);
        cmValidator.validateForAdd(conformanceMethod);
        Long createdConformanceMethodId = conformanceMethodDao.create(conformanceMethod);
        if (!CollectionUtils.isEmpty(conformanceMethod.getCriteria())) {
            conformanceMethod.getCriteria().stream()
                    .forEach(crit -> conformanceMethodDao.createConformanceMethodCriteriaMap(createdConformanceMethodId, crit));
        }

        ConformanceMethod createdConformanceMethod = conformanceMethodDao.getById(createdConformanceMethodId);
        try {
            activityManager.addActivity(ActivityConcept.CONFORMANCE_METHOD, createdConformanceMethodId,
                    createdConformanceMethod.getName() + " was created.",
                    null, createdConformanceMethod);
        } catch (ActivityException ex) {
            LOGGER.error("Error adding activity about creating Conformance Method " + createdConformanceMethod.getName(), ex);
        }

        return createdConformanceMethod;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CONFORMANCE_METHOD, "
            + "T(gov.healthit.chpl.permissions.domains.ConformanceMethodDomainPermissions).UPDATE)")
    @Transactional
    @ListingStoreRemove(removeBy = RemoveBy.ALL)
    @GenerateListingDownloadFile(listingSet = {ListingSet.EDITION_2011, ListingSet.EDITION_2014})
    public ConformanceMethod update(ConformanceMethod conformanceMethod) throws EntityRetrievalException, ValidationException {
        ConformanceMethod origConformanceMethod = conformanceMethodDao.getById(conformanceMethod.getId());
        normalize(conformanceMethod);
        cmValidator.validateForEdit(conformanceMethod);

        conformanceMethodDao.update(conformanceMethod);
        createCriteriaMappingsAddedToConformanceMethod(conformanceMethod, origConformanceMethod);
        deleteCriteriaRemovedFromConformanceMethod(conformanceMethod, origConformanceMethod);

        ConformanceMethod updatedConformanceMethod = conformanceMethodDao.getById(conformanceMethod.getId());
        try {
            activityManager.addActivity(ActivityConcept.CONFORMANCE_METHOD, origConformanceMethod.getId(),
                    updatedConformanceMethod.getName() + " was updated.",
                    origConformanceMethod, updatedConformanceMethod);
        } catch (ActivityException ex) {
            LOGGER.error("Error adding activity about updating Conformance Method " + updatedConformanceMethod.getName(), ex);
        }

        return updatedConformanceMethod;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CONFORMANCE_METHOD, "
            + "T(gov.healthit.chpl.permissions.domains.ConformanceMethodDomainPermissions).DELETE)")
    @Transactional
    public void delete(Long conformanceMethodId) throws EntityRetrievalException, ValidationException {
        ConformanceMethod conformanceMethod = conformanceMethodDao.getById(conformanceMethodId);
        if (conformanceMethod == null) {
            ValidationException e = new ValidationException(msgUtil.getMessage("conformanceMethod.notFound"));
            throw e;
        }

        cmValidator.validateForDelete(conformanceMethod);
        conformanceMethod.getCriteria()
            .forEach(crit -> conformanceMethodDao.removeConformanceMethodCriteriaMap(conformanceMethodId, crit));
        conformanceMethodDao.remove(conformanceMethod);

        try {
            activityManager.addActivity(ActivityConcept.CONFORMANCE_METHOD, conformanceMethodId,
                    conformanceMethod.getName() + " was deleted.",
                    conformanceMethod, null);
        } catch (ActivityException ex) {
            LOGGER.error("Error adding activity about deleting conformance method with ID " + conformanceMethodId, ex);
        }
    }

    private void normalize(ConformanceMethod conformanceMethod) {
        conformanceMethod.setName(StringUtils.trim(conformanceMethod.getName()));
    }

    private void createCriteriaMappingsAddedToConformanceMethod(ConformanceMethod conformanceMethod,
            ConformanceMethod originalConformanceMethod) {
        getCriteriaAddedToConformanceMethod(conformanceMethod, originalConformanceMethod).stream()
                .forEach(crit -> conformanceMethodDao.createConformanceMethodCriteriaMap(conformanceMethod.getId(), crit));
    }

    private void deleteCriteriaRemovedFromConformanceMethod(ConformanceMethod conformanceMethod,
            ConformanceMethod originalConformanceMethod) {
        getCriteriaRemovedFromConformanceMethod(conformanceMethod, originalConformanceMethod).stream()
                .forEach(crit -> conformanceMethodDao.removeConformanceMethodCriteriaMap(conformanceMethod.getId(), crit));
    }

    private List<CertificationCriterion> getCriteriaAddedToConformanceMethod(ConformanceMethod updatedConformanceMethod,
            ConformanceMethod originalConformanceMethod) {
        return subtractLists(updatedConformanceMethod.getCriteria(), originalConformanceMethod.getCriteria());
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
