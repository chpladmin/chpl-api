package gov.healthit.chpl.scheduler.job.changerequest.presenter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestListingUrl;
import gov.healthit.chpl.util.DateUtil;

public abstract class DownloadableListingUrlPresenter extends ChangeRequestCsvPresenter {

    public DownloadableListingUrlPresenter(Logger logger) {
       super(logger);
    }

    protected abstract boolean isSupported(ChangeRequest data);

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
                CR_CHPL_PRODUCT_NUMBER,
                CR_URL_CHANGE_HEADING)
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
        result.add(DateUtil.formatInEasternTime(changeRequest.getSubmittedDateTime()));
        result.add(DateUtil.formatInEasternTime(changeRequest.getCurrentStatus().getStatusChangeDateTime()));
        result.add(changeRequest.getCertificationBodies().stream()
                .map(acb -> acb.getName())
                .collect(Collectors.joining("; ")));
        result.add(((ChangeRequestListingUrl) changeRequest.getDetails()).getListing().getChplProductNumber());
        result.add(((ChangeRequestListingUrl) changeRequest.getDetails()).getUrl());
        return result;
    }

}
