package gov.healthit.chpl.svap.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.svap.dao.SvapDAO;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.svap.domain.SvapCriteriaMap;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SvapManager {
    private SvapDAO svapDao;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public SvapManager(SvapDAO svapDao, ErrorMessageUtil errorMessageUtil) {
        this.svapDao = svapDao;
        this.errorMessageUtil = errorMessageUtil;
    }

    public List<SvapCriteriaMap> getAllSvapCriteriaMaps() throws EntityRetrievalException {
        return svapDao.getAllSvapCriteriaMap();
    }

    //TODO - add permissions
    @Transactional
    public List<Svap> getAll() {
        return svapDao.getAll();
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SVAP, "
            + "T(gov.healthit.chpl.permissions.domains.SvapDomainPermissions).UPDATE)")
    @Transactional
    public Svap update(Svap svap) throws EntityRetrievalException, ValidationException {
        Svap originalSvap = svapDao.getById(svap.getSvapId());
        validateForEdit(svap,  originalSvap);
        updateSvap(svap);
        addNewCriteriaForExistingSvap(svap, originalSvap);
        deleteCriteriaRemovedFromSvap(svap, originalSvap);
        return getSvap(svap.getSvapId());
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SVAP, "
            + "T(gov.healthit.chpl.permissions.domains.SvapDomainPermissions).CREATE)")
    @Transactional
    public Svap create(Svap svap) throws EntityRetrievalException {
        Svap newSvap = addSvap(svap);
        addNewCriteriaForNewSvap(newSvap, svap.getCriteria());
        return getSvap(newSvap.getSvapId());
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SVAP, "
            + "T(gov.healthit.chpl.permissions.domains.SvapDomainPermissions).DELETE)")
    @Transactional
    public void delete(Svap svap) throws EntityRetrievalException, ValidationException {
        Svap originalSvap = getSvap(svap.getSvapId());
        validateForDelete(originalSvap);
        deleteAllCriteriaFromSvap(originalSvap);
        deleteSvap(originalSvap);
    }

    private Svap getSvap(Long svapId) throws EntityRetrievalException {
        return svapDao.getById(svapId);
    }

    private void updateSvap(Svap svap) throws EntityRetrievalException {
        svapDao.update(svap);
    }

    private Svap addSvap(Svap svap) throws EntityRetrievalException {
        return svapDao.create(svap);
    }

    private void deleteSvap(Svap svap) throws EntityRetrievalException {
        svapDao.remove(svap);
    }

    private void deleteCriteriaRemovedFromSvap(Svap updatedSvap, Svap originalSvap) {
        getCriteriaRemovedFromSvap(updatedSvap, originalSvap).stream()
                .forEach(crit -> svapDao.removeSvapCriteriaMap(updatedSvap, crit));
    }

    private void deleteAllCriteriaFromSvap(Svap svap) {
        svap.getCriteria().stream()
                .forEach(crit -> svapDao.removeSvapCriteriaMap(svap, crit));
    }

    private void addNewCriteriaForExistingSvap(Svap updatedSvap, Svap originalSvap) {
        getCriteriaAddedToSvap(updatedSvap, originalSvap).stream()
                .forEach(crit -> svapDao.addSvapCriteriMap(updatedSvap, crit));
    }

    private void addNewCriteriaForNewSvap(Svap svap, List<CertificationCriterion> criteria) {
        criteria.stream()
                .forEach(crit -> svapDao.addSvapCriteriMap(svap, crit));
    }

    private void validateForDelete(Svap svap) throws ValidationException {
        List<CertifiedProductDetailsDTO> listings = svapDao.getCertifiedProductsBySvap(svap);
        if (listings.size() > 0) {
            String message = errorMessageUtil.getMessage("svap.delete.listingsExist",
                    listings.size(),
                    listings.stream()
                            .map(listing -> listing.getChplProductNumber())
                            .collect(Collectors.joining(", ")));
            ValidationException e = new ValidationException(message);
            throw e;
        }
    }

    private void validateForEdit(Svap updatedSvap, Svap originalSvap) throws ValidationException {
        Set<String> messages = new HashSet<String>();
        //If there are removed criteria, make sure there are no listings attesting to SVAP/criteria
        getCriteriaRemovedFromSvap(updatedSvap, originalSvap).stream()
                .forEach(crit -> {
                    List<CertifiedProductDetailsDTO> listings = svapDao.getCertifiedProductsBySvapAndCriteria(originalSvap, crit);
                    if (listings.size() > 0) {
                        messages.add(errorMessageUtil.getMessage("svap.edit.deletedCriteria.listingsExist",
                                crit.getNumber() + (crit.getTitle().indexOf("Cures Update") == -1 ? "" : " (Cures Update)"),
                                listings.size(),
                                listings.stream()
                                        .map(listing -> listing.getChplProductNumber())
                                        .collect(Collectors.joining(", "))));
                    }
                });

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    private List<CertificationCriterion> getCriteriaAddedToSvap(Svap updatedSvap, Svap originalSvap) {
        return subtractLists(updatedSvap.getCriteria(), originalSvap.getCriteria());
    }

    private List<CertificationCriterion> getCriteriaRemovedFromSvap(Svap updatedSvap, Svap originalSvap) {
        return  subtractLists(originalSvap.getCriteria(), updatedSvap.getCriteria());
    }

    private List<CertificationCriterion> subtractLists(List<CertificationCriterion> listA, List<CertificationCriterion> listB) {
        Predicate<CertificationCriterion> notInListB = certFromA -> !listB.stream()
                .anyMatch(cert -> certFromA.equals(cert));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }
}
