package gov.healthit.chpl.permissions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.AccessibilityStandardDomainPermissions;
import gov.healthit.chpl.permissions.domains.ActivityDomainPermissions;
import gov.healthit.chpl.permissions.domains.AnnouncementDomainPermissions;
import gov.healthit.chpl.permissions.domains.AttestationDomainPermissions;
import gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions;
import gov.healthit.chpl.permissions.domains.CertificationIdDomainPermissions;
import gov.healthit.chpl.permissions.domains.CertificationResultsDomainPermissions;
import gov.healthit.chpl.permissions.domains.CertifiedProductDomainPermissions;
import gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions;
import gov.healthit.chpl.permissions.domains.CodeSetDomainPermissions;
import gov.healthit.chpl.permissions.domains.ComplaintDomainPermissions;
import gov.healthit.chpl.permissions.domains.CorrectiveActionPlanDomainPermissions;
import gov.healthit.chpl.permissions.domains.DeveloperDomainPermissions;
import gov.healthit.chpl.permissions.domains.DomainPermissions;
import gov.healthit.chpl.permissions.domains.FunctionalityTestedDomainPermissions;
import gov.healthit.chpl.permissions.domains.InvitationDomainPermissions;
import gov.healthit.chpl.permissions.domains.ListingUploadDomainPerissions;
import gov.healthit.chpl.permissions.domains.ProductDomainPermissions;
import gov.healthit.chpl.permissions.domains.ProductVersionDomainPermissions;
import gov.healthit.chpl.permissions.domains.QmsStandardDomainPermissions;
import gov.healthit.chpl.permissions.domains.QuestionableActivityDomainPermissions;
import gov.healthit.chpl.permissions.domains.RealWorldTestingDomainPermissions;
import gov.healthit.chpl.permissions.domains.SchedulerDomainPermissions;
import gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions;
import gov.healthit.chpl.permissions.domains.StandardDomainPermissions;
import gov.healthit.chpl.permissions.domains.SubscriptionDomainPermissions;
import gov.healthit.chpl.permissions.domains.SurveillanceDomainPermissions;
import gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions;
import gov.healthit.chpl.permissions.domains.SvapDomainPermissions;
import gov.healthit.chpl.permissions.domains.TestToolDomainPermissions;
import gov.healthit.chpl.permissions.domains.TestingLabDomainPermissions;
import gov.healthit.chpl.permissions.domains.UcdProcessDomainPermissions;
import gov.healthit.chpl.permissions.domains.UserPermissionsDomainPermissions;

@Component
public class Permissions {
    public static final String CERTIFICATION_RESULTS = "CERTIFICATION_RESULTS";
    public static final String CERTIFIED_PRODUCT = "CERTIFIED_PRODUCT";
    public static final String CORRECTIVE_ACTION_PLAN = "CORRECTIVE_ACTION_PLAN";
    public static final String CERTIFICATION_ID = "CERTIFICATION_ID";
    public static final String INVITATION = "INVITATION";
    public static final String LISTING_UPLOAD = "LISTING_UPLOAD";
    public static final String SURVEILLANCE = "SURVEILLANCE";
    public static final String SURVEILLANCE_REPORT = "SURVEILLANCE_REPORT";
    public static final String CERTIFICATION_BODY = "CERTIFICATION_BODY";
    public static final String SCHEDULER = "SCHEDULER";
    public static final String USER_PERMISSIONS = "USER_PERMISSIONS";
    public static final String ACTIVITY = "ACTIVITY";
    public static final String JOB = "JOB";
    public static final String PRODUCT = "PRODUCT";
    public static final String DEVELOPER = "DEVELOPER";
    public static final String PRODUCT_VERSION = "PRODUCT_VERSION";
    public static final String SECURED_USER = "SECURED_USER";
    public static final String TESTING_LAB = "TESTING_LAB";
    public static final String COMPLAINT = "COMPLAINT";
    public static final String ACTIVITY_METADATA = "ACTIVTY_METADATA";
    public static final String ANNOUNCEMENT = "ANNOUNCEMENT";
    public static final String CHANGE_REQUEST = "CHANGE_REQUEST";
    public static final String SUBSCRIPTION = "SUBSCRIPTION";
    public static final String REAL_WORLD_TESTING = "REAL_WORLD_TESTING";
    public static final String SVAP = "SVAP";
    public static final String UCD_PROCESS = "UCD_PROCESS";
    public static final String ACCESSIBILITY_STANDARD = "ACCESSIBILITY_STANDARD";
    public static final String QMS_STANDARD = "QMS_STANDARD";
    public static final String ATTESTATION = "ATTESTATION";
    public static final String QUESTIONABLE_ACTIVITY = "QUESTIONABLE_ACTIVITY";
    public static final String TEST_TOOL = "TEST_TOOL";
    public static final String FUNCTIONALITY_TESTED = "FUNCTIONALITY_TESTED";
    public static final String STANDARD = "STANDARD";
    public static final String CODE_SET = "CODE_SET";

    private Map<String, DomainPermissions> domainPermissions = new HashMap<String, DomainPermissions>();

