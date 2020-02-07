package old.gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.dao.UserTestingLabMapDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;
import gov.healthit.chpl.dto.UserTestingLabMapDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.manager.impl.UserPermissionsManagerImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
public class UserPermissionsManagerImplTest {
    @Mock
    private UserCertificationBodyMapDAO userCertificationBodyMapDAO;

    @Mock
    private UserTestingLabMapDAO userTestingLabMapDAO;

    @Mock
    private UserDeveloperMapDAO userDeveloperMapDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private UserManager userManager;

    @Mock
    private MutableAclService mutableAclService;

    @Mock
    private ActivityManager activityManager;

    @InjectMocks
    private UserPermissionsManagerImpl manager;

    @Before
    public void setup() throws JsonProcessingException, EntityCreationException, EntityRetrievalException {
        MockitoAnnotations.initMocks(this);

        // activityManager.addActivity(ActivityConcept.USER, userId, message, originalUser, updatedUser);
        Mockito.doNothing().when(activityManager).addActivity(ArgumentMatchers.any(ActivityConcept.class),
                ArgumentMatchers.anyLong(), ArgumentMatchers.anyString(), ArgumentMatchers.any(),
                ArgumentMatchers.any());
    }

    @Test
    public void addAcbPermission() throws Exception {
        Mockito.when(userCertificationBodyMapDAO.getByUserId(ArgumentMatchers.anyLong()))
                .thenReturn(new ArrayList<UserCertificationBodyMapDTO>());

        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1L);
        manager.addAcbPermission(dto, 2L);

