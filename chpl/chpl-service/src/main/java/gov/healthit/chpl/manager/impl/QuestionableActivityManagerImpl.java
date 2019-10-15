package gov.healthit.chpl.manager.impl;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityCertificationResultDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityProductDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityTriggerDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityVersionDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.manager.QuestionableActivityManager;
import gov.healthit.chpl.questionableactivity.CertificationResultQuestionableActivityProvider;
import gov.healthit.chpl.questionableactivity.DeveloperQuestionableActivityProvider;
import gov.healthit.chpl.questionableactivity.ListingQuestionableActivityProvider;
import gov.healthit.chpl.questionableactivity.ProductQuestionableActivityProvider;
import gov.healthit.chpl.questionableactivity.VersionQuestionableActivityProvider;
import gov.healthit.chpl.util.CertificationResultRules;

@Service("questionableActivityManager")
public class QuestionableActivityManagerImpl implements QuestionableActivityManager, EnvironmentAware  {
    private static final Logger LOGGER = LogManager.getLogger(QuestionableActivityManagerImpl.class);
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
    private long listingActivityThresholdMillis = -1;
    private List<QuestionableActivityTriggerDTO> triggerTypes;
    private Environment env;
    private DeveloperQuestionableActivityProvider developerQuestionableActivityProvider;
    private ProductQuestionableActivityProvider productQuestionableActivityProvider;
    private VersionQuestionableActivityProvider versionQuestionableActivityProvider;
    private ListingQuestionableActivityProvider listingQuestionableActivityProvider;
    private CertificationResultQuestionableActivityProvider certResultQuestionableActivityProvider;
    private CertificationResultRules certResultRules;
    private QuestionableActivityDAO questionableActivityDao;
    private CertifiedProductDAO listingDao;

    /**
     * Autowired constructor for dependency injection.
     * @param developerQuestionableActivityProvider - DeveloperQuestionableActivityProvider
     * @param productQuestionableActivityProvider - ProductQuestionableActivityProvider
     * @param versionQuestionableActivityProvider - VersionQuestionableActivityProvider
     * @param listingQuestionableActivityProvider - ListingQuestionableActivityProvider
     * @param certResultQuestionableActivityProvider - CertificationResultQuestionableActivityProvider
     * @param certResultRules - CertificationResultRules
     * @param questionableActivityDao - QuestionableActivityDAO
     * @param listingDao - CertifiedProductDAO
     */
    @Autowired
    public QuestionableActivityManagerImpl(
            final DeveloperQuestionableActivityProvider developerQuestionableActivityProvider,
            final ProductQuestionableActivityProvider productQuestionableActivityProvider,
            final VersionQuestionableActivityProvider versionQuestionableActivityProvider,
            final ListingQuestionableActivityProvider listingQuestionableActivityProvider,
            final CertificationResultQuestionableActivityProvider certResultQuestionableActivityProvider,
            final CertificationResultRules certResultRules,
            final QuestionableActivityDAO questionableActivityDao,
            final CertifiedProductDAO listingDao,
            final Environment env) {

        this.developerQuestionableActivityProvider = developerQuestionableActivityProvider;
        this.productQuestionableActivityProvider = productQuestionableActivityProvider;
        this.versionQuestionableActivityProvider = versionQuestionableActivityProvider;
        this.listingQuestionableActivityProvider = listingQuestionableActivityProvider;
        this.certResultQuestionableActivityProvider = certResultQuestionableActivityProvider;
        this.certResultRules = certResultRules;
        this.questionableActivityDao = questionableActivityDao;
        this.listingDao = listingDao;
        this.env = env;
        triggerTypes = questionableActivityDao.getAllTriggers();
    }

    @Override
    public void setEnvironment(final Environment e) {
        this.env = e;
        String activityThresholdDaysStr = env.getProperty("questionableActivityThresholdDays");
        int activityThresholdDays = Integer.parseInt(activityThresholdDaysStr);
        listingActivityThresholdMillis = activityThresholdDays * MILLIS_PER_DAY;
    }

