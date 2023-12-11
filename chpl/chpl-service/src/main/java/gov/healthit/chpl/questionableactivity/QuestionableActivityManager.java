package gov.healthit.chpl.questionableactivity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityDeveloper;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityProduct;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityTrigger;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityVersion;
import gov.healthit.chpl.questionableactivity.service.CertificationResultQuestionableActivityService;
import gov.healthit.chpl.questionableactivity.service.DeveloperQuestionableActivityProvider;
import gov.healthit.chpl.questionableactivity.service.ListingQuestionableActivityService;
import gov.healthit.chpl.questionableactivity.service.ProductQuestionableActivityProvider;
import gov.healthit.chpl.questionableactivity.service.VersionQuestionableActivityProvider;
import gov.healthit.chpl.util.CertificationResultRules;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("questionableActivityManager")
public class QuestionableActivityManager {
    private List<QuestionableActivityTrigger> triggerTypes;
    private DeveloperQuestionableActivityProvider developerQuestionableActivityProvider;
    private ProductQuestionableActivityProvider productQuestionableActivityProvider;
    private VersionQuestionableActivityProvider versionQuestionableActivityProvider;
    private ListingQuestionableActivityService listingQuestionableActivityService;
    private CertificationResultQuestionableActivityService certResultQuestionableActivityService;
    private CertificationResultRules certResultRules;
    private QuestionableActivityDAO questionableActivityDao;

