package old.gov.healthit.chpl.app.permissions.domain;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.ConfirmActionPermissions;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.CreateOrReplaceActionPermissions;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.GetAllActionPermissions;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.GetAllMetadataActionPermissions;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.GetByAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.GetDetailsByIdActionPermissions;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.GetDetailsByIdForActivityActionPermissions;
import gov.healthit.chpl.permissions.domains.pendingcertifiedproduct.UpdateableActionPermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
public class PendingCertifiedProductDomainPermissionsTest {
    @Autowired
    private PendingCertifiedProductDomainPermissions permissions;

    @Test
    public void setupTest() {
        assertTrue(permissions.getActionPermissions().size() == 9);

        assertTrue(permissions.getActionPermissions()
                .get(PendingCertifiedProductDomainPermissions.CONFIRM) instanceof ConfirmActionPermissions);

        assertTrue(permissions.getActionPermissions().get(
                PendingCertifiedProductDomainPermissions.CREATE_OR_REPLACE) instanceof CreateOrReplaceActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(PendingCertifiedProductDomainPermissions.DELETE) instanceof DeleteActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(PendingCertifiedProductDomainPermissions.GET_ALL) instanceof GetAllActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(PendingCertifiedProductDomainPermissions.GET_ALL_METADATA) instanceof GetAllMetadataActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(PendingCertifiedProductDomainPermissions.GET_BY_ACB) instanceof GetByAcbActionPermissions);

        assertTrue(permissions.getActionPermissions().get(
                PendingCertifiedProductDomainPermissions.GET_DETAILS_BY_ID) instanceof GetDetailsByIdActionPermissions);

        assertTrue(permissions.getActionPermissions().get(
                PendingCertifiedProductDomainPermissions.GET_DETAILS_BY_ID_FOR_ACTIVITY) instanceof GetDetailsByIdForActivityActionPermissions);

        assertTrue(permissions.getActionPermissions()
                .get(PendingCertifiedProductDomainPermissions.UPDATEABLE) instanceof UpdateableActionPermissions);
    }
}
