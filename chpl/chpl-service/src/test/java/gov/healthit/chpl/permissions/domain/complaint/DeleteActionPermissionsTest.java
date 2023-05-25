package gov.healthit.chpl.permissions.domain.complaint;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.complaint.ComplaintDAO;
import gov.healthit.chpl.complaint.domain.Complaint;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.complaint.DeleteActionPermissions;

public class DeleteActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private ComplaintDAO complaintDAO;

    @InjectMocks
    private DeleteActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser()).thenReturn(getAllAcbForUser(2L, 4L));
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);
        assertFalse(permissions.hasAccess());

        assertTrue(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(new Complaint()));
    }

    @Override
    @Test
    public void hasAccess_OncStaff() throws Exception {
        setupForOncStaffUser(resourcePermissions);

        assertFalse(permissions.hasAccess());
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);
        assertFalse(permissions.hasAccess());

        Complaint complaint = new Complaint();
        complaint.setId(1L);
        complaint.setCertificationBody(new CertificationBody());
        complaint.getCertificationBody().setId(2L);

        Mockito.when(complaintDAO.getComplaint(ArgumentMatchers.anyLong())).thenReturn(complaint);
        assertTrue(permissions.hasAccess(1L));

        complaint.getCertificationBody().setId(1L);
        Mockito.when(complaintDAO.getComplaint(ArgumentMatchers.anyLong())).thenReturn(complaint);
        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);
        assertFalse(permissions.hasAccess());

        assertFalse(permissions.hasAccess(1L));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);
        assertFalse(permissions.hasAccess());

        assertFalse(permissions.hasAccess(1L));
    }

}
