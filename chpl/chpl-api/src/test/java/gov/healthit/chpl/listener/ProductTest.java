package gov.healthit.chpl.listener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductOwner;
import gov.healthit.chpl.domain.UpdateProductsRequest;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityProductDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.web.controller.ProductController;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class ProductTest extends TestCase {

    @Autowired
    private QuestionableActivityDAO qaDao;
    @Autowired
    private ProductManager productManager;
    @Autowired
    private ProductController productController;
    @Autowired
    private DeveloperManager developerManager;
    private static JWTAuthenticatedUser adminUser;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(UnitTestUtil.ADMIN_ID);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateName() throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date();
        ProductDTO product = productManager.getById(-1L);
        product.setName("NEW PRODUCT NAME");
        productManager.update(product);
        Date afterActivity = new Date();

        List<QuestionableActivityProductDTO> activities = qaDao.findProductActivityBetweenDates(beforeActivity,
                afterActivity);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        QuestionableActivityProductDTO activity = activities.get(0);
        assertEquals(-1, activity.getProductId().longValue());
        assertEquals("Test Product 1", activity.getBefore());
        assertEquals("NEW PRODUCT NAME", activity.getAfter());
        assertEquals(QuestionableActivityTriggerConcept.PRODUCT_NAME_EDITED.getName(), activity.getTrigger().getName());

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateCurrentOwner()
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date();
        ProductDTO product = productManager.getById(-1L);
        product.setDeveloperId(-2L);
        productManager.update(product);
        Date afterActivity = new Date();

        List<QuestionableActivityProductDTO> activities = qaDao.findProductActivityBetweenDates(beforeActivity,
                afterActivity);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        QuestionableActivityProductDTO activity = activities.get(0);
        assertEquals(-1, activity.getProductId().longValue());
        assertEquals("Test Developer 1", activity.getBefore());
        assertEquals("Test Developer 2", activity.getAfter());
        assertEquals(QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED.getName(),
                activity.getTrigger().getName());

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testRemoveOwnerHistory()
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date();
        ProductDTO product = productManager.getById(-1L);
        product.getOwnerHistory().clear();
        productManager.update(product);
        Date afterActivity = new Date();

        List<QuestionableActivityProductDTO> activities = qaDao.findProductActivityBetweenDates(beforeActivity,
                afterActivity);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        QuestionableActivityProductDTO activity = activities.get(0);
        assertEquals(-1, activity.getProductId().longValue());
        assertEquals("Test Developer 2 (2015-09-20)", activity.getBefore());
        assertNull(activity.getAfter());
        assertEquals(QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_REMOVED.getName(),
                activity.getTrigger().getName());

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testAddOwnerHistory()
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date();
        ProductDTO product = productManager.getById(-1L);
        ProductOwnerDTO ownerToAdd = new ProductOwnerDTO();
        ownerToAdd.setProductId(-1L);
        DeveloperDTO newOwnerDev = developerManager.getById(-3L);
        ownerToAdd.setDeveloper(newOwnerDev);
        Calendar transferDate = new GregorianCalendar(2014, 0, 1);
        ownerToAdd.setTransferDate(new Long(transferDate.getTimeInMillis()));
        product.getOwnerHistory().add(ownerToAdd);
        productManager.update(product);
        Date afterActivity = new Date();

        List<QuestionableActivityProductDTO> activities = qaDao.findProductActivityBetweenDates(beforeActivity,
                afterActivity);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        QuestionableActivityProductDTO activity = activities.get(0);
        assertEquals(-1, activity.getProductId().longValue());
        assertNull(null);
        assertEquals("Test Developer 3 (2014-01-01)", activity.getAfter());
        assertEquals(QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_ADDED.getName(),
                activity.getTrigger().getName());

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testEditOwnerHistoryItem() throws InvalidArgumentsException, EntityCreationException,
            EntityRetrievalException, JsonProcessingException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        Date beforeActivity = new Date();
        Product toEdit = productController.getProductById(-1L);
        assertTrue(toEdit.getOwnerHistory().size() > 0);
        ProductOwner historyItem = toEdit.getOwnerHistory().get(0);
        Calendar newTransferDate = new GregorianCalendar(2014, 0, 1);
        historyItem.setTransferDate(new Long(newTransferDate.getTimeInMillis()));

        UpdateProductsRequest updateProductsRequest = new UpdateProductsRequest();
        updateProductsRequest.setProduct(toEdit);
        List<Long> updateProductIds = new ArrayList<Long>();
        updateProductIds.add(toEdit.getProductId());
        updateProductsRequest.setProductIds(updateProductIds);
        updateProductsRequest.setNewDeveloperId(toEdit.getOwner().getDeveloperId());
        try {
            productController.updateProduct(updateProductsRequest);
        } catch (ValidationException ex) {
            fail(ex.getMessage());
        }
        Date afterActivity = new Date();

        List<QuestionableActivityProductDTO> activities = qaDao.findProductActivityBetweenDates(beforeActivity,
                afterActivity);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        QuestionableActivityProductDTO activity = activities.get(0);
        assertEquals(-1, activity.getProductId().longValue());
        assertEquals("Test Developer 2 (2015-09-20)", activity.getBefore());
        assertEquals("Test Developer 2 (2014-01-01)", activity.getAfter());
        assertEquals(QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_EDITED.getName(),
                activity.getTrigger().getName());

        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
