package gov.healthit.chpl.codesetdate;

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
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.downloadfile.GenerateListingDownloadFile;
import gov.healthit.chpl.scheduler.job.downloadfile.ListingSet;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CodeSetDateManager {

    private CodeSetDateDAO codeSetDateDAO;
    private CertificationCriterionAttributeDAO certificationCriterionAttributeDAO;

    @Autowired
    public CodeSetDateManager(CodeSetDateDAO codeSetDateDAO, CertificationCriterionAttributeDAO certificationCriterionAttributeDAO) {
        this.codeSetDateDAO = codeSetDateDAO;
        this.certificationCriterionAttributeDAO = certificationCriterionAttributeDAO;
    }

    @Transactional
    public List<CodeSetDate> getAll() {
        return codeSetDateDAO.findAll();
    }

    @Transactional
    public List<CertificationCriterion> getCertificationCriteriaForCodeSetDates() {
        return certificationCriterionAttributeDAO.getCriteriaForCodeSetDates();
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FUNCTIONALITY_TESTED, "
            + "T(gov.healthit.chpl.permissions.domains.FunctionalityTestedDomainPermissions).UPDATE)")
    @Transactional
    @ListingStoreRemove(removeBy = RemoveBy.ALL)
    @GenerateListingDownloadFile(listingSet = {ListingSet.EDITION_2011, ListingSet.EDITION_2014, ListingSet.INACTIVE})
    public CodeSetDate update(CodeSetDate codeSetDate) throws EntityRetrievalException {
        CodeSetDate origCodeSetDate = codeSetDateDAO.getById(codeSetDate.getId());
        codeSetDateDAO.update(codeSetDate);
        addNewCriteriaForExistingCodeSetDate(codeSetDate, origCodeSetDate);
        deleteCriteriaRemovedFromCodeSetDate(codeSetDate, origCodeSetDate);
        return codeSetDateDAO.getById(codeSetDate.getId());
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FUNCTIONALITY_TESTED, "
            + "T(gov.healthit.chpl.permissions.domains.FunctionalityTestedDomainPermissions).CREATE)")
    @Transactional
    public CodeSetDate create(CodeSetDate codeSetDate) throws EntityRetrievalException {
        CodeSetDate newCodeSetDate = codeSetDateDAO.add(codeSetDate);
        if (!CollectionUtils.isEmpty(codeSetDate.getCriteria())) {
            codeSetDate.getCriteria().stream()
                    .forEach(crit -> codeSetDateDAO.addCodeSetDateCriteriaMap(newCodeSetDate, crit));
        }
        return codeSetDateDAO.getById(newCodeSetDate.getId());
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FUNCTIONALITY_TESTED, "
            + "T(gov.healthit.chpl.permissions.domains.FunctionalityTestedDomainPermissions).DELETE)")
    @Transactional
    public void delete(Long codeSetDateId) throws EntityRetrievalException {
        CodeSetDate codeSetDate = codeSetDateDAO.getById(codeSetDateId);
        codeSetDate.getCriteria().forEach(crit -> codeSetDateDAO.removeCodeSetDateCriteriaMap(codeSetDate, crit));
        codeSetDateDAO.remove(codeSetDate);

    }

    private void addNewCriteriaForExistingCodeSetDate(CodeSetDate codeSetDate, CodeSetDate originalCodeSetDate) {
        LOGGER.info("Looking for criteria to add.");
        getCriteriaAddedToCodeSetDate(codeSetDate, originalCodeSetDate).stream()
                .forEach(crit -> codeSetDateDAO.addCodeSetDateCriteriaMap(codeSetDate, crit));
    }

    private void deleteCriteriaRemovedFromCodeSetDate(CodeSetDate codeSetDate, CodeSetDate originalCodeSetDate) {
        LOGGER.info("Looking for criteria to remove.");
        getCriteriaRemovedFromCodeSetDate(codeSetDate, originalCodeSetDate).stream()
                .forEach(crit -> codeSetDateDAO.removeCodeSetDateCriteriaMap(codeSetDate, crit));
    }

    private List<CertificationCriterion> getCriteriaAddedToCodeSetDate(CodeSetDate updatedCodeSetDate, CodeSetDate originalCodeSetDate) {
        return subtractLists(updatedCodeSetDate.getCriteria(), originalCodeSetDate.getCriteria());
    }

    private List<CertificationCriterion> getCriteriaRemovedFromCodeSetDate(CodeSetDate updatedCodeSetDate, CodeSetDate originalCodeSetDate) {
        return  subtractLists(originalCodeSetDate.getCriteria(), updatedCodeSetDate.getCriteria());
    }

    private List<CertificationCriterion> subtractLists(List<CertificationCriterion> listA, List<CertificationCriterion> listB) {
        Predicate<CertificationCriterion> notInListB = certFromA -> !listB.stream()
                .anyMatch(cert -> certFromA.getId().equals(cert.getId()));

        return listA.stream()
                .filter(notInListB)
                .peek(x -> LOGGER.info("{} - {}", x.getId(), x.getNumber()))
                .collect(Collectors.toList());
    }

}
