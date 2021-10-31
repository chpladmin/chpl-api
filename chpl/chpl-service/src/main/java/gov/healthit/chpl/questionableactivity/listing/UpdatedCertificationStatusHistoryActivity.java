package gov.healthit.chpl.questionableactivity.listing;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;

@Component
public class UpdatedCertificationStatusHistoryActivity extends ListingActivity {

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        QuestionableActivityListingDTO activity = null;
        List<CertificationStatusEvent> prevEvents = origListing.getCertificationEvents();
        List<CertificationStatusEvent> currEvents = newListing.getCertificationEvents();
        Collections.sort(prevEvents, new CertificationStatusEventComparator());
        Collections.sort(currEvents, new CertificationStatusEventComparator());
        int p = 0, c = 0;
        List<String> beforeRes = new ArrayList<String>();
        List<String> afterRes = new ArrayList<String>();
        List<String> reasRes = new ArrayList<String>();
        String pRes, cRes, reas;
        CertificationStatusEvent pEvent, cEvent;
        Calendar displayDate = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
        while (p < prevEvents.size() - 1 && c < currEvents.size() - 1) {
            pRes = "";
            cRes = "";
            reas = "";
            pEvent = prevEvents.get(p);
            cEvent = currEvents.get(c);
            if (pEvent.getEventDate().longValue() < cEvent.getEventDate().longValue()) {
                if (pEvent.getStatus().getId().equals(cEvent.getStatus().getId())) {
                    displayDate.setTimeInMillis(pEvent.getEventDate());
                    pRes = pEvent.getStatus().getName() + " (" + displayDate.getTime().toString() + ")";
                    displayDate.setTimeInMillis(cEvent.getEventDate());
                    cRes = cEvent.getStatus().getName() + " (" + displayDate.getTime().toString() + ")";
                    reas = cEvent.getReason();
                    p += 1;
                    c += 1;
                } else {
                    displayDate.setTimeInMillis(pEvent.getEventDate());
                    pRes = pEvent.getStatus().getName() + " (" + displayDate.getTime().toString() + ")";
                    cRes = "Removed";
                    reas = pEvent.getReason();
                    p += 1;
                }
            } else if (pEvent.getEventDate().longValue() > cEvent.getEventDate().longValue()) {
                if (pEvent.getStatus().getId().equals(cEvent.getStatus().getId())) {
                    displayDate.setTimeInMillis(pEvent.getEventDate());
                    pRes = pEvent.getStatus().getName() + " (" + displayDate.getTime().toString() + ")";
                    displayDate.setTimeInMillis(cEvent.getEventDate());
                    cRes = cEvent.getStatus().getName() + " (" + displayDate.getTime().toString() + ")";
                    reas = cEvent.getReason();
                    p += 1;
                    c += 1;
                } else {
                    pRes = "Added";
                    displayDate.setTimeInMillis(cEvent.getEventDate());
                    cRes = cEvent.getStatus().getName() + " (" + displayDate.getTime().toString() + ")";
                    reas = cEvent.getReason();
                    c += 1;
                }
            } else if (!pEvent.getStatus().getId().equals(cEvent.getStatus().getId())) {
                displayDate.setTimeInMillis(pEvent.getEventDate());
                pRes = pEvent.getStatus().getName() + " (" + displayDate.getTime().toString() + ")";
                displayDate.setTimeInMillis(cEvent.getEventDate());
                cRes = cEvent.getStatus().getName() + " (" + displayDate.getTime().toString() + ")";
                reas = cEvent.getReason();
                p += 1;
                c += 1;
            } else {
                p += 1;
                c += 1;
            }
            if (pRes.length() > 0 || cRes.length() > 0) {
                beforeRes.add(pRes);
                afterRes.add(cRes);
                reasRes.add(reas);
            }
        }
        while (p < prevEvents.size() - 1) {
            pEvent = prevEvents.get(p);
            displayDate.setTimeInMillis(pEvent.getEventDate());
            beforeRes.add(pEvent.getStatus().getName() + " (" + displayDate.getTime().toString() + ")");
            afterRes.add("Removed");
            reasRes.add(pEvent.getReason());
            p += 1;
        }
        while (c < currEvents.size() - 1) {
            cEvent = currEvents.get(c);
            beforeRes.add("Added");
            displayDate.setTimeInMillis(cEvent.getEventDate());
            afterRes.add(cEvent.getStatus().getName() + " (" + displayDate.getTime().toString() + ")");
            reasRes.add(cEvent.getReason());
            c += 1;
        }
        if (beforeRes.size() > 0 || afterRes.size() > 0) {
            activity = new QuestionableActivityListingDTO();
            activity.setBefore(beforeRes.toString());
            activity.setAfter(afterRes.toString());
            activity.setCertificationStatusChangeReason(reasRes.toString());
        }

        return Arrays.asList(activity);
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_HISTORY;
    }

    static class CertificationStatusEventComparator implements Comparator<CertificationStatusEvent>, Serializable {
        private static final long serialVersionUID = 1315674742856524797L;

        @Override
        public int compare(CertificationStatusEvent a, CertificationStatusEvent b) {
            return a.getEventDate().longValue() < b.getEventDate().longValue()
                    ? -1
                            : a.getEventDate().longValue() == b.getEventDate().longValue() ? 0 : 1;
        }
    }

}
