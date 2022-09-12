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
import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchResult;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "developerAttestationCheckinReportJobLogger")
public class DeveloperAttestationCheckInReportDataCollector {

    private static final Long INFO_BLOCKING_CONDITION = 1L;
    private static final Long ASSURANCES_CONDITION = 2L;
    private static final Long COMMUNICATIONS_CONDITION = 3L;
    private static final Long API_CONDITION = 4L;
    private static final Long RWT_CONDITION = 5L;

    private AttestationManager attestationManager;
    private ChangeRequestDAO changeRequestDAO;
    private DeveloperAttestationPeriodCalculator developerAttestationPeriodCalculator;
    private DeveloperDAO developerDAO;
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDAO;

    public DeveloperAttestationCheckInReportDataCollector(AttestationManager attestationManager, ChangeRequestDAO changeRequestDAO,
            DeveloperAttestationPeriodCalculator developerAttestationPeriodCalculator,
            DeveloperDAO developerDAO, DeveloperCertificationBodyMapDAO developerCertificationBodyMapDAO) {
        this.attestationManager = attestationManager;
        this.changeRequestDAO = changeRequestDAO;
        this.developerAttestationPeriodCalculator = developerAttestationPeriodCalculator;
        this.developerDAO = developerDAO;
        this.developerCertificationBodyMapDAO = developerCertificationBodyMapDAO;
    }

    public List<DeveloperAttestationCheckInReport> collect() throws EntityRetrievalException {
        return getDevelopersWithAttestationChangeRequestsForMostRecentAttestationPeriod().entrySet().stream()
                .map(e -> convert(e))
                .sorted((o1, o2) -> o1.getDeveloperName().compareTo(o2.getDeveloperName()))
                .toList();

    }

    private DeveloperAttestationCheckInReport convert(Entry<Long, List<ChangeRequestSearchResult>> entry) {
        return entry.getValue() != null && entry.getValue().size() > 0
                ? convert(entry.getValue())
                : convert(entry.getKey());
    }

    private DeveloperAttestationCheckInReport convert(Long developerId) {
        try {
            Developer developer = developerDAO.getById(developerId);
            List<CertificationBody> acbs = developerCertificationBodyMapDAO.getCertificationBodiesForDeveloper(developerId);
            return DeveloperAttestationCheckInReport.builder()
                    .developerName(developer.getName())
                    .published(false)
                    .relevantAcbs(acbs.stream()
                            .map(acb -> acb.getName())
                            .collect(Collectors.joining("; ")))
                    .build();
        } catch (EntityRetrievalException e) {
            return null;
        }
    }

