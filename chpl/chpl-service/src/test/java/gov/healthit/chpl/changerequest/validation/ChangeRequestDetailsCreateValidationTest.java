package gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestTypeBuilder;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;

public class ChangeRequestDetailsCreateValidationTest {

    private ChangeRequestDetailsCreateValidation validator;

    @Before
    public void setup() {
        validator = new ChangeRequestDetailsCreateValidation();
        ReflectionTestUtils.setField(validator, "websiteChangeRequestType", 1l);
    }

    @Test
    public void isValid_Success_Website() {
        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                getValidWebsiteChangeRequest(), null, null, null, null);

        assertTrue(validator.isValid(context));

    }

    @Test
    public void isValid_Fail_Website() {
        ChangeRequestValidationContext context = new ChangeRequestValidationContext(
                getInvalidWebsiteChangeRequest(), null, null, null, null);

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
