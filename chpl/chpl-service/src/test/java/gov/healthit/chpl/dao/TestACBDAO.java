package gov.healthit.chpl.dao;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class TestACBDAO {
	
	@Autowired
	CertificationBodyDAO dao;
	
	@Test
	public void daoIsNotNull(){
		assertNotNull(dao);
	}
}
