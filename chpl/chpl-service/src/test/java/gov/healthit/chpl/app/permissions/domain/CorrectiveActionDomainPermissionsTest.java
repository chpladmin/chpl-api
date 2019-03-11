package gov.healthit.chpl.app.permissions.domain;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.permissions.domains.CorrectiveActionPlanDomainPermissions;
import gov.healthit.chpl.permissions.domains.correctiveactionplans.AddCertificationsActionPermissions;
import gov.healthit.chpl.permissions.domains.correctiveactionplans.AddDocumentationActionPermissions;
import gov.healthit.chpl.permissions.domains.correctiveactionplans.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.correctiveactionplans.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.correctiveactionplans.RemoveCertificationsActionPermissions;
import gov.healthit.chpl.permissions.domains.correctiveactionplans.RemoveDocumentationActionPermissions;
import gov.healthit.chpl.permissions.domains.correctiveactionplans.UpdateActionPermissions;
import gov.healthit.chpl.permissions.domains.correctiveactionplans.UpdateCertificationActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class CorrectiveActionDomainPermissionsTest {
    @Autowired
    private CorrectiveActionPlanDomainPermissions permissions;

    @Test
    public void setupTest() {
        assertTrue(permissions.getActionPermissions().size() == 8);

        assertTrue(permissions.getActionPermissions().get(
                CorrectiveActionPlanDomainPermissions.ADD_CERTIFICATIONS) instanceof AddCertificationsActionPermissions);

        assertTrue(permissions.getActionPermissions().get(
                CorrectiveActionPlanDomainPermissions.ADD_DOCUMENTATION) instanceof AddDocumentationActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(CorrectiveActionPlanDomainPermissions.CREATE) instanceof CreateActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(CorrectiveActionPlanDomainPermissions.DELETE) instanceof DeleteActionPermissions);

        assertTrue(permissions.getActionPermissions().get(
                CorrectiveActionPlanDomainPermissions.REMOVE_CERTIFICATIONS) instanceof RemoveCertificationsActionPermissions);

        assertTrue(permissions.getActionPermissions().get(
                CorrectiveActionPlanDomainPermissions.REMOVE_DOCUMENTATION) instanceof RemoveDocumentationActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(CorrectiveActionPlanDomainPermissions.UPDATE) instanceof UpdateActionPermissions);

        assertTrue(permissions.getActionPermissions().get(
                CorrectiveActionPlanDomainPermissions.UPDATE_CERTIFICATION) instanceof UpdateCertificationActionPermissions);
    }
}
