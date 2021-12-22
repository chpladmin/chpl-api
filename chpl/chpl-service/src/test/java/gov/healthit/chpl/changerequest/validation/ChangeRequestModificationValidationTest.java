package gov.healthit.chpl.changerequest.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestation;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.domain.Developer;

public class ChangeRequestModificationValidationTest {

    private ChangeRequestModificationValidation validator;

    @Before
    public void setup() {
        validator = Mockito.spy(ChangeRequestModificationValidation.class);
    }

    @Test
    public void isValid_NewStatusAdded_ReturnTrueWithNoErrors() {
        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .origChangeRequest(ChangeRequest.builder()
                        .id(1L)
                        .changeRequestType(ChangeRequestType.builder()
                                .id(3L)
                                .name("Developer Attestation Change Request")
                                .build())
                        .developer(new Developer())
                        .currentStatus(ChangeRequestStatus.builder()
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(1L)
                                        .name("Pending ONC-ACB Action")
                                        .build())
                                .build())
                        .details(ChangeRequestAttestation.builder()
                                .attestation("TEST_ATTESTATION")
                                .build())
                        .build())
                .newChangeRequest(ChangeRequest.builder()
                        .id(1L)
                        .changeRequestType(ChangeRequestType.builder()
                                .id(3L)
                                .name("Developer Attestation Change Request")
                                .build())
                        .developer(new Developer())
                        .currentStatus(ChangeRequestStatus.builder()
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(3L)
                                        .name("Accepted")
                                        .build())
                                .build())
                        .details(ChangeRequestAttestation.builder()
                                .attestation("TEST_ATTESTATION_UPDATED")
                                .build())
                        .build())
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
        assertEquals(0, validator.getMessages().size());
    }

    @Test
    public void isValid_ChangeRequestDetailsUpdated_ReturnTrueWithNoErrors() {
        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .origChangeRequest(ChangeRequest.builder()
                        .id(1L)
                        .changeRequestType(ChangeRequestType.builder()
                                .id(3L)
                                .name("Developer Attestation Change Request")
                                .build())
                        .developer(new Developer())
                        .currentStatus(ChangeRequestStatus.builder()
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(1L)
                                        .name("Pending ONC-ACB Action")
                                        .build())
                                .build())
                        .details(ChangeRequestAttestation.builder()
                                .attestation("TEST_ATTESTATION")
                                .build())
                        .build())
                .newChangeRequest(ChangeRequest.builder()
                        .id(1L)
                        .changeRequestType(ChangeRequestType.builder()
                                .id(3L)
                                .name("Developer Attestation Change Request")
                                .build())
                        .developer(new Developer())
                        .currentStatus(ChangeRequestStatus.builder()
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(1L)
                                        .name("Pending ONC-ACB Action")
                                        .build())
                                .build())
                        .details(ChangeRequestAttestation.builder()
                                .attestation("TEST_ATTESTATION_UPDATED")
                                .build())
                        .build())
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(true, isValid);
        assertEquals(0, validator.getMessages().size());
    }

    @Test
    public void isValid_ChangeRequestDetailsNoChanged_ReturnFalseWith1Error() {
        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
                .origChangeRequest(ChangeRequest.builder()
                        .id(1L)
                        .changeRequestType(ChangeRequestType.builder()
                                .id(3L)
                                .name("Developer Attestation Change Request")
                                .build())
                        .developer(new Developer())
                        .currentStatus(ChangeRequestStatus.builder()
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(1L)
                                        .name("Pending ONC-ACB Action")
                                        .build())
                                .build())
                        .details(ChangeRequestAttestation.builder()
                                .attestation("TEST_ATTESTATION")
                                .build())
                        .build())
                .newChangeRequest(ChangeRequest.builder()
                        .id(1L)
                        .changeRequestType(ChangeRequestType.builder()
                                .id(3L)
                                .name("Developer Attestation Change Request")
                                .build())
                        .developer(new Developer())
                        .currentStatus(ChangeRequestStatus.builder()
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(1L)
                                        .name("Pending ONC-ACB Action")
                                        .build())
                                .build())
                        .details(ChangeRequestAttestation.builder()
                                .attestation("TEST_ATTESTATION")
                                .build())
                        .build())
                .build();

        Boolean isValid = validator.isValid(context);

        assertEquals(false, isValid);
        assertEquals(1, validator.getMessages().size());
    }
}
