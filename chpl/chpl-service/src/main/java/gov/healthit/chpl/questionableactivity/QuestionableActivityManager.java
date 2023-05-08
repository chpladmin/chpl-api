package gov.healthit.chpl.questionableactivity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityCertificationResultDTO;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityProductDTO;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityTriggerDTO;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityVersionDTO;
import gov.healthit.chpl.questionableactivity.service.CertificationResultQuestionableActivityProvider;
import gov.healthit.chpl.questionableactivity.service.DeveloperQuestionableActivityProvider;
import gov.healthit.chpl.questionableactivity.service.ListingQuestionableActivityService;
import gov.healthit.chpl.questionableactivity.service.ProductQuestionableActivityProvider;
import gov.healthit.chpl.questionableactivity.service.VersionQuestionableActivityProvider;
import gov.healthit.chpl.util.CertificationResultRules;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("questionableActivityManager")
public class QuestionableActivityManager {
    private List<QuestionableActivityTriggerDTO> triggerTypes;
    private DeveloperQuestionableActivityProvider developerQuestionableActivityProvider;
    private ProductQuestionableActivityProvider productQuestionableActivityProvider;
    private VersionQuestionableActivityProvider versionQuestionableActivityProvider;
    private ListingQuestionableActivityService listingQuestionableActivityService;
    private CertificationResultQuestionableActivityProvider certResultQuestionableActivityProvider;
    private CertificationResultRules certResultRules;
    private QuestionableActivityDAO questionableActivityDao;

    @Autowired
    @SuppressWarnings({"checkstyle:parameternumber"})
    public QuestionableActivityManager(
            DeveloperQuestionableActivityProvider developerQuestionableActivityProvider,
            ProductQuestionableActivityProvider productQuestionableActivityProvider,
            VersionQuestionableActivityProvider versionQuestionableActivityProvider,
            ListingQuestionableActivityService listingQuestionableActivityService,
            CertificationResultQuestionableActivityProvider certResultQuestionableActivityProvider,
            CertificationResultRules certResultRules,
            QuestionableActivityDAO questionableActivityDao) {

        this.developerQuestionableActivityProvider = developerQuestionableActivityProvider;
        this.productQuestionableActivityProvider = productQuestionableActivityProvider;
        this.versionQuestionableActivityProvider = versionQuestionableActivityProvider;
        this.listingQuestionableActivityService = listingQuestionableActivityService;
        this.certResultQuestionableActivityProvider = certResultQuestionableActivityProvider;
        this.certResultRules = certResultRules;
        this.questionableActivityDao = questionableActivityDao;
        triggerTypes = questionableActivityDao.getAllTriggers();
    }

    public void checkDeveloperQuestionableActivity(Developer origDeveloper, Developer newDeveloper, ActivityDTO activity) {
        QuestionableActivityDeveloperDTO devActivity = null;
        List<QuestionableActivityDeveloperDTO> devActivities = null;

        devActivity = developerQuestionableActivityProvider.checkNameUpdated(origDeveloper, newDeveloper);
        if (devActivity != null) {
            createDeveloperActivity(devActivity, newDeveloper.getId(), activity, QuestionableActivityTriggerConcept.DEVELOPER_NAME_EDITED);
        }

        devActivity = developerQuestionableActivityProvider.checkCurrentStatusChanged(origDeveloper, newDeveloper);
        if (devActivity != null) {
            createDeveloperActivity(devActivity, newDeveloper.getId(), activity, QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED);
        }

        devActivities = developerQuestionableActivityProvider.checkStatusHistoryAdded(
                origDeveloper.getStatusEvents(), newDeveloper.getStatusEvents());
        for (QuestionableActivityDeveloperDTO currDevActivity : devActivities) {
            createDeveloperActivity(currDevActivity, newDeveloper.getId(), activity,
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_ADDED,
                    currDevActivity.getReason());
        }

        devActivities = developerQuestionableActivityProvider.checkStatusHistoryRemoved(
                origDeveloper.getStatusEvents(), newDeveloper.getStatusEvents());
        for (QuestionableActivityDeveloperDTO currDevActivity : devActivities) {
            createDeveloperActivity(currDevActivity, newDeveloper.getId(), activity,
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_REMOVED,
                    currDevActivity.getReason());
        }

        devActivities = developerQuestionableActivityProvider.checkStatusHistoryItemEdited(
                origDeveloper.getStatusEvents(), newDeveloper.getStatusEvents());
        for (QuestionableActivityDeveloperDTO currDevActivity : devActivities) {
            createDeveloperActivity(currDevActivity, newDeveloper.getId(), activity,
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_EDITED,
                    currDevActivity.getReason());
        }
    }

