package gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import gov.healthit.chpl.changerequest.builders.ChangeRequestBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestStatusBuilder;
import gov.healthit.chpl.changerequest.builders.ChangeRequestStatusTypeBuilder;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;

public class CommentRequiredValidationTest {

    @InjectMocks
    private CommentRequiredValidation validator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(validator, "pendingDeveloperAction", 2l);
        ReflectionTestUtils.setField(validator, "rejectedStatus", 4l);
    }

    @Test
    public void isValid_NoStatusChange() {
        ChangeRequest crFromClient = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(1l)
                                .build())
                        .build())
                .build();

        ChangeRequest crFromDb = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(1l)
                                .build())
                        .build())
                .build();

        ChangeRequestValidationContext ctx = new ChangeRequestValidationContext(crFromClient, crFromDb);

        boolean isValid = validator.isValid(ctx);

        assertTrue(isValid);
        assertTrue(validator.getMessages().size() == 0);
    }

    @Test
    public void isValid_CommentNotRequired() {
        ChangeRequest crFromClient = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(3l)
                                .build())
                        .build())
                .build();

        ChangeRequest crFromDb = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(1l)
                                .build())
                        .build())
                .build();

        ChangeRequestValidationContext ctx = new ChangeRequestValidationContext(crFromClient, crFromDb);

        boolean isValid = validator.isValid(ctx);

        assertTrue(isValid);
        assertTrue(validator.getMessages().size() == 0);
    }

    @Test
    public void isValid_CommentRequiredAndProvided() {
        ChangeRequest crFromClient = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(2l)
                                .build())
                        .withComment("This is a comment")
                        .build())
                .build();

        ChangeRequest crFromDb = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(1l)
                                .build())
                        .build())
                .build();

        ChangeRequestValidationContext ctx = new ChangeRequestValidationContext(crFromClient, crFromDb);

        boolean isValid = validator.isValid(ctx);

        assertTrue(isValid);
        assertTrue(validator.getMessages().size() == 0);
    }

    @Test
    public void isValid_CommentRequiredAndNotProvided() {
        ChangeRequest crFromClient = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(2l)
                                .build())
                        .build())
                .build();

        ChangeRequest crFromDb = new ChangeRequestBuilder()
                .withId(1l)
                .withCurrentStatus(new ChangeRequestStatusBuilder()
                        .withChangeRequestStatusType(new ChangeRequestStatusTypeBuilder()
                                .withId(1l)
                                .build())
                        .build())
                .build();

        ChangeRequestValidationContext ctx = new ChangeRequestValidationContext(crFromClient, crFromDb);

        boolean isValid = validator.isValid(ctx);

        assertFalse(isValid);
        assertTrue(validator.getMessages().size() == 1);
    }

}
