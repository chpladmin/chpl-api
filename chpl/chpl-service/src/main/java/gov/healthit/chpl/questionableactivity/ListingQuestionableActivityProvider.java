package gov.healthit.chpl.questionableactivity;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.SpecialProperties;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;

@Component
public class ListingQuestionableActivityProvider {
    private static Logger LOGGER = LogManager.getLogger(ListingQuestionableActivityProvider.class);
    private static String B3_CHANGE_DATE = "questionableActivity.b3ChangeDate";
    private static String B3_CRITERIA_NUMER = "170.315 (b)(3)";
    private CertificationCriterion d2Criterion;
    private CertificationCriterion d3Criterion;
    private CertificationCriterion d10Criterion;

    private FF4j ff4j;
    private Environment env;
    private CertificationCriterionDAO certificationCriterionDAO;
    private SpecialProperties specialProperties;
    private CertificationCriterionService criterionService;

    @Autowired
    public ListingQuestionableActivityProvider(CertificationCriterionDAO certificationCriterionDAO, FF4j ff4j, Environment env,
            SpecialProperties specialProperties, CertificationCriterionService criterionService) {
        this.certificationCriterionDAO = certificationCriterionDAO;
        this.ff4j = ff4j;
        this.env = env;
        this.specialProperties = specialProperties;
        this.criterionService = criterionService;
    }

    @PostConstruct
    public void postConstruct() throws EntityRetrievalException {
        d2Criterion = new CertificationCriterion(
                certificationCriterionDAO.getById(Long.parseLong(env.getProperty("criterion.170_315_d_2"))));
        d3Criterion = new CertificationCriterion(
                certificationCriterionDAO.getById(Long.parseLong(env.getProperty("criterion.170_315_d_3"))));
        d10Criterion = new CertificationCriterion(
                certificationCriterionDAO.getById(Long.parseLong(env.getProperty("criterion.170_315_d_10"))));
    }

    public QuestionableActivityListingDTO check2011EditionUpdated(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {

        QuestionableActivityListingDTO activity = null;
        if (origListing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).equals("2011")) {
            activity = new QuestionableActivityListingDTO();
            activity.setBefore(null);
            activity.setAfter(null);
        }

        return activity;
    }

    public QuestionableActivityListingDTO check2014EditionUpdated(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        if (!ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)) {
            return null;
        }

