package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.core.tools.picocli.CommandLine.Command;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationSubmission;
import gov.healthit.chpl.attestation.manager.AttestationManager;
import gov.healthit.chpl.attestation.manager.AttestationSubmissionService;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestAttestationService;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchManager;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchRequest;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchResult;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.util.DateUtil;

@Component
public class CheckInReportSourceService {
	
	private AttestationSubmissionService attestationManager;
	private ChangeRequestSearchManager changeRequestSearchManager;
	private ChangeRequestManager changeRequestManager;
	
	public CheckInReportSourceService(AttestationSubmissionService attestationManager, 
			ChangeRequestSearchManager changeRequestSearchManager,
			ChangeRequestManager changeRequestManager) {
		this.attestationManager = attestationManager;
		this.changeRequestSearchManager = changeRequestSearchManager;
		this.changeRequestManager = changeRequestManager;
	}
	
	public CheckInAttestation getCheckinReport(Developer developer, AttestationPeriod period) {
		AttestationSubmission fromDeveloper = getMostRecentAttestationSubmission(developer, period);
		ChangeRequest fromChangeRequest = getMostRecentChangeRequest(developer, period);
		
		if (fromDeveloper == null && fromChangeRequest == null) {
			return null;
		} else if (fromDeveloper != null && fromChangeRequest == null) {
			return CheckInAttestation.builder()
					.attestationSubmission(fromDeveloper)
					.source(CheckInReportSource.DEVELOPER_ATTESTATION)
					.build();
		} else if (fromDeveloper == null && fromChangeRequest != null) {
			return CheckInAttestation.builder()
					.changeRequest(fromChangeRequest)
					.source(CheckInReportSource.CHANGE_REQUEST)
					.build();
		} else if (fromDeveloper.getDatePublished().atTime(LocalTime.MIDNIGHT).isAfter(fromChangeRequest.getSubmittedDateTime())) {
			return CheckInAttestation.builder()
					.attestationSubmission(fromDeveloper)
					.source(CheckInReportSource.DEVELOPER_ATTESTATION)
					.build();
		} else {
			return CheckInAttestation.builder()
					.changeRequest(fromChangeRequest)
					.source(CheckInReportSource.CHANGE_REQUEST)
					.build();
		}
	}
		
	private AttestationSubmission getMostRecentAttestationSubmission(Developer developer, AttestationPeriod period) {
		return attestationManager.getAttestationSubmissions(developer.getId()).stream()
				.filter(att -> att.getAttestationPeriod().getId().equals(period.getId()))
				.sorted((result1, result2) -> result1.getDatePublished().compareTo(result2.getDatePublished()) * -1)
				.findFirst()
				.orElse(null);
	}

	private ChangeRequest getMostRecentChangeRequest(Developer developer, AttestationPeriod period) {
		ChangeRequestSearchRequest request = ChangeRequestSearchRequest.builder()
				.developerId(developer.getId())
				.changeRequestTypeNames(Set.of(ChangeRequestType.ATTESTATION_TYPE.toString()))
				.build();
		
		try {
			return changeRequestSearchManager.searchChangeRequests(request).getResults().stream()
					.sorted((result1, result2) -> result1.getSubmittedDateTime().compareTo(result2.getSubmittedDateTime()) * -1)
					.map(result -> {
						try {
							//return changeRequestAttestationService.getByChangeRequestId(result.getId());
							return changeRequestManager.getChangeRequest(result.getId());
						} catch (Exception e) {
							//TODO:  NEED TO MAKE SURE WE LOG THIS
							return null;	
						}
					})
					.filter(cr -> cr != null && ((ChangeRequestAttestationSubmission)cr.getDetails()).getAttestationPeriod().getId().equals(period.getId()))
					.findFirst()
					.orElse(null);
		} catch (Exception e) {
			//TODO:  NEED TO MAKE SURE WE LOG THIS
			return null;
		}	
	}
}
