package old.gov.healthit.chpl.web.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.domain.search.BasicSearchResponse;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.web.controller.CollectionsController;
import junit.framework.TestCase;

/**
 * Tests for Collections controller.
 * @author alarned
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { old.gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class CollectionsControllerTest extends TestCase {
    @Autowired CollectionsController collectionsController;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    private static JWTAuthenticatedUser adminUser;
    private static final long ADMIN_ID = -2L;
    private static final int EXPECTED_LISTING_COUNT = 18;
    private static final Set<Long> LISTINGS_WITH_PRACTICE_TYPES = new HashSet<Long>(Arrays.asList(1L, 2L, 3L, 4L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L));
    private static final Set<Long> LISTINGS_WITH_PREV_DEVS = new HashSet<Long>(Arrays.asList(1L, 2L, 3L, 9L));
    private static final Set<Long> LISTINGS_WITH_CRITERIA = new HashSet<Long>(Arrays.asList(1L, 2L, 3L, 5L, 10L));

    /**
     * Set up user as Admin.
     * @throws Exception if permission cannot be added
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(ADMIN_ID);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    /**
     * Ensure all default fields are in result.
     * @throws JsonProcessingException if JSON can't be processed
     * @throws EntityRetrievalException if entity can't be read
     */
    @Transactional
    @Test
    public void testBasicSearchDefaultViewHasRequiredFields() throws JsonProcessingException, EntityRetrievalException {
        String resp = collectionsController.getAllCertifiedProducts(null);
        ObjectMapper mapper = new ObjectMapper();
        BasicSearchResponse results = null;

        try {
            results = mapper.readValue(resp, BasicSearchResponse.class);
        } catch (Exception ex) {
            fail("Caught exception " + ex.getMessage());
        }

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(EXPECTED_LISTING_COUNT, results.getResults().size());

        //check that all fields are present for products in which we know those fields exist
        for (CertifiedProductFlatSearchResult result : results.getResults()) {
            assertNotNull(result.getId());
            assertNotNull(result.getChplProductNumber());
            assertNotNull(result.getEdition());
            assertNotNull(result.getAcb());
            assertNotNull(result.getAcbCertificationId());
            assertNotNull(result.getDeveloper());
            assertNotNull(result.getProduct());
            assertNotNull(result.getVersion());
            assertNotNull(result.getCertificationDate());
            assertNotNull(result.getCertificationStatus());
            assertNull(result.getDecertificationDate());
            assertNull(result.getNumMeaningfulUse());
            assertNotNull(result.getSurveillanceCount());
            assertNotNull(result.getOpenNonconformityCount());
            assertNotNull(result.getClosedNonconformityCount());

            if(LISTINGS_WITH_PRACTICE_TYPES.contains(result.getId())) {
                assertNotNull(result.getPracticeType());
            }
            if (LISTINGS_WITH_PREV_DEVS.contains(result.getId())) {
                assertNotNull(result.getPreviousDevelopers());
            }
            if (LISTINGS_WITH_CRITERIA.contains(result.getId())) {
                assertNotNull(result.getCriteriaMet());
            }
            if (result.getId().longValue() == 2) {
                assertNotNull(result.getCqmsMet());
            }
        }
    }

    /**
     * Ensure subset of columns can be searched for.
     * @throws JsonProcessingException if cannot process JSON
     * @throws EntityRetrievalException if cannot retrieve listings
     */
    @Transactional
    @Test
    public void testBasicSearchWithCustomFields() throws JsonProcessingException, EntityRetrievalException {
        String resp = collectionsController.getAllCertifiedProducts("id,chplProductNumber");
        ObjectMapper mapper = new ObjectMapper();
        BasicSearchResponse results = null;

        try {
            results = mapper.readValue(resp, BasicSearchResponse.class);
        } catch (Exception ex) {
            fail("Caught exception " + ex.getMessage());
        }

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(EXPECTED_LISTING_COUNT, results.getResults().size());

        for (CertifiedProductFlatSearchResult result : results.getResults()) {
            assertNotNull(result.getId());
            assertNotNull(result.getChplProductNumber());
            assertNull(result.getEdition());
            assertNull(result.getAcb());
            assertNull(result.getAcbCertificationId());
            assertNull(result.getDeveloper());
            assertNull(result.getProduct());
            assertNull(result.getVersion());
            assertNull(result.getCertificationDate());
            assertNull(result.getCertificationStatus());
            assertNull(result.getDecertificationDate());
            assertNull(result.getNumMeaningfulUse());
            assertNull(result.getSurveillanceCount());
            assertNull(result.getOpenNonconformityCount());
            assertNull(result.getClosedNonconformityCount());
            assertNull(result.getPracticeType());
            assertNull(result.getPreviousDevelopers());
            assertNull(result.getCriteriaMet());
            assertNull(result.getCqmsMet());
        }
    }


    /**
     * Ensure can search for just one field.
     * @throws JsonProcessingException if JSON cannot be processed
     * @throws EntityRetrievalException if entity cannot be retrieved
     */
    @Transactional
    @Test
    public void testBasicSearchWithOneCustomField() throws JsonProcessingException, EntityRetrievalException {
        String resp = collectionsController.getAllCertifiedProducts("id");
        ObjectMapper mapper = new ObjectMapper();
        BasicSearchResponse results = null;

        try {
            results = mapper.readValue(resp, BasicSearchResponse.class);
        } catch (Exception ex) {
            fail("Caught exception " + ex.getMessage());
        }

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(EXPECTED_LISTING_COUNT, results.getResults().size());

        for (CertifiedProductFlatSearchResult result : results.getResults()) {
            assertNotNull(result.getId());
            assertNull(result.getChplProductNumber());
            assertNull(result.getEdition());
            assertNull(result.getAcb());
            assertNull(result.getAcbCertificationId());
            assertNull(result.getDeveloper());
            assertNull(result.getProduct());
            assertNull(result.getVersion());
            assertNull(result.getCertificationDate());
            assertNull(result.getCertificationStatus());
            assertNull(result.getDecertificationDate());
            assertNull(result.getNumMeaningfulUse());
            assertNull(result.getSurveillanceCount());
            assertNull(result.getOpenNonconformityCount());
            assertNull(result.getClosedNonconformityCount());
            assertNull(result.getPracticeType());
            assertNull(result.getPreviousDevelopers());
            assertNull(result.getCriteriaMet());
            assertNull(result.getCqmsMet());
        }
    }

    /**
     * Ensure subclass fields can be retrieved.
     * @throws JsonProcessingException if JSON cannot be processed
     * @throws EntityRetrievalException if entity retrieval fails
     */
    @Transactional
    @Test
    public void testBasicSearchWithCustomFieldsFromSubclass() throws JsonProcessingException, EntityRetrievalException {
        String resp = collectionsController.getAllCertifiedProducts("id,edition,acb,criteriaMet");
        ObjectMapper mapper = new ObjectMapper();
        BasicSearchResponse results = null;

        try {
            results = mapper.readValue(resp, BasicSearchResponse.class);
        } catch (Exception ex) {
            fail("Caught exception " + ex.getMessage());
        }

        assertNotNull(results);
        assertNotNull(results.getResults());
        assertEquals(EXPECTED_LISTING_COUNT, results.getResults().size());

        for (CertifiedProductFlatSearchResult result : results.getResults()) {
            assertNotNull(result.getId());
            assertNull(result.getChplProductNumber());
            assertNotNull(result.getEdition());
            assertNotNull(result.getAcb());
            assertNull(result.getAcbCertificationId());
            assertNull(result.getDeveloper());
            assertNull(result.getProduct());
            assertNull(result.getVersion());
            assertNull(result.getCertificationDate());
            assertNull(result.getCertificationStatus());
            assertNull(result.getDecertificationDate());
            assertNull(result.getNumMeaningfulUse());
            assertNull(result.getSurveillanceCount());
            assertNull(result.getOpenNonconformityCount());
            assertNull(result.getClosedNonconformityCount());
            assertNull(result.getPracticeType());
            assertNull(result.getPreviousDevelopers());
            if (LISTINGS_WITH_CRITERIA.contains(result.getId())) {
                assertNotNull(result.getCriteriaMet());
            }
            assertNull(result.getCqmsMet());
        }
    }
}
