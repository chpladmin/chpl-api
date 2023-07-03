package gov.healthit.chpl.scheduler.job.changerequest.presenter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.util.DateUtil;

public class PendingChangeRequestPresenter extends ChangeRequestCsvPresenter {
    private static final String ACB_APPLICABLE_TEXT = "Applicable";
    private static final String ACB_NOT_APPLICABLE_TEXT = "Not Applicable";

    private List<String> acbNames;
    private LocalDateTime currentDate;

    public PendingChangeRequestPresenter(Logger logger, List<String> acbNames) {
       super(logger);
       if (!CollectionUtils.isEmpty(acbNames)) {
           this.acbNames = acbNames.stream().sorted().collect(Collectors.toList());
       }
       this.currentDate = LocalDateTime.now();
    }

    protected boolean isSupported(ChangeRequest data) {
        return true;
    }

    protected List<String> generateHeaderValues() {
        List<String> headers = Stream.of(DEV_NAME_HEADING,
                DEV_CODE_HEADING,
                DEV_CONTACT_NAME_HEADING,
                DEV_CONTACT_EMAIL_HEADING,
                DEV_CONTACT_PHONE_HEADING,
                CR_TYPE_HEADING,
                CR_STATUS_HEADING,
                CR_CREATED_DATE_HEADING,
                CR_DAYS_OPEN_HEADING,
                CR_LAST_UPDATED_DATE_HEADING,
                CR_DAYS_IN_STATE_HEADING,
                CR_LAST_COMMENT_HEADING)
                .collect(Collectors.toList());
        acbNames.stream()
            .forEach(acbName -> headers.add(acbName));
        return headers;
    }

    protected List<String> generateRowValue(ChangeRequest changeRequest) {
        List<String> row = new ArrayList<String>();
        row.add(changeRequest.getDeveloper().getName());
        row.add(changeRequest.getDeveloper().getDeveloperCode());
        if (changeRequest.getDeveloper().getContact() != null) {
            row.add(changeRequest.getDeveloper().getContact().getFullName());
            row.add(changeRequest.getDeveloper().getContact().getEmail());
            row.add(changeRequest.getDeveloper().getContact().getPhoneNumber());
        } else {
            row.add("");
            row.add("");
            row.add("");
        }
        row.add(changeRequest.getChangeRequestType().getName());
        row.add(changeRequest.getCurrentStatus().getChangeRequestStatusType().getName());
        row.add(DateUtil.formatInEasternTime(changeRequest.getSubmittedDateTime()));
        row.add(calculateDaysOpen(changeRequest));
        row.add(DateUtil.formatInEasternTime(changeRequest.getCurrentStatus().getStatusChangeDateTime()));
        row.add(calculateDaysInState(changeRequest));
        row.add(changeRequest.getCurrentStatus().getComment());
        acbNames.stream()
            .forEach(acbName -> row.add(getAcbValueForRow(changeRequest, acbName)));
        return row;
    }

    private String calculateDaysOpen(ChangeRequest changeRequest) {
        long daysOpen = ChronoUnit.DAYS.between(changeRequest.getSubmittedDateTime(), currentDate);
        return Double.toString(daysOpen);
    }

    private String calculateDaysInState(ChangeRequest changeRequest) {
        long daysInState = ChronoUnit.DAYS.between(changeRequest.getCurrentStatus().getStatusChangeDateTime(), currentDate);
        return Double.toString(daysInState);
    }

    private String getAcbValueForRow(ChangeRequest changeRequest, String acbName) {
        Optional<CertificationBody> foundAcb = changeRequest.getCertificationBodies().stream()
            .filter(crAcb -> crAcb.getName().equals(acbName))
            .findAny();
        if (foundAcb.isPresent()) {
            return ACB_APPLICABLE_TEXT;
        }
        return ACB_NOT_APPLICABLE_TEXT;
    }
}
