package gov.healthit.chpl.scheduler.job.changerequest.presenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.util.DateUtil;

public class DownloadableAttestationPresenter extends ChangeRequestCsvPresenter {

    public DownloadableAttestationPresenter(Logger logger) {
       super(logger);
    }

    protected boolean isSupported(ChangeRequest data) {
        return data.getChangeRequestType().getName().equalsIgnoreCase(ChangeRequestType.ATTESTATION_TYPE);
    }

    protected List<String> generateHeaderValues() {
        return Stream.of(DEV_NAME_HEADING,
                DEV_CODE_HEADING,
                DEV_CONTACT_NAME_HEADING,
                DEV_CONTACT_EMAIL_HEADING,
                DEV_CONTACT_PHONE_HEADING,
                CR_TYPE_HEADING,
                CR_STATUS_HEADING,
                CR_CREATED_DATE_HEADING,
                CR_LAST_UPDATED_DATE_HEADING,
                CR_ACBS_HEADING,
                CR_QUESTION1_HEADING,
                CR_QUESTION2_HEADING,
                CR_QUESTION3_HEADING,
                CR_QUESTION4_HEADING,
                CR_QUESTION5_HEADING,
                CR_SUBMITTER_NAME_HEADING,
                CR_SUBMITTER_EMAIL_HEADING)
                .collect(Collectors.toList());
    }

    protected List<String> generateRowValue(ChangeRequest changeRequest) {
        List<String> result = new ArrayList<String>();
        result.add(changeRequest.getDeveloper().getName());
        result.add(changeRequest.getDeveloper().getDeveloperCode());
        if (changeRequest.getDeveloper().getContact() != null) {
            result.add(changeRequest.getDeveloper().getContact().getFullName());
            result.add(changeRequest.getDeveloper().getContact().getEmail());
            result.add(changeRequest.getDeveloper().getContact().getPhoneNumber());
        } else {
            result.add("");
            result.add("");
            result.add("");
        }
        result.add(changeRequest.getChangeRequestType().getName());
        result.add(changeRequest.getCurrentStatus().getChangeRequestStatusType().getName());
        result.add(DateUtil.formatInEasternTime(changeRequest.getSubmittedDate()));
        result.add(DateUtil.formatInEasternTime(changeRequest.getCurrentStatus().getStatusChangeDate()));
        result.add(changeRequest.getCertificationBodies().stream()
                .map(acb -> acb.getName())
                .collect(Collectors.joining(",")));
        ChangeRequestAttestationSubmission details = (ChangeRequestAttestationSubmission) changeRequest.getDetails();
        result.add(getResponse(details, CR_QUESTION1_HEADING));
        result.add(getResponse(details, CR_QUESTION2_HEADING));
        result.add(getResponse(details, CR_QUESTION3_HEADING));
        result.add(getResponse(details, CR_QUESTION4_HEADING));
        result.add(getResponse(details, CR_QUESTION5_HEADING));
        result.add(details.getSignature());
        result.add(details.getSignatureEmail());
        return result;
    }

    private String getResponse(ChangeRequestAttestationSubmission attestation, String conditionName) {
        Optional<String> attestationResponse = attestation.getAttestationResponses().stream()
            .filter(response -> response.getAttestation().getCondition()
                    .getName().equals(conditionName))
            .map(response -> response.getResponse().getResponse())
            .findFirst();
        if (!attestationResponse.isPresent()) {
            return "";
        }
        return attestationResponse.get();
    }

}
