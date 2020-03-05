package old.gov.healthit.chpl.auth.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.auth.authentication.Authenticator;
import gov.healthit.chpl.auth.authentication.JWTUserConverterImpl;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.auth.UserPermissionDAO;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.auth.UserPermissionDTO;
import gov.healthit.chpl.exception.JWTCreationException;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
public class JWTUserConverterImplTest {

    @Autowired
    private UserPermissionDAO permDao;

    @Autowired
    private JWTUserConverterImpl converter;

    @Autowired
    private Authenticator authenticator;

    @Test
    public void converterConvertsJWTToUser() throws JWTCreationException, JWTValidationException {

        UserDTO testUser = new UserDTO();
        testUser.setId(-2L);
        testUser.setAccountEnabled(true);
        testUser.setAccountExpired(false);
        testUser.setAccountLocked(false);
        testUser.setCredentialsExpired(false);
        testUser.setEmail("test@test.com");
        testUser.setFullName("admin");
        testUser.setPhoneNumber("123-456-7890");
        testUser.setSubjectName("testUser");
        testUser.setTitle("Dr.");
        try {
            UserPermissionDTO permission = permDao.getPermissionFromAuthority(Authority.ROLE_ADMIN);
            testUser.setPermission(permission);
        } catch (UserPermissionRetrievalException ex) {
            fail("Could not find permission for " + Authority.ROLE_ADMIN);
        }

        String jwt = authenticator.getJWT(testUser);
        User user = converter.getAuthenticatedUser(jwt);

        assertEquals(user.getFullName(), testUser.getFullName());
        assertEquals(user.getSubjectName(), testUser.getSubjectName());

    }

    @Test(expected = JWTValidationException.class)
    public void converterConvertsRejectsInvalidStringForJWTToUser() throws JWTValidationException {

        String garbage = "Garbage In";
        converter.getAuthenticatedUser(garbage);
    }
}
