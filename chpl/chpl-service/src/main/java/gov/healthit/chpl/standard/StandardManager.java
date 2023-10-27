package gov.healthit.chpl.standard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;
import gov.healthit.chpl.dao.CertificationCriterionAttributeDAO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.scheduler.job.downloadfile.GenerateListingDownloadFile;
import gov.healthit.chpl.scheduler.job.downloadfile.ListingSet;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class StandardManager {
    private StandardValidator standardValidator;
    private StandardService standardService;
    private StandardDAO standardDAO;
    private CertificationCriterionAttributeDAO certificationCriterionAttributeDAO;
    private ErrorMessageUtil errorMessageUtil;
    private CertificationCriterionComparator criteriaComparator;
    private StandardComparator standardComparator;

    @Autowired
    public StandardManager(StandardDAO standardDAO, StandardValidator standardValidator,
            StandardService standardService, CertificationCriterionAttributeDAO certificationCriterionAttributeDAO,
            ErrorMessageUtil errorMessageUtil, CertificationCriterionComparator criteriaComparator) {

        this.standardDAO = standardDAO;
        this.standardValidator = standardValidator;
        this.standardService = standardService;
        this.certificationCriterionAttributeDAO = certificationCriterionAttributeDAO;
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
        return certificationCriterionAttributeDAO.getCriteriaForFunctionalitiesTested();
    }


    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).STANDARD, "
            + "T(gov.healthit.chpl.permissions.domains.StandardDomainPermissions).UPDATE)")
    @Transactional
    @ListingStoreRemove(removeBy = RemoveBy.ALL)
    @GenerateListingDownloadFile(listingSet = {ListingSet.EDITION_2011, ListingSet.EDITION_2014})
    public Standard update(Standard standard) throws EntityRetrievalException, ValidationException {
        standardValidator.validateForEdit(standard);
        standardService.update(standard);
        return standardDAO.getById(standard.getId());
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).STANDARD, "
            + "T(gov.healthit.chpl.permissions.domains.StandardDomainPermissions).CREATE)")
    @Transactional
    public Standard create(Standard standard) throws EntityRetrievalException, ValidationException {
        standardValidator.validateForAdd(standard);
        return standardService.add(standard);
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
