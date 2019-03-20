package gov.healthit.chpl.manager.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.TestingUsers;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.UserPermissionsManager;
import gov.healthit.chpl.permissions.ResourcePermissions;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class TestingLabManagerTest extends TestingUsers {

    @InjectMocks
    private TestingLabManagerImpl atlManager;

    @Mock
    private TestingLabDAO atlDao;

    @Mock
    private UserDAO userDao;

    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private UserPermissionsManager userPermissionsManager;

    @Mock
    private ActivityManager activityManager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createTest()
            throws EntityCreationException, EntityRetrievalException, UserRetrievalException, JsonProcessingException {
        setupForAdminUser(resourcePermissions);

        TestingLabDTO dto = new TestingLabDTO();
        dto.setAccredidationNumber("ACC_NBR");
        dto.setName("Testing Name");
        dto.setWebsite("http://www.abc.com");
        dto.setId(-99L);

        Mockito.when(atlDao.getMaxCode()).thenReturn("05");
        Mockito.when(atlDao.create(ArgumentMatchers.any(TestingLabDTO.class))).thenReturn(dto);

        atlManager.create(dto);

        Mockito.verify(atlDao).create(ArgumentMatchers.any(TestingLabDTO.class));
        Mockito.verify(userPermissionsManager).addAtlPermission(ArgumentMatchers.any(TestingLabDTO.class),
                ArgumentMatchers.anyLong());
        Mockito.verify(activityManager).addActivity(ArgumentMatchers.any(ActivityConcept.class),
                ArgumentMatchers.anyLong(), ArgumentMatchers.anyString(), ArgumentMatchers.isNull(),
                ArgumentMatchers.any(TestingLabDTO.class));

        assertTrue(dto.getTestingLabCode().contentEquals("06"));
    }

    @Test(expected = EntityCreationException.class)
    public void createTest_tooManyAtls()
            throws EntityRetrievalException, UserRetrievalException, JsonProcessingException, EntityCreationException {
        setupForAdminUser(resourcePermissions);

        TestingLabDTO dto = new TestingLabDTO();
        dto.setAccredidationNumber("ACC_NBR");
        dto.setName("Testing Name");
        dto.setWebsite("http://www.abc.com");
        dto.setId(-99L);

        Mockito.when(atlDao.getMaxCode()).thenReturn("99");
        Mockito.when(atlDao.create(ArgumentMatchers.any(TestingLabDTO.class))).thenReturn(dto);

        atlManager.create(dto);
    }

    @Test
    public void updateTest() throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
            UpdateTestingLabException {
        setupForAdminUser(resourcePermissions);

        TestingLabDTO original = new TestingLabDTO();
        original.setId(-99l);
        original.setName("Testing Lab");
        original.setRetired(false);
        original.setTestingLabCode("05");
        original.setAccredidationNumber("accr_nbr");

        TestingLabDTO updated = new TestingLabDTO();
        updated.setId(-99l);
        updated.setName("Testing Lab New Name");
        updated.setRetired(false);
        updated.setTestingLabCode("05");
        updated.setAccredidationNumber("accr_nbr");

        Mockito.when(atlDao.getById(ArgumentMatchers.anyLong())).thenReturn(original);
        Mockito.when(atlDao.update(ArgumentMatchers.any(TestingLabDTO.class))).thenReturn(updated);

        updated = atlManager.update(updated);

        Mockito.verify(activityManager).addActivity(ArgumentMatchers.any(ActivityConcept.class),
                ArgumentMatchers.anyLong(), ArgumentMatchers.anyString(), ArgumentMatchers.any(TestingLabDTO.class),
                ArgumentMatchers.any(TestingLabDTO.class));
    }

    @Test
    public void retireTest() throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
            UpdateTestingLabException {
        setupForAdminUser(resourcePermissions);

        TestingLabDTO original = new TestingLabDTO();
        original.setId(-99l);
        original.setName("Testing Lab");
        original.setRetired(false);
        original.setTestingLabCode("05");
        original.setAccredidationNumber("accr_nbr");
        original.setRetirementDate(new Date());

        Mockito.when(atlDao.getById(ArgumentMatchers.anyLong())).thenReturn(original);
        Mockito.when(atlDao.update(ArgumentMatchers.any(TestingLabDTO.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        original = atlManager.retire(original);

        assertTrue(original.isRetired());

        Mockito.verify(activityManager).addActivity(ArgumentMatchers.any(ActivityConcept.class),
                ArgumentMatchers.anyLong(), ArgumentMatchers.anyString(), ArgumentMatchers.any(TestingLabDTO.class),
                ArgumentMatchers.any(TestingLabDTO.class));
    }

    @Test(expected = UpdateTestingLabException.class)
    public void retireTest_invalidDate() throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, UpdateTestingLabException {
        setupForAdminUser(resourcePermissions);

        // Create a date in the future
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);

        TestingLabDTO original = new TestingLabDTO();
        original.setId(-99l);
        original.setName("Testing Lab");
        original.setRetired(false);
        original.setTestingLabCode("05");
        original.setAccredidationNumber("accr_nbr");
        original.setRetirementDate(cal.getTime());

        Mockito.when(atlDao.getById(ArgumentMatchers.anyLong())).thenReturn(original);
        Mockito.when(atlDao.update(ArgumentMatchers.any(TestingLabDTO.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        original = atlManager.retire(original);

        assertTrue(original.isRetired());

        Mockito.verify(activityManager).addActivity(ArgumentMatchers.any(ActivityConcept.class),
                ArgumentMatchers.anyLong(), ArgumentMatchers.anyString(), ArgumentMatchers.any(TestingLabDTO.class),
                ArgumentMatchers.any(TestingLabDTO.class));
    }

    @Test
    public void unretireTest() throws EntityRetrievalException, JsonProcessingException, EntityCreationException,
            UpdateTestingLabException {
        setupForAdminUser(resourcePermissions);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);

        TestingLabDTO original = new TestingLabDTO();
        original.setId(-99l);
        original.setName("Testing Lab");
        original.setRetired(false);
        original.setTestingLabCode("05");
        original.setAccredidationNumber("accr_nbr");
        original.setRetirementDate(cal.getTime());
        original.setRetired(true);

        Mockito.when(atlDao.getById(ArgumentMatchers.anyLong())).thenReturn(original);
        Mockito.when(atlDao.update(ArgumentMatchers.any(TestingLabDTO.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        original = atlManager.unretire(-99L);

        assertFalse(original.isRetired());
        assertNull(original.getRetirementDate());

        Mockito.verify(activityManager).addActivity(ArgumentMatchers.any(ActivityConcept.class),
                ArgumentMatchers.anyLong(), ArgumentMatchers.anyString(), ArgumentMatchers.any(TestingLabDTO.class),
                ArgumentMatchers.any(TestingLabDTO.class));
    }
}
