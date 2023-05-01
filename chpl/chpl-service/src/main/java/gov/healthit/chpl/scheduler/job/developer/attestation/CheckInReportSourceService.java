package gov.healthit.chpl.scheduler.job.developer.attestation;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationSubmission;
import gov.healthit.chpl.attestation.service.AttestationSubmissionService;
import gov.healthit.chpl.changerequest.dao.ChangeRequestAttestationDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.domain.Developer;

@Component
public class CheckInReportSourceService {

    private AttestationSubmissionService attestationManager;
    private ChangeRequestAttestationDAO crAttestationDao;
    private ChangeRequestManager changeRequestManager;

    public CheckInReportSourceService(AttestationSubmissionService attestationManager,
            ChangeRequestAttestationDAO crAttestationDao,
            ChangeRequestManager changeRequestManager) {
        this.attestationManager = attestationManager;
        this.crAttestationDao = crAttestationDao;
        this.changeRequestManager = changeRequestManager;
    }

    public CheckInAttestation getCheckinReport(Developer developer, AttestationPeriod period, Logger logger) {
        return CheckInAttestation.builder()
                .attestationSubmission(getMostRecentAttestationSubmission(developer, period))
                .changeRequest(getMostRecentChangeRequest(developer, period, logger))
                .build();
    }

    private AttestationSubmission getMostRecentAttestationSubmission(Developer developer, AttestationPeriod period) {
        return attestationManager.getAttestationSubmissions(developer.getId()).stream()
                .filter(att -> att.getAttestationPeriod().getId().equals(period.getId()))
                .sorted((result1, result2) -> result1.getDatePublished().compareTo(result2.getDatePublished()) * -1)
                .findFirst().orElse(null);
    }

    private ChangeRequest getMostRecentChangeRequest(Developer developer, AttestationPeriod period, Logger logger) {
        Long crId = crAttestationDao.getIdOfMostRecentAttestationChangeRequest(developer.getId(), period.getId());
        if (crId == null) {
            logger.warn("No change request was found for developer " + developer.getId() + " and attestation period " + period.getId());
            return null;
        }
        ChangeRequest changeRequest = null;
        try {
            changeRequest = changeRequestManager.getChangeRequest(crId);
        } catch (Exception ex) {
            logger.warn("Error getting change request with ID " + crId);
        }
        return changeRequest;
    }


}
