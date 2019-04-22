package gov.healthit.chpl.questionableactivity;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.entity.CertificationStatusType;

/**
 * Parses activity on Listings to see if there are questionable activities.
 */
@Component
public class ListingQuestionableActivityProvider {

    /**
     * Create questionable activity if the listing was a 2011 listing.
     * @param origListing original listing
     * @param newListing new listing
     * @return questionable activity, if it exists
     */
    public QuestionableActivityListingDTO check2011EditionUpdated(
            final CertifiedProductSearchDetails origListing, final CertifiedProductSearchDetails newListing) {

        QuestionableActivityListingDTO activity = null;
        if (origListing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).equals("2011")) {
            activity = new QuestionableActivityListingDTO();
            activity.setBefore(null);
            activity.setAfter(null);
        }

        return activity;
    }

    /**
     * Create questionable activity if the current certification status was updated.
     * @param origListing original listing
     * @param newListing new listing
     * @return questionable activity if it exists
     */
    public QuestionableActivityListingDTO checkCertificationStatusUpdated(
            final CertifiedProductSearchDetails origListing, final CertifiedProductSearchDetails newListing) {

        QuestionableActivityListingDTO activity = null;
        CertificationStatusEvent prev = origListing.getCurrentStatus();
        CertificationStatusEvent curr = newListing.getCurrentStatus();
        if (!prev.getStatus().getId().equals(curr.getStatus().getId())) {
            activity = new QuestionableActivityListingDTO();
            activity.setBefore(prev.getStatus().getName());
            activity.setAfter(curr.getStatus().getName());
            activity.setCertificationStatusChangeReason(curr.getReason());
        }

        return activity;
    }

    /**
     * Create questionable activity if the historical certification statuses or dates were updated.
     * Sorts certification status events by date, earliest first, then compares them, incrementing
     * through them with the "earlier" date getting moved up. If both are equal both move.
     * Does not compare "latest" events, as those are the "current" values, and compared in other functions.
     * @param origListing original listing
     * @param newListing new listing
     * @return questionable activity if it exists
     */
    public QuestionableActivityListingDTO checkCertificationStatusHistoryUpdated(
            final CertifiedProductSearchDetails origListing, final CertifiedProductSearchDetails newListing) {

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

        return activity;
    }


    /**
     * Create questionable activity if the current certification status event date was updated.
     * @param origListing original listing
     * @param newListing new listing
     * @return questionable activity if it exists
     */
    public QuestionableActivityListingDTO checkCertificationStatusDateUpdated(
            final CertifiedProductSearchDetails origListing, final CertifiedProductSearchDetails newListing) {

        QuestionableActivityListingDTO activity = null;
        CertificationStatusEvent prev = origListing.getCurrentStatus();
        CertificationStatusEvent curr = newListing.getCurrentStatus();
        Calendar displayDate = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
        if (prev.getEventDate().longValue() != curr.getEventDate().longValue()) {
            activity = new QuestionableActivityListingDTO();
            displayDate.setTimeInMillis(prev.getEventDate());
            activity.setBefore(displayDate.getTime().toString());
            displayDate.setTimeInMillis(curr.getEventDate());
            activity.setAfter(displayDate.getTime().toString());
            activity.setCertificationStatusChangeReason(curr.getReason());
        }

        return activity;
    }

    /**
     * questionable only if the certification status has updated
     * to the supplied updateTo value.
     * @param updateTo status to check against
     * @param origListing original listing
     * @param newListing new listing
     * @return activity if it is questionable
     */
    public QuestionableActivityListingDTO checkCertificationStatusUpdated(
            final CertificationStatusType updateTo, final CertifiedProductSearchDetails origListing,
            final CertifiedProductSearchDetails newListing) {

        QuestionableActivityListingDTO activity = null;
        if (!origListing.getCurrentStatus().getStatus().getName().equals(updateTo.getName())
                && newListing.getCurrentStatus().getStatus().getName().equals(updateTo.getName())) {
            activity = new QuestionableActivityListingDTO();
            activity.setBefore(origListing.getCurrentStatus().getStatus().getName());
            activity.setAfter(newListing.getCurrentStatus().getStatus().getName());
            activity.setCertificationStatusChangeReason(newListing.getCurrentStatus().getReason());
        }

        return activity;
    }

    /**
     * Create questionable activity if CQMs were added.
     * @param origListing original listing
     * @param newListing new listing
     * @return questionable activity, if it exists
     */
    public List<QuestionableActivityListingDTO> checkCqmsAdded(
            final CertifiedProductSearchDetails origListing, final CertifiedProductSearchDetails newListing) {

        List<QuestionableActivityListingDTO> cqmAddedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if (origListing.getCqmResults() != null && origListing.getCqmResults().size() > 0
                && newListing.getCqmResults() != null && newListing.getCqmResults().size() > 0) {
            //all cqms are in the details so find the same one in the orig and new objects
            //based on cms id and compare the success boolean to see if one was added
            for (CQMResultDetails origCqm : origListing.getCqmResults()) {
                for (CQMResultDetails newCqm : newListing.getCqmResults()) {
                    if (StringUtils.isEmpty(newCqm.getCmsId())
                            && StringUtils.isEmpty(origCqm.getCmsId())
                            && !StringUtils.isEmpty(newCqm.getNqfNumber())
                            && !StringUtils.isEmpty(origCqm.getNqfNumber())
                            && !newCqm.getNqfNumber().equals("N/A") && !origCqm.getNqfNumber().equals("N/A")
                            && newCqm.getNqfNumber().equals(origCqm.getNqfNumber())) {
                        // NQF is the same if the NQF numbers are equal
                        if (!origCqm.isSuccess() && newCqm.isSuccess()) {
                            //orig did not have this cqm but new does so it was added
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setBefore(null);
                            activity.setAfter(newCqm.getCmsId() != null ? newCqm.getCmsId() : newCqm.getNqfNumber());
                            cqmAddedActivities.add(activity);
                        }
                        break;
                    } else if (newCqm.getCmsId() != null && origCqm.getCmsId() != null
                            && newCqm.getCmsId().equals(origCqm.getCmsId())) {
                        // CMS is the same if the CMS ID and version is equal
                        if (!origCqm.isSuccess() && newCqm.isSuccess()) {
                            //orig did not have this cqm but new does so it was added
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setBefore(null);
                            activity.setAfter(newCqm.getCmsId() != null ? newCqm.getCmsId() : newCqm.getNqfNumber());
                            cqmAddedActivities.add(activity);
                        }
                        break;
                    }
                }
            }
        }
        return cqmAddedActivities;
    }

    /**
     * Create questionable activity if CQMs were removed.
     * @param origListing original listing
     * @param newListing new listing
     * @return questionable activity, if it exists
     */
    public List<QuestionableActivityListingDTO> checkCqmsRemoved(
            final CertifiedProductSearchDetails origListing, final CertifiedProductSearchDetails newListing) {

        List<QuestionableActivityListingDTO> cqmRemovedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if (origListing.getCqmResults() != null && origListing.getCqmResults().size() > 0
                && newListing.getCqmResults() != null && newListing.getCqmResults().size() > 0) {
            //all cqms are in the details so find the same one in the orig and new objects
            //based on cms id and compare the success boolean to see if one was removed
            for (CQMResultDetails origCqm : origListing.getCqmResults()) {
                for (CQMResultDetails newCqm : newListing.getCqmResults()) {
                    if (StringUtils.isEmpty(newCqm.getCmsId())
                            && StringUtils.isEmpty(origCqm.getCmsId())
                            && !StringUtils.isEmpty(newCqm.getNqfNumber())
                            && !StringUtils.isEmpty(origCqm.getNqfNumber())
                            && !newCqm.getNqfNumber().equals("N/A") && !origCqm.getNqfNumber().equals("N/A")
                            && newCqm.getNqfNumber().equals(origCqm.getNqfNumber())) {
                        // NQF is the same if the NQF numbers are equal
                        if (origCqm.isSuccess() && !newCqm.isSuccess()) {
                            //orig did have this cqm but new does not so it was removed
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setBefore(
                                    origCqm.getCmsId() != null ? origCqm.getCmsId() : origCqm.getNqfNumber());
                            activity.setAfter(null);
                            cqmRemovedActivities.add(activity);
                        }
                        break;
                    } else if (newCqm.getCmsId() != null && origCqm.getCmsId() != null
                            && newCqm.getCmsId().equals(origCqm.getCmsId())) {
                        // CMS is the same if the CMS ID and version is equal
                        if (origCqm.isSuccess() && !newCqm.isSuccess()) {
                            //orig did not have this cqm but new does so it was added
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setBefore(
                                    origCqm.getCmsId() != null ? origCqm.getCmsId() : origCqm.getNqfNumber());
                            activity.setAfter(null);
                            cqmRemovedActivities.add(activity);
                        } else if (origCqm.getSuccessVersions() != null && origCqm.getSuccessVersions().size() > 0
                                && (newCqm.getSuccessVersions() == null || newCqm.getSuccessVersions().size() == 0)) {
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setBefore(
                                    origCqm.getCmsId() != null ? origCqm.getCmsId() : origCqm.getNqfNumber());
                            activity.setAfter(null);
                            cqmRemovedActivities.add(activity);
                        }
                        break;
                    }
                }
            }
        }
        return cqmRemovedActivities;
    }

    /**
     * Create questionable activity if certification criteria were added.
     * @param origListing original listing
     * @param newListing new listing
     * @return questionable activity, if it exists
     */
    public List<QuestionableActivityListingDTO> checkCertificationsAdded(
            final CertifiedProductSearchDetails origListing, final CertifiedProductSearchDetails newListing) {

        List<QuestionableActivityListingDTO> certAddedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if (origListing.getCertificationResults() != null && origListing.getCertificationResults().size() > 0
                && newListing.getCertificationResults() != null && newListing.getCertificationResults().size() > 0) {
            //all cert results are in the details so find the same one in the orig and new objects
            //based on number and compare the success boolean to see if one was added
            for (CertificationResult origCertResult : origListing.getCertificationResults()) {
                for (CertificationResult newCertResult : newListing.getCertificationResults()) {
                    if (origCertResult.getNumber().equals(newCertResult.getNumber())) {
                        if (!origCertResult.isSuccess() && newCertResult.isSuccess()) {
                            //orig did not have this cert result but new does so it was added
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setBefore(null);
                            activity.setAfter(newCertResult.getNumber());
                            certAddedActivities.add(activity);
                        }
                        break;
                    }
                }
            }
        }

        return certAddedActivities;
    }

    /**
     * Create questionable activity if it has removal of certification criteria.
     * @param origListing original listing
     * @param newListing new listing
     * @return questionable activity, if it is
     */
    public List<QuestionableActivityListingDTO> checkCertificationsRemoved(
            final CertifiedProductSearchDetails origListing, final CertifiedProductSearchDetails newListing) {

        List<QuestionableActivityListingDTO> certRemovedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if (origListing.getCertificationResults() != null && origListing.getCertificationResults().size() > 0
                && newListing.getCertificationResults() != null && newListing.getCertificationResults().size() > 0) {
            //all cert results are in the details so find the same one in the orig and new objects
            //based on number and compare the success boolean to see if one was removed
            for (CertificationResult origCertResult : origListing.getCertificationResults()) {
                for (CertificationResult newCertResult : newListing.getCertificationResults()) {
                    if (origCertResult.getNumber().equals(newCertResult.getNumber())) {
                        if (origCertResult.isSuccess() && !newCertResult.isSuccess()) {
                            //orig did have this cert result but new does not so it was removed
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setBefore(origCertResult.getNumber());
                            activity.setAfter(null);
                            certRemovedActivities.add(activity);
                        }
                        break;
                    }
                }
            }
        }
        return certRemovedActivities;
    }

    /**
     * Check to see if activity has has deletion of surveillance.
     * @param origListing original listing
     * @param newListing new listing
     * @return activity if it is questionable
     */
    public QuestionableActivityListingDTO checkSurveillanceDeleted(
            final CertifiedProductSearchDetails origListing, final CertifiedProductSearchDetails newListing) {

        QuestionableActivityListingDTO activity = null;
        if (origListing.getSurveillance() != null && origListing.getSurveillance().size() > 0
                && (newListing.getSurveillance() == null
                || newListing.getSurveillance().size() < origListing.getSurveillance().size())) {

            activity = new QuestionableActivityListingDTO();
            activity.setBefore(null);
            activity.setAfter(null);
        }
        return activity;
    }

    /**
     * Check to see if activity has any changes in ATLs.
     * @param origListing original listing
     * @param newListing new listing
     * @return activity if it is questionable
     */
    public QuestionableActivityListingDTO checkTestingLabChanged(
            final CertifiedProductSearchDetails origListing, final CertifiedProductSearchDetails newListing) {
        QuestionableActivityListingDTO activity = null;
        List<CertifiedProductTestingLab> origAtls = origListing.getTestingLabs();
        List<CertifiedProductTestingLab> newAtls = newListing.getTestingLabs();
        List<String> addedAtls = new ArrayList<String>();
        List<String> removedAtls = new ArrayList<String>();
        boolean found;
        for (CertifiedProductTestingLab oa : origAtls) {
            found = false;
            for (CertifiedProductTestingLab na : newAtls) {
                if (oa.getTestingLabName().equalsIgnoreCase(na.getTestingLabName())) {
                    found = true;
                }
            }
            if (!found) {
                removedAtls.add(oa.getTestingLabName());
            }
        }
        for (CertifiedProductTestingLab na : newAtls) {
            found = false;
            for (CertifiedProductTestingLab oa : origAtls) {
                if (oa.getTestingLabName().equalsIgnoreCase(na.getTestingLabName())) {
                    found = true;
                }
            }
            if (!found) {
                addedAtls.add(na.getTestingLabName());
            }
        }
        if (!addedAtls.isEmpty() || !removedAtls.isEmpty()) {
            activity = new QuestionableActivityListingDTO();
            if (!removedAtls.isEmpty()) {
                activity.setBefore("Removed " + StringUtils.collectionToCommaDelimitedString(removedAtls));
            }
            if (!addedAtls.isEmpty()) {
                activity.setAfter("Added " + StringUtils.collectionToCommaDelimitedString(addedAtls));
            }
        }
        return activity;
    }

    static class CertificationStatusEventComparator implements Comparator<CertificationStatusEvent>, Serializable {
        private static final long serialVersionUID = 1315674742856524797L;

        @Override
        public int compare(final CertificationStatusEvent a, final CertificationStatusEvent b) {
            return a.getEventDate().longValue() < b.getEventDate().longValue()
                    ? -1 : a.getEventDate().longValue() == b.getEventDate().longValue() ? 0 : 1;
        }
    }
}
