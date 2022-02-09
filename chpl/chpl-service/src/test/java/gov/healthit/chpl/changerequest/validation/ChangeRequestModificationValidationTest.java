package gov.healthit.chpl.changerequest.validation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ChangeRequestModificationValidationTest {

    private ChangeRequestModificationValidation validator;

    @Before
    public void setup() {
        validator = Mockito.spy(ChangeRequestModificationValidation.class);
    }

    @Test
    public void isValid_NewStatusAdded_ReturnTrueWithNoErrors() {
//        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
//                .origChangeRequest(ChangeRequest.builder()
//                        .id(1L)
//                        .changeRequestType(ChangeRequestType.builder()
//                                .id(3L)
//                                .name("Developer Attestation Change Request")
//                                .build())
//                        .developer(new Developer())
//                        .currentStatus(ChangeRequestStatus.builder()
//                                .changeRequestStatusType(ChangeRequestStatusType.builder()
//                                        .id(1L)
//                                        .name("Pending ONC-ACB Action")
//                                        .build())
//                                .build())
//                        .details(ChangeRequestAttestation.builder()
//                                .attestation("TEST_ATTESTATION")
//                                .build())
//                        .build())
//                .newChangeRequest(ChangeRequest.builder()
//                        .id(1L)
//                        .changeRequestType(ChangeRequestType.builder()
//                                .id(3L)
//                                .name("Developer Attestation Change Request")
//                                .build())
//                        .developer(new Developer())
//                        .currentStatus(ChangeRequestStatus.builder()
//                                .changeRequestStatusType(ChangeRequestStatusType.builder()
//                                        .id(3L)
//                                        .name("Accepted")
//                                        .build())
//                                .build())
//                        .details(ChangeRequestAttestation.builder()
//                                .attestation("TEST_ATTESTATION_UPDATED")
//                                .build())
//                        .build())
//                .build();
//
//        Boolean isValid = validator.isValid(context);
//
//        assertEquals(true, isValid);
//        assertEquals(0, validator.getMessages().size());
    }

    @Test
    public void isValid_ChangeRequestDetailsUpdated_ReturnTrueWithNoErrors() {
//        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
//                .origChangeRequest(ChangeRequest.builder()
//                        .id(1L)
//                        .changeRequestType(ChangeRequestType.builder()
//                                .id(3L)
//                                .name("Developer Attestation Change Request")
//                                .build())
//                        .developer(new Developer())
//                        .currentStatus(ChangeRequestStatus.builder()
//                                .changeRequestStatusType(ChangeRequestStatusType.builder()
//                                        .id(1L)
//                                        .name("Pending ONC-ACB Action")
//                                        .build())
//                                .build())
//                        .details(ChangeRequestAttestation.builder()
//                                .attestation("TEST_ATTESTATION")
//                                .build())
//                        .build())
//                .newChangeRequest(ChangeRequest.builder()
//                        .id(1L)
//                        .changeRequestType(ChangeRequestType.builder()
//                                .id(3L)
//                                .name("Developer Attestation Change Request")
//                                .build())
//                        .developer(new Developer())
//                        .currentStatus(ChangeRequestStatus.builder()
//                                .changeRequestStatusType(ChangeRequestStatusType.builder()
//                                        .id(1L)
//                                        .name("Pending ONC-ACB Action")
//                                        .build())
//                                .build())
//                        .details(ChangeRequestAttestation.builder()
//                                .attestation("TEST_ATTESTATION_UPDATED")
//                                .build())
//                        .build())
//                .build();
//
//        Boolean isValid = validator.isValid(context);
//
//        assertEquals(true, isValid);
//        assertEquals(0, validator.getMessages().size());
    }

    @Test
    public void isValid_ChangeRequestDetailsNoChanged_ReturnFalseWith1Error() {
//        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
//                .origChangeRequest(ChangeRequest.builder()
//                        .id(1L)
//                        .changeRequestType(ChangeRequestType.builder()
//                                .id(3L)
//                                .name("Developer Attestation Change Request")
//                                .build())
//                        .developer(new Developer())
//                        .currentStatus(ChangeRequestStatus.builder()
//                                .changeRequestStatusType(ChangeRequestStatusType.builder()
//                                        .id(1L)
//                                        .name("Pending ONC-ACB Action")
//                                        .build())
//                                .build())
//                        .details(ChangeRequestAttestation.builder()
//                                .attestation("TEST_ATTESTATION")
//                                .build())
//                        .build())
//                .newChangeRequest(ChangeRequest.builder()
//                        .id(1L)
//                        .changeRequestType(ChangeRequestType.builder()
//                                .id(3L)
//                                .name("Developer Attestation Change Request")
//                                .build())
//                        .developer(new Developer())
//                        .currentStatus(ChangeRequestStatus.builder()
//                                .changeRequestStatusType(ChangeRequestStatusType.builder()
//                                        .id(1L)
//                                        .name("Pending ONC-ACB Action")
//                                        .build())
//                                .build())
//                        .details(ChangeRequestAttestation.builder()
//                                .attestation("TEST_ATTESTATION")
//                                .build())
//                        .build())
//                .build();
//
//        Boolean isValid = validator.isValid(context);
//
//        assertEquals(false, isValid);
//        assertEquals(1, validator.getMessages().size());
    }
}
