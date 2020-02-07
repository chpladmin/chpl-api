package old.gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.validation.ChangeRequestDetailsCreateValidation;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import old.gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import old.gov.healthit.chpl.changerequest.builders.ChangeRequestTypeBuilder;

public class ChangeRequestDetailsCreateValidationTest {

    @InjectMocks
    private ChangeRequestDetailsCreateValidation validator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(validator, "websiteChangeRequestType", 1l);
    }

    @Test
    public void isValid_Success_Website() {
        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                getValidWebsiteChangeRequest(), null);

        assertTrue(validator.isValid(context));

    }

    @Test
    public void isValid_Fail_Website() {
        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                getInvalidWebsiteChangeRequest(), null);

        assertFalse(validator.isValid(context));
    }

    private ChangeRequest getValidWebsiteChangeRequest() {
        ChangeRequest cr = new ChangeRequestBuilder()
                .withId(1l)
                .withChangeRequestType(new ChangeRequestTypeBuilder()
                        .withId(1l)
                        .withName("Website Change Request")
                        .build())
                .build();

        Map<String, Object> details = new HashMap<String, Object>();
        details.put("website", "http://www.abc.com");
        cr.setDetails(details);
        return cr;
    }

    private ChangeRequest getInvalidWebsiteChangeRequest() {
        ChangeRequest cr = new ChangeRequestBuilder()
                .withId(1l)
                .withChangeRequestType(new ChangeRequestTypeBuilder()
                        .withId(1l)
                        .withName("Website Change Request")
                        .build())
                .build();

        Map<String, Object> details = new HashMap<String, Object>();
        details.put("webste", "http://www.abc.com"); // misspelled
        cr.setDetails(details);
        return cr;
    }

}
