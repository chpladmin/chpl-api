package old.gov.healthit.chpl.web.controller;

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
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.UnitTestUtil;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.search.NonconformitySearchOptions;
import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.domain.search.SearchResponse;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.web.controller.SearchViewController;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class SearchViewControllerTest extends TestCase {
    @Autowired
    SearchViewController searchViewController = new SearchViewController();

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    private static JWTAuthenticatedUser adminUser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(UnitTestUtil.ADMIN_ID);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    @Transactional
    @Test
    public void test_basicSearch_allProducts() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {

        SearchResponse searchResponse = searchViewController.searchGet(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, 0, 10, SearchRequest.ORDER_BY_DEVELOPER, true);
        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getRecordCount());
        assertEquals(18, searchResponse.getRecordCount().intValue());
        assertNotNull(searchResponse.getResults());
        assertEquals(10, searchResponse.getResults().size());
    }

    @Transactional
    @Test
    public void testGetSearchInvalidCertificationStatuses() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {
        try {
            searchViewController.searchGet(null, "Active,Bad", null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, 0, 50, SearchRequest.ORDER_BY_DEVELOPER, true);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("Bad"));
        }
    }

    @Transactional
    @Test
    public void testPostSearchInvalidCertificationStatuses() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {
        try {
            SearchRequest badRequest = new SearchRequest();
            badRequest.getCertificationStatuses().add("Active");
            badRequest.getCertificationStatuses().add("Bad");
            searchViewController.searchPost(badRequest);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("Bad"));
        }
    }

    @Transactional
    @Test()
    public void testGetSearchInvalidCertificationEditions() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {

        try {
            searchViewController.searchGet(null, "Active", "2000,2011", null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, 0, 50, SearchRequest.ORDER_BY_DEVELOPER, true);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("2000"));
        }
    }

    @Transactional
    @Test()
    public void testPostSearchInvalidCertificationEditions() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {
        SearchRequest badRequest = new SearchRequest();
        badRequest.getCertificationStatuses().add("Active");
        badRequest.getCertificationEditions().add("2000");
        badRequest.getCertificationEditions().add("2011");
        try {
            searchViewController.searchPost(badRequest);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("2000"));
        }
    }

    @Transactional
    @Test()
    public void testGetSearchInvalidCertificationCriteria() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {

        try {
            searchViewController.searchGet(null, "Active", "2014", "170.314 (a)(1),170.314 (r)(5)", null, null, null,
                    null, null, null, null, null, null, null, null, null, null, 0, 50, SearchRequest.ORDER_BY_DEVELOPER,
                    true);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("170.314 (r)(5)"));
        }
    }

    @Transactional
    @Test()
    public void testPostSearchInvalidCertificationCriteria() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {
        SearchRequest badRequest = new SearchRequest();
        badRequest.getCertificationStatuses().add("Active");
        badRequest.getCertificationEditions().add("2011");
        badRequest.getCertificationCriteria().add("170.314 (a)(1)");
        badRequest.getCertificationCriteria().add("170.314 (r)(5)");
        try {
            searchViewController.searchPost(badRequest);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("170.314 (r)(5)"));
        }
    }

    @Transactional
    @Test()
    public void testGetSearchInvalidCertificationCriteriaOperator() throws EntityRetrievalException,
            JsonProcessingException, EntityCreationException, InvalidArgumentsException {

        try {
            searchViewController.searchGet(null, "Active", "2014", "170.314 (a)(1)", "NEITHER", null, null, null, null,
                    null, null, null, null, null, null, null, null, 0, 50, SearchRequest.ORDER_BY_DEVELOPER, true);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("NEITHER"));
        }
    }

    @Transactional
    @Test()
    public void testGetSearchInvalidCqms() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {

        try {
            searchViewController.searchGet(null, "Active", "2014", "170.314 (a)(1)", "OR", "CMSBAD,CMS122", null, null,
                    null, null, null, null, null, null, null, null, null, 0, 50, SearchRequest.ORDER_BY_DEVELOPER,
                    true);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("CMSBAD"));
        }
    }

    @Transactional
    @Test()
    public void testGetSearchInvalidCqmOperator() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {

        try {
            searchViewController.searchGet(null, "Active", "2014", "170.314 (a)(1)", "OR", "CMS122", "NEITHER", null,
                    null, null, null, null, null, null, null, null, null, 0, 50, SearchRequest.ORDER_BY_DEVELOPER,
                    true);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("NEITHER"));
        }
    }

    @Transactional
    @Test()
    public void testPostSearchInvalidCqms() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {
        SearchRequest badRequest = new SearchRequest();
        badRequest.getCertificationStatuses().add("Active");
        badRequest.getCertificationEditions().add("2011");
        badRequest.getCertificationCriteria().add("170.314 (a)(1)");
        badRequest.getCqms().add("CMSBAD");
        badRequest.getCqms().add("CMS122");
        try {
            searchViewController.searchPost(badRequest);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("CMSBAD"));
        }
    }

    @Transactional
    @Test()
    public void testGetSearchInvalidCertificationBody() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {

        try {
            searchViewController.searchGet(null, "Active", "2014", "170.314 (a)(1)", "OR", "CMS122", "AND",
                    "BAD ACB,UL LLC", null, null, null, null, null, null, null, null, null, 0, 50,
                    SearchRequest.ORDER_BY_DEVELOPER, true);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("BAD ACB"));
        }
    }

    @Transactional
    @Test()
    public void testPostSearchInvalidCertificationBody() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {
        SearchRequest badRequest = new SearchRequest();
        badRequest.getCertificationStatuses().add("Active");
        badRequest.getCertificationEditions().add("2011");
        badRequest.getCertificationCriteria().add("170.314 (a)(1)");
        badRequest.getCqms().add("CMS122");
        badRequest.getCertificationBodies().add("UL LLC");
        badRequest.getCertificationBodies().add("BAD ACB");
        try {
            searchViewController.searchPost(badRequest);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("BAD ACB"));
        }
    }

    @Transactional
    @Test()
    public void testGetSearchInvalidHasHadSurveillance() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {

        try {
            searchViewController.searchGet(null, "Active", "2014", "170.314 (a)(1)", "OR", "CMS122", "AND", "UL LLC",
                    "TRUEORFALSE", null, null, null, null, null, null, null, null, 0, 50,
                    SearchRequest.ORDER_BY_DEVELOPER, true);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("TRUEORFALSE"));
        }
    }

    @Transactional
    @Test()
    public void testGetSearchInvalidNonconformityOptions() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {

        try {
            searchViewController.searchGet(null, "Active", "2014", "170.314 (a)(1)", "OR", "CMS122", "AND", "UL LLC",
                    "FALSE", NonconformitySearchOptions.CLOSED_NONCONFORMITY + "," + "BAD_OPTION", null, null, null,
                    null, null, null, null, 0, 50, SearchRequest.ORDER_BY_DEVELOPER, true);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("BAD_OPTION"));
        }
    }

    @Transactional
    @Test()
    public void testGetSearchInvalidNonconformityOptionsOperator() throws EntityRetrievalException,
            JsonProcessingException, EntityCreationException, InvalidArgumentsException {

        try {
            searchViewController.searchGet(null, "Active", "2014", "170.314 (a)(1)", "OR", "CMS122", "AND", "UL LLC",
                    "TRUE", NonconformitySearchOptions.CLOSED_NONCONFORMITY.toString(), "NEITHER", null, null, null,
                    null, null, null, 0, 50, SearchRequest.ORDER_BY_DEVELOPER, true);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("NEITHER"));
        }
    }

    @Transactional
    @Test()
    public void testGetSearchInvalidPracticeType() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {

        try {
            searchViewController.searchGet(null, "Active", "2014", "170.314 (a)(1)", "OR", "CMS122", "AND", "UL LLC",
                    "FALSE", null, null, null, null, null, "BAD_PRACTICE_TYPE", null, null, 0, 50,
                    SearchRequest.ORDER_BY_DEVELOPER, true);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("BAD_PRACTICE_TYPE"));
        }
    }

    @Transactional
    @Test()
    public void testPostSearchInvalidPracticeType() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {
        SearchRequest badRequest = new SearchRequest();
        badRequest.getCertificationStatuses().add("Active");
        badRequest.getCertificationEditions().add("2011");
        badRequest.getCertificationCriteria().add("170.314 (a)(1)");
        badRequest.getCqms().add("CMS122");
        badRequest.getCertificationBodies().add("UL LLC");
        badRequest.setPracticeType("BAD_PRACTICE_TYPE");
        try {
            searchViewController.searchPost(badRequest);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("BAD_PRACTICE_TYPE"));
        }
    }

    @Transactional
    @Test()
    public void testGetSearchInvalidCertificationDateStart() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {

        try {
            searchViewController.searchGet(null, "Active", "2014", "170.314 (a)(1)", "OR", "CMS122", "AND", "UL LLC",
                    "FALSE", null, null, null, null, null, "Ambulatory", "20110101", null, 0, 50,
                    SearchRequest.ORDER_BY_DEVELOPER, true);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("20110101"));
        }
    }

    @Transactional
    @Test()
    public void testPostSearchInvalidCertificationDateStart() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {
        SearchRequest badRequest = new SearchRequest();
        badRequest.getCertificationStatuses().add("Active");
        badRequest.getCertificationEditions().add("2011");
        badRequest.getCertificationCriteria().add("170.314 (a)(1)");
        badRequest.getCqms().add("CMS122");
        badRequest.getCertificationBodies().add("UL LLC");
        badRequest.setPracticeType("Ambulatory");
        badRequest.setCertificationDateStart("20110101");
        try {
            searchViewController.searchPost(badRequest);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("20110101"));
        }
    }

    @Transactional
    @Test()
    public void testGetSearchInvalidCertificationDateEnd() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {

        try {
            searchViewController.searchGet(null, "Active", "2014", "170.314 (a)(1)", "OR", "CMS122", "AND", "UL LLC",
                    "FALSE", null, null, null, null, null, "Ambulatory", "2011-01-01", "20110131", 0, 50,
                    SearchRequest.ORDER_BY_DEVELOPER, true);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("20110131"));
        }
    }

    @Transactional
    @Test()
    public void testPostSearchInvalidCertificationDateEnd() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {
        SearchRequest badRequest = new SearchRequest();
        badRequest.getCertificationStatuses().add("Active");
        badRequest.getCertificationEditions().add("2011");
        badRequest.getCertificationCriteria().add("170.314 (a)(1)");
        badRequest.getCqms().add("CMS122");
        badRequest.getCertificationBodies().add("UL LLC");
        badRequest.setPracticeType("Ambulatory");
        badRequest.setCertificationDateStart("2011-01-01");
        badRequest.setCertificationDateEnd("20110131");
        try {
            searchViewController.searchPost(badRequest);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("20110131"));
        }
    }

    @Transactional
    @Test()
    public void testGetSearchInvalidPageSize() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {

        try {
            searchViewController.searchGet(null, "Active", "2014", "170.314 (a)(1)", "OR", "CMS122", "AND", "UL LLC",
                    "FALSE", null, null, null, null, null, "Ambulatory", "2011-01-01", "2011-01-31", 0, 5000,
                    SearchRequest.ORDER_BY_DEVELOPER, true);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("100"));
        }
    }

    @Transactional
    @Test()
    public void testPostSearchInvalidPageSize() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {
        SearchRequest badRequest = new SearchRequest();
        badRequest.getCertificationStatuses().add("Active");
        badRequest.getCertificationEditions().add("2011");
        badRequest.getCertificationCriteria().add("170.314 (a)(1)");
        badRequest.getCqms().add("CMS122");
        badRequest.getCertificationBodies().add("UL LLC");
        badRequest.setPracticeType("Ambulatory");
        badRequest.setCertificationDateStart("2011-01-01");
        badRequest.setCertificationDateEnd("2011-01-31");
        badRequest.setPageSize(5000);
        try {
            searchViewController.searchPost(badRequest);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("100"));
        }
    }

    @Transactional
    @Test()
    public void testGetSearchInvalidOrderBy() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {

        try {
            searchViewController.searchGet(null, "Active", "2014", "170.314 (a)(1)", "OR", "CMS122", "AND", "UL LLC",
                    "FALSE", null, null, null, null, null, "Ambulatory", "2011-01-01", "2011-01-31", 0, 50,
                    "bad_order_by", true);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("bad_order_by"));
        }
    }

    @Transactional
    @Test()
    public void testPostSearchInvalidOrderBy() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException {
        SearchRequest badRequest = new SearchRequest();
        badRequest.getCertificationStatuses().add("Active");
        badRequest.getCertificationEditions().add("2011");
        badRequest.getCertificationCriteria().add("170.314 (a)(1)");
        badRequest.getCqms().add("CMS122");
        badRequest.getCertificationBodies().add("UL LLC");
        badRequest.setPracticeType("Ambulatory");
        badRequest.setCertificationDateStart("2011-01-01");
        badRequest.setCertificationDateEnd("2011-01-31");
        badRequest.setPageSize(50);
        badRequest.setOrderBy("bad_order_by");
        try {
            searchViewController.searchPost(badRequest);
            fail();
        } catch (InvalidArgumentsException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("bad_order_by"));
        }
    }

    @Transactional
    @Test()
    public void testPostSearch() throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
            InvalidArgumentsException {
        SearchRequest searchRequest = new SearchRequest();

        searchRequest.setSearchTerm("Test");
        searchRequest.setDeveloper("Test Developer 1");
        searchRequest.setProduct("Test");
        searchRequest.setVersion("2.0");
        searchRequest.getCertificationEditions().add("2014");
        searchRequest.getCertificationBodies().add("UL LLC");
        searchRequest.setPracticeType("Ambulatory");
        searchRequest.setOrderBy("product");
        searchRequest.setSortDescending(true);
        searchRequest.setPageNumber(0);

        SearchResponse response = searchViewController.searchPost(searchRequest);
        assertNotNull(response);
        assertEquals(1, response.getRecordCount().intValue());
        assertNotNull(response.getResults());
        assertEquals(1, response.getResults().size());
    }

    @Transactional
    @Test()
    public void testGetSearch() throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
            InvalidArgumentsException {
        SearchResponse response = searchViewController.searchGet("Test", null, "2014", null, null, null, null,
                "UL LLC", null, null, null, "Test Developer 1", "Test", "2.0", "Ambulatory", null, null, 0, 50,
                "product", true);
        assertNotNull(response);
        assertEquals(1, response.getRecordCount().intValue());
        assertNotNull(response.getResults());
        assertEquals(1, response.getResults().size());
    }

    @Transactional
    @Test()
    public void getQuarters() {
        Set<KeyValueModel> response = searchViewController.getQuarters();
        assertNotNull(response);
        assertEquals(4, response.size());
        for (KeyValueModel item : response) {
            assertNotNull(item.getId());
            assertTrue(item.getId() > 0);
            assertTrue(!StringUtils.isEmpty(item.getName()));
            assertTrue(item.getName().startsWith("Q"));
            assertNotNull(item.getDescription());
        }
    }
}