    public void checkProductQuestionableActivity(Product origProduct, Product newProduct, ActivityDTO activity) {
        QuestionableActivityProductDTO productActivity = null;
        List<QuestionableActivityProductDTO> productActivities = null;

        productActivity = productQuestionableActivityProvider.checkNameUpdated(origProduct, newProduct);
        if (productActivity != null) {
            createProductActivity(productActivity, newProduct.getId(), activity, QuestionableActivityTriggerConcept.PRODUCT_NAME_EDITED);
        }

        productActivity = productQuestionableActivityProvider.checkCurrentOwnerChanged(origProduct, newProduct);
        if (productActivity != null) {
            createProductActivity(productActivity, newProduct.getId(), activity, QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED);
        }

        productActivities = productQuestionableActivityProvider.checkOwnerHistoryAdded(origProduct.getOwnerHistory(),
                newProduct.getOwnerHistory());
        for (QuestionableActivityProductDTO currProductActivity : productActivities) {
            createProductActivity(currProductActivity, newProduct.getId(), activity, QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_ADDED);
        }

        productActivities = productQuestionableActivityProvider.checkOwnerHistoryRemoved(origProduct.getOwnerHistory(),
                newProduct.getOwnerHistory());
        for (QuestionableActivityProductDTO currProductActivity : productActivities) {
            createProductActivity(currProductActivity, newProduct.getId(), activity, QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_REMOVED);
        }

        productActivities = productQuestionableActivityProvider.checkOwnerHistoryItemEdited(
                origProduct.getOwnerHistory(), newProduct.getOwnerHistory());
        for (QuestionableActivityProductDTO currProductActivity : productActivities) {
            createProductActivity(currProductActivity, newProduct.getId(), activity, QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_EDITED);
        }
    }

    public void checkVersionQuestionableActivity(ProductVersionDTO origVersion, ProductVersionDTO newVersion,
            ActivityDTO activity) {
        QuestionableActivityVersionDTO versionActivity = versionQuestionableActivityProvider.checkNameUpdated(origVersion, newVersion);
        if (versionActivity != null) {
            createVersionActivity(versionActivity, origVersion.getId(), activity, QuestionableActivityTriggerConcept.VERSION_NAME_EDITED);
        }
    }

    public void checkListingQuestionableActivityOnEdit(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing,
            ActivityDTO activity, String activityReason) {
        listingQuestionableActivityService.processQuestionableActivity(origListing, newListing, activity, activityReason);
    }

