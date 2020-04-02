package old.gov.healthit.chpl.changerequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestWebsiteDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestWebsite;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestWebsiteService;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.DeveloperManager;
import old.gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import old.gov.healthit.chpl.changerequest.builders.ChangeRequestWebsiteBuilder;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {
//        gov.healthit.chpl.CHPLTestConfig.class
//})
public class ChangeRequestWebsiteServiceTest {
    @Mock
    private ChangeRequestDAO crDAO;

    @Mock
    private ChangeRequestWebsiteDAO crWebsiteDAO;

    @Mock
    private DeveloperDAO developerDAO;

    @Mock
    private DeveloperManager developerManager;

    @Mock
    private UserDeveloperMapDAO userDeveloperMapDAO;

    @Mock
    private ActivityManager activityManager;

    @Mock
    private Environment env;

    @InjectMocks
    private ChangeRequestWebsiteService service;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getByChangeRequestId_Success() throws EntityRetrievalException {
        // Setup
        Mockito.when(crWebsiteDAO.getByChangeRequestId(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestWebsiteBuilder()
                        .withId(1l)
                        .withWebsite("http://www.abc.com")
                        .build());

        // Run
        ChangeRequestWebsite crWebsite = service.getByChangeRequestId(1l);

        // Check
        assertEquals(Long.valueOf(1l), crWebsite.getId());
        assertEquals("http://www.abc.com", crWebsite.getWebsite());
    }

    @Test(expected = EntityRetrievalException.class)
    public void getByChangeRequestId_Exception() throws EntityRetrievalException {
        // Setup
        Mockito.when(crWebsiteDAO.getByChangeRequestId(ArgumentMatchers.anyLong()))
                .thenThrow(EntityRetrievalException.class);

        // Run
        service.getByChangeRequestId(1l);
    }

    @Test
    public void create_Success() throws EntityRetrievalException {
        Mockito.when(crWebsiteDAO.create(ArgumentMatchers.any(ChangeRequest.class),
                ArgumentMatchers.any(ChangeRequestWebsite.class)))
                .thenReturn(new ChangeRequestWebsiteBuilder()
                        .withId(3l)
                        .withWebsite("http://www.abc.com")
                        .build());

        ChangeRequestWebsite crWebsite = crWebsiteDAO.create(
                new ChangeRequestBuilder().withId(23l).build(),
                new ChangeRequestWebsiteBuilder().withWebsite("http://www.abc.com").build());

        assertEquals(Long.valueOf(3l), crWebsite.getId());
        assertEquals("http://www.abc.com", crWebsite.getWebsite());
    }

    @Test(expected = EntityRetrievalException.class)
    public void create_Exception() throws EntityRetrievalException {
        Mockito.when(crWebsiteDAO.create(ArgumentMatchers.any(ChangeRequest.class),
                ArgumentMatchers.any(ChangeRequestWebsite.class)))
                .thenThrow(EntityRetrievalException.class);

        crWebsiteDAO.create(
                new ChangeRequestBuilder().withId(23l).build(),
                new ChangeRequestWebsiteBuilder().withWebsite("http://www.abc.com").build());
    }

    @Test
    public void update_Success() throws EntityRetrievalException, JsonProcessingException, EntityCreationException, InvalidArgumentsException {
        // Setup
        Mockito.when(crDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestBuilder()
                        .withId(1l)
                        .withDetails(new ChangeRequestWebsiteBuilder()
                                .withId(22l)
                                .withWebsite("http://www.orig.com")
                                .build())
                        .build());

        Mockito.when(crWebsiteDAO.update(Mockito.isA(ChangeRequestWebsite.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        Mockito.doNothing().when(activityManager).addActivity(
                Mockito.isA(ActivityConcept.class), Mockito.isA(Long.class), Mockito.isA(String.class),
                Mockito.isA(Object.class), Mockito.isA(Object.class));

        // Run
        ChangeRequest fromClientCr = new ChangeRequestBuilder()
                .withId(1l)
                .withDetails(getChangeRequestWebsiteMap(null, "http://www.new.com"))
                .build();

        ChangeRequest updatedCr = service.update(fromClientCr);

        // Test

        // Make sure update is called
        Mockito.verify(crWebsiteDAO, Mockito.times(1)).update(Mockito.isA(ChangeRequestWebsite.class));

        // Make sure activity is not called
        Mockito.verify(activityManager, Mockito.times(1)).addActivity(Mockito.isA(ActivityConcept.class),
                Mockito.isA(Long.class), Mockito.isA(String.class), Mockito.isA(Object.class),
                Mockito.isA(Object.class));

        assertEquals("http://www.new.com", ((ChangeRequestWebsite) updatedCr.getDetails()).getWebsite());
    }

    @Test
    public void update_WebsiteNotChanged()
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, InvalidArgumentsException {
        // Setup
        Mockito.when(crDAO.get(ArgumentMatchers.anyLong()))
                .thenReturn(new ChangeRequestBuilder()
                        .withId(1l)
                        .withDetails(new ChangeRequestWebsiteBuilder()
                                .withId(22l)
                                .withWebsite("http://www.orig.com")
                                .build())
                        .build());

        Mockito.when(crWebsiteDAO.update(Mockito.isA(ChangeRequestWebsite.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        Mockito.doNothing().when(activityManager).addActivity(
                Mockito.isA(ActivityConcept.class), Mockito.isA(Long.class),
                Mockito.isA(String.class), Mockito.isA(Object.class), Mockito.isA(Object.class));

        // Run
        ChangeRequest fromClientCr = new ChangeRequestBuilder()
                .withId(1l)
                .withDetails(getChangeRequestWebsiteMap(null, "http://www.orig.com"))
                .build();

        ChangeRequest updatedCr = service.update(fromClientCr);

        // Test

        // Make sure update is not called
        Mockito.verify(crWebsiteDAO, Mockito.never()).update(Mockito.isA(ChangeRequestWebsite.class));

        // Make sure activity is not called
        Mockito.verify(activityManager, Mockito.never()).addActivity(Mockito.isA(ActivityConcept.class),
                Mockito.isA(Long.class), Mockito.isA(String.class), Mockito.isA(Object.class),
                Mockito.isA(Object.class));

        assertEquals("http://www.orig.com", ((ChangeRequestWebsite) updatedCr.getDetails()).getWebsite());
    }

    @Test(expected = RuntimeException.class)
    public void update_Exception_CouldNotFindCr() throws EntityRetrievalException, InvalidArgumentsException {
        // Setup
        Mockito.when(crDAO.get(ArgumentMatchers.anyLong()))
                .thenThrow(EntityRetrievalException.class);

        // Run
        service.update(new ChangeRequestBuilder().withId(1l).build());

        // Test - handled by the 'expected'
    }

    @Ignore
    @Test
    public void postStatusChangeProcessing_PendingDeveloperAction() {
        // Need to determine how to mock/replicate email object
        assertTrue(true);
    }

    @Ignore
    @Test
    public void postStatusChangeProcessing_Accepted() {
        // Need to determine how to mock/replicate email object
        assertTrue(true);
    }

    private HashMap<String, Object> getChangeRequestWebsiteMap(final Long id, final String website) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        if (id != null) {
            map.put("id", id);
        }
        if (website != null) {
            map.put("website", website);
        }
        return map;
    }
}
