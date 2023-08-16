package gov.healthit.chpl.criteriaattribute.functionalitytested;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.exception.EntityCreationException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CertificationResultFunctionalityTestedService {
    private CertificationResultFunctionalityTestedDAO certResultTestFunctionalityDAO;

    @Autowired
    public CertificationResultFunctionalityTestedService(CertificationResultFunctionalityTestedDAO certResultTestFunctionalityDAO) {
        this.certResultTestFunctionalityDAO = certResultTestFunctionalityDAO;
    }

    public int synchronizeFunctionalitiesTested(CertificationResult certResult, List<CertificationResultFunctionalityTested> certResultFunctionalitiesTestedFromDb,
            List<CertificationResultFunctionalityTested> certResultFunctionalitiesTested) throws EntityCreationException {

        List<CertificationResultFunctionalityTested> updatedFunctionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        List<CertificationResultFunctionalityTested> addedFunctionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        List<CertificationResultFunctionalityTested> removedFunctionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();

        //Find the updated Functionalities Tested
        if (!CollectionUtils.isEmpty(certResultFunctionalitiesTested)) {
            updatedFunctionalitiesTested = certResultFunctionalitiesTested.stream()
                    .filter(crtt -> {
                        Optional<CertificationResultFunctionalityTested> found = getMatchingItemInList(crtt, certResultFunctionalitiesTestedFromDb);
                        return found.isPresent();
                    })
                    .toList();

            updatedFunctionalitiesTested.forEach(x -> LOGGER.info("Updated Functionality Tested: {}", x.getFunctionalityTested().getValue()));

            updatedFunctionalitiesTested.forEach(updatedFunctionalityTested -> certResultTestFunctionalityDAO.updateFunctionalityTestedMapping(
                    getMatchingItemInList(updatedFunctionalityTested, certResultFunctionalitiesTestedFromDb).get().getId(),
                    updatedFunctionalityTested));
        }

        //Find the added Functionalities Tested
        if (!CollectionUtils.isEmpty(certResultFunctionalitiesTested)) {
            addedFunctionalitiesTested = certResultFunctionalitiesTested.stream()
                    .filter(crtt -> getMatchingItemInList(crtt, certResultFunctionalitiesTestedFromDb).isEmpty())
                    .toList();

            addedFunctionalitiesTested.forEach(x -> LOGGER.info("Added Test Tool: {}", x.getFunctionalityTested().getValue()));

            addedFunctionalitiesTested.forEach(addedFunctionalityTested -> addCertificationResultFunctionalityTested(addedFunctionalityTested, certResult.getId()));
        }

        //Find the removed Functionalities Tested
        if (!CollectionUtils.isEmpty(certResultFunctionalitiesTestedFromDb)) {
            removedFunctionalitiesTested = certResultFunctionalitiesTestedFromDb.stream()
                    .filter(crtt -> getMatchingItemInList(crtt, certResultFunctionalitiesTested).isEmpty())
                    .toList();

            removedFunctionalitiesTested.forEach(x -> LOGGER.info("Removed Test Tool: {}", x.getFunctionalityTested().getValue()));

            removedFunctionalitiesTested.forEach(removedFunctionalityTested -> certResultTestFunctionalityDAO.deleteFunctionalityTestedMapping(
                    getMatchingItemInList(removedFunctionalityTested, certResultFunctionalitiesTestedFromDb).get().getId()));
        }

        return updatedFunctionalitiesTested.size() + addedFunctionalitiesTested.size() + removedFunctionalitiesTested.size();
    }

    private CertificationResultFunctionalityTested addCertificationResultFunctionalityTested(CertificationResultFunctionalityTested crtt, Long certificationResultId) {
        try {
            return certResultTestFunctionalityDAO.addFunctionalityTestedMapping(
                    CertificationResultFunctionalityTested.builder()
                            .certificationResultId(certificationResultId)
                            .functionalityTested(FunctionalityTested.builder()
                                    .id(crtt.getFunctionalityTested().getId())
                                    .build())
                            .build());

        } catch (EntityCreationException e) {
            LOGGER.error("Could not create Certification Result Test Tool.", e);
            return null;
        }
    }

    private Optional<CertificationResultFunctionalityTested> getMatchingItemInList(CertificationResultFunctionalityTested crtt, List<CertificationResultFunctionalityTested> certificationResultFunctionalityTesteds) {
        if (CollectionUtils.isEmpty(certificationResultFunctionalityTesteds)) {
            return Optional.empty();
        }
        return certificationResultFunctionalityTesteds.stream()
                .filter(certificationResultFunctionalityTested ->
                        certificationResultFunctionalityTested != null ? certificationResultFunctionalityTested.getFunctionalityTested().getId().equals(crtt.getFunctionalityTested().getId()) : false)
                .findAny();
    }

}
