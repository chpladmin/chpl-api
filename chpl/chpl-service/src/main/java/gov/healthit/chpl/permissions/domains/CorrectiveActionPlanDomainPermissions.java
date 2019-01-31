package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Qualifier;

import gov.healthit.chpl.permissions.domains.correctiveactionplans.AddCertificationsActionPermissions;
import gov.healthit.chpl.permissions.domains.correctiveactionplans.AddDocumentationActionPermissions;
import gov.healthit.chpl.permissions.domains.correctiveactionplans.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.correctiveactionplans.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.correctiveactionplans.RemoveCertificationsActionPermissions;
import gov.healthit.chpl.permissions.domains.correctiveactionplans.RemoveDocumentationActionPermissions;
import gov.healthit.chpl.permissions.domains.correctiveactionplans.UpdateActionPermissions;
import gov.healthit.chpl.permissions.domains.correctiveactionplans.UpdateCertificationActionPermissions;

public class CorrectiveActionPlanDomainPermissions extends DomainPermissions {
    public static final String ADD_CERTIFICATIONS = "ADD_CERTIFICATIONS";
    public static final String ADD_DOCUMENTATION = "ADD_DOCUMENTATION";
    public static final String CREATE = "CREATE";
    public static final String DELETE = "DELETE";
    public static final String REMOVE_CERTIFICATIONS = "REMOVE_CERTIFICATIONS";
    public static final String REMOVE_DOCUMENTATION = "REMOVE_DOCUMENTATION";
    public static final String UPDATE = "UPDATE";
    public static final String UPDATE_CERTIFICATION = "UPDATE_CERTIFICATION";

    public CorrectiveActionPlanDomainPermissions(
            @Qualifier("correctiveActionPlansAddCertificationsActionPermissions") AddCertificationsActionPermissions addCertificationsActionPermissions,
            @Qualifier("correctiveActionPlansAddDocumentationActionPermissions") AddDocumentationActionPermissions addDocumentationActionPermissions,
            @Qualifier("correctiveActionPlansCreateActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("correctiveActionPlansDeleteActionPermissions") DeleteActionPermissions deleteActionPermissions,
            @Qualifier("correctiveActionPlansRemoveCertificationsActionPermissions") RemoveCertificationsActionPermissions removeCertificationsActionPermissions,
            @Qualifier("correctiveActionPlansRemoveDocumentationActionPermissions") RemoveDocumentationActionPermissions removeDocumentationActionPermissions,
            @Qualifier("correctiveActionPlansUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("correctiveActionPlansUpdateCertificationActionPermissions") UpdateCertificationActionPermissions updateCertificationActionPermissions) {

        getActionPermissions().put(ADD_CERTIFICATIONS, addCertificationsActionPermissions);
        getActionPermissions().put(ADD_DOCUMENTATION, addDocumentationActionPermissions);
        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(DELETE, deleteActionPermissions);
        getActionPermissions().put(REMOVE_CERTIFICATIONS, removeCertificationsActionPermissions);
        getActionPermissions().put(REMOVE_DOCUMENTATION, removeDocumentationActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(UPDATE_CERTIFICATION, updateCertificationActionPermissions);
    }
}
