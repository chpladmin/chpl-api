package gov.healthit.chpl.changerequest;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import gov.healthit.chpl.changerequest.domain.service.ChangeRequestDetailsFactory;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestWebsiteService;

public class ChangeRequestDetailsFactoryTest {
    private static final Long WEBSITE = 1l;

    @Mock
    private ChangeRequestWebsiteService crWebsiteService;

    @InjectMocks
    private ChangeRequestDetailsFactory factory;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(factory, "websiteChangeRequestType", WEBSITE);
    }

    @Test
    public void get() {
        assertTrue(factory.get(WEBSITE) instanceof ChangeRequestWebsiteService);
    }

}
