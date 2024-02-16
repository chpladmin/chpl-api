package gov.healthit.chpl.codesetdate;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CodeSetManager {

    private CodeSetDAO codeSetDAO;
    private CertificationCriterionAttributeDAO certificationCriterionAttributeDAO;

    @Autowired
    public CodeSetManager(CodeSetDAO codeSetDAO, CertificationCriterionAttributeDAO certificationCriterionAttributeDAO) {
        this.codeSetDAO = codeSetDAO;
        this.certificationCriterionAttributeDAO = certificationCriterionAttributeDAO;
    }

    @Transactional
    public List<CodeSet> getAll() {
        return codeSetDAO.findAll();
    }

    @Transactional
    public List<CertificationCriterion> getCertificationCriteriaForCodeSets() {
        return certificationCriterionAttributeDAO.getCriteriaForCodeSets();
    }

    //@PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FUNCTIONALITY_TESTED, "
    //        + "T(gov.healthit.chpl.permissions.domains.FunctionalityTestedDomainPermissions).UPDATE)")
    @Transactional
    @ListingStoreRemove(removeBy = RemoveBy.ALL)
    @GenerateListingDownloadFile(listingSet = {ListingSet.EDITION_2011, ListingSet.EDITION_2014, ListingSet.INACTIVE})
    public CodeSet update(CodeSet codeSet) throws EntityRetrievalException {
        CodeSet origCodeSet = codeSetDAO.getById(codeSet.getId());
        codeSetDAO.update(codeSet);
        addNewCriteriaForExistingCodeSet(codeSet, origCodeSet);
        deleteCriteriaRemovedFromCodeSet(codeSet, origCodeSet);
        return codeSetDAO.getById(codeSet.getId());
    }

    //@PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FUNCTIONALITY_TESTED, "
    //        + "T(gov.healthit.chpl.permissions.domains.FunctionalityTestedDomainPermissions).CREATE)")
    @Transactional
    public CodeSet create(CodeSet codeSet) throws EntityRetrievalException {
        CodeSet newCodeSet = codeSetDAO.add(codeSet);
        if (!CollectionUtils.isEmpty(codeSet.getCriteria())) {
            codeSet.getCriteria().stream()
                    .forEach(crit -> codeSetDAO.addCodeSetCriteriaMap(newCodeSet, crit));
        }
        return codeSetDAO.getById(newCodeSet.getId());
    }

    //@PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FUNCTIONALITY_TESTED, "
    //        + "T(gov.healthit.chpl.permissions.domains.FunctionalityTestedDomainPermissions).DELETE)")
    @Transactional
    public void delete(Long codeSetId) throws EntityRetrievalException {
        CodeSet codeSet = codeSetDAO.getById(codeSetId);
        codeSet.getCriteria().forEach(crit -> codeSetDAO.removeCodeSetCriteriaMap(codeSet, crit));
        codeSetDAO.remove(codeSet);

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
