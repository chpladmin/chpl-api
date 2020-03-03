package old.gov.healthit.chpl.changerequest.validation;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.changerequest.validation.ChangeRequestDetailsCreateValidation;
import gov.healthit.chpl.changerequest.validation.ChangeRequestDetailsUpdateValidation;
import gov.healthit.chpl.changerequest.validation.ChangeRequestNotUpdatableDueToStatusValidation;
import gov.healthit.chpl.changerequest.validation.ChangeRequestTypeInProcessValidation;
import gov.healthit.chpl.changerequest.validation.ChangeRequestTypeValidation;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationFactory;
import gov.healthit.chpl.changerequest.validation.CommentRequiredValidation;
import gov.healthit.chpl.changerequest.validation.CurrentStatusValidation;
import gov.healthit.chpl.changerequest.validation.DeveloperActiveValidation;
import gov.healthit.chpl.changerequest.validation.DeveloperExistenceValidation;
import gov.healthit.chpl.changerequest.validation.RoleAcbHasMultipleCertificationBodiesValidation;

public class ChangeRequestValidationFactoryTest {
    @Mock
    private ChangeRequestDetailsCreateValidation changeRequestDetailsCreateValidation;

    @Mock
    private ChangeRequestDetailsUpdateValidation changeRequestDetailsUpdateValidation;

    @Mock
    private ChangeRequestTypeValidation changeRequestTypeValidation;

    @Mock
    private CurrentStatusValidation currentStatusValidation;

    @Mock
    private DeveloperExistenceValidation developerExistenceValidation;

    @Mock
    private DeveloperActiveValidation developerActiveValidation;

    @Mock
    private ChangeRequestNotUpdatableDueToStatusValidation changeRequestNotUpdatableDueToStatusValidation;

    @Mock
    private ChangeRequestTypeInProcessValidation changeRequestTypeInProcessValidation;

    @Mock
    private CommentRequiredValidation commentRequiredValidation;

    @Mock
    private RoleAcbHasMultipleCertificationBodiesValidation roleAcbHasMultipleCertificationBodiesValidation;

    @InjectMocks
    private ChangeRequestValidationFactory factory;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getRule() {
        assertTrue(factory.getRule(
                ChangeRequestValidationFactory.CHANGE_REQUEST_DETAILS_CREATE) instanceof ChangeRequestDetailsCreateValidation);
        assertTrue(factory.getRule(
                ChangeRequestValidationFactory.CHANGE_REQUEST_DETAILS_UPDATE) instanceof ChangeRequestDetailsUpdateValidation);

        assertTrue(factory.getRule(
                ChangeRequestValidationFactory.CHANGE_REQUEST_TYPE) instanceof ChangeRequestTypeValidation);

        assertTrue(factory.getRule(
                ChangeRequestValidationFactory.CHANGE_REQUEST_IN_PROCESS) instanceof ChangeRequestTypeInProcessValidation);

        assertTrue(factory.getRule(
                ChangeRequestValidationFactory.DEVELOPER_EXISTENCE) instanceof DeveloperExistenceValidation);

        assertTrue(factory.getRule(
                ChangeRequestValidationFactory.DEVELOPER_ACTIVE) instanceof DeveloperActiveValidation);

        assertTrue(factory.getRule(
                ChangeRequestValidationFactory.CHANGE_REQUEST_DETAILS_CREATE) instanceof ChangeRequestDetailsCreateValidation);

        assertTrue(factory.getRule(
                ChangeRequestValidationFactory.CHANGE_REQUEST_DETAILS_UPDATE) instanceof ChangeRequestDetailsUpdateValidation);

        assertTrue(factory.getRule(
                ChangeRequestValidationFactory.STATUS_TYPE) instanceof CurrentStatusValidation);

        assertTrue(factory.getRule(
                ChangeRequestValidationFactory.STATUS_NOT_UPDATABLE) instanceof ChangeRequestNotUpdatableDueToStatusValidation);

        assertTrue(factory.getRule(
                ChangeRequestValidationFactory.COMMENT_REQUIRED) instanceof CommentRequiredValidation);

        assertTrue(factory.getRule(
                ChangeRequestValidationFactory.MULTIPLE_ACBS) instanceof RoleAcbHasMultipleCertificationBodiesValidation);
    }
}
