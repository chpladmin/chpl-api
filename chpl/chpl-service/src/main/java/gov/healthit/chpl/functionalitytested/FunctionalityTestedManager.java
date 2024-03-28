package gov.healthit.chpl.functionalitytested;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;
import gov.healthit.chpl.dao.CertificationCriterionAttributeDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.exception.EntityCreationException;
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
public class FunctionalityTestedManager {
    private FunctionalityTestedValidator functionalityTestedValidator;
    private FunctionalityTestedService functionalityTestedService;
    private FunctionalityTestedDAO functionalityTestedDAO;
    private CertificationCriterionAttributeDAO certificationCriterionAttributeDAO;
    private ActivityManager activityManager;
    private ErrorMessageUtil errorMessageUtil;
    private CertificationCriterionComparator criteriaComparator;
    private FunctionalityTestedComparator funcTestedComparator;

    @Autowired
    public FunctionalityTestedManager(FunctionalityTestedDAO functionalityTestedDAO,
            FunctionalityTestedValidator functionalityTestedValidator,
            FunctionalityTestedService functionalityTestedService,
            CertificationCriterionAttributeDAO certificationCriterionAttributeDAO,
            ActivityManager activityManager,
            ErrorMessageUtil errorMessageUtil,
            CertificationCriterionComparator criteriaComparator) {

        this.functionalityTestedDAO = functionalityTestedDAO;
        this.functionalityTestedValidator = functionalityTestedValidator;
        this.functionalityTestedService = functionalityTestedService;
        this.certificationCriterionAttributeDAO = certificationCriterionAttributeDAO;
        this.activityManager = activityManager;
        this.errorMessageUtil = errorMessageUtil;
        this.criteriaComparator = criteriaComparator;
        this.funcTestedComparator = new FunctionalityTestedComparator();
    }

    @Transactional
    public List<FunctionalityTested> getAll() {
        return functionalityTestedDAO.findAll();
    }

    @Transactional
    public List<CertificationCriterion> getCertificationCriteriaForFunctionalitiesTested() {
        return certificationCriterionAttributeDAO.getCriteriaForFunctionalitiesTested();
    }


    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FUNCTIONALITY_TESTED, "
            + "T(gov.healthit.chpl.permissions.domains.FunctionalityTestedDomainPermissions).UPDATE)")
    @Transactional
    @ListingStoreRemove(removeBy = RemoveBy.ALL)
    @GenerateListingDownloadFile(listingSet = {ListingSet.EDITION_2011, ListingSet.EDITION_2014, ListingSet.INACTIVE})
    public FunctionalityTested update(FunctionalityTested functionalityTested) throws EntityRetrievalException, ValidationException {
        FunctionalityTested origFuncTested = functionalityTestedDAO.getById(functionalityTested.getId());
        functionalityTestedValidator.validateForEdit(functionalityTested);
        functionalityTestedService.update(functionalityTested);
        FunctionalityTested updatedFuncTested = functionalityTestedDAO.getById(functionalityTested.getId());

        try {
            activityManager.addActivity(ActivityConcept.FUNCTIONALITY_TESTED,
                    origFuncTested.getId(),
                    origFuncTested.getRegulatoryTextCitation() + " was updated.",
                    origFuncTested, updatedFuncTested);
        } catch (JsonProcessingException | EntityCreationException ex) {
            LOGGER.error("Error adding activity about updating functionality tested " + origFuncTested.getRegulatoryTextCitation(), ex);
        }

        return updatedFuncTested;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FUNCTIONALITY_TESTED, "
            + "T(gov.healthit.chpl.permissions.domains.FunctionalityTestedDomainPermissions).CREATE)")
    @Transactional
    public FunctionalityTested create(FunctionalityTested functionalityTested) throws EntityRetrievalException, ValidationException {
        functionalityTestedValidator.validateForAdd(functionalityTested);
        FunctionalityTested createdFuncTested = functionalityTestedService.add(functionalityTested);

        try {
            activityManager.addActivity(ActivityConcept.FUNCTIONALITY_TESTED,
                    createdFuncTested.getId(),
                    createdFuncTested.getRegulatoryTextCitation() + " was created.",
                    null, createdFuncTested);
        } catch (JsonProcessingException | EntityCreationException ex) {
            LOGGER.error("Error adding activity about creating functionality tested " + createdFuncTested.getRegulatoryTextCitation(), ex);
        }

        return createdFuncTested;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FUNCTIONALITY_TESTED, "
            + "T(gov.healthit.chpl.permissions.domains.FunctionalityTestedDomainPermissions).DELETE)")
    @Transactional
    public void delete(Long functionalityTestedId) throws EntityRetrievalException, ValidationException {
        FunctionalityTested functionalityTested = functionalityTestedDAO.getById(functionalityTestedId);
        if (functionalityTested == null) {
            ValidationException e = new ValidationException(errorMessageUtil.getMessage("testTool.notFound"));
            throw e;
        }

        functionalityTestedValidator.validateForDelete(functionalityTested);
        functionalityTestedService.delete(functionalityTested);

        try {
            activityManager.addActivity(ActivityConcept.FUNCTIONALITY_TESTED,
                    functionalityTested.getId(),
                    functionalityTested.getRegulatoryTextCitation() + " was deleted.",
                    functionalityTested, null);
        } catch (JsonProcessingException | EntityCreationException ex) {
            LOGGER.error("Error adding activity about deleting functionality tested with ID " + functionalityTestedId, ex);
        }
    }

    public List<FunctionalityTested> getFunctionalitiesTested(Long criteriaId, Long practiceTypeId) {
        List<FunctionalityTested> functionalitiesTestedForCriterion = new ArrayList<FunctionalityTested>();
        Map<Long, List<FunctionalityTested>> functionalitiesTestedByCriteria = functionalityTestedDAO.getFunctionalitiesTestedCriteriaMaps();
        if (functionalitiesTestedByCriteria.containsKey(criteriaId)) {
            functionalitiesTestedForCriterion = functionalitiesTestedByCriteria.get(criteriaId);
            if (practiceTypeId != null) {
                functionalitiesTestedForCriterion = functionalitiesTestedForCriterion.stream()
                        .filter(funcTest -> funcTest.getPracticeType() == null || funcTest.getPracticeType().getId().equals(practiceTypeId))
                        .toList();
            }
        }
        functionalitiesTestedForCriterion.stream()
            .forEach(funcTested -> funcTested.setCriteria(funcTested.getCriteria().stream()
                .sorted(criteriaComparator)
                .toList()));
        return functionalitiesTestedForCriterion.stream()
                .sorted(funcTestedComparator)
                .toList();
    }
}
