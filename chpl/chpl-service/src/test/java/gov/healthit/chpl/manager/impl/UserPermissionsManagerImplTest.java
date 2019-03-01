package gov.healthit.chpl.manager.impl;

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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class UserPermissionsManagerImplTest {
    @Mock
    private UserCertificationBodyMapDAO userCertificationBodyMapDAO;

    @Mock
    private UserDAO userDAO;

    // @Mock
    // private Permissions permissionChecker;

    @InjectMocks
    private UserPermissionsManagerImpl manager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
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

        CertificationBodyDTO dto = new CertificationBodyDTO();
        dto.setId(1L);
        manager.deleteAcbPermission(dto, 2l);

        // Ensure the DAO 'delete' method was called...
        Mockito.verify(userCertificationBodyMapDAO).delete(ArgumentMatchers.any(UserCertificationBodyMapDTO.class));
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
    public void deleteAllAcbPermissionsForUser() throws Exception {
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

        manager.deleteAllAcbPermissionsForUser(2l);

        // Ensure the DAO 'delete' method was called...
        Mockito.verify(userCertificationBodyMapDAO, Mockito.times(1))
                .delete(ArgumentMatchers.any(UserCertificationBodyMapDTO.class));

    }

    @Test
    public void deleteAllAcbPermissionsForUser_2Acbs() throws Exception {
        List<UserCertificationBodyMapDTO> ucbms = new ArrayList<UserCertificationBodyMapDTO>();
        UserCertificationBodyMapDTO ucbm1 = new UserCertificationBodyMapDTO();
        ucbm1.setId(1L);
        UserDTO user = new UserDTO();
        user.setId(2L);
        ucbm1.setUser(user);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(2L);
        ucbm1.setCertificationBody(acb);
        ucbms.add(ucbm1);

        UserCertificationBodyMapDTO ucbm2 = new UserCertificationBodyMapDTO();
        ucbm2.setId(2L);
        ucbm2.setUser(user);
        CertificationBodyDTO acb2 = new CertificationBodyDTO();
        acb.setId(3L);
        ucbm2.setCertificationBody(acb);
        ucbms.add(ucbm2);
        Mockito.when(userCertificationBodyMapDAO.getByUserId(ArgumentMatchers.anyLong())).thenReturn(ucbms);

        manager.deleteAllAcbPermissionsForUser(2l);

        // Ensure the DAO 'delete' method was called twice...
        Mockito.verify(userCertificationBodyMapDAO, Mockito.times(2))
                .delete(ArgumentMatchers.any(UserCertificationBodyMapDTO.class));
    }

    @Test
    public void deleteAllAcbPermissionsForUser_0Acbs() throws Exception {
        List<UserCertificationBodyMapDTO> ucbms = new ArrayList<UserCertificationBodyMapDTO>();
        Mockito.when(userCertificationBodyMapDAO.getByUserId(ArgumentMatchers.anyLong())).thenReturn(ucbms);

        manager.deleteAllAcbPermissionsForUser(2l);

        // Ensure the DAO 'delete' method was not called...
        Mockito.verify(userCertificationBodyMapDAO, Mockito.times(0))
                .delete(ArgumentMatchers.any(UserCertificationBodyMapDTO.class));
    }

    @Test
    public void deleteAllAcbPermissionsForUser_ListOfUserCertBodyMapsIsNull() throws Exception {
        List<UserCertificationBodyMapDTO> ucbms = new ArrayList<UserCertificationBodyMapDTO>();
        Mockito.when(userCertificationBodyMapDAO.getByUserId(ArgumentMatchers.anyLong())).thenReturn(null);

        manager.deleteAllAcbPermissionsForUser(2l);

        // Ensure the DAO 'delete' method was not called...
        Mockito.verify(userCertificationBodyMapDAO, Mockito.times(0))
                .delete(ArgumentMatchers.any(UserCertificationBodyMapDTO.class));
    }

}