        Mockito.verify(userCertificationBodyMapDAO).create(ArgumentMatchers.any(UserCertificationBodyMapDTO.class));
    }

    @Test
    public void addAcbPermission_PermissionAlreadyExists() throws Exception {
        List<UserCertificationBodyMapDTO> ucbms = new ArrayList<UserCertificationBodyMapDTO>();
        UserCertificationBodyMapDTO ucbm = new UserCertificationBodyMapDTO();
        ucbm.setId(1L);
        UserDTO user = new UserDTO();
        user.setId(2L);
        ucbm.setUser(user);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(1L);
        ucbm.setCertificationBody(acb);
        ucbms.add(ucbm);
        Mockito.when(userCertificationBodyMapDAO.getByUserId(ArgumentMatchers.anyLong())).thenReturn(ucbms);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(2L);
        Mockito.when(userDAO.getById(ArgumentMatchers.anyLong())).thenReturn(userDTO);

        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1L);
        manager.addAcbPermission(dto, 2L);

        // Since the user already has permission - defined above, the 'create'
        // method should not be called
        Mockito.verify(userCertificationBodyMapDAO, Mockito.times(0))
                .create(ArgumentMatchers.any(UserCertificationBodyMapDTO.class));
    }

    @Test
    public void deleteAcbPermission() throws Exception {
        List<UserCertificationBodyMapDTO> ucbms = new ArrayList<UserCertificationBodyMapDTO>();
        UserCertificationBodyMapDTO ucbm = new UserCertificationBodyMapDTO();
        ucbm.setId(1L);
        UserDTO user = new UserDTO();
        user.setId(2L);
        ucbm.setUser(user);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(1L);
        ucbm.setCertificationBody(acb);
        ucbms.add(ucbm);
        Mockito.when(userCertificationBodyMapDAO.getByUserId(ArgumentMatchers.anyLong())).thenReturn(ucbms);

        Mockito.when(userDAO.getById(ArgumentMatchers.anyLong())).thenReturn(user);
        Mockito.doNothing().when(activityManager).addActivity(
                ArgumentMatchers.any(), ArgumentMatchers.anyLong(), ArgumentMatchers.anyString(),
                ArgumentMatchers.any(), ArgumentMatchers.any());

        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1L);
        manager.deleteAcbPermission(dto, 2l);

        // Ensure the DAO 'delete' method was called...
        Mockito.verify(userCertificationBodyMapDAO).delete(ArgumentMatchers.any(UserCertificationBodyMapDTO.class));

        // Ensure the activity was added
        Mockito.verify(activityManager).addActivity(
                ArgumentMatchers.any(), ArgumentMatchers.anyLong(), ArgumentMatchers.anyString(),
                ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    public void deleteAcbPermission_PermissionsDoesNotExist() throws Exception {
        List<UserCertificationBodyMapDTO> ucbms = new ArrayList<UserCertificationBodyMapDTO>();
        UserCertificationBodyMapDTO ucbm = new UserCertificationBodyMapDTO();
        ucbm.setId(1L);
        UserDTO user = new UserDTO();
        user.setId(2L);
        ucbm.setUser(user);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(2L);
        ucbm.setCertificationBody(acb);
        ucbms.add(ucbm);
        Mockito.when(userCertificationBodyMapDAO.getByUserId(ArgumentMatchers.anyLong())).thenReturn(ucbms);

        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1L);
        manager.deleteAcbPermission(dto, 2l);

        // Ensure the DAO 'delete' method was not called...
        Mockito.verify(userCertificationBodyMapDAO, Mockito.times(0))
                .delete(ArgumentMatchers.any(UserCertificationBodyMapDTO.class));
    }

    @Test
    public void addAtlPermission_PermissionAlreadyExists() throws Exception {
        List<UserTestingLabMapDTO> utlms = new ArrayList<UserTestingLabMapDTO>();
        UserTestingLabMapDTO utlm = new UserTestingLabMapDTO();
        utlm.setId(1L);
        UserDTO user = new UserDTO();
        user.setId(2L);
        utlm.setUser(user);
        TestingLabDTO atl = new TestingLabDTO();
        atl.setId(1L);
        utlm.setTestingLab(atl);
        utlms.add(utlm);
        Mockito.when(userTestingLabMapDAO.getByUserId(ArgumentMatchers.anyLong())).thenReturn(utlms);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(2L);
        Mockito.when(userDAO.getById(ArgumentMatchers.anyLong())).thenReturn(userDTO);

        TestingLabDTO dto = new TestingLabDTO();
        dto.setId(1L);
        manager.addAtlPermission(dto, 2L);

        // Since the user already has permission - defined above, the 'create'
        // method should not be called
        Mockito.verify(userTestingLabMapDAO, Mockito.times(0)).create(ArgumentMatchers.any(UserTestingLabMapDTO.class));
    }

    @Test
    public void deleteAtlPermission() throws Exception {
        List<UserTestingLabMapDTO> utlms = new ArrayList<UserTestingLabMapDTO>();
        UserTestingLabMapDTO utlm = new UserTestingLabMapDTO();
        utlm.setId(1L);
        UserDTO user = new UserDTO();
        user.setId(2L);
        utlm.setUser(user);
        TestingLabDTO atl = new TestingLabDTO();
        atl.setId(1L);
        utlm.setTestingLab(atl);
        utlms.add(utlm);
        Mockito.when(userTestingLabMapDAO.getByUserId(ArgumentMatchers.anyLong())).thenReturn(utlms);

        TestingLabDTO dto = new TestingLabDTO();
        dto.setId(1L);
        manager.deleteAtlPermission(dto, 2l);

        // Ensure the DAO 'delete' method was called...
        Mockito.verify(userTestingLabMapDAO).delete(ArgumentMatchers.any(UserTestingLabMapDTO.class));
    }

    @Test
    public void deleteAtlPermission_PermissionsDoesNotExist() throws Exception {
        List<UserTestingLabMapDTO> utlms = new ArrayList<UserTestingLabMapDTO>();
        UserTestingLabMapDTO utlm = new UserTestingLabMapDTO();
        utlm.setId(1L);
        UserDTO user = new UserDTO();
        user.setId(2L);
        utlm.setUser(user);
        TestingLabDTO atl = new TestingLabDTO();
        atl.setId(2L);
        utlm.setTestingLab(atl);
        utlms.add(utlm);
        Mockito.when(userTestingLabMapDAO.getByUserId(ArgumentMatchers.anyLong())).thenReturn(utlms);

        TestingLabDTO dto = new TestingLabDTO();
        dto.setId(1L);
        manager.deleteAtlPermission(dto, 2l);

        // Ensure the DAO 'delete' method was not called...
        Mockito.verify(userTestingLabMapDAO, Mockito.times(0)).delete(ArgumentMatchers.any(UserTestingLabMapDTO.class));
    }
}
