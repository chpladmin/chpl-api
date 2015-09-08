package gov.healthit.chpl.manager.impl;

import java.util.List;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class CertifiedProductSearchManagerTest extends TestCase {
	
	
	@Autowired
	private CertifiedProductSearchManager certifiedProductSearchManager;
	
	@Test
	public void testMultiFilterSearch(){
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setVendor("Test");
		SearchResponse response = certifiedProductSearchManager.multiFilterSearch(searchRequest, 0, 10);
		assertEquals((int)response.getRecordCount(), response.getResults().size());
		assertEquals((int)response.getResults().size(), 3);
		
		searchRequest.setVersion("1.0.0");
		SearchResponse responseWithVersion = certifiedProductSearchManager.multiFilterSearch(searchRequest, 0, 10);
		assertEquals((int) responseWithVersion.getRecordCount(),  responseWithVersion.getResults().size());
		assertEquals((int) responseWithVersion.getResults().size(), 1);
	}
	
	@Test
	public void testSimpleSearch(){
		SearchResponse response = certifiedProductSearchManager.simpleSearch("Test", 0, 10);
		assertEquals((int)response.getRecordCount(), response.getResults().size());
		assertEquals(3, response.getResults().size());
		
		SearchResponse responseSorted = certifiedProductSearchManager.simpleSearch("Test", 0, 10, "product", false);
		assertEquals((int)responseSorted.getRecordCount(), responseSorted.getResults().size());
		assertEquals(3, responseSorted.getResults().size());
	}
	
	@Test
	public void TestGetCertifiedProductDetails() throws EntityRetrievalException{
		
		CertifiedProductSearchDetails details = certifiedProductSearchManager.getCertifiedProductDetails(1L);
		
		
	}
	
	
}
