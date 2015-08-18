package gov.healthit.chpl.auth.jwt;

import static org.junit.Assert.assertNotNull;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(classes = { gov.healthit.chpl.auth.CHPLAuthenticationSecurityTestConfig.class })
public class JWTConsumerRsaJoseJImplTest {
	
	@Autowired
	JWTConsumer consumer;
	
	@Test
	public void consumerIsNotNull(){
		assertNotNull(consumer);
	}
	
	
    @BeforeClass
    public static void setUpClass() throws Exception {
        
    }
    
}
