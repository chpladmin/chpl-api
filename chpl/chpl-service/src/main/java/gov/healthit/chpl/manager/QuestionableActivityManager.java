package gov.healthit.chpl.manager;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityCertificationResultDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityProductDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityTriggerDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityVersionDTO;
import gov.healthit.chpl.questionableactivity.CertificationResultQuestionableActivityProvider;
import gov.healthit.chpl.questionableactivity.DeveloperQuestionableActivityProvider;
import gov.healthit.chpl.questionableactivity.ListingQuestionableActivityService;
import gov.healthit.chpl.questionableactivity.ProductQuestionableActivityProvider;
import gov.healthit.chpl.questionableactivity.VersionQuestionableActivityProvider;
import gov.healthit.chpl.util.CertificationResultRules;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("questionableActivityManager")
public class QuestionableActivityManager implements EnvironmentAware {
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
    private long listingActivityThresholdMillis = -1;
    private List<QuestionableActivityTriggerDTO> triggerTypes;
    private Environment env;
    private DeveloperQuestionableActivityProvider developerQuestionableActivityProvider;
    private ProductQuestionableActivityProvider productQuestionableActivityProvider;
    private VersionQuestionableActivityProvider versionQuestionableActivityProvider;
    private ListingQuestionableActivityService listingQuestionableActivityService;
    private CertificationResultQuestionableActivityProvider certResultQuestionableActivityProvider;
    private CertificationResultRules certResultRules;
    private QuestionableActivityDAO questionableActivityDao;
    private CertifiedProductDAO listingDao;

    @Autowired
    @SuppressWarnings({"checkstyle:parameternumber"})
    public QuestionableActivityManager(
            DeveloperQuestionableActivityProvider developerQuestionableActivityProvider,
            ProductQuestionableActivityProvider productQuestionableActivityProvider,
            VersionQuestionableActivityProvider versionQuestionableActivityProvider,
            ListingQuestionableActivityService listingQuestionableActivityService,
            CertificationResultQuestionableActivityProvider certResultQuestionableActivityProvider,
            CertificationResultRules certResultRules,
            QuestionableActivityDAO questionableActivityDao,
            CertifiedProductDAO listingDao,
            Environment env) {

        this.developerQuestionableActivityProvider = developerQuestionableActivityProvider;
        this.productQuestionableActivityProvider = productQuestionableActivityProvider;
        this.versionQuestionableActivityProvider = versionQuestionableActivityProvider;
        this.listingQuestionableActivityService = listingQuestionableActivityService;
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

    public void checkDeveloperQuestionableActivity(Developer origDeveloper, Developer newDeveloper,
            Date activityDate, Long activityUser) {
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

    public void checkProductQuestionableActivity(Product origProduct, Product newProduct,
            Date activityDate, Long activityUser) {
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

    public void checkVersionQuestionableActivity(ProductVersionDTO origVersion, ProductVersionDTO newVersion,
            Date activityDate, Long activityUser) {
        QuestionableActivityVersionDTO activity = versionQuestionableActivityProvider.checkNameUpdated(origVersion, newVersion);
        if (activity != null) {
            createVersionActivity(activity, origVersion.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.VERSION_NAME_EDITED);
        }
    }

    public void checkListingQuestionableActivityOnEdit(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing,
            Date activityDate, Long activityUser, String activityReason) {
        listingQuestionableActivityService.processQuestionableActivity(origListing, newListing, activityReason);
    }

    public void checkCertificationResultQuestionableActivity(CertificationResult origCertResult,
            CertificationResult newCertResult, Date activityDate, Long activityUser, String activityReason) {
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
        if (isSvapAllowedForCriteria(origCertResult)) {
            certResultQuestionableActivityProvider.checkReplacedSvapAdded(origCertResult, newCertResult).stream()
                    .forEach(dto -> createCertificationActivity(dto, origCertResult.getId(), activityDate, activityUser,
                            QuestionableActivityTriggerConcept.REPLACED_SVAP_ADDED, activityReason));
        }

    }

    private boolean isSvapAllowedForCriteria(CertificationResult certResult) {
        return certResult.getAllowedSvaps() != null && certResult.getAllowedSvaps().size() > 0;
    }

    private void createListingActivity(QuestionableActivityListingDTO activity, Long listingId, Date activityDate,
            Long activityUser, QuestionableActivityTriggerConcept trigger, String activityReason) {
        activity.setListingId(listingId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        activity.setReason(activityReason);
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        activity.setTriggerId(triggerDto.getId());
        questionableActivityDao.create(activity);
    }

    private void createCertificationActivity(QuestionableActivityCertificationResultDTO activity,
            Long certResultId, Date activityDate, Long activityUser, QuestionableActivityTriggerConcept trigger,
            String activityReason) {
        activity.setCertResultId(certResultId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        activity.setReason(activityReason);
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        activity.setTriggerId(triggerDto.getId());
        questionableActivityDao.create(activity);
    }

    private void createDeveloperActivity(QuestionableActivityDeveloperDTO activity, Long developerId,
            Date activityDate, Long activityUser, QuestionableActivityTriggerConcept trigger,
            String reason) {
        activity.setDeveloperId(developerId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        activity.setReason(reason);
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        activity.setTriggerId(triggerDto.getId());
        questionableActivityDao.create(activity);
    }

    private void createDeveloperActivity(QuestionableActivityDeveloperDTO activity, Long developerId,
            Date activityDate, Long activityUser, QuestionableActivityTriggerConcept trigger) {
        createDeveloperActivity(activity, developerId, activityDate, activityUser, trigger, null);
    }

    private void createProductActivity(QuestionableActivityProductDTO activity, Long productId,
            Date activityDate, Long activityUser, QuestionableActivityTriggerConcept trigger) {
        activity.setProductId(productId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        activity.setTriggerId(triggerDto.getId());
        questionableActivityDao.create(activity);
    }

    private void createVersionActivity(QuestionableActivityVersionDTO activity, Long versionId,
            Date activityDate, Long activityUser, QuestionableActivityTriggerConcept trigger) {
        activity.setVersionId(versionId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        activity.setTriggerId(triggerDto.getId());
        questionableActivityDao.create(activity);
    }

    private QuestionableActivityTriggerDTO getTrigger(QuestionableActivityTriggerConcept trigger) {
        QuestionableActivityTriggerDTO result = null;
        for (QuestionableActivityTriggerDTO currTrigger : triggerTypes) {
            if (trigger.getName().equalsIgnoreCase(currTrigger.getName())) {
                result = currTrigger;
            }
        }
        return result;
    }
}
