package gov.healthit.chpl.app.permissions.domain;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.permissions.domains.SurveillanceDomainPermissions;
import gov.healthit.chpl.permissions.domains.surveillance.AddDocumentActionPermissions;
import gov.healthit.chpl.permissions.domains.surveillance.BasicReportActionPermissions;
import gov.healthit.chpl.permissions.domains.surveillance.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.surveillance.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.surveillance.DeleteDocumentActionPermissions;
import gov.healthit.chpl.permissions.domains.surveillance.UpdateActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class SurveillanceDomainPermissionsTest {
    @Autowired
    private SurveillanceDomainPermissions permissions;

    @Test
    public void setupTest() {
        assertTrue(permissions.getActionPermissions().size() == 6);

        assertTrue(permissions.getActionPermissions()
                .get(SurveillanceDomainPermissions.ADD_DOCUMENT) instanceof AddDocumentActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(SurveillanceDomainPermissions.BASIC_REPORT) instanceof BasicReportActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(SurveillanceDomainPermissions.CREATE) instanceof CreateActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(SurveillanceDomainPermissions.DELETE) instanceof DeleteActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(SurveillanceDomainPermissions.DELETE_DOCUMENT) instanceof DeleteDocumentActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(SurveillanceDomainPermissions.UPDATE) instanceof UpdateActionPermissions);
    }
}
