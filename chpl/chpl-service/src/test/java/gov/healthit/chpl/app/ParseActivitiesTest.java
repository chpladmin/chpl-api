package gov.healthit.chpl.app;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class ParseActivitiesTest {

	@Autowired
	private ParseActivities parseActivities;
	private static JWTAuthenticatedUser adminUser;
	private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}
	
	/** Description: Tests the getCommaSeparatedOutput method of the ParseActivities class
	 * Expected Result: Valid results
	 * Assumptions: tableHeaderFieldsMap and activitiesOutputFieldsMap are populated
	 * @throws Exception 
	 */
	@Transactional
	@Rollback(true)
	@Test
	@Ignore
	public void test_getCommaSeparatedOutput_ReturnsValidResult() throws Exception{
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		String[] args = {"2015-01-01", "2016-09-29", "7"};
		parseActivities.setCommandLineArgs(args);
		InputStream in = DownloadableResourceCreatorApp.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
		parseActivities.loadProperties(in);
		Properties props;
		if (in == null) {
			props = null;
			throw new FileNotFoundException("Environment Properties File not found in class path.");
		} else {
			props = new Properties();
			props.load(in);
			in.close();
		}
		LocalContext ctx = LocalContextFactory.createLocalContext(props.getProperty("dbDriverClass"));
		ctx.addDataSource(props.getProperty("dataSourceName"),props.getProperty("dataSourceConnection"), 
				 props.getProperty("dataSourceUsername"), props.getProperty("dataSourcePassword"));
		 AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		 parseActivities.initializeSpringClasses(context);
		 parseActivities.setNumDaysInSummaryEmail(parseActivities.getNumDaysInSummaryEmail());
		 parseActivities.setSummaryTimePeriod(parseActivities.getSummaryTimePeriod());
		 parseActivities.developerDTOs = parseActivities.developerDAO.findAll();
		 parseActivities.certifiedProductDTOs = parseActivities.certifiedProductDAO.findAll();
		 parseActivities.productDTOs = parseActivities.productDAO.findAll();
		 parseActivities.setCertifiedProductDetailsDTOs();
		 parseActivities.activitiesList = parseActivities.getActivitiesByPeriodUsingStartAndEndDate();
		 parseActivities.setTableHeaders(parseActivities.getTableHeaders());
		 String commaSeparatedOutput = parseActivities.getCommaSeparatedOutput();
		 assertNotNull("getCommaSeparatedOutput returned null results", commaSeparatedOutput);
		 assertTrue("getCommaSeparatedOutput should return valid results", commaSeparatedOutput.length() > 0);
	}
}