    @SuppressWarnings("checkstyle:parameternumber")
    @Autowired
    public Permissions(CertificationResultsDomainPermissions certificationResultsDomainPermissions,
            CertifiedProductDomainPermissions certifiedProductDomainPermissions,
            CorrectiveActionPlanDomainPermissions correctiveActionPlanDomainPermissions,
            CertificationIdDomainPermissions certificationIdDomainPermissions,
            InvitationDomainPermissions invitationDomainPermissions,
            ListingUploadDomainPerissions listingUploadDomainPermissions,
            SurveillanceDomainPermissions surveillanceDomainPermissions,
            SurveillanceReportDomainPermissions surveillanceReportDomainPermissions,
            CertificationBodyDomainPermissions certificationBodyDomainPermissions,
            UserPermissionsDomainPermissions userPermissionsDomainPermissions,
            ActivityDomainPermissions activityDomainPermissions,
            ProductDomainPermissions productDomainPermissions,
            DeveloperDomainPermissions developerDomainPermissions,
            ProductVersionDomainPermissions productVersionDomainPermissions,
            SecuredUserDomainPermissions securedUserDomainPermissions,
            SchedulerDomainPermissions schedulerDomainPermissions,
            TestingLabDomainPermissions testingLabDomainPermissions,
            ComplaintDomainPermissions complaintDomainPermissions,
            AnnouncementDomainPermissions announcementDomainPermissions,
            ChangeRequestDomainPermissions changeRequestDomainPermissions,
            SubscriptionDomainPermissions subscriptionDomainPermissions,
            RealWorldTestingDomainPermissions realWorldTestingDomainPermissions,
            SvapDomainPermissions svapDomainPermissions,
            UcdProcessDomainPermissions ucdProcessDomainPermissions,
            AccessibilityStandardDomainPermissions accessibilityStandardDomainPermissions,
            QmsStandardDomainPermissions qmsStandardDomainPermissions,
            AttestationDomainPermissions attestationDomainPermissions,
            QuestionableActivityDomainPermissions qaDomainPermissions,
            TestToolDomainPermissions testToolDomainPermissions,
            FunctionalityTestedDomainPermissions functionalityTestedDomainPermissions,
            StandardDomainPermissions standardDomainPermissions,
            CodeSetDomainPermissions codeSetPermissions) {

        domainPermissions.put(ACCESSIBILITY_STANDARD, accessibilityStandardDomainPermissions);
        domainPermissions.put(ACTIVITY, activityDomainPermissions);
        domainPermissions.put(ANNOUNCEMENT, announcementDomainPermissions);
        domainPermissions.put(ATTESTATION, attestationDomainPermissions);
        domainPermissions.put(CERTIFICATION_BODY, certificationBodyDomainPermissions);
        domainPermissions.put(CERTIFICATION_ID, certificationIdDomainPermissions);
        domainPermissions.put(CERTIFICATION_RESULTS, certificationResultsDomainPermissions);
        domainPermissions.put(CERTIFIED_PRODUCT, certifiedProductDomainPermissions);
        domainPermissions.put(CHANGE_REQUEST, changeRequestDomainPermissions);
        domainPermissions.put(CODE_SET, codeSetPermissions);
        domainPermissions.put(COMPLAINT, complaintDomainPermissions);
        domainPermissions.put(CORRECTIVE_ACTION_PLAN, correctiveActionPlanDomainPermissions);
        domainPermissions.put(DEVELOPER, developerDomainPermissions);
        domainPermissions.put(FUNCTIONALITY_TESTED, functionalityTestedDomainPermissions);
        domainPermissions.put(INVITATION, invitationDomainPermissions);
        domainPermissions.put(LISTING_UPLOAD, listingUploadDomainPermissions);
        domainPermissions.put(PRODUCT, productDomainPermissions);
        domainPermissions.put(PRODUCT_VERSION, productVersionDomainPermissions);
        domainPermissions.put(QMS_STANDARD, qmsStandardDomainPermissions);
        domainPermissions.put(QUESTIONABLE_ACTIVITY, qaDomainPermissions);
        domainPermissions.put(REAL_WORLD_TESTING, realWorldTestingDomainPermissions);
        domainPermissions.put(SCHEDULER, schedulerDomainPermissions);
        domainPermissions.put(SECURED_USER, securedUserDomainPermissions);
        domainPermissions.put(STANDARD, standardDomainPermissions);
        domainPermissions.put(SUBSCRIPTION, subscriptionDomainPermissions);
        domainPermissions.put(SURVEILLANCE, surveillanceDomainPermissions);
        domainPermissions.put(SURVEILLANCE_REPORT, surveillanceReportDomainPermissions);
        domainPermissions.put(SVAP, svapDomainPermissions);
        domainPermissions.put(TEST_TOOL, testToolDomainPermissions);
        domainPermissions.put(TESTING_LAB, testingLabDomainPermissions);
        domainPermissions.put(UCD_PROCESS, ucdProcessDomainPermissions);
        domainPermissions.put(USER_PERMISSIONS, userPermissionsDomainPermissions);
    }

    public boolean hasAccess(final String domain, final String action) {
        if (domainPermissions.containsKey(domain)) {
            return domainPermissions.get(domain).hasAccess(action);
        } else {
            return false;
        }
    }

    public boolean hasAccess(final String domain, final String action, final Object obj) {
        if (domainPermissions.containsKey(domain)) {
            return domainPermissions.get(domain).hasAccess(action, obj);
        } else {
            return false;
        }
    }
}
