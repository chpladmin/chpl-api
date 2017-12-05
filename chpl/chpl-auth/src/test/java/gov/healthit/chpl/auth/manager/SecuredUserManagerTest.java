package gov.healthit.chpl.auth.manager;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.auth.CHPLAuthenticationSecurityTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class SecuredUserManagerTest {
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    
	@Test
	public void checkDifferentPasswordsWithSalt() {
	    String pw = "katys123Password";
	    String encodedPassword = bCryptPasswordEncoder.encode(pw);
	    String encodedPassword2 = bCryptPasswordEncoder.encode(pw);
	    System.out.println(encodedPassword);
	    System.out.println(encodedPassword2);
	    assertNotEquals(encodedPassword, encodedPassword2);
	    
	    assertTrue(bCryptPasswordEncoder.matches(pw, encodedPassword));
	    assertTrue(bCryptPasswordEncoder.matches(pw, encodedPassword2));
	    assertFalse(bCryptPasswordEncoder.matches("different123Password", encodedPassword2));
	}

}
