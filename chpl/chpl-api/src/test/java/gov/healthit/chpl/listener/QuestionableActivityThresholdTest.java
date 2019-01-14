package gov.healthit.chpl.listener;


import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.junit.Before;
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
import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityCertificationResultDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.web.controller.CertifiedProductController;
import junit.framework.TestCase;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class QuestionableActivityThresholdTest extends TestCase {

    @PersistenceContext EntityManager entityManager;
    @Autowired private QuestionableActivityDAO qaDao;
    @Autowired private CertifiedProductController cpController;
    @Autowired private CertifiedProductDetailsManager cpdManager;

    private CertifiedProductSearchDetails listing;
    private static JWTAuthenticatedUser adminUser;
    private static final long ADMIN_ID = -2L;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(ADMIN_ID);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));
    }

    @Before
    public void setup() throws EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        listing = cpdManager.getCertifiedProductDetails(1L);
        //set creation date to now so we don't trigger questionable activity
        Query updateCreationDateQuery =
                entityManager.createNativeQuery("UPDATE openchpl.certified_product "
                        + "SET creation_date = NOW() "
                        + "WHERE certified_product_id = " + listing.getId());
        updateCreationDateQuery.executeUpdate();
        entityManager.flush();
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateGapInsideActivityThreshold_DoesNotRecordActivity()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException,
            JsonProcessingException, MissingReasonException, IOException, ValidationException {

        // perform an update that would generate questionable activity outside
        // of the threshold but make sure that no questionable activity was
        // entered.
        Date beforeActivity = new Date();
        boolean changedGap = false;
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getNumber().equals("170.314 (a)(1)")) {
                certResult.setGap(Boolean.FALSE);
                changedGap = true;
            }
        }
        assertTrue(changedGap);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(listing);
        cpController.updateCertifiedProduct(updateRequest);
        Date afterActivity = new Date();

        List<QuestionableActivityCertificationResultDTO> activities = qaDao
                .findCertificationResultActivityBetweenDates(beforeActivity, afterActivity);
        assertTrue(activities == null || activities.size() == 0);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testAddCqmInsideActivityThreshold_DoesNotRecordActivity() throws
    EntityCreationException, EntityRetrievalException, InvalidArgumentsException, JsonProcessingException,
    MissingReasonException, IOException, ValidationException {

        //perform an update that would generate questionable activity outside
        //of the threshold but make sure that no questionable activity was entered.
        final long cms82Id = 60L;
        Date beforeActivity = new Date();
        CQMResultDetails addedCqm = new CQMResultDetails();
        addedCqm.setId(cms82Id);
        addedCqm.setCmsId("CMS82");
        addedCqm.setSuccess(Boolean.TRUE);
        Set<String> successVersions = new HashSet<String>();
        successVersions.add("v0");
        addedCqm.setSuccessVersions(successVersions);
        listing.getCqmResults().add(addedCqm);

        ListingUpdateRequest updateRequest = new ListingUpdateRequest();
        updateRequest.setListing(listing);
        cpController.updateCertifiedProduct(updateRequest);
        Date afterActivity = new Date();

        List<QuestionableActivityListingDTO> activities =
                qaDao.findListingActivityBetweenDates(beforeActivity, afterActivity);
        assertTrue(activities == null || activities.size() == 0);
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
