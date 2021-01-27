package gov.healthit.chpl.svap.manager;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.svap.dao.SvapDAO;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.svap.domain.SvapCriteriaMap;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SvapManager {
    private SvapDAO svapDao;

    @Autowired
    public SvapManager(SvapDAO svapDao) {
        this.svapDao = svapDao;
    }

    public List<SvapCriteriaMap> getAllSvapCriteriaMaps() throws EntityRetrievalException {
        return svapDao.getAllSvapCriteriaMap();
    }

    //TODO - add permissions
    @Transactional
    public List<Svap> getAll() {
        return svapDao.getAll();
    }

    //TODO - add permissions
    @Transactional
    public Svap update(Svap svap) throws EntityRetrievalException, ValidationException {
        Svap originalSvap = svapDao.getById(svap.getSvapId());

        svapDao.update(svap);

        getCriteriaAddedToSvap(svap, originalSvap).stream()
                .forEach(crit -> svapDao.addSvapCriteriMap(svap, crit));
        getCriteriaRemovedFromSvap(svap, originalSvap).stream()
                .forEach(crit -> svapDao.removeSvapCriteriaMap(svap, crit));

        return svapDao.getById(svap.getSvapId());
    }

    //TODO - add permissions
    @Transactional
    public Svap create(Svap svap) throws EntityRetrievalException {
        Svap newSvap = svapDao.create(svap);
        svap.getCriteria().stream()
                .forEach(crit -> svapDao.addSvapCriteriMap(newSvap, crit));

        return svapDao.getById(newSvap.getSvapId());
    }

    @Transactional
    public void delete(Svap svap) throws EntityRetrievalException {
        Svap originalSvap = svapDao.getById(svap.getSvapId());

        originalSvap.getCriteria().stream()
                .forEach(crit -> svapDao.removeSvapCriteriaMap(originalSvap, crit));

        svapDao.remove(originalSvap);
    }

    private List<CertificationCriterion> getCriteriaAddedToSvap(Svap updatedSvap, Svap originalSvap) {
        List<CertificationCriterion> added = subtractLists(updatedSvap.getCriteria(), originalSvap.getCriteria());
        added.stream()
                .forEach(crit -> LOGGER.info("Added criteria: " + crit.getNumber()));
        return added;
    }

    private List<CertificationCriterion> getCriteriaRemovedFromSvap(Svap updatedSvap, Svap originalSvap) {
        List<CertificationCriterion> added = subtractLists(originalSvap.getCriteria(), updatedSvap.getCriteria());
        added.stream()
                .forEach(crit -> LOGGER.info("Removed criteria: " + crit.getNumber()));
        return added;
    }

    private List<CertificationCriterion> subtractLists(List<CertificationCriterion> listA,
            List<CertificationCriterion> listB) {

        Predicate<CertificationCriterion> notInListB = certFromA -> !listB.stream()
                .anyMatch(cert -> certFromA.equals(cert));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }


}
