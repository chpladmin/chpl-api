package old.gov.healthit.chpl.changerequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestStatusService;
import gov.healthit.chpl.dto.auth.UserPermissionDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import old.gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import old.gov.healthit.chpl.changerequest.builders.ChangeRequestStatusBuilder;
import old.gov.healthit.chpl.changerequest.builders.ChangeRequestStatusTypeBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
public class ChangeRequestStatusServiceTest {
    @Mock
    private ChangeRequestStatusDAO crStatusDAO;

    @Mock
    private ChangeRequestStatusTypeDAO crStatusTypeDAO;

    @InjectMocks
    private ChangeRequestStatusService changeRequestStatusHelper;

    @Value("${changerequest.status.pendingacbaction}")
    private Long pendingAcbActionStatus;

    @Value("${changerequest.status.cancelledbyrequester}")
    private Long cancelledByRequesterStatus;

    @Mock
    private ResourcePermissions resourcePermissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void saveInitialStatus_Success() throws EntityRetrievalException {
        // Setup
        setupForAcbUser(resourcePermissions);

        Mockito.when(crStatusDAO.create(ArgumentMatchers.any(ChangeRequest.class),
                ArgumentMatchers.any(ChangeRequestStatus.class)))
                .thenReturn(new ChangeRequestStatusBuilder()
                        .withId(1l)
                        .withComment("")
                        .withStatusChangeDate(new Date())
                        .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(pendingAcbActionStatus)
                                .withName("Pending ONC-ACB Action")
                                .build())
                        .build());

        Mockito.when(resourcePermissions.getRoleByUserId(ArgumentMatchers.anyLong()))
                .thenReturn(getAcbUserPermission());

        // Run
        ChangeRequestStatus crStatus = changeRequestStatusHelper.saveInitialStatus(
                new ChangeRequestBuilder().withId(1l).build());

        // Check
        assertNotNull(crStatus);
        assertNotNull(crStatus.getId());
        assertEquals(pendingAcbActionStatus, crStatus.getChangeRequestStatusType().getId());
    }

    @Test(expected = EntityRetrievalException.class)
    public void saveInitialStatus_Exception() throws EntityRetrievalException {
        // Setup
        setupForAcbUser(resourcePermissions);

        Mockito.when(crStatusDAO.create(ArgumentMatchers.any(ChangeRequest.class),
                ArgumentMatchers.any(ChangeRequestStatus.class)))
                .thenThrow(EntityRetrievalException.class);

        Mockito.when(resourcePermissions.getRoleByUserId(ArgumentMatchers.anyLong()))
                .thenReturn(getAcbUserPermission());

        // Run
        changeRequestStatusHelper.saveInitialStatus(
                new ChangeRequestBuilder().withId(1l).build());
    }

    private void setupForAcbUser(ResourcePermissions resourcePermissions) {
        SecurityContextHolder.getContext().setAuthentication(getAcbUser());
    }

    private JWTAuthenticatedUser getAcbUser() {
        JWTAuthenticatedUser acbUser = new JWTAuthenticatedUser();
        acbUser.setFullName("Test");
        acbUser.setId(3L);
        acbUser.setFriendlyName("User3");
        acbUser.setSubjectName("testUser3");
        acbUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));
        return acbUser;
    }

    private UserPermissionDTO getAcbUserPermission() {
        UserPermissionDTO dto = new UserPermissionDTO();
        dto.setId(2l);
        dto.setDescription("Description of ROLE_ACB");
        dto.setName("ACB");
        dto.setAuthority("ROLE_ACB");
        return dto;
    }
}
