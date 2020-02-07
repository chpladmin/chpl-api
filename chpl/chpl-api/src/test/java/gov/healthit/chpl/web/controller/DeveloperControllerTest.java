package gov.healthit.chpl.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
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
import gov.healthit.chpl.domain.Contact;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.UpdateDevelopersRequest;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;

@ActiveProfiles({
    "Ff4jMock"
})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class, gov.healthit.chpl.Ff4jTestConfiguration.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class DeveloperControllerTest {
    private static JWTAuthenticatedUser adminUser;

    @Autowired
    private DeveloperController developerController;

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

    @Transactional
    @Test(expected = EntityRetrievalException.class)
    public void testGetDeveloperByBadId() throws EntityRetrievalException, IOException, ValidationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        developerController.getDeveloperById(-100L);
    }

    @Test(expected = ValidationException.class)
    @Transactional
    @Rollback
    public void testUpdateDeveloperWithoutContact() throws
    EntityCreationException, EntityRetrievalException,
    ValidationException, InvalidArgumentsException, JsonProcessingException,
    MissingReasonException, IOException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Developer devToUpdate = developerController.getDeveloperById(-1L);
        UpdateDevelopersRequest req = new UpdateDevelopersRequest();
        devToUpdate.setName("Updated Name");
        req.setDeveloper(devToUpdate);
        List<Long> idsToUpdate = new ArrayList<Long>();
        idsToUpdate.add(-1L);
        req.setDeveloperIds(idsToUpdate);
        developerController.updateDeveloper(req);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateDeveloperWithRequiredData() throws
    EntityCreationException, EntityRetrievalException,
    ValidationException, InvalidArgumentsException, JsonProcessingException,
    MissingReasonException, IOException {
        Long devId = -1L;
        String updatedName = "Updated Name";

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Developer devToUpdate = developerController.getDeveloperById(devId);
        UpdateDevelopersRequest req = new UpdateDevelopersRequest();
        devToUpdate.setName(updatedName);
        //set other required information
        Contact contact = new Contact();
        contact.setEmail("test@test.com");
        contact.setFullName("Test Fullname");
        contact.setPhoneNumber("111-222-3333");
        devToUpdate.setContact(contact);
        req.setDeveloper(devToUpdate);
        List<Long> idsToUpdate = new ArrayList<Long>();
        idsToUpdate.add(-1L);
        req.setDeveloperIds(idsToUpdate);
        developerController.updateDeveloper(req);

        Developer updatedDeveloper = developerController.getDeveloperById(devId);
        assertNotNull(updatedDeveloper);
        assertEquals(updatedName, updatedDeveloper.getName());
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
