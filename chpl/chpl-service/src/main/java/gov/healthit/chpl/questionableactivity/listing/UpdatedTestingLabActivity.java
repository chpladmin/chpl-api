package gov.healthit.chpl.questionableactivity.listing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityListing;

@Component
public class UpdatedTestingLabActivity implements ListingActivity {

    @Override
    public List<QuestionableActivityListing> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        QuestionableActivityListing activity = null;
        List<CertifiedProductTestingLab> origAtls = origListing.getTestingLabs();
        List<CertifiedProductTestingLab> newAtls = newListing.getTestingLabs();
        List<String> addedAtls = new ArrayList<String>();
        List<String> removedAtls = new ArrayList<String>();
        boolean found;
        for (CertifiedProductTestingLab oa : origAtls) {
            found = false;
            for (CertifiedProductTestingLab na : newAtls) {
                if (oa.getTestingLab().getName().equalsIgnoreCase(na.getTestingLab().getName())) {
                    found = true;
                }
            }
            if (!found) {
                removedAtls.add(oa.getTestingLab().getName());
            }
        }
        for (CertifiedProductTestingLab na : newAtls) {
            found = false;
            for (CertifiedProductTestingLab oa : origAtls) {
                if (oa.getTestingLab().getName().equalsIgnoreCase(na.getTestingLab().getName())) {
                    found = true;
                }
            }
            if (!found) {
                addedAtls.add(na.getTestingLab().getName());
            }
        }
        if (!addedAtls.isEmpty() || !removedAtls.isEmpty()) {
            activity = new QuestionableActivityListing();
            if (!removedAtls.isEmpty()) {
                activity.setBefore("Removed " + StringUtils.collectionToCommaDelimitedString(removedAtls));
            }
            if (!addedAtls.isEmpty()) {
                activity.setAfter("Added " + StringUtils.collectionToCommaDelimitedString(addedAtls));
            }
        }
        return Arrays.asList(activity);
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.TESTING_LAB_CHANGED;
    }

}
