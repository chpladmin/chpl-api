package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.manager.AttestationManager;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Component
public class DeveloperAttestationCheckInReportDataCollector {

    private AttestationManager attestationManager;
    private ChangeRequestDAO changeRequestDAO;
    private DeveloperAttestationReportDataCollection developerAttestationReportDataCollection;
    private CertificationBodyDAO certificationBodyDAO;
    private DeveloperDAO developerDAO;

    public DeveloperAttestationCheckInReportDataCollector(AttestationManager attestationManager, ChangeRequestDAO changeRequestDAO,
            DeveloperAttestationReportDataCollection developerAttestationReportDataCollection, CertificationBodyDAO certificationBodyDAO,
            DeveloperDAO developerDAO) {
        this.attestationManager = attestationManager;
        this.changeRequestDAO = changeRequestDAO;
        this.developerAttestationReportDataCollection = developerAttestationReportDataCollection;
        this.certificationBodyDAO = certificationBodyDAO;
        this.developerDAO = developerDAO;
    }

    public List<DeveloperAttestationCheckInReport> collect() throws EntityRetrievalException {
        return getDevelopersWithAttestationChangeRequestsForMostRecentAttestationPeriod().entrySet().stream()
                .map(e -> convert(e))
                .sorted((o1, o2) -> o1.getDeveloperName().compareTo(o2.getDeveloperName()))
                .toList();

    }

    private DeveloperAttestationCheckInReport convert(Entry<Long, List<ChangeRequest>> entry) {
        return entry.getValue() != null && entry.getValue().size() > 0
                ? convert(entry.getValue())
                : convert(entry.getKey());
    }

    private DeveloperAttestationCheckInReport convert(Long developerId) {
        try {
            Developer developer = developerDAO.getById(developerId);
            return DeveloperAttestationCheckInReport.builder()
                    .developerName(developer.getName())
                    .build();
        } catch (EntityRetrievalException e) {
            return null;
        }
    }

    private DeveloperAttestationCheckInReport convert(List<ChangeRequest> crs) {
        ChangeRequest cr = getMostRecentChangeRequest(crs);
        return DeveloperAttestationCheckInReport.builder()
                .developerName(cr.getDeveloper().getName())
                .submittedDate(cr.getSubmittedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .currentStatusName(cr.getCurrentStatus().getChangeRequestStatusType().getName())
                .lastStatusChangeDate(cr.getCurrentStatus().getStatusChangeDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .relevantAcbs(cr.getCertificationBodies().stream()
                        .map(acb -> acb.getName())
                        .collect(Collectors.joining("; ")))
                .build();
    }

    private ChangeRequest getMostRecentChangeRequest(List<ChangeRequest> changeRequests) {
        if (changeRequests == null || changeRequests.size() == 0) {
            return null;
        } else if (changeRequests.size() == 1) {
            return changeRequests.get(0);
        } else {
            return changeRequests.stream()
                    .sorted((cr1, cr2) -> cr1.getSubmittedDate().compareTo(cr2.getSubmittedDate()) * -1)
                    .toList()
                    .get(0);
        }
    }

    private Map<Long, List<ChangeRequest>> getDevelopersWithAttestationChangeRequestsForMostRecentAttestationPeriod() throws EntityRetrievalException {
        Map<Long, List<ChangeRequest>> map = new HashMap<Long, List<ChangeRequest>>();
        List<Long> developerIds = getDeveloperIdsFromDeveloperAttestationReport();
        List<ChangeRequest> changeRequests = getAllAttestationChangeRequestsForMostRecentPastAttestationPeriod();

        for (Long developerId : developerIds) {
            map.put(developerId, changeRequests.stream()
                    .filter(cr -> cr.getDeveloper().getDeveloperId().equals(developerId))
                    .toList());
        }
        return map;
    }

    private List<ChangeRequest> getAllAttestationChangeRequestsForMostRecentPastAttestationPeriod() throws EntityRetrievalException {
        AttestationPeriod period = attestationManager.getMostRecentPastAttestationPeriod();
        return changeRequestDAO.getAll().stream()
                .filter(cr -> cr.getChangeRequestType().isAttestation()
                        && ((ChangeRequestAttestationSubmission) cr.getDetails()).getAttestationPeriod().equals(period))
                .toList();
    }

    private List<Long> getDeveloperIdsFromDeveloperAttestationReport() {
        return developerAttestationReportDataCollection.collect(certificationBodyDAO.findAll().stream().map(acb -> acb.getId()).toList()).stream()
                .map(developerAttestationReport -> developerAttestationReport.getDeveloperId())
                .toList();
    }

}
