package gov.healthit.chpl.manager.impl;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import junit.framework.TestCase;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class CertifiedProductDetailsManagerTest extends TestCase {
	
	
	@Autowired
	private CertifiedProductDetailsManager certifiedProductDetailsManager;
	@Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;
	
	@Test
	@Transactional
	public void testCertifiedProductDetailsReturned() throws EntityRetrievalException{
		
		assertNotNull(certifiedProductDetailsManager.getCertifiedProductDetails(1L));
		
	}
	
	@Test
	@Transactional
	public void testCertifiedProductDetails() throws EntityRetrievalException{
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(1L);	
		assertNotNull(detail);
	}
	
	@Test
	@Transactional
	public void testCertifiedProductDetails_hasProductOwnerHistory() throws EntityRetrievalException {
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(1L);	
		assertNotNull(detail);
		assertNotNull(detail.getProduct());
		assertEquals("Test Product 1", detail.getProduct().getName());
		assertEquals(-1, detail.getProduct().getProductId().longValue());
		assertNotNull(detail.getProduct().getOwnerHistory());
		assertEquals(1, detail.getProduct().getOwnerHistory().size());
	}
	
	@Test
	@Transactional
	public void testCertifiedProductDetailsCertificationDate() throws EntityRetrievalException{
		
		//CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(1L);
		//assertEquals(new Long(1440090840000L).longValue(), detail.getCertificationDate().longValue());
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(1L);
		Calendar expected = GregorianCalendar.getInstance();
		expected.set(2015, 7, 20, 13, 14, 0);
		expected.set(Calendar.MILLISECOND, 0);
		assertEquals(expected.getTime().getTime(), detail.getCertificationDate().longValue());
		
	}
	
	@Test
	@Transactional
	public void testCertifiedProductDetailsDecertificationDate() throws EntityRetrievalException{
		
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(17L);
		Calendar expected = GregorianCalendar.getInstance();
		expected.set(2016, 4, 20, 13, 14, 0);
		expected.set(Calendar.MILLISECOND, 0);
		assertEquals(expected.getTime().getTime(), detail.getDecertificationDate().longValue());
		
	}
	
	@Test
	@Transactional
	public void testCertifiedProductDetailsChplProductNumber() throws EntityRetrievalException{
		
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(1L);
		assertEquals("CHP-024050", detail.getChplProductNumber());		
	}
	
	@Test
	@Transactional
	public void testCertifiedProductDetailsChplProductEdition() throws EntityRetrievalException{
		
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(1L);
		assertEquals("2014", detail.getCertificationEdition().get("name"));
		
	}
	
	@Test
	@Transactional
	public void testCertifiedProductDetailsChplProductCertificationEvents() throws EntityRetrievalException{
		
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(1L);
		assertEquals(1, detail.getCertificationEvents().size());
		
	}
	
	@Test
	@Transactional
	public void testCertifiedProductDetailsCertificationResults() throws EntityRetrievalException{
		
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(1L);
		assertEquals(6 , detail.getCertificationResults().size());
		
		//check additional software
		Boolean hasTwoAdditionalSoftware = false;
		for(CertificationResult result : detail.getCertificationResults()){
			assertNotNull(result.getAdditionalSoftware());
			if(result.getAdditionalSoftware().size() == 2){
				hasTwoAdditionalSoftware = true;
			}
		}
		CertificationResult cert = detail.getCertificationResults().get(1);
		assertNotNull(cert.getAdditionalSoftware());
		assertTrue(hasTwoAdditionalSoftware);
		
		//check test functionality
		assertNull(cert.getTestFunctionality());
		
		//check test standard
		assertNotNull(cert.getTestStandards());
		assertEquals(1, cert.getTestStandards().size());
		CertificationResultTestStandard ts = cert.getTestStandards().get(0);
		assertNotNull(ts.getTestStandardDescription());
		
		//check test procedures
		assertNotNull(cert.getTestProcedures());
		assertEquals(2, cert.getTestProcedures().size());
		CertificationResultTestProcedure tp = cert.getTestProcedures().get(0);
		assertNotNull(tp.getTestProcedureVersion());
		
		//check test data
		assertNotNull(cert.getTestDataUsed());
		assertEquals(1, cert.getTestDataUsed().size());
		
		//test tools
		assertNull(cert.getTestToolsUsed());
	}
	
	@Test
	@Transactional
	public void testCertifiedProductDetailsQms() throws EntityRetrievalException{
		
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(1L);
		assertNotNull(detail.getQmsStandards());
		assertEquals(1 , detail.getQmsStandards().size());
		
		detail = certifiedProductDetailsManager.getCertifiedProductDetails(2L);
		assertNotNull(detail.getQmsStandards());
		assertEquals(2 , detail.getQmsStandards().size());
		CertifiedProductQmsStandard qms = detail.getQmsStandards().get(0);
		assertNotNull(qms.getId());
		assertNotNull(qms.getQmsStandardName());
	}
	
	@Test
	@Transactional
	public void testCertifiedProductDetailsAccessibilityStandards() throws EntityRetrievalException{
		
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(1L);
		assertNotNull(detail.getAccessibilityStandards());
		assertEquals(0 , detail.getAccessibilityStandards().size());
		
	}
	
	@Test
	@Transactional
	public void testCertifiedProductDetailsCountCerts() throws EntityRetrievalException{
		
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(1L);
		assertEquals(4 , detail.getCountCerts().intValue());
	}
	
	@Test
	@Transactional
	public void testCertifiedProductDetailsCQMResults() throws EntityRetrievalException{
		
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(1L);
		assertNotNull(detail.getCqmResults());
		
		int cqmSuccessCount = 0;
		for(CQMResultDetails cqmDetail : detail.getCqmResults()) {
			if(cqmDetail.isSuccess()) {
				cqmSuccessCount++;
			}
		}
		assertEquals(0 ,cqmSuccessCount);
		
		detail = certifiedProductDetailsManager.getCertifiedProductDetails(2L);
		assertNotNull(detail.getCqmResults());
		
		cqmSuccessCount = 0;
		for(CQMResultDetails cqmDetail : detail.getCqmResults()) {
			if(cqmDetail.getId() != null && cqmDetail.getId() == 4L) {
				List<CQMResultCertification> criteriaMapping = cqmDetail.getCriteria();
				assertNotNull(criteriaMapping);
				assertEquals(1, criteriaMapping.size());
				assertEquals(66, criteriaMapping.get(0).getCertificationId().longValue());
			}
			if(cqmDetail.isSuccess()) {
				cqmSuccessCount++;
			}
		}
		assertEquals(2 ,cqmSuccessCount);
	}
	
	@Test
	@Transactional
	public void testCertifiedProductTargetedUsers() throws EntityRetrievalException{
		
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(1L);
		assertNotNull(detail.getTargetedUsers());
		assertEquals(1, detail.getTargetedUsers().size());
		assertEquals("Pediatrics", detail.getTargetedUsers().get(0).getTargetedUserName());
	}
	
	@Test
	@Transactional
	public void testCertifiedProductDetailsCountCQMs() throws EntityRetrievalException{
		
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(1L);
		assertEquals(0, detail.getCountCqms().intValue());
		
	}
	
	@Test
	@Transactional
	public void testCertifiedProductDetailsPracticeType() throws EntityRetrievalException{
		
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(1L);
		assertEquals("Ambulatory", detail.getPracticeType().get("name"));
		assertEquals(1, new Long(detail.getPracticeType().get("id").toString()).longValue());
		
	}
	
	@Test
	@Transactional
	public void testCertifiedProductDetailsProduct() throws EntityRetrievalException{
		
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(1L);
		assertEquals("Test Product 1", detail.getProduct().getName());
		assertEquals(-1, detail.getProduct().getProductId().longValue());
		assertEquals("1.0.0", detail.getVersion().getVersion());
		assertEquals(1, detail.getVersion().getVersionId().longValue());
		
	}

	@Test
	@Transactional
	public void testCertifiedProductDetailsDeveloper() throws EntityRetrievalException{
		
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(1L);
		assertEquals("Test Developer 1", detail.getDeveloper().getName());
		assertEquals(-1, detail.getProduct().getProductId().longValue());
	}

	@Test
	@Transactional
	public void testCertifiedProductDetailsTransparencyAttestation() throws EntityRetrievalException{
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(1L);
		assertEquals("Affirmative", detail.getTransparencyAttestation());
	}
	
	@Test
	@Transactional
	public void testCertifiedProductDetailsTransparencyAttestationFalse() throws EntityRetrievalException{
		CertifiedProductSearchDetails detail = certifiedProductDetailsManager.getCertifiedProductDetails(4L);
		assertNull(detail.getTransparencyAttestation());
	}
}