    private DeveloperAttestationCheckInReport convert(List<ChangeRequestSearchResult> crs) {
        ChangeRequestSearchResult crSearchResult = getMostRecentChangeRequest(crs);
        try {
            ChangeRequest cr = changeRequestDAO.get(crSearchResult.getId());

            return DeveloperAttestationCheckInReport.builder()
                .developerName(cr.getDeveloper().getName())
                .submittedDate(cr.getSubmittedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .published(cr.getDeveloper().getAttestations().stream()
                        .filter(pa -> pa.getAttestationPeriod().getId().equals(((ChangeRequestAttestationSubmission) cr.getDetails()).getAttestationPeriod().getId()))
                        .findAny()
                        .isPresent())
                .currentStatusName(cr.getCurrentStatus().getChangeRequestStatusType().getName())
                .lastStatusChangeDate(cr.getCurrentStatus().getStatusChangeDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .relevantAcbs(cr.getCertificationBodies().stream()
                        .map(acb -> acb.getName())
                        .collect(Collectors.joining("; ")))
                .informationBlockingResponse(getAttestationResponse(cr, INFO_BLOCKING_CONDITION))
                .informationBlockingNoncompliantResponse(getAttestationOptionalResponse(cr, INFO_BLOCKING_CONDITION))
                .assurancesResponse(getAttestationResponse(cr, ASSURANCES_CONDITION))
                .assurancesNoncompliantResponse(getAttestationOptionalResponse(cr, ASSURANCES_CONDITION))
                .communicationsResponse(getAttestationResponse(cr, COMMUNICATIONS_CONDITION))
                .communicationsNoncompliantResponse(getAttestationOptionalResponse(cr, COMMUNICATIONS_CONDITION))
                .rwtResponse(getAttestationResponse(cr, RWT_CONDITION))
                .rwtNoncompliantResponse(getAttestationOptionalResponse(cr, RWT_CONDITION))
                .apiResponse(getAttestationResponse(cr, API_CONDITION))
                .apiNoncompliantResponse(getAttestationOptionalResponse(cr, API_CONDITION))
                .signature(((ChangeRequestAttestationSubmission) cr.getDetails()).getSignature())
                .signatureEmail(((ChangeRequestAttestationSubmission) cr.getDetails()).getSignatureEmail())
                .build();
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Cannot get change request details for ID " + crSearchResult.getId());
            return null;
        }
    }

    private ChangeRequestSearchResult getMostRecentChangeRequest(List<ChangeRequestSearchResult> changeRequests) {
        if (changeRequests == null || changeRequests.size() == 0) {
            return null;
        } else if (changeRequests.size() == 1) {
            return changeRequests.get(0);
        } else {
            return changeRequests.stream()
                    .sorted((cr1, cr2) -> cr1.getSubmittedDateTime().compareTo(cr2.getSubmittedDateTime()) * -1)
                    .toList()
                    .get(0);
        }
    }

    private Map<Long, List<ChangeRequestSearchResult>> getDevelopersWithAttestationChangeRequestsForMostRecentAttestationPeriod() throws EntityRetrievalException {
        Map<Long, List<ChangeRequestSearchResult>> map = new HashMap<Long, List<ChangeRequestSearchResult>>();
        List<Long> developerIds = getDeveloperIdsWithActiveListingsDuringMostRecentPastAttestationPeriod();
        List<ChangeRequestSearchResult> changeRequests = getAllAttestationChangeRequestsForMostRecentPastAttestationPeriod();

        developerIds.forEach(developerId -> {
            List<ChangeRequestSearchResult> crs = changeRequests.stream()
                    .filter(cr -> cr.getDeveloper().getId().equals(developerId))
                    .toList();
            map.put(developerId, crs);
            LOGGER.info("Found {} total attestation change requests for developer {}", crs.size(), developerId);
        });
        return map;
    }

    private List<ChangeRequestSearchResult> getAllAttestationChangeRequestsForMostRecentPastAttestationPeriod() throws EntityRetrievalException {
        AttestationPeriod period = attestationManager.getMostRecentPastAttestationPeriod();
        LOGGER.info("Most recent past att period: {}", period.toString());

        List<ChangeRequestSearchResult> crs = changeRequestDAO.getAttestationChangeRequestsForPeriod(period.getId());
        LOGGER.info("Found {} total attestation change requests for the most recent past period.", crs.size());
        return crs;
    }

    private List<Long> getDeveloperIdsWithActiveListingsDuringMostRecentPastAttestationPeriod() {
        return developerAttestationPeriodCalculator.getDevelopersWithActiveListingsDuringMostRecentPastAttestationPeriod(LOGGER).stream()
                .map(developer -> developer.getId())
                .toList();
    }

    private String getAttestationResponse(ChangeRequest changeRequest, Long conditionId) {
        if (changeRequest == null || changeRequest.getDetails() == null) {
            return "";
        }
        ChangeRequestAttestationSubmission details = (ChangeRequestAttestationSubmission) changeRequest.getDetails();
        return details.getForm().formatResponse(conditionId);
    }

    private String getAttestationOptionalResponse(ChangeRequest changeRequest, Long conditionId) {
        if (changeRequest == null || changeRequest.getDetails() == null) {
            return "";
        }
        ChangeRequestAttestationSubmission details = (ChangeRequestAttestationSubmission) changeRequest.getDetails();
        return details.getForm().formatOptionalResponsesForCondition(conditionId);
    }
}
