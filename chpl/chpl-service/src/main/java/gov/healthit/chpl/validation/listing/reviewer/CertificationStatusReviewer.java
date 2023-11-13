package gov.healthit.chpl.validation.listing.reviewer;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class CertificationStatusReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public CertificationStatusReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        CertificationStatusEvent earliestStatus = listing.getOldestStatus();
        if (earliestStatus != null) {
            CertificationStatus earliestStatusInUpdate = listing.getOldestStatus().getStatus();
            if (earliestStatusInUpdate == null
                    || !CertificationStatusType.Active.getName().equals(earliestStatusInUpdate.getName())) {
                String msg = msgUtil.getMessage(
                        "listing.firstStatusNotActive", CertificationStatusType.Active.getName());
                listing.addBusinessErrorMessage(msg);
            }
        }
        doStatusesRepeat(listing);
    }

    private void doStatusesRepeat(CertifiedProductSearchDetails listing) {
        List<CertificationStatusEvent> sortedStatusEvents = listing.getCertificationEvents().stream()
                .sorted((o1, o2) -> o1.getEventDate().compareTo(o2.getEventDate()))
                .collect(Collectors.toList());

        CertificationStatusEvent lastStatusEvent = null;
        for (CertificationStatusEvent statusEvent : sortedStatusEvents) {
            if (lastStatusEvent != null) {
                if (lastStatusEvent.getStatus().getId().equals(statusEvent.getStatus().getId())) {
                    listing.addBusinessErrorMessage(msgUtil.getMessage("listing.duplicateStatus"));
                }
            }
            lastStatusEvent = statusEvent;
        }
    }
}