    @Override
    public void checkDeveloperQuestionableActivity(final DeveloperDTO origDeveloper, final DeveloperDTO newDeveloper,
             final Date activityDate, final Long activityUser) {
        QuestionableActivityDeveloperDTO devActivity = null;
        List<QuestionableActivityDeveloperDTO> devActivities = null;

        devActivity = developerQuestionableActivityProvider.checkNameUpdated(origDeveloper, newDeveloper);
        if (devActivity != null) {
            createDeveloperActivity(devActivity, newDeveloper.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.DEVELOPER_NAME_EDITED);
        }

        devActivity = developerQuestionableActivityProvider.checkCurrentStatusChanged(origDeveloper, newDeveloper);
        if (devActivity != null) {
            createDeveloperActivity(devActivity, newDeveloper.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED);
        }

        devActivities = developerQuestionableActivityProvider.checkStatusHistoryAdded(
                origDeveloper.getStatusEvents(), newDeveloper.getStatusEvents());
        for (QuestionableActivityDeveloperDTO currDevActivity : devActivities) {
            createDeveloperActivity(currDevActivity, newDeveloper.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_ADDED,
                    currDevActivity.getReason());
        }

        devActivities = developerQuestionableActivityProvider.checkStatusHistoryRemoved(
                origDeveloper.getStatusEvents(), newDeveloper.getStatusEvents());
        for (QuestionableActivityDeveloperDTO currDevActivity : devActivities) {
            createDeveloperActivity(currDevActivity, newDeveloper.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_REMOVED,
                    currDevActivity.getReason());
        }

        devActivities = developerQuestionableActivityProvider.checkStatusHistoryItemEdited(
                origDeveloper.getStatusEvents(), newDeveloper.getStatusEvents());
        for (QuestionableActivityDeveloperDTO currDevActivity : devActivities) {
            createDeveloperActivity(currDevActivity, newDeveloper.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_EDITED,
                    currDevActivity.getReason());
        }
    }

    @Override
    public void checkProductQuestionableActivity(final ProductDTO origProduct, final ProductDTO newProduct,
            final Date activityDate, final Long activityUser) {
        QuestionableActivityProductDTO productActivity = null;
        List<QuestionableActivityProductDTO> productActivities = null;

        productActivity = productQuestionableActivityProvider.checkNameUpdated(origProduct, newProduct);
        if (productActivity != null) {
            createProductActivity(productActivity, newProduct.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.PRODUCT_NAME_EDITED);
        }

        productActivity = productQuestionableActivityProvider.checkCurrentOwnerChanged(origProduct, newProduct);
        if (productActivity != null) {
            createProductActivity(productActivity, newProduct.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED);
        }

        productActivities = productQuestionableActivityProvider.checkOwnerHistoryAdded(origProduct.getOwnerHistory(),
                newProduct.getOwnerHistory());
        for (QuestionableActivityProductDTO currProductActivity : productActivities) {
            createProductActivity(currProductActivity, newProduct.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_ADDED);
        }

        productActivities = productQuestionableActivityProvider.checkOwnerHistoryRemoved(origProduct.getOwnerHistory(),
                newProduct.getOwnerHistory());
        for (QuestionableActivityProductDTO currProductActivity : productActivities) {
            createProductActivity(currProductActivity, newProduct.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_REMOVED);
        }

        productActivities = productQuestionableActivityProvider.checkOwnerHistoryItemEdited(
                origProduct.getOwnerHistory(), newProduct.getOwnerHistory());
        for (QuestionableActivityProductDTO currProductActivity : productActivities) {
            createProductActivity(currProductActivity, newProduct.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_EDITED);
        }
    }

    @Override
    public void checkVersionQuestionableActivity(final ProductVersionDTO origVersion,
            final ProductVersionDTO newVersion, final Date activityDate, final Long activityUser) {
        QuestionableActivityVersionDTO activity
        = versionQuestionableActivityProvider.checkNameUpdated(origVersion, newVersion);
        if (activity != null) {
            createVersionActivity(activity, origVersion.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.VERSION_NAME_EDITED);
        }
    }

