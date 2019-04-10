package gov.healthit.chpl.auth.jwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dto.auth.UserDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class JWTConsumerTest {

    @Autowired
    private UserDAO userDao;

    @Autowired
    private JWTAuthor jwtAuthor;

    @Autowired
    private JWTConsumer jwtConsumer;

    @Test
    public void consumerIsNotNull() {
        assertNotNull(jwtConsumer);
    }

    @Test
    public void testConsumer() throws Exception {
        String role = "ROLE_SUPERSTAR";
        UserDTO user = userDao.getById(-2L);
        Map<String, String> claims = new HashMap<String, String>();
        claims.put("Role", role);
        String jwt = jwtAuthor.createJWT(user, claims, null);

        Map<String, Object> claimObjects = jwtConsumer.consume(jwt);
        String recoveredRole = (String) claimObjects.get("Role");
        assertEquals(role, recoveredRole);

    }

}
