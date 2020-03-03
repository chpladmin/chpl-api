package old.gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import gov.healthit.chpl.changerequest.validation.WebsiteValidation;
import gov.healthit.chpl.permissions.ResourcePermissions;
import old.gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;

public class WebsiteValidationTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @InjectMocks
    private WebsiteValidation validator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isValid_NonDeveloperWebisiteIsIgnored() {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(false);

        assertTrue(validator.isValid(new ChangeRequestValidationContext(null, null)));
    }

    @Test
    public void isValid_DeveloperNoDetails() {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);

        Map<String, Object> details = new HashMap<String, Object>();
        ChangeRequest cr = new ChangeRequestBuilder()
                .withId(1l)
                .withDetails(details)
                .build();

        assertTrue(validator.isValid(new ChangeRequestValidationContext(cr, null)));
    }

    @Test
    public void isValid_DeveloperWithDetailsValid() {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);

        Map<String, Object> details = new HashMap<String, Object>();
        details.put("website", "http://www.test.com");

        ChangeRequest cr = new ChangeRequestBuilder()
                .withId(1l)
                .withDetails(details)
                .build();

        assertTrue(validator.isValid(new ChangeRequestValidationContext(cr, null)));
    }

    @Test
    public void isValid_DeveloperWithDetailsInvalid() {
        Mockito.when(resourcePermissions.isUserRoleDeveloperAdmin()).thenReturn(true);

        Map<String, Object> details = new HashMap<String, Object>();
        details.put("website", "www.test.com");

        ChangeRequest cr = new ChangeRequestBuilder()
                .withId(1l)
                .withDetails(details)
                .build();

        assertFalse(validator.isValid(new ChangeRequestValidationContext(cr, null)));
    }

}
