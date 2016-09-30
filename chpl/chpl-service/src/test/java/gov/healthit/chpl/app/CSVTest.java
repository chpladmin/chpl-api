package gov.healthit.chpl.app;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml") 
public class CSVTest {

//	@Autowired 
//	private CSV csv;
	
	/** Description: Tests the 
	 * Expected Result: 
	 * Assumptions: 
	 * @throws Exception 
	 */
	@Transactional
	@Rollback(true)
	@Test
	public void test_getCommaSeparatedList_ReturnsValidResult() throws Exception{
		TableHeader dateHeader = new TableHeader("Date", 17, String.class);
		TableHeader totalDevsHeader = new TableHeader("Total Developers", 17, String.class);
		TableHeader totalProdsHeader = new TableHeader("Total Products", 15, String.class);
		TableHeader totalCPsHeader = new TableHeader("Total CPs", 10, String.class);
		TableHeader totalCPs2014Header = new TableHeader("Total 2014 CPs", 15, String.class);
		TableHeader totalCPs2015Header = new TableHeader("Total 2015 CPs", 15, String.class);
		
		List<TableHeader> tableHeaders = new LinkedList<TableHeader>();
		tableHeaders.addAll(Arrays.asList(dateHeader, totalDevsHeader, totalProdsHeader, totalCPsHeader, totalCPs2014Header, totalCPs2015Header));
		
		List<String> list = CSV.getCommaSeparatedList(tableHeaders, "headerName");
		assertNotNull(list);
		//assertNotNull("getCommaSeparatedOutput returned null results", commaSeparatedOutput);
		//Assert.assertTrue("getCommaSeparatedOutput should return valid results", commaSeparatedOutput.size() > 0);
		
	}
}
