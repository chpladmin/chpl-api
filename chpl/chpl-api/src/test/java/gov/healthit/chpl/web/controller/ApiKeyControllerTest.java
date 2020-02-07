package gov.healthit.chpl.web.controller;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

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

import gov.healthit.chpl.UnitTestUtil;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.impl.ApiKeyActivityDAOImpl;
import gov.healthit.chpl.domain.ApiKeyActivity;
import gov.healthit.chpl.dto.ApiKeyActivityDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class ApiKeyControllerTest {

    @Autowired
    private ApiKeyController apiKeyController;

    @Autowired
    private ApiKeyActivityDAOImpl apiKeyActivityDao;

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

    /**
     * Description: Tests the startDate parameter in the listActivity(Integer
     * pageNumber, Integer pageSize, String apiKeyFilter, boolean dateAscending,
     * Long startDateMilli, Long endDateMilli) method when passing in
     * null/undefined for &startDate Expected Result: One API key activity is
     * returned that has a api key activity id == first api key activity id in
     * the openchpl_test DB Assumptions: Pre-existing data in openchpl_test DB
     * is there per the
     * \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void test_listActivity_startDate_UndefinedReturnsOldestApiKeyActivity()
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        // Get the oldest API key id
        ApiKeyActivityDTO oldestApiKeyActivity = apiKeyActivityDao.getById(-1L);

        // Simulate API inputs
        String apiKeyFilter = null; // Valid API Key in openchpl_test DB
        Integer pageNumber = 0;
        Integer pageSize = 1;
        boolean dateAscending = true;

        List<ApiKeyActivity> activitiesList = apiKeyController.listActivity(pageNumber, pageSize, apiKeyFilter,
                dateAscending, null, null);
        assertTrue("Activities list should contain some activities", activitiesList.size() > 0);

        long firstActivityApiKeyCreationDate = activitiesList.get(0).getCreationDate().getTime();
        assertTrue("The api key id returned is not equivalent to the oldest recorded",
                oldestApiKeyActivity.getCreationDate().getTime() == firstActivityApiKeyCreationDate);
    }

    /**
     * Description: Tests the endDate parameter in the listActivity(Integer
     * pageNumber, Integer pageSize, String apiKeyFilter, boolean dateAscending,
     * Long startDateMilli, Long endDateMilli) method when passing in
     * null/undefined for &endDate Expected Result: The most recent API key
     * activity id is returned Assumptions: Pre-existing data in openchpl_test
     * DB is there per the
     * \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
     */
    @Transactional
    @Rollback(true)
    @Test
    public void test_listActivity_endDate_UndefinedReturnsNewestApiKeyActivity()
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        long newestApiKeyActivityCreationDate = 0;

        // Get the newest API key id
        ApiKeyActivityDTO newestApiKeyActivityEntity = apiKeyActivityDao.getById(-7L);

        // Simulate API inputs
        String apiKeyFilter = null; // Valid API Key in openchpl_test DB
        Integer pageNumber = 0;
        Integer pageSize = 1;
        boolean dateAscending = false;

        List<ApiKeyActivity> activitiesList = apiKeyController.listActivity(pageNumber, pageSize, apiKeyFilter,
                dateAscending, null, null);
        assertTrue("Activities list should contain some activities but contains " + activitiesList.size(),
                activitiesList.size() > 0);

        for (ApiKeyActivity apiKeyActivity : activitiesList) {
            if (apiKeyActivity.getCreationDate().getTime() > newestApiKeyActivityCreationDate) {
                newestApiKeyActivityCreationDate = apiKeyActivity.getCreationDate().getTime();
            }
        }

        assertTrue("The api key entity returned is not equivalent to the newest recorded",
                newestApiKeyActivityCreationDate == newestApiKeyActivityEntity.getCreationDate().getTime());
    }

    /**
     * Description: Tests the pageNumber parameter in the
     * getApiKeyActivity(String apiKeyFilter, Integer pageNumber, Integer
     * pageSize, boolean dateAscending, Long startDateMilli, Long endDateMilli)
     * method when passing in &pageNumber=2 and &pageSize = 1 Expected Result:
     * Returns a new api key activity for each page Assumptions: An API key
     * activity exists with creation_date <= the value of endDateMilli
     * Pre-existing data in openchpl_test DB is there per the
     * \CHPL\chpl-api\chpl\chpl-service\src\test\resources\data\testData.xml
     */
    @Transactional
    @Rollback(true)
    @Test
    public void test_listActivity_pageNumber_returnsNewAPIKeyActivityForEachPage()
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        // Simulate API inputs
        String apiKeyFilter = null;
        Integer pageNumber = 0;
        Integer pageSize = 3;
        boolean dateAscending = true;
        long startDateMilli = 0;
        Long endDateMilli = null;

        List<ApiKeyActivity> activitiesListPage0 = apiKeyController.listActivity(pageNumber, pageSize, apiKeyFilter,
                dateAscending, startDateMilli, endDateMilli);
        assertTrue("Activities list should contain some activities", activitiesListPage0.size() > 0);

        pageNumber = 1;

        List<ApiKeyActivity> activitiesListPage1 = apiKeyController.listActivity(pageNumber, pageSize, apiKeyFilter,
                dateAscending, startDateMilli, endDateMilli);
        assertTrue("Activities list should contain some activities", activitiesListPage1.size() > 0);

        for (ApiKeyActivity activityPage0 : activitiesListPage0) {
            for (ApiKeyActivity activityPage1 : activitiesListPage1) {
                assertNotSame(
                        "The API activity id in one page should not be included on a subsequent page: "
                                + "first page shows API activity id = " + activityPage0.getId()
                                + " and second page shows API activity id = " + activityPage1.getId(),
                        activityPage0.getId(), activityPage1.getId());
            }
        }
    }

    @Transactional
    @Test(expected = EntityRetrievalException.class)
    public void testGetApiKeyActivityByBadId() throws EntityRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        apiKeyController.listActivityByKey("BADKEY", 0, 10);
    }
}
