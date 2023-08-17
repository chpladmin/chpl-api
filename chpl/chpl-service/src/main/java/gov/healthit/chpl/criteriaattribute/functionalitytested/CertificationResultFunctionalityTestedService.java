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

            addedFunctionalitiesTested.forEach(x -> LOGGER.info("Added Functionality Tested: {}", x.getFunctionalityTested().getValue()));

            addedFunctionalitiesTested.forEach(addedFunctionalityTested -> addCertificationResultFunctionalityTested(addedFunctionalityTested, certResult.getId()));
        }

        //Find the removed Functionalities Tested
        if (!CollectionUtils.isEmpty(certResultFunctionalitiesTestedFromDb)) {
            removedFunctionalitiesTested = certResultFunctionalitiesTestedFromDb.stream()
                    .filter(crtt -> getMatchingItemInList(crtt, certResultFunctionalitiesTested).isEmpty())
                    .toList();

            removedFunctionalitiesTested.forEach(x -> LOGGER.info("Removed Functionality Tested: {}", x.getFunctionalityTested().getValue()));

            removedFunctionalitiesTested.forEach(removedFunctionalityTested -> certResultTestFunctionalityDAO.deleteFunctionalityTestedMapping(
                    getMatchingItemInList(removedFunctionalityTested, certResultFunctionalitiesTestedFromDb).get().getId()));
        }

        return updatedFunctionalitiesTested.size() + addedFunctionalitiesTested.size() + removedFunctionalitiesTested.size();
    }

    private CertificationResultFunctionalityTested addCertificationResultFunctionalityTested(CertificationResultFunctionalityTested crft, Long certificationResultId) {
        try {
            return certResultTestFunctionalityDAO.addFunctionalityTestedMapping(
                    CertificationResultFunctionalityTested.builder()
                            .certificationResultId(certificationResultId)
                            .functionalityTested(FunctionalityTested.builder()
                                    .id(crft.getFunctionalityTested().getId())
                                    .build())
                            .build());

        } catch (EntityCreationException e) {
            LOGGER.error("Could not create Certification Result Functionality Tested.", e);
            return null;
        }
    }

    private Optional<CertificationResultFunctionalityTested> getMatchingItemInList(CertificationResultFunctionalityTested crft, List<CertificationResultFunctionalityTested> certificationResultFunctionalitiesTested) {
        if (CollectionUtils.isEmpty(certificationResultFunctionalitiesTested)) {
            return Optional.empty();
        }
        return certificationResultFunctionalitiesTested.stream()
                .filter(certificationResultFunctionalityTested ->
                        certificationResultFunctionalityTested != null ? certificationResultFunctionalityTested.getFunctionalityTested().getId().equals(crft.getFunctionalityTested().getId()) : false)
                .findAny();
    }

}