    @Override
    public void checkListingQuestionableActivity(final CertifiedProductSearchDetails origListing,
            final CertifiedProductSearchDetails newListing, final Date activityDate, final Long activityUser,
            final String activityReason) {
        QuestionableActivityListingDTO activity = listingQuestionableActivityProvider.check2011EditionUpdated(
                origListing, newListing);
        if (activity != null) {
            createListingActivity(activity, origListing.getId(), activityDate, activityUser,
                    QuestionableActivityTriggerConcept.EDITION_2011_EDITED, activityReason);
        } else {
            //it wasn't a 2011 update, check for any changes that are questionable at any time
            activity = listingQuestionableActivityProvider.check2014EditionUpdated(
                    origListing, newListing);
            if (activity != null) {
                createListingActivity(activity, origListing.getId(), activityDate, activityUser,
                        QuestionableActivityTriggerConcept.EDITION_2014_EDITED, activityReason);
            }
            activity = listingQuestionableActivityProvider.checkCertificationStatusUpdated(
                    CertificationStatusType.WithdrawnByDeveloperUnderReview, origListing, newListing);
            if (activity != null) {
                createListingActivity(activity, origListing.getId(), activityDate, activityUser,
                        QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_CURRENT, activityReason);
            }
            activity = listingQuestionableActivityProvider.checkCertificationStatusHistoryUpdated(
                    origListing, newListing);
            if (activity != null) {
                createListingActivity(activity, origListing.getId(), activityDate, activityUser,
                        QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_HISTORY, activityReason);
            }
            activity = listingQuestionableActivityProvider.checkTestingLabChanged(
                    origListing, newListing);
            if (activity != null) {
                createListingActivity(activity, origListing.getId(), activityDate, activityUser,
                        QuestionableActivityTriggerConcept.TESTING_LAB_CHANGED, activityReason);
            }
            //finally check for other changes that are only questionable
            //outside of the acceptable activity threshold

            //get the confirm date of the listing to check against the threshold
            Date confirmDate = listingDao.getConfirmDate(origListing.getId());
            if (confirmDate != null && newListing.getLastModifiedDate() != null
                    && (newListing.getLastModifiedDate().longValue()
                            - confirmDate.getTime() > listingActivityThresholdMillis)) {
                activity = listingQuestionableActivityProvider.checkCertificationStatusUpdated(origListing, newListing);
                if (activity != null) {
                    createListingActivity(activity, origListing.getId(), activityDate,
                            activityUser, QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_CURRENT,
                            activityReason);
                }
                activity = listingQuestionableActivityProvider.checkCertificationStatusDateUpdated(
                        origListing, newListing);
                if (activity != null) {
                    createListingActivity(activity, origListing.getId(), activityDate,
                            activityUser, QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_DATE_EDITED_CURRENT,
                            activityReason);
                }
                activity = listingQuestionableActivityProvider.checkSurveillanceDeleted(origListing, newListing);
                if (activity != null) {
                    createListingActivity(activity, origListing.getId(), activityDate,
                            activityUser, QuestionableActivityTriggerConcept.SURVEILLANCE_REMOVED,
                            activityReason);
                }

                List<QuestionableActivityListingDTO> activities = listingQuestionableActivityProvider
                        .checkCqmsAdded(origListing, newListing);
                for (QuestionableActivityListingDTO currActivity : activities) {
                    createListingActivity(currActivity, origListing.getId(), activityDate,
                            activityUser, QuestionableActivityTriggerConcept.CQM_ADDED,
                            activityReason);
                }

                activities = listingQuestionableActivityProvider.checkCqmsRemoved(origListing, newListing);
                for (QuestionableActivityListingDTO currActivity : activities) {
                    createListingActivity(currActivity, origListing.getId(), activityDate,
                            activityUser, QuestionableActivityTriggerConcept.CQM_REMOVED,
                            activityReason);
                }

                activities = listingQuestionableActivityProvider.checkCertificationsAdded(origListing, newListing);
                for (QuestionableActivityListingDTO currActivity : activities) {
                    createListingActivity(currActivity, origListing.getId(), activityDate,
                            activityUser, QuestionableActivityTriggerConcept.CRITERIA_ADDED,
                            activityReason);
                }

                activities = listingQuestionableActivityProvider.checkCertificationsRemoved(origListing, newListing);
                for (QuestionableActivityListingDTO currActivity : activities) {
                    createListingActivity(currActivity, origListing.getId(), activityDate,
                            activityUser, QuestionableActivityTriggerConcept.CRITERIA_REMOVED,
                            activityReason);
                }
            }
        }
    }

