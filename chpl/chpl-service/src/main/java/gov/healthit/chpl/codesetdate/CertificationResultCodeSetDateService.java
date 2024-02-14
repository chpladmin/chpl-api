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
public class CertificationResultCodeSetDateService {
    private CertificationResultCodeSetDateDAO certResultCodeSetDateDAO;

    @Autowired
    public CertificationResultCodeSetDateService(CertificationResultCodeSetDateDAO certResultCodeSetDateDAO) {
        this.certResultCodeSetDateDAO = certResultCodeSetDateDAO;
    }

    public int synchronizeCodeSetDates(CertificationResult certResult, List<CertificationResultCodeSetDate> certResultCodeSetDateFromDb,
            List<CertificationResultCodeSetDate> certResultCodeSetDates) throws EntityCreationException {

        List<CertificationResultCodeSetDate> updatedCodeSetDates = new ArrayList<CertificationResultCodeSetDate>();
        List<CertificationResultCodeSetDate> addedCodeSetDates = new ArrayList<CertificationResultCodeSetDate>();
        List<CertificationResultCodeSetDate> removedCodeSetDates = new ArrayList<CertificationResultCodeSetDate>();

        //Find the updated Code Set Dates
        if (!CollectionUtils.isEmpty(certResultCodeSetDates)) {
            updatedCodeSetDates = certResultCodeSetDates.stream()
                    .filter(crcsd -> {
                        Optional<CertificationResultCodeSetDate> found = getMatchingItemInList(crcsd, certResultCodeSetDateFromDb);
                        return found.isPresent();
                    })
                    .toList();

            updatedCodeSetDates.forEach(updatedCodeSetDate -> certResultCodeSetDateDAO.updateCodeSetDateMapping(
                    getMatchingItemInList(updatedCodeSetDate, certResultCodeSetDateFromDb).get().getId(),
                    updatedCodeSetDate));
        }

        //Find the added Code Set Dates
        if (!CollectionUtils.isEmpty(certResultCodeSetDates)) {
            addedCodeSetDates = certResultCodeSetDates.stream()
                    .filter(crcsd -> getMatchingItemInList(crcsd, certResultCodeSetDateFromDb).isEmpty())
                    .toList();

            addedCodeSetDates.forEach(addedCodeSetDate -> addCertificationResultCodeSetDate(addedCodeSetDate, certResult.getId()));
        }

        //Find the removed Functionalities Tested
        if (!CollectionUtils.isEmpty(certResultCodeSetDateFromDb)) {
            removedCodeSetDates = certResultCodeSetDateFromDb.stream()
                    .filter(crcsd -> getMatchingItemInList(crcsd, certResultCodeSetDates).isEmpty())
                    .toList();

            removedCodeSetDates.forEach(removedCodeSetDate -> certResultCodeSetDateDAO.deleteCodeSetDateMapping(
                    getMatchingItemInList(removedCodeSetDate, certResultCodeSetDateFromDb).get().getId()));
        }

        return updatedCodeSetDates.size() + addedCodeSetDates.size() + removedCodeSetDates.size();
    }

    private CertificationResultCodeSetDate addCertificationResultCodeSetDate(CertificationResultCodeSetDate crcsd, Long certificationResultId) {
        try {
            return certResultCodeSetDateDAO.addCodeSetDateMapping(
                    CertificationResultCodeSetDate.builder()
                            .certificationResultId(certificationResultId)
                            .codeSetDate(CodeSetDate.builder()
                                    .id(crcsd.getCodeSetDate().getId())
                                    .build())
                            .build());

        } catch (EntityCreationException e) {
            LOGGER.error("Could not create Certification Result Code Set Date.", e);
            return null;
        }
    }

    private Optional<CertificationResultCodeSetDate> getMatchingItemInList(CertificationResultCodeSetDate crcsd, List<CertificationResultCodeSetDate> certificationResultCodeSetDate) {
        if (CollectionUtils.isEmpty(certificationResultCodeSetDate)) {
            return Optional.empty();
        }
        return certificationResultCodeSetDate.stream()
                .filter(certificationResultFunctionalityTested ->
                        certificationResultFunctionalityTested != null ? certificationResultFunctionalityTested.getCodeSetDate().getId().equals(crcsd.getCodeSetDate().getId()) : false)
                .findAny();
    }

}
