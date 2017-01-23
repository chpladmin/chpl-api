package gov.healthit.chpl.dao.impl;

import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.MeaningfulUseDAO;
import gov.healthit.chpl.dto.MeaningfulUseAccurateAsOfDTO;import gov.healthit.chpl.entity.MeaningfulUseAccurateAsOfEntity;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class MeaningfulUseDAOTest {
	@Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;
	
	@Autowired private MeaningfulUseDAO meaningfulUseDao;
	
	private static JWTAuthenticatedUser authUser;

	@BeforeClass
	public static void setUpClass() throws Exception {
		authUser = new JWTAuthenticatedUser();
		authUser.setFirstName("Admin");
		authUser.setId(-2L);
		authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}
	
	/**
	 * Given a user has set the Meaningful Use User Accurate As Of Date on the UI
	 * When the user sends an HTTP.GET request
	 * Then the MeaningfulUseDAO returns the date
	 */
	@Test
	@Transactional
	public void getAccurateAsOfDate() {
		MeaningfulUseAccurateAsOfDTO accurateAsOfDTO = meaningfulUseDao.getMeaningfulUseAccurateAsOf();
		assertTrue(accurateAsOfDTO.getAccurateAsOfDate() != null);
		assertTrue(accurateAsOfDTO.getAccurateAsOfDate().getTime() >= 1480482000000L);
	}
	
	/**
	 * Given a user has set the Meaningful Use User Accurate As Of Date on the UI
	 * When the user sends an HTTP.GET request
	 * Then the MeaningfulUseDAO returns the date
	 */
	@Test
	@Transactional
	@Rollback
	public void updateAccurateAsOfDate() {
		MeaningfulUseAccurateAsOfDTO accurateAsOfDTO = meaningfulUseDao.getMeaningfulUseAccurateAsOf();
		Calendar cal = Calendar.getInstance();
		Long timeInMillis = cal.getTimeInMillis();
		Date date = new Date(timeInMillis);
		
		accurateAsOfDTO.setAccurateAsOfDate(date);
		meaningfulUseDao.updateAccurateAsOf(accurateAsOfDTO);
		accurateAsOfDTO = meaningfulUseDao.getMeaningfulUseAccurateAsOf();
		Date returnedDate = accurateAsOfDTO.getAccurateAsOfDate();
		assertTrue(date.compareTo(returnedDate) == 0);
	}
}
