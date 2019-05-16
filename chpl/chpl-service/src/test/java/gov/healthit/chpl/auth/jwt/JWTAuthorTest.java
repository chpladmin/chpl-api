package gov.healthit.chpl.auth.jwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jose4j.jwt.consumer.InvalidJwtException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.UserRetrievalException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class JWTAuthorTest {

    @Autowired
    private UserDAO userDao;

    @Autowired
    private JWTAuthor jwtAuthor;

    @Autowired
    private JWTConsumer jwtConsumer;

    @Test
    public void testCreateJWT() throws UserRetrievalException {
        String role = "ROLE_SUPERSTAR";
        UserDTO user = userDao.getById(-2L);
        Map<String, String> claims = new HashMap<String, String>();
        claims.put("Authority", role);
        String jwt = jwtAuthor.createJWT(user, claims, null);

        Map<String, Object> claimObjects;
        try {
            claimObjects = jwtConsumer.consume(jwt);
            String recoveredRole = (String) claimObjects.get("Authority");
            assertEquals(role, recoveredRole);
        } catch (InvalidJwtException e) {
            fail();
        }

    }

}