    @Override
    public void checkCertificationResultQuestionableActivity(final CertificationResult origCertResult,
            final CertificationResult newCertResult, final Date activityDate, final Long activityUser,
            final String activityReason) {
        QuestionableActivityCertificationResultDTO certActivity = null;
        List<QuestionableActivityCertificationResultDTO> certActivities = null;

        if (certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.G1_SUCCESS)) {
            certActivity = certResultQuestionableActivityProvider.checkG1SuccessUpdated(origCertResult, newCertResult);
            if (certActivity != null) {
                createCertificationActivity(certActivity, origCertResult.getId(), activityDate,
                        activityUser, QuestionableActivityTriggerConcept.G1_SUCCESS_EDITED, activityReason);
            }
        }
        if (certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.G2_SUCCESS)) {
            certActivity = certResultQuestionableActivityProvider.checkG2SuccessUpdated(origCertResult, newCertResult);
            if (certActivity != null) {
                createCertificationActivity(certActivity, origCertResult.getId(), activityDate,
                        activityUser, QuestionableActivityTriggerConcept.G2_SUCCESS_EDITED, activityReason);
            }
        }
        if (certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.GAP)) {
            certActivity = certResultQuestionableActivityProvider.checkGapUpdated(origCertResult, newCertResult);
            if (certActivity != null) {
                createCertificationActivity(certActivity, origCertResult.getId(), activityDate,
                        activityUser, QuestionableActivityTriggerConcept.GAP_EDITED, activityReason);
            }
        }
        if (certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.G1_MACRA)) {
            certActivities = certResultQuestionableActivityProvider
                    .checkG1MacraMeasuresAdded(origCertResult, newCertResult);
            for (QuestionableActivityCertificationResultDTO currCertActivity : certActivities) {
                createCertificationActivity(currCertActivity, origCertResult.getId(), activityDate,
                        activityUser, QuestionableActivityTriggerConcept.G1_MEASURE_ADDED, activityReason);
            }
        }
        if (certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.G1_MACRA)) {
            certActivities = certResultQuestionableActivityProvider
                    .checkG1MacraMeasuresRemoved(origCertResult, newCertResult);
            for (QuestionableActivityCertificationResultDTO currCertActivity : certActivities) {
                createCertificationActivity(currCertActivity, origCertResult.getId(), activityDate,
                        activityUser, QuestionableActivityTriggerConcept.G1_MEASURE_REMOVED, activityReason);
            }
        }
        if (certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.G2_MACRA)) {
            certActivities = certResultQuestionableActivityProvider
                    .checkG2MacraMeasuresAdded(origCertResult, newCertResult);
            for (QuestionableActivityCertificationResultDTO currCertActivity : certActivities) {
                createCertificationActivity(currCertActivity, origCertResult.getId(), activityDate,
                        activityUser, QuestionableActivityTriggerConcept.G2_MEASURE_ADDED, activityReason);
            }
        }
        if (certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.G2_MACRA)) {
            certActivities = certResultQuestionableActivityProvider
                    .checkG2MacraMeasuresRemoved(origCertResult, newCertResult);
            for (QuestionableActivityCertificationResultDTO currCertActivity : certActivities) {
                createCertificationActivity(currCertActivity, origCertResult.getId(), activityDate,
                        activityUser, QuestionableActivityTriggerConcept.G2_MEASURE_REMOVED, activityReason);
            }
        }
    }

    private void createListingActivity(final QuestionableActivityListingDTO activity, final Long listingId,
            final Date activityDate, final Long activityUser, final QuestionableActivityTriggerConcept trigger,
            final String activityReason) {
        activity.setListingId(listingId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        activity.setReason(activityReason);
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        activity.setTriggerId(triggerDto.getId());
        questionableActivityDao.create(activity);
    }

    private void createCertificationActivity(final QuestionableActivityCertificationResultDTO activity,
            final Long certResultId, final Date activityDate, final Long activityUser,
            final QuestionableActivityTriggerConcept trigger, final String activityReason) {
        activity.setCertResultId(certResultId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        activity.setReason(activityReason);
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        activity.setTriggerId(triggerDto.getId());
        questionableActivityDao.create(activity);
    }

    private void createDeveloperActivity(final QuestionableActivityDeveloperDTO activity, final Long developerId,
            final Date activityDate, final Long activityUser, final QuestionableActivityTriggerConcept trigger,
            final String reason) {
        activity.setDeveloperId(developerId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        activity.setReason(reason);
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        activity.setTriggerId(triggerDto.getId());
        questionableActivityDao.create(activity);
    }

    private void createDeveloperActivity(final QuestionableActivityDeveloperDTO activity, final Long developerId,
            final Date activityDate, final Long activityUser, final QuestionableActivityTriggerConcept trigger) {
        createDeveloperActivity(activity, developerId, activityDate, activityUser, trigger, null);
    }

    private void createProductActivity(final QuestionableActivityProductDTO activity, final Long productId,
            final Date activityDate, final Long activityUser, final QuestionableActivityTriggerConcept trigger) {
        activity.setProductId(productId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        activity.setTriggerId(triggerDto.getId());
        questionableActivityDao.create(activity);
    }

    private void createVersionActivity(final QuestionableActivityVersionDTO activity, final Long versionId,
            final Date activityDate, final Long activityUser, final QuestionableActivityTriggerConcept trigger) {
        activity.setVersionId(versionId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        activity.setTriggerId(triggerDto.getId());
        questionableActivityDao.create(activity);
    }

    private QuestionableActivityTriggerDTO getTrigger(final QuestionableActivityTriggerConcept trigger) {
        QuestionableActivityTriggerDTO result = null;
        for (QuestionableActivityTriggerDTO currTrigger : triggerTypes) {
            if (trigger.getName().equalsIgnoreCase(currTrigger.getName())) {
                result = currTrigger;
            }
        }
        return result;
    }
}
