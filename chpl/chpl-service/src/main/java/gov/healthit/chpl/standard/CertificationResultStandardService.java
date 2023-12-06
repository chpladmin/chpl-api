package gov.healthit.chpl.standard;

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
public class CertificationResultStandardService {
    private CertificationResultStandardDAO certResultStandardDAO;

    @Autowired
    public CertificationResultStandardService(CertificationResultStandardDAO certResultStandardDAO) {
        this.certResultStandardDAO = certResultStandardDAO;
    }

    public int synchronizeStandards(CertificationResult certResult, List<CertificationResultStandard> certResultStandardsFromDb,
            List<CertificationResultStandard> certResultStandards) throws EntityCreationException {

        List<CertificationResultStandard> updatedStandards = new ArrayList<CertificationResultStandard>();
        List<CertificationResultStandard> addedStandards = new ArrayList<CertificationResultStandard>();
        List<CertificationResultStandard> removedStandards = new ArrayList<CertificationResultStandard>();

        //Find the updated Standards
        if (!CollectionUtils.isEmpty(certResultStandards)) {
            updatedStandards = certResultStandards.stream()
                    .filter(crtt -> {
                        Optional<CertificationResultStandard> found = getMatchingItemInList(crtt, certResultStandardsFromDb);
                        return found.isPresent();
                    })
                    .toList();

            updatedStandards.forEach(updatedStandard -> certResultStandardDAO.updateStandardMapping(
                    getMatchingItemInList(updatedStandard, certResultStandardsFromDb).get().getId(),
                    updatedStandard));
        }

        //Find the added Standards
        if (!CollectionUtils.isEmpty(certResultStandards)) {
            addedStandards = certResultStandards.stream()
                    .filter(crs -> getMatchingItemInList(crs, certResultStandardsFromDb).isEmpty())
                    .toList();

            addedStandards.forEach(addedStandard -> addCertificationResultStandard(addedStandard, certResult.getId()));
        }

        //Find the removed Functionalities Tested
        if (!CollectionUtils.isEmpty(certResultStandardsFromDb)) {
            removedStandards = certResultStandardsFromDb.stream()
                    .filter(crtt -> getMatchingItemInList(crtt, certResultStandards).isEmpty())
                    .toList();

            removedStandards.forEach(removedStandard -> certResultStandardDAO.deleteStandardMapping(
                    getMatchingItemInList(removedStandard, certResultStandardsFromDb).get().getId()));
        }

        return updatedStandards.size() + addedStandards.size() + removedStandards.size();
    }

    private CertificationResultStandard addCertificationResultStandard(CertificationResultStandard crs, Long certificationResultId) {
        try {
            return certResultStandardDAO.addStandardMapping(
                    CertificationResultStandard.builder()
                            .certificationResultId(certificationResultId)
                            .standard(Standard.builder()
                                    .id(crs.getStandard().getId())
                                    .build())
                            .build());

        } catch (EntityCreationException e) {
            LOGGER.error("Could not create Certification Result Functionality Tested.", e);
            return null;
        }
    }

    private Optional<CertificationResultStandard> getMatchingItemInList(CertificationResultStandard crs, List<CertificationResultStandard> certificationResultStandards) {
        if (CollectionUtils.isEmpty(certificationResultStandards)) {
            return Optional.empty();
        }
        return certificationResultStandards.stream()
                .filter(certificationResultStandard ->
                        certificationResultStandard != null ? certificationResultStandard.getStandard().getId().equals(crs.getStandard().getId()) : false)
                .findAny();
    }
}
