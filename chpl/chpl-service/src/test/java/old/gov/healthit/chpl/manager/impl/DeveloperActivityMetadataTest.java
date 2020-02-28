package old.gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityMetadataManager;
import gov.healthit.chpl.manager.DeveloperManager;
import junit.framework.TestCase;

@ActiveProfiles({
        "ListingValidatorMock"
})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class DeveloperActivityMetadataTest extends TestCase {
    @Autowired
    private ActivityMetadataManager metadataManager;

    @Autowired
    private DeveloperManager devManager;

    @Autowired
    private DeveloperStatusDAO devStatusDao;

    private static JWTAuthenticatedUser adminUser;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    @Test
    @Rollback(true)
    @Transactional
    public void testModifyDeveloperStatusAndActivityHasStatusCategory()
            throws EntityRetrievalException, IOException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Long developerId = -1L;
        DeveloperDTO developer = devManager.getById(developerId);
        assertNotNull(developer);
        DeveloperStatusDTO newStatus = devStatusDao.getById(2L);
        DeveloperStatusEventDTO newStatusHistory = new DeveloperStatusEventDTO();
        newStatusHistory.setDeveloperId(developer.getId());
        newStatusHistory.setStatus(newStatus);
        newStatusHistory.setStatusDate(new Date());
        developer.getStatusEvents().add(newStatusHistory);

        boolean failed = false;
        try {
            developer = devManager.update(developer, false);
        } catch (ValidationException | EntityCreationException ex) {
            System.out.println(ex.getMessage());
            failed = true;
        }
        assertFalse(failed);

        Calendar start = getBeginningOfToday();
        Calendar end = getEndOfToday();
        List<ActivityMetadata> metadatas = metadataManager.getActivityMetadataByConcept(
                ActivityConcept.DEVELOPER, start.getTime(), end.getTime());
        assertEquals(1, metadatas.size());
        ActivityMetadata metadata = metadatas.get(0);
        assertEquals(developerId.longValue(), metadata.getObjectId().longValue());
        assertTrue(metadata.getCategories().contains(ActivityCategory.DEVELOPER));
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void testModifyDeveloperNameAndActivityHasStatusCategory()
            throws EntityRetrievalException, IOException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Long developerId = -1L;
        DeveloperDTO developer = devManager.getById(developerId);
        assertNotNull(developer);
        developer.setName("UPDATED NAME");

        boolean failed = false;
        try {
            developer = devManager.update(developer, false);
        } catch (ValidationException | EntityCreationException ex) {
            System.out.println(ex.getMessage());
            failed = true;
        }
        assertFalse(failed);

        Calendar start = getBeginningOfToday();
        Calendar end = getEndOfToday();
        List<ActivityMetadata> metadatas = metadataManager.getActivityMetadataByConcept(
                ActivityConcept.DEVELOPER, start.getTime(), end.getTime());
        assertEquals(1, metadatas.size());
        ActivityMetadata metadata = metadatas.get(0);
        assertEquals(developerId.longValue(), metadata.getObjectId().longValue());
        assertTrue(metadata.getCategories().contains(ActivityCategory.DEVELOPER));
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private Calendar getBeginningOfToday() {
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.set(Calendar.HOUR, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        return start;
    }

    private Calendar getEndOfToday() {
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end.set(Calendar.HOUR, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
        return end;
    }
}
