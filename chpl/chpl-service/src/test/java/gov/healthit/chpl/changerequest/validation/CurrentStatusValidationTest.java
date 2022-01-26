package gov.healthit.chpl.changerequest.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext.ChangeRequestStatusIds;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class CurrentStatusValidationTest {
    private static final Long PENDING_ACB_ACTION = 1L;
    private static final Long PENDING_DEVELOPER_ACTION = 2L;
    private static final Long ACCEPTED = 3L;
    private static final Long REJECTED = 4L;
    private static final Long CANCELLED_BY_REQUESTER = 5L;

    private CurrentStatusValidation validator;
    private ChangeRequest newChangeRequest;
    private ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO;

    @Before
    public void before() throws EntityRetrievalException {
        validator = new CurrentStatusValidation();

        changeRequestStatusTypeDAO = Mockito.mock(ChangeRequestStatusTypeDAO.class);
        Mockito.when(changeRequestStatusTypeDAO.getChangeRequestStatusTypeById(ArgumentMatchers.anyLong()))
                .thenReturn(ChangeRequestStatusType.builder().build());

        newChangeRequest = ChangeRequest.builder()
                .id(1L)
                .developer(Developer.builder()
                        .developerId(1L)
                        .build())
                .build();

    }

    @Test
    public void isValid_NewCRDoesNotHaveCurrentStatus_ReturnTrue() {
        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .newChangeRequest(newChangeRequest)
                .changeRequestStatusIds(new ChangeRequestStatusIds(CANCELLED_BY_REQUESTER, ACCEPTED, REJECTED, PENDING_ACB_ACTION, PENDING_DEVELOPER_ACTION))
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
    }

    @Test
    public void isValid_NewCRDoesNotHaveValidCurrentStatus_ReturnFalse() {

    }

    @Test
    public void isValid_SubmittedByDeveloperWithStatusOfCancelled_ReturnTrue() {

    }

    @Test
    public void isValid_SubmittedByDeveloperWithStatusOfPendingAcvAction_ReturnTrue() {

    }

    @Test
    public void isValid_SubmittedByAcbithStatusOfCancelled_ReturnFalse() {

    }

    @Test
    public void isValid_SubmittedByAcbWithStatusOfPendingAcvAction_ReturnFalse() {

    }

}
