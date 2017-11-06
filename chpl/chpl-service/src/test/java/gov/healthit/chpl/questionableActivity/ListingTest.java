package gov.healthit.chpl.questionableActivity;


import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.impl.SurveillanceAuthorityAccessDeniedException;
import gov.healthit.chpl.web.controller.CertifiedProductController;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;
import gov.healthit.chpl.web.controller.SurveillanceController;
import gov.healthit.chpl.web.controller.exception.ValidationException;
import junit.framework.TestCase;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class ListingTest extends TestCase {
	
	@Autowired private QuestionableActivityDAO qaDao;	
	@Autowired private CertifiedProductController cpController;
	@Autowired private SurveillanceController survController;
	@Autowired private CertifiedProductDetailsManager cpdManager;
	private static JWTAuthenticatedUser adminUser;
	
	@Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;
	
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}
	
	@Test
	@Transactional
	@Rollback
	public void testUpdate2011Listing() throws 
	    EntityCreationException, EntityRetrievalException, 
	    ValidationException, InvalidArgumentsException, JsonProcessingException {
	    SecurityContextHolder.getContext().setAuthentication(adminUser);

	    Date beforeActivity = new Date(); 
	    CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetails(3L);
	    listing.setAcbCertificationId("NEWACBCERTIFICATIONID");
	    ListingUpdateRequest updateRequest = new ListingUpdateRequest();
	    updateRequest.setBanDeveloper(false);
	    updateRequest.setListing(listing);
	    cpController.updateCertifiedProduct(updateRequest);
		Date afterActivity = new Date();
		
		List<QuestionableActivityListingDTO> activities = 
		        qaDao.findListingActivityBetweenDates(beforeActivity, afterActivity);
		assertNotNull(activities);
		assertEquals(1, activities.size());
		QuestionableActivityListingDTO activity = activities.get(0);
		assertEquals(3, activity.getListingId().longValue());
		assertNull(activity.getBefore());
		assertNull(activity.getAfter());
		assertEquals(QuestionableActivityTriggerConcept.EDITION_2011_EDITED.getName(), activity.getTrigger().getName());
		
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	@Test
    @Transactional
    @Rollback
    public void testUpdateCertificationStatus() throws 
        EntityCreationException, EntityRetrievalException, 
        ValidationException, InvalidArgumentsException, JsonProcessingException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date(); 
        CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetails(1L);
        listing.getCertificationStatus().put("id", 2L);
        listing.getCertificationStatus().put("name", "Retired");
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setBanDeveloper(false);
        updateRequest.setListing(listing);
        cpController.updateCertifiedProduct(updateRequest);
        Date afterActivity = new Date();
        
        List<QuestionableActivityListingDTO> activities = 
                qaDao.findListingActivityBetweenDates(beforeActivity, afterActivity);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        QuestionableActivityListingDTO activity = activities.get(0);
        assertEquals(1, activity.getListingId().longValue());
        assertEquals("Active", activity.getBefore());
        assertEquals("Retired", activity.getAfter());
        assertEquals(QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED.getName(), activity.getTrigger().getName());
        
        SecurityContextHolder.getContext().setAuthentication(null);
    }
	
	@Test
    @Transactional
    @Rollback
    public void testAddCqm() throws 
        EntityCreationException, EntityRetrievalException, 
        ValidationException, InvalidArgumentsException, JsonProcessingException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date(); 
        CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetails(1L);
        CQMResultDetails addedCqm = new CQMResultDetails();
        addedCqm.setId(60L);
        addedCqm.setCmsId("CMS82");
        addedCqm.setSuccess(Boolean.TRUE);
        Set<String> successVersions = new HashSet<String>();
        successVersions.add("v0");
        addedCqm.setSuccessVersions(successVersions);
        listing.getCqmResults().add(addedCqm);
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setBanDeveloper(false);
        updateRequest.setListing(listing);
        cpController.updateCertifiedProduct(updateRequest);
        Date afterActivity = new Date();
        
        List<QuestionableActivityListingDTO> activities = 
                qaDao.findListingActivityBetweenDates(beforeActivity, afterActivity);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        QuestionableActivityListingDTO activity = activities.get(0);
        assertEquals(1, activity.getListingId().longValue());
        assertNull(activity.getBefore());
        assertEquals("CMS82", activity.getAfter());
        assertEquals(QuestionableActivityTriggerConcept.CQM_ADDED.getName(), activity.getTrigger().getName());
        
        SecurityContextHolder.getContext().setAuthentication(null);
    }
	
	@Test
    @Transactional
    @Rollback
    public void testRemoveCqm() throws 
        EntityCreationException, EntityRetrievalException, 
        ValidationException, InvalidArgumentsException, JsonProcessingException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date(); 
        CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetails(1L);
        for(CQMResultDetails cqm : listing.getCqmResults()) {
            if(cqm.getCmsId() != null && cqm.getCmsId().equals("CMS146")) {
                cqm.setSuccess(Boolean.FALSE);
                cqm.setSuccessVersions(null);
            }
        }

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setBanDeveloper(false);
        updateRequest.setListing(listing);
        cpController.updateCertifiedProduct(updateRequest);
        Date afterActivity = new Date();
        
        List<QuestionableActivityListingDTO> activities = 
                qaDao.findListingActivityBetweenDates(beforeActivity, afterActivity);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        QuestionableActivityListingDTO activity = activities.get(0);
        assertEquals(1, activity.getListingId().longValue());
        assertEquals("CMS146", activity.getBefore());
        assertNull(activity.getAfter());
        assertEquals(QuestionableActivityTriggerConcept.CQM_REMOVED.getName(), activity.getTrigger().getName());
        
        SecurityContextHolder.getContext().setAuthentication(null);
    }
	
    @Test
    @Transactional
    @Rollback
    public void testAddCriteria() throws EntityCreationException, EntityRetrievalException, ValidationException,
            InvalidArgumentsException, JsonProcessingException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date();
        CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetails(1L);
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.getId().longValue() == 4) {
                cert.setSuccess(Boolean.TRUE);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setBanDeveloper(false);
        updateRequest.setListing(listing);
        cpController.updateCertifiedProduct(updateRequest);
        Date afterActivity = new Date();

        List<QuestionableActivityListingDTO> activities = qaDao.findListingActivityBetweenDates(beforeActivity,
                afterActivity);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        QuestionableActivityListingDTO activity = activities.get(0);
        assertEquals(1, activity.getListingId().longValue());
        assertNull(activity.getBefore());
        assertEquals("170.314 (a)(4)", activity.getAfter());
        assertEquals(QuestionableActivityTriggerConcept.CRITERIA_ADDED.getName(), activity.getTrigger().getName());

        SecurityContextHolder.getContext().setAuthentication(null);
    }
    
    @Test
    @Transactional
    @Rollback
    public void testRemoveCriteria() throws EntityCreationException, EntityRetrievalException, ValidationException,
            InvalidArgumentsException, JsonProcessingException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date();
        CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetails(1L);
        for (CertificationResult cert : listing.getCertificationResults()) {
            if (cert.getId().longValue() == 1) {
                cert.setSuccess(Boolean.FALSE);
            }
        }
        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setBanDeveloper(false);
        updateRequest.setListing(listing);
        cpController.updateCertifiedProduct(updateRequest);
        Date afterActivity = new Date();

        List<QuestionableActivityListingDTO> activities = qaDao.findListingActivityBetweenDates(beforeActivity,
                afterActivity);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        QuestionableActivityListingDTO activity = activities.get(0);
        assertEquals(1, activity.getListingId().longValue());
        assertEquals("170.314 (a)(1)", activity.getBefore());
        assertNull(activity.getAfter());
        assertEquals(QuestionableActivityTriggerConcept.CRITERIA_REMOVED.getName(), activity.getTrigger().getName());

        SecurityContextHolder.getContext().setAuthentication(null);
    }
    //TODO: add test for surveillance deleted. Need surveillance associated with 
    //product ID 10 to pass surveillance validation
}