        QuestionableActivityListingDTO activity = null;
        if (origListing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).equals("2014")) {
            activity = new QuestionableActivityListingDTO();
            activity.setBefore(null);
            activity.setAfter(null);
        }

        return activity;
    }

    public QuestionableActivityListingDTO checkCertificationStatusUpdated(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {

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

    public QuestionableActivityListingDTO checkCertificationStatusHistoryUpdated(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {

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

    public QuestionableActivityListingDTO checkCertificationStatusDateUpdated(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {

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

    public QuestionableActivityListingDTO checkCertificationStatusUpdated(
            CertificationStatusType updateTo, CertifiedProductSearchDetails origListing,
            CertifiedProductSearchDetails newListing) {

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

    public List<QuestionableActivityListingDTO> checkCqmsAdded(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {

        List<QuestionableActivityListingDTO> cqmAddedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if (origListing.getCqmResults() != null && origListing.getCqmResults().size() > 0
                && newListing.getCqmResults() != null && newListing.getCqmResults().size() > 0) {
            // all cqms are in the details so find the same one in the orig and new objects
            // based on cms id and compare the success boolean to see if one was added
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
                            // orig did not have this cqm but new does so it was added
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
                            // orig did not have this cqm but new does so it was added
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

    public List<QuestionableActivityListingDTO> checkCqmsRemoved(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {

        List<QuestionableActivityListingDTO> cqmRemovedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if (origListing.getCqmResults() != null && origListing.getCqmResults().size() > 0
                && newListing.getCqmResults() != null && newListing.getCqmResults().size() > 0) {
            // all cqms are in the details so find the same one in the orig and new objects
            // based on cms id and compare the success boolean to see if one was removed
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
                            // orig did have this cqm but new does not so it was removed
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
                            // orig did not have this cqm but new does so it was added
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

    public List<QuestionableActivityListingDTO> checkCertificationsAdded(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {

        List<QuestionableActivityListingDTO> certAddedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if (origListing.getCertificationResults() != null && origListing.getCertificationResults().size() > 0
                && newListing.getCertificationResults() != null && newListing.getCertificationResults().size() > 0) {
            // all cert results are in the details so find the same one in the orig and new objects
            // based on number and compare the success boolean to see if one was added
            for (CertificationResult origCertResult : origListing.getCertificationResults()) {
                for (CertificationResult newCertResult : newListing.getCertificationResults()) {
                    if (origCertResult.getCriterion().getId().equals(newCertResult.getCriterion().getId())) {
                        if (!origCertResult.isSuccess() && newCertResult.isSuccess()) {
                            // orig did not have this cert result but new does so it was added
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setBefore(null);
                            activity.setAfter(criterionService.formatCriteriaNumber(newCertResult.getCriterion()));
                            certAddedActivities.add(activity);
                        }
                        break;
                    }
                }
            }
        }

        return certAddedActivities;
    }

    public List<QuestionableActivityListingDTO> checkCertificationsRemoved(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {

        List<QuestionableActivityListingDTO> certRemovedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if (origListing.getCertificationResults() != null && origListing.getCertificationResults().size() > 0
                && newListing.getCertificationResults() != null && newListing.getCertificationResults().size() > 0) {
            // all cert results are in the details so find the same one in the orig and new objects
            // based on number and compare the success boolean to see if one was removed
            for (CertificationResult origCertResult : origListing.getCertificationResults()) {
                for (CertificationResult newCertResult : newListing.getCertificationResults()) {
                    if (origCertResult.getCriterion().getId().equals(newCertResult.getCriterion().getId())) {
                        if (origCertResult.isSuccess() && !newCertResult.isSuccess()) {
                            // orig did have this cert result but new does not so it was removed
                            QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                            activity.setBefore(criterionService.formatCriteriaNumber(origCertResult.getCriterion()));
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

    public QuestionableActivityListingDTO checkSurveillanceDeleted(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {

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

    public QuestionableActivityListingDTO checkTestingLabChanged(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
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

    public QuestionableActivityListingDTO checkCriteriaB3WithoutIcsChangedOnEdit(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        QuestionableActivityListingDTO activity = null;
        CertificationResult originalB3 = getB3Criteria(origListing);
        CertificationResult newB3 = getB3Criteria(newListing);
        Date b3ChangeDate = null;
        Date currentDate = new Date();
        try {
            b3ChangeDate = getB3ChangeDate();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }

        if (currentDate.after(b3ChangeDate)
                && isB3CriteriaNew(originalB3, newB3)
                && !hasICS(newListing)) {
            activity = new QuestionableActivityListingDTO();
            activity.setAfter(B3_CRITERIA_NUMER);
        }
        return activity;
    }

    public QuestionableActivityListingDTO checkCriteriaB3WithIcsChangedOnEdit(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        QuestionableActivityListingDTO activity = null;
        CertificationResult originalB3 = getB3Criteria(origListing);
        CertificationResult newB3 = getB3Criteria(newListing);
        Date b3ChangeDate = null;
        Date currentDate = new Date();
        try {
            b3ChangeDate = getB3ChangeDate();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }

        if (currentDate.after(b3ChangeDate)
                && isB3CriteriaNew(originalB3, newB3)
                && hasICS(newListing)) {
            activity = new QuestionableActivityListingDTO();
            activity.setAfter(B3_CRITERIA_NUMER);
        }
        return activity;
    }

    public QuestionableActivityListingDTO checkIcsChangedWithCriteriaB3OnEdit(
            CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        QuestionableActivityListingDTO activity = null;
        CertificationResult newB3 = getB3Criteria(newListing);
        if (newB3 == null) {
            // Can't check anything regarding B3, since it does not exist
            return null;
        }
        Date b3ChangeDate = null;
        Date currentDate = new Date();
        try {
            b3ChangeDate = getB3ChangeDate();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }

        if (currentDate.after(b3ChangeDate)
                && hasICS(newListing)
                && !hasICS(origListing)
                && newB3.isSuccess()) {
            activity = new QuestionableActivityListingDTO();
            activity.setAfter("ics=1");
            activity.setBefore("ics=0");
        }
        return activity;
    }

    public QuestionableActivityListingDTO checkCriteriaB3SuccessOnCreate(CertifiedProductSearchDetails newListing) {
        QuestionableActivityListingDTO activity = null;
        CertificationResult newB3 = getB3Criteria(newListing);
        Date b3ChangeDate = null;
        Date currentDate = new Date();
        try {
            b3ChangeDate = getB3ChangeDate();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }

        if (currentDate.after(b3ChangeDate)
                && newB3.isSuccess()
                && !hasICS(newListing)) {
            activity = new QuestionableActivityListingDTO();
            activity.setAfter(B3_CRITERIA_NUMER);
        }
        return activity;
    }

    private Date getB3ChangeDate() throws ParseException {
        String dateAsString = env.getProperty(B3_CHANGE_DATE);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        return sdf.parse(dateAsString);
    }

    private CertificationResult getB3Criteria(CertifiedProductSearchDetails listing) {
        return listing.getCertificationResults().stream()
                .filter(result -> result.getNumber().equals(B3_CRITERIA_NUMER))
                .findFirst()
                .orElse(null);
    }

    private boolean isB3CriteriaNew(CertificationResult origB3, CertificationResult newB3) {
        return origB3 != null && newB3 != null && !origB3.isSuccess() && newB3.isSuccess();
    }

    private boolean hasICS(CertifiedProductSearchDetails listing) {
        if (listing != null && listing.getIcs() != null && listing.getIcs().getInherits() != null) {
            return listing.getIcs().getInherits();
        } else {
            return false;
        }
    }

    public QuestionableActivityListingDTO checkNonCuresAuditCriteriaOnCreate(CertifiedProductSearchDetails newListing) {
        QuestionableActivityListingDTO activity = null;
        Date certificationDate = new Date(newListing.getCertificationDate());

        if (certificationDate.equals(specialProperties.getEffectiveRuleDate())
                || certificationDate.after(specialProperties.getEffectiveRuleDate())) {
            // If ICS=0 and they attested to D2, D3, or D10
            if (!hasICS(newListing)) {
                List<CertificationResult> matchingCertResults = newListing.getCertificationResults().stream()
                        .filter(cr -> cr.isSuccess() &&
                                (cr.getCriterion().getId().equals(d2Criterion.getId())
                                        || cr.getCriterion().getId().equals(d3Criterion.getId())
                                        || cr.getCriterion().getId().equals(d10Criterion.getId())))
                        .collect(Collectors.toList());

                if (matchingCertResults.size() > 0) {
                    String criteriaNumbers = matchingCertResults.stream()
                            .map(cr -> cr.getCriterion().getNumber())
                            .collect(Collectors.joining(", "));
                    activity = new QuestionableActivityListingDTO();
                    activity.setAfter(criteriaNumbers);
                }
            }
        }
        return activity;
    }

    public QuestionableActivityListingDTO checkNonCuresAuditCriteriaOnEdit(CertifiedProductSearchDetails origListing,
            CertifiedProductSearchDetails newListing) {
        QuestionableActivityListingDTO activity = null;
        if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)) {
            List<String> matchingCriteriaNumbers = new ArrayList<String>();
            if (hasCriteriaChangedToAttestedTo(d2Criterion, newListing, origListing)) {
                matchingCriteriaNumbers.add(d2Criterion.getNumber());
            }
            if (hasCriteriaChangedToAttestedTo(d3Criterion, newListing, origListing)) {
                matchingCriteriaNumbers.add(d3Criterion.getNumber());
            }
            if (hasCriteriaChangedToAttestedTo(d10Criterion, newListing, origListing)) {
                matchingCriteriaNumbers.add(d10Criterion.getNumber());
            }

            if (matchingCriteriaNumbers.size() > 0) {
                String criteriaNumbers = matchingCriteriaNumbers.stream()
                        .collect(Collectors.joining(", "));
                activity = new QuestionableActivityListingDTO();
                activity.setAfter(criteriaNumbers);
            }
        }
        return activity;
    }

    public QuestionableActivityListingDTO checkNonCuresAuditCriteriaAndAddedIcsOnEdit(CertifiedProductSearchDetails origListing,
            CertifiedProductSearchDetails newListing) {
        QuestionableActivityListingDTO activity = null;
        if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)) {
            if (!hasICS(origListing) && hasICS(newListing)) { // ICS has been added
                List<String> matchingNewCriteriaNumbers = new ArrayList<String>();
                List<String> matchingOldCriteriaNumbers = new ArrayList<String>();
                // Does the new listing attest to one of the listing in question?
                if (isCriteriaAttestedTo(d2Criterion, newListing)) {
                    matchingNewCriteriaNumbers.add(d2Criterion.getNumber());
                }
                if (isCriteriaAttestedTo(d3Criterion, newListing)) {
                    matchingNewCriteriaNumbers.add(d3Criterion.getNumber());
                }
                if (isCriteriaAttestedTo(d10Criterion, newListing)) {
                    matchingNewCriteriaNumbers.add(d10Criterion.getNumber());
                }
                if (matchingNewCriteriaNumbers.size() > 0) {

                    String newCriteriaNumbers = matchingNewCriteriaNumbers.stream()
                            .collect(Collectors.joining(", "));
                    activity = new QuestionableActivityListingDTO();
                    activity.setAfter(newCriteriaNumbers);
                }
            }
        }

        return activity;
    }

    private Boolean isCriteriaAttestedTo(CertificationCriterion criteriaToCheck, CertifiedProductSearchDetails listing) {
        return listing.getCertificationResults().stream()
                .filter(cr -> cr.getCriterion().getId().equals(criteriaToCheck.getId()) && cr.isSuccess())
                .findAny()
                .isPresent();
    }

    private Boolean hasCriteriaChangedToAttestedTo(CertificationCriterion criteriaToCheck,
            CertifiedProductSearchDetails newListing, CertifiedProductSearchDetails origListing) {

        Optional<CertificationResult> newCertResult = newListing.getCertificationResults().stream()
                .filter(certResult -> certResult.isSuccess() && certResult.getCriterion().getId().equals(criteriaToCheck.getId()))
                .findAny();
        Optional<CertificationResult> origCertResult = origListing.getCertificationResults().stream()
                .filter(certResult -> !certResult.isSuccess()
                        && certResult.getCriterion().getId().equals(criteriaToCheck.getId()))
                .findAny();

        return newCertResult.isPresent() && origCertResult.isPresent();
    }

    static class CertificationStatusEventComparator implements Comparator<CertificationStatusEvent>, Serializable {
        private static long serialVersionUID = 1315674742856524797L;

        @Override
        public int compare(CertificationStatusEvent a, CertificationStatusEvent b) {
            return a.getEventDate().longValue() < b.getEventDate().longValue()
                    ? -1
                    : a.getEventDate().longValue() == b.getEventDate().longValue() ? 0 : 1;
        }
    }
}
