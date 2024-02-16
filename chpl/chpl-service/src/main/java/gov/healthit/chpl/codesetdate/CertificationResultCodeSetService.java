package gov.healthit.chpl.codesetdate;

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
public class CertificationResultCodeSetService {
    private CertificationResultCodeSetDAO certResultCodeSetDAO;

    @Autowired
    public CertificationResultCodeSetService(CertificationResultCodeSetDAO certResultCodeSetDAO) {
        this.certResultCodeSetDAO = certResultCodeSetDAO;
    }

    public int synchronizeCodeSets(CertificationResult certResult, List<CertificationResultCodeSet> certResultCodeSetFromDb,
            List<CertificationResultCodeSet> certResultCodeSets) throws EntityCreationException {

        List<CertificationResultCodeSet> updatedCodeSets = new ArrayList<CertificationResultCodeSet>();
        List<CertificationResultCodeSet> addedCodeSets = new ArrayList<CertificationResultCodeSet>();
        List<CertificationResultCodeSet> removedCodeSets = new ArrayList<CertificationResultCodeSet>();

        //Find the updated Code Set Dates
        if (!CollectionUtils.isEmpty(certResultCodeSets)) {
            updatedCodeSets = certResultCodeSets.stream()
                    .filter(crcsd -> {
                        Optional<CertificationResultCodeSet> found = getMatchingItemInList(crcsd, certResultCodeSetFromDb);
                        return found.isPresent();
                    })
                    .toList();

            updatedCodeSets.forEach(updatedCodeSet -> certResultCodeSetDAO.updateCodeSetMapping(
                    getMatchingItemInList(updatedCodeSet, certResultCodeSetFromDb).get().getId(),
                    updatedCodeSet));
        }

        //Find the added Code Set Dates
        if (!CollectionUtils.isEmpty(certResultCodeSets)) {
            addedCodeSets = certResultCodeSets.stream()
                    .filter(crcsd -> getMatchingItemInList(crcsd, certResultCodeSetFromDb).isEmpty())
                    .toList();

            addedCodeSets.forEach(addedCodeSet -> addCertificationResultCodeSet(addedCodeSet, certResult.getId()));
        }

        //Find the removed Functionalities Tested
        if (!CollectionUtils.isEmpty(certResultCodeSetFromDb)) {
            removedCodeSets = certResultCodeSetFromDb.stream()
                    .filter(crcsd -> getMatchingItemInList(crcsd, certResultCodeSets).isEmpty())
                    .toList();

            removedCodeSets.forEach(removedCodeSet -> certResultCodeSetDAO.deleteCodeSetMapping(
                    getMatchingItemInList(removedCodeSet, certResultCodeSetFromDb).get().getId()));
        }

        return updatedCodeSets.size() + addedCodeSets.size() + removedCodeSets.size();
    }

    private CertificationResultCodeSet addCertificationResultCodeSet(CertificationResultCodeSet crcsd, Long certificationResultId) {
        try {
            return certResultCodeSetDAO.addCodeSetMapping(
                    CertificationResultCodeSet.builder()
                            .certificationResultId(certificationResultId)
                            .codeSet(CodeSet.builder()
                                    .id(crcsd.getCodeSet().getId())
                                    .build())
                            .build());

        } catch (EntityCreationException e) {
            LOGGER.error("Could not create Certification Result Code Set Date.", e);
            return null;
        }
    }

    private Optional<CertificationResultCodeSet> getMatchingItemInList(CertificationResultCodeSet crcsd, List<CertificationResultCodeSet> certificationResultCodeSet) {
        if (CollectionUtils.isEmpty(certificationResultCodeSet)) {
            return Optional.empty();
        }
        return certificationResultCodeSet.stream()
                .filter(certificationResultFunctionalityTested ->
                        certificationResultFunctionalityTested != null ? certificationResultFunctionalityTested.getCodeSet().getId().equals(crcsd.getCodeSet().getId()) : false)
                .findAny();
    }

}