    @Autowired
    @SuppressWarnings({"checkstyle:parameternumber"})
    public QuestionableActivityManager(
            DeveloperQuestionableActivityProvider developerQuestionableActivityProvider,
            ProductQuestionableActivityProvider productQuestionableActivityProvider,
            VersionQuestionableActivityProvider versionQuestionableActivityProvider,
            ListingQuestionableActivityService listingQuestionableActivityService,
            CertificationResultQuestionableActivityService certResultQuestionableActivityService,
            CertificationResultRules certResultRules,
            QuestionableActivityDAO questionableActivityDao) {

        this.developerQuestionableActivityProvider = developerQuestionableActivityProvider;
        this.productQuestionableActivityProvider = productQuestionableActivityProvider;
        this.versionQuestionableActivityProvider = versionQuestionableActivityProvider;
        this.listingQuestionableActivityService = listingQuestionableActivityService;
        this.certResultQuestionableActivityService = certResultQuestionableActivityService;
        this.certResultRules = certResultRules;
        this.questionableActivityDao = questionableActivityDao;
        triggerTypes = questionableActivityDao.getAllTriggers();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).QUESTIONABLE_ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.QuestionableActivityDomainPermissions).GET)")
    public List<QuestionableActivityTrigger> getTriggerTypes() {
        return triggerTypes;
    }

    public void checkDeveloperQuestionableActivity(Developer origDeveloper, Developer newDeveloper, ActivityDTO activity) {
        QuestionableActivityDeveloper devActivity = null;
        List<QuestionableActivityDeveloper> devActivities = null;

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
        for (QuestionableActivityDeveloper currDevActivity : devActivities) {
            createDeveloperActivity(currDevActivity, newDeveloper.getId(), activity,
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_ADDED,
                    currDevActivity.getReason());
        }

        devActivities = developerQuestionableActivityProvider.checkStatusHistoryRemoved(
                origDeveloper.getStatusEvents(), newDeveloper.getStatusEvents());
        for (QuestionableActivityDeveloper currDevActivity : devActivities) {
            createDeveloperActivity(currDevActivity, newDeveloper.getId(), activity,
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_REMOVED,
                    currDevActivity.getReason());
        }

        devActivities = developerQuestionableActivityProvider.checkStatusHistoryItemEdited(
                origDeveloper.getStatusEvents(), newDeveloper.getStatusEvents());
        for (QuestionableActivityDeveloper currDevActivity : devActivities) {
            createDeveloperActivity(currDevActivity, newDeveloper.getId(), activity,
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_EDITED,
                    currDevActivity.getReason());
        }
    }

    public void checkProductQuestionableActivity(Product origProduct, Product newProduct, ActivityDTO activity) {
        QuestionableActivityProduct productActivity = null;
        List<QuestionableActivityProduct> productActivities = null;

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
        for (QuestionableActivityProduct currProductActivity : productActivities) {
            createProductActivity(currProductActivity, newProduct.getId(), activity, QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_ADDED);
        }

        productActivities = productQuestionableActivityProvider.checkOwnerHistoryRemoved(origProduct.getOwnerHistory(),
                newProduct.getOwnerHistory());
        for (QuestionableActivityProduct currProductActivity : productActivities) {
            createProductActivity(currProductActivity, newProduct.getId(), activity, QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_REMOVED);
        }

        productActivities = productQuestionableActivityProvider.checkOwnerHistoryItemEdited(
                origProduct.getOwnerHistory(), newProduct.getOwnerHistory());
        for (QuestionableActivityProduct currProductActivity : productActivities) {
            createProductActivity(currProductActivity, newProduct.getId(), activity, QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_EDITED);
        }
    }

    public void checkVersionQuestionableActivity(ProductVersionDTO origVersion, ProductVersionDTO newVersion,
            ActivityDTO activity) {
        QuestionableActivityVersion versionActivity = versionQuestionableActivityProvider.checkNameUpdated(origVersion, newVersion);
        if (versionActivity != null) {
            createVersionActivity(versionActivity, origVersion.getId(), activity, QuestionableActivityTriggerConcept.VERSION_NAME_EDITED);
        }
    }

    public void checkListingQuestionableActivity(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing,
            ActivityDTO activity, String activityReason) {
        listingQuestionableActivityService.processQuestionableActivity(origListing, newListing, activity, activityReason);
    }

    public void checkCertificationResultQuestionableActivity(CertificationResult origCertResult,
            CertificationResult newCertResult, ActivityDTO activity, String activityReason) {
        certResultQuestionableActivityService.processQuestionableActivity(origCertResult, newCertResult, activity, activityReason);
    }

    private void createDeveloperActivity(QuestionableActivityDeveloper questionableActivity, Long developerId,
            ActivityDTO activity, QuestionableActivityTriggerConcept triggerConcept, String reason) {
        questionableActivity.setActivityId(activity.getId());
        questionableActivity.setDeveloperId(developerId);
        questionableActivity.setActivityDate(activity.getActivityDate());
        questionableActivity.setUserId(activity.getUser().getId());
        questionableActivity.setReason(reason);
        QuestionableActivityTrigger trigger = getTrigger(triggerConcept);
        questionableActivity.setTrigger(trigger);
        questionableActivityDao.create(questionableActivity);
    }

    private void createDeveloperActivity(QuestionableActivityDeveloper questionableActivity, Long developerId,
            ActivityDTO activity, QuestionableActivityTriggerConcept triggerConcept) {
        createDeveloperActivity(questionableActivity, developerId, activity, triggerConcept, null);
    }

    private void createProductActivity(QuestionableActivityProduct questionableActivity, Long productId,
            ActivityDTO activity, QuestionableActivityTriggerConcept triggerConcept) {
        questionableActivity.setActivityId(activity.getId());
        questionableActivity.setProductId(productId);
        questionableActivity.setActivityDate(activity.getActivityDate());
        questionableActivity.setUserId(activity.getUser().getId());
        QuestionableActivityTrigger trigger = getTrigger(triggerConcept);
        questionableActivity.setTrigger(trigger);
        questionableActivityDao.create(questionableActivity);
    }

    private void createVersionActivity(QuestionableActivityVersion questionableActivity, Long versionId,
            ActivityDTO activity, QuestionableActivityTriggerConcept triggerConcept) {
        questionableActivity.setActivityId(activity.getId());
        questionableActivity.setVersionId(versionId);
        questionableActivity.setActivityDate(activity.getActivityDate());
        questionableActivity.setUserId(activity.getUser().getId());
        QuestionableActivityTrigger trigger = getTrigger(triggerConcept);
        questionableActivity.setTrigger(trigger);
        questionableActivityDao.create(questionableActivity);
    }

    private QuestionableActivityTrigger getTrigger(QuestionableActivityTriggerConcept triggerConcept) {
        QuestionableActivityTrigger result = null;
        for (QuestionableActivityTrigger currTrigger : triggerTypes) {
            if (triggerConcept.getName().equalsIgnoreCase(currTrigger.getName())) {
                result = currTrigger;
            }
        }
        return result;
    }
}
