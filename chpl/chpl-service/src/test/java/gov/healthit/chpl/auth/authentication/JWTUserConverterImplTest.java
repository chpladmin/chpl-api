package gov.healthit.chpl.auth.authentication;

import static org.junit.Assert.assertEquals;

import org.jose4j.jwt.consumer.InvalidJwtException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.jwt.JWTCreationException;
import gov.healthit.chpl.auth.jwt.JWTValidationException;
import gov.healthit.chpl.auth.user.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.auth.CHPLAuthenticationSecurityTestConfig.class })
public class JWTUserConverterImplTest {

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
