package gov.healthit.chpl.changerequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestWebsiteBuilder;
import gov.healthit.chpl.changerequest.dao.ChangeRequestWebsiteDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestWebsite;
import gov.healthit.chpl.changerequest.manager.ChangeRequestWebsiteHelper;
import gov.healthit.chpl.exception.EntityRetrievalException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class ChangeRequestWebsiteHelperTest {

    @Mock
    private ChangeRequestWebsiteDAO crWebsiteDAO;;

    @InjectMocks
    private ChangeRequestWebsiteHelper crWebsiteHelper;

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
        ChangeRequestWebsite crWebsite = crWebsiteHelper.getByChangeRequestId(1l);

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
        crWebsiteHelper.getByChangeRequestId(1l);
    }

    @Test
    public void getChangeRequestWebsiteFromHashMap_Success1() {
        // Run
        ChangeRequestWebsite crWebsite = crWebsiteHelper
                .getChangeRequestWebsiteFromHashMap(
                        getChangeRequestWebsiteMap(1l, "http://www.abc.com"));

        // Check
        assertEquals(Long.valueOf(1l), crWebsite.getId());
        assertEquals("http://www.abc.com", crWebsite.getWebsite());
    }

    @Test
    public void getChangeRequestWebsiteFromHashMap_Success2() {
        // Run
        ChangeRequestWebsite crWebsite = crWebsiteHelper
                .getChangeRequestWebsiteFromHashMap(
                        getChangeRequestWebsiteMap(null, "http://www.abc.com"));

        // Check
        assertNull(crWebsite.getId());
        assertEquals("http://www.abc.com", crWebsite.getWebsite());
    }

    @Test
    public void createChangeRequestWebsite_Success() throws EntityRetrievalException {
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
    public void createChangeRequestWebsite_Exception() throws EntityRetrievalException {
        Mockito.when(crWebsiteDAO.create(ArgumentMatchers.any(ChangeRequest.class),
                ArgumentMatchers.any(ChangeRequestWebsite.class)))
                .thenThrow(EntityRetrievalException.class);

        crWebsiteDAO.create(
                new ChangeRequestBuilder().withId(23l).build(),
                new ChangeRequestWebsiteBuilder().withWebsite("http://www.abc.com").build());
    }

    @Test
    public void updateChangeRequestWebsite_Success() throws EntityRetrievalException {
        Mockito.when(crWebsiteDAO.update(ArgumentMatchers.any(ChangeRequestWebsite.class)))
                .thenReturn(new ChangeRequestWebsiteBuilder()
                        .withId(3l)
                        .withWebsite("http://www.abc.com")
                        .build());

        ChangeRequestWebsite crWebsite = crWebsiteDAO.update(
                new ChangeRequestWebsiteBuilder().withWebsite("http://www.abc.com").build());

        assertEquals(Long.valueOf(3l), crWebsite.getId());
        assertEquals("http://www.abc.com", crWebsite.getWebsite());
    }

    @Test(expected = EntityRetrievalException.class)
    public void updateChangeRequestWebsite_Exception() throws EntityRetrievalException {
        Mockito.when(crWebsiteDAO.update(ArgumentMatchers.any(ChangeRequestWebsite.class)))
                .thenThrow(EntityRetrievalException.class);

        crWebsiteDAO.update(
                new ChangeRequestWebsiteBuilder().withWebsite("http://www.abc.com").build());
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