    public void checkCertificationResultQuestionableActivity(CertificationResult origCertResult,
            CertificationResult newCertResult, ActivityDTO activity, String activityReason) {
        QuestionableActivityCertificationResultDTO certActivity = null;

        if (certResultRules.hasCertOption(origCertResult.getCriterion().getId(), CertificationResultRules.G1_SUCCESS)) {
            certActivity = certResultQuestionableActivityProvider.checkG1SuccessUpdated(origCertResult, newCertResult);
            if (certActivity != null) {
                createCertificationActivity(certActivity, origCertResult.getId(), activity,
                        QuestionableActivityTriggerConcept.G1_SUCCESS_EDITED, activityReason);
            }
        }
        if (certResultRules.hasCertOption(origCertResult.getCriterion().getId(), CertificationResultRules.G2_SUCCESS)) {
            certActivity = certResultQuestionableActivityProvider.checkG2SuccessUpdated(origCertResult, newCertResult);
            if (certActivity != null) {
                createCertificationActivity(certActivity, origCertResult.getId(), activity,
                        QuestionableActivityTriggerConcept.G2_SUCCESS_EDITED, activityReason);
            }
        }
        if (certResultRules.hasCertOption(origCertResult.getCriterion().getId(), CertificationResultRules.GAP)) {
            certActivity = certResultQuestionableActivityProvider.checkGapUpdated(origCertResult, newCertResult);
            if (certActivity != null) {
                createCertificationActivity(certActivity, origCertResult.getId(), activity,
                        QuestionableActivityTriggerConcept.GAP_EDITED, activityReason);
            }
        }
        if (isSvapAllowedForCriteria(origCertResult)) {
            certResultQuestionableActivityProvider.checkReplacedSvapAdded(origCertResult, newCertResult).stream()
                    .forEach(dto -> createCertificationActivity(dto, origCertResult.getId(), activity,
                            QuestionableActivityTriggerConcept.REPLACED_SVAP_ADDED, activityReason));
        }

    }

    private boolean isSvapAllowedForCriteria(CertificationResult certResult) {
        return certResult.getAllowedSvaps() != null && certResult.getAllowedSvaps().size() > 0;
    }

    private void createCertificationActivity(QuestionableActivityCertificationResultDTO questionableActivity,
            Long certResultId, ActivityDTO activity, QuestionableActivityTriggerConcept trigger,
            String activityReason) {
        questionableActivity.setActivityId(activity.getId());
        questionableActivity.setCertResultId(certResultId);
        questionableActivity.setActivityDate(activity.getActivityDate());
        questionableActivity.setUserId(activity.getUser().getId());
        questionableActivity.setReason(activityReason);
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        questionableActivity.setTrigger(triggerDto);
        questionableActivityDao.create(questionableActivity);
    }

    private void createDeveloperActivity(QuestionableActivityDeveloperDTO questionableActivity, Long developerId,
            ActivityDTO activity, QuestionableActivityTriggerConcept trigger, String reason) {
        questionableActivity.setActivityId(activity.getId());
        questionableActivity.setDeveloperId(developerId);
        questionableActivity.setActivityDate(activity.getActivityDate());
        questionableActivity.setUserId(activity.getUser().getId());
        questionableActivity.setReason(reason);
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        questionableActivity.setTrigger(triggerDto);
        questionableActivityDao.create(questionableActivity);
    }

    private void createDeveloperActivity(QuestionableActivityDeveloperDTO questionableActivity, Long developerId,
            ActivityDTO activity, QuestionableActivityTriggerConcept trigger) {
        createDeveloperActivity(questionableActivity, developerId, activity, trigger, null);
    }

    private void createProductActivity(QuestionableActivityProductDTO questionableActivity, Long productId,
            ActivityDTO activity, QuestionableActivityTriggerConcept trigger) {
        questionableActivity.setActivityId(activity.getId());
        questionableActivity.setProductId(productId);
        questionableActivity.setActivityDate(activity.getActivityDate());
        questionableActivity.setUserId(activity.getUser().getId());
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        questionableActivity.setTrigger(triggerDto);
        questionableActivityDao.create(questionableActivity);
    }

    private void createVersionActivity(QuestionableActivityVersionDTO questionableActivity, Long versionId,
            ActivityDTO activity, QuestionableActivityTriggerConcept trigger) {
        questionableActivity.setActivityId(activity.getId());
        questionableActivity.setVersionId(versionId);
        questionableActivity.setActivityDate(activity.getActivityDate());
        questionableActivity.setUserId(activity.getUser().getId());
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        questionableActivity.setTrigger(triggerDto);
        questionableActivityDao.create(questionableActivity);
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
