package gov.healthit.chpl.standard;

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
public class StandardManager {
    private StandardValidator standardValidator;
    private StandardService standardService;
    private StandardDAO standardDAO;
    private CertificationCriterionAttributeDAO certificationCriterionAttributeDAO;
    private ActivityManager activityManager;
    private ErrorMessageUtil errorMessageUtil;
    private CertificationCriterionComparator criteriaComparator;
    private StandardComparator standardComparator;

    @Autowired
    public StandardManager(StandardDAO standardDAO,
            StandardValidator standardValidator,
            StandardService standardService,
            CertificationCriterionAttributeDAO certificationCriterionAttributeDAO,
            ActivityManager activityManager,
            ErrorMessageUtil errorMessageUtil, CertificationCriterionComparator criteriaComparator) {

        this.standardDAO = standardDAO;
        this.standardValidator = standardValidator;
        this.standardService = standardService;
        this.certificationCriterionAttributeDAO = certificationCriterionAttributeDAO;
        this.activityManager = activityManager;
        this.errorMessageUtil = errorMessageUtil;
        this.criteriaComparator = criteriaComparator;
        this.standardComparator = new StandardComparator();
    }

    @Transactional
    public List<Standard> getAll() {
        return standardDAO.findAll();
    }

    @Transactional
    public List<CertificationCriterion> getCertificationCriteriaForStandards() {
        return certificationCriterionAttributeDAO.getCriteriaForStandards();
    }


    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).STANDARD, "
            + "T(gov.healthit.chpl.permissions.domains.StandardDomainPermissions).UPDATE)")
    @Transactional
    @ListingStoreRemove(removeBy = RemoveBy.ALL)
    @GenerateListingDownloadFile(listingSet = {ListingSet.EDITION_2011, ListingSet.EDITION_2014})
    public Standard update(Standard standard) throws EntityRetrievalException, ValidationException {
        Standard origStandard = standardDAO.getById(standard.getId());
        standardValidator.validateForEdit(standard);
        standardService.update(standard);
        Standard updatedStandard = standardDAO.getById(standard.getId());

        try {
            activityManager.addActivity(ActivityConcept.STANDARD, origStandard.getId(),
                    origStandard.getRegulatoryTextCitation() + " was updated.",
                    origStandard, updatedStandard);
        } catch (JsonProcessingException | EntityCreationException ex) {
            LOGGER.error("Error adding activity about updating Standard " + origStandard.getRegulatoryTextCitation(), ex);
        }

        return updatedStandard;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).STANDARD, "
            + "T(gov.healthit.chpl.permissions.domains.StandardDomainPermissions).CREATE)")
    @Transactional
    public Standard create(Standard standard) throws EntityRetrievalException, ValidationException {
        standardValidator.validateForAdd(standard);
        Standard createdStandard = standardService.add(standard);

        try {
            activityManager.addActivity(ActivityConcept.STANDARD, createdStandard.getId(),
                    createdStandard.getRegulatoryTextCitation() + " was created.",
                    null, createdStandard);
        } catch (JsonProcessingException | EntityCreationException ex) {
            LOGGER.error("Error adding activity about creating Standard " + createdStandard.getRegulatoryTextCitation(), ex);
        }

        return createdStandard;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).STANDARD, "
            + "T(gov.healthit.chpl.permissions.domains.StandardDomainPermissions).DELETE)")
    @Transactional
    public void delete(Long standardId) throws EntityRetrievalException, ValidationException {
        Standard standard = standardDAO.getById(standardId);
        if (standard == null) {
            ValidationException e = new ValidationException(errorMessageUtil.getMessage("testTool.notFound"));
            throw e;
        }

        standardValidator.validateForDelete(standard);
        standardService.delete(standard);

        try {
            activityManager.addActivity(ActivityConcept.STANDARD, standard.getId(),
                    standard.getRegulatoryTextCitation() + " was deleted.",
                    standard, null);
        } catch (JsonProcessingException | EntityCreationException ex) {
            LOGGER.error("Error adding activity about deleting standard with ID " + standardId, ex);
        }
    }

    public List<Standard> getStandardsByCriteria(Long criteriaId) {
        List<Standard> standardsForCriterion = new ArrayList<Standard>();
        Map<Long, List<Standard>> standardsByCriteria = standardDAO.getStandardCriteriaMaps();
        if (standardsByCriteria.containsKey(criteriaId)) {
            standardsForCriterion = standardsByCriteria.get(criteriaId);
        }

        standardsForCriterion.stream()
            .forEach(standard -> standard.setCriteria(standard.getCriteria().stream()
                .sorted(criteriaComparator)
                .toList()));

        return standardsForCriterion.stream()
                .sorted(standardComparator)
                .toList();
    }
}
