package gov.healthit.chpl.questionableActivity;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.entity.CertificationStatusType;

/**
 * Parses activity on Listings to see if there are questionable activities.
 * @author alarned
 *
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
        if (origListing.getCertificationEdition().get("name").equals("2011")) {
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
     * Create questionable activity if the historical certification statuses were updated.
     * @param origListing original listing
     * @param newListing new listing
     * @return questionable activity if it exists
     */
    public QuestionableActivityListingDTO checkCertificationStatusHistoryUpdated(
            final CertifiedProductSearchDetails origListing, final CertifiedProductSearchDetails newListing) {

        QuestionableActivityListingDTO activity = null;
        //TODO: Logic

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
        if (prev.getEventDate() != curr.getEventDate()) {
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
     * Create questionable activity if the historical certification status event date was updated.
     * @param origListing original listing
     * @param newListing new listing
     * @return questionable activity if it exists
     */
    public QuestionableActivityListingDTO checkCertificationStatusDateHistoryUpdated(
            final CertifiedProductSearchDetails origListing, final CertifiedProductSearchDetails newListing) {

        QuestionableActivityListingDTO activity = null;
        //TODO: Logic
        
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
                        if (origCqm.isSuccess() == Boolean.FALSE && newCqm.isSuccess() == Boolean.TRUE) {
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
                        if (origCqm.isSuccess() == Boolean.FALSE && newCqm.isSuccess() == Boolean.TRUE) {
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
                        if (origCqm.isSuccess() == Boolean.TRUE && newCqm.isSuccess() == Boolean.FALSE) {
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
                        if (origCqm.isSuccess() == Boolean.TRUE && newCqm.isSuccess() == Boolean.FALSE) {
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
                        if (origCertResult.isSuccess() == Boolean.FALSE && newCertResult.isSuccess() == Boolean.TRUE) {
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
                        if (origCertResult.isSuccess() == Boolean.TRUE && newCertResult.isSuccess() == Boolean.FALSE) {
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
}
