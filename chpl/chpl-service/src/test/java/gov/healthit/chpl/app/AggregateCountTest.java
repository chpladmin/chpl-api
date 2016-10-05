package gov.healthit.chpl.app;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.context.SecurityContextHolder;
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
import gov.healthit.chpl.domain.AggregateCount;
import gov.healthit.chpl.dto.DeveloperDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class AggregateCountTest {
	private static JWTAuthenticatedUser adminUser;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}
	
	/** Description: Tests getCountDuringPeriod
	 * For a given period of output:
    For an item not marked as deleted:
    The item will be counted if its creationDate is before the periodEndDate
    For an item marked as deleted:
    The item will be counted if its creationDate is before the periodEndDate AND the lastModifiedDate is after the periodEndDate
    The item will not be counted if the lastModifiedDate is before the periodEndDate

	 * Expected Result: 
	 * 1. Non-deleted developer with creationDate before periodEndDate is counted
	 * 2. Non-deleted developer with creationDate after periodEndDate is NOT counted
	 * 3. Deleted developer is counted when creationDate is before periodEndDate AND the lastModifiedDate is after the periodEndDate
	 * 4. Deleted developer is NOT counted when creationDate is before periodEndDate but the lastModifiedDate is before the periodEndDate
	 * Assumptions: 
	 * @throws Exception 
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_getCountDuringPeriod_ReturnsValidResults() throws Exception{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		List<DeveloperDTO> developerDTOs = new ArrayList<DeveloperDTO>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		// 1. Non-deleted developer with creationDate before periodEndDate is counted
		DeveloperDTO firstDTO = new DeveloperDTO();
		firstDTO.setCreationDate(dateFormat.parse("2016-05-01"));
		firstDTO.setDeleted(false);
		firstDTO.setId(1001L);
		firstDTO.setName("nonDeletedDTO");
		firstDTO.setDeveloperCode("firstDTODevCode");
		developerDTOs.add(firstDTO);
		AggregateCount firstDeveloperCount = new AggregateCount(developerDTOs);
		Integer devCount = firstDeveloperCount.getCountDuringPeriod(dateFormat.parse("2016-04-01"), dateFormat.parse("2016-10-05"), 
				"creationDate", "lastModifiedDate", "deleted");
		org.junit.Assert.assertTrue("Non-deleted developer with creationDate before periodEndDate should be counted", devCount == 1);
		developerDTOs.remove(firstDTO);
		
		// 2. Non-deleted developer with creationDate after periodEndDate is NOT counted
		DeveloperDTO secondDTO = new DeveloperDTO();
		secondDTO.setCreationDate(dateFormat.parse("2016-11-02"));
		secondDTO.setDeleted(false);
		secondDTO.setId(1002L);
		secondDTO.setName("nonDeletedDTOTwo");
		secondDTO.setDeveloperCode("secondDTODevCode");
		developerDTOs.add(secondDTO);
		AggregateCount secondDeveloperCount = new AggregateCount(developerDTOs);
		devCount = secondDeveloperCount.getCountDuringPeriod(dateFormat.parse("2016-04-01"), dateFormat.parse("2016-10-05"), 
				"creationDate", "lastModifiedDate", "deleted");
		org.junit.Assert.assertTrue("Non-deleted developer with creationDate after periodEndDate should NOT be counted", devCount == 0);
		developerDTOs.remove(secondDTO);
		
		// 3. Deleted developer is counted when creationDate is before periodEndDate AND the lastModifiedDate is after the periodEndDate
		DeveloperDTO thirdDTO = new DeveloperDTO();
		thirdDTO.setCreationDate(dateFormat.parse("2016-5-02"));
		thirdDTO.setDeleted(true);
		thirdDTO.setId(1002L);
		thirdDTO.setName("nonDeletedDTOTwo");
		thirdDTO.setDeveloperCode("secondDTODevCode");
		thirdDTO.setLastModifiedDate(dateFormat.parse("2016-11-01"));
		developerDTOs.add(thirdDTO);
		AggregateCount thirdDeveloperCount = new AggregateCount(developerDTOs);
		devCount = thirdDeveloperCount.getCountDuringPeriod(dateFormat.parse("2016-04-01"), dateFormat.parse("2016-10-05"), 
				"creationDate", "lastModifiedDate", "deleted");
		org.junit.Assert.assertTrue("Deleted developer is counted when creationDate is before periodEndDate AND the lastModifiedDate is after the periodEndDate", devCount == 1);
		developerDTOs.remove(thirdDTO);
		
		// 4. Deleted developer is NOT counted when creationDate is before periodEndDate but the lastModifiedDate is before the periodEndDate
		DeveloperDTO fourthDTO = new DeveloperDTO();
		fourthDTO.setCreationDate(dateFormat.parse("2016-5-02"));
		fourthDTO.setDeleted(true);
		fourthDTO.setId(1002L);
		fourthDTO.setName("nonDeletedDTOTwo");
		fourthDTO.setDeveloperCode("secondDTODevCode");
		fourthDTO.setLastModifiedDate(dateFormat.parse("2016-5-02"));
		developerDTOs.add(fourthDTO);
		AggregateCount fourthDeveloperCount = new AggregateCount(developerDTOs);
		devCount = fourthDeveloperCount.getCountDuringPeriod(dateFormat.parse("2016-04-01"), dateFormat.parse("2016-10-05"), 
				"creationDate", "lastModifiedDate", "deleted");
		org.junit.Assert.assertTrue("Deleted developer is NOT counted when creationDate is before periodEndDate but the lastModifiedDate is before the periodEndDate", devCount == 0);
		developerDTOs.remove(fourthDTO);
	}
}
