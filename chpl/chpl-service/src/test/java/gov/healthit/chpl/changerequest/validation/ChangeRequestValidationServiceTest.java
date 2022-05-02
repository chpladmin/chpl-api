package gov.healthit.chpl.changerequest.validation;

import java.util.Arrays;

import org.junit.Before;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class ChangeRequestValidationServiceTest {

    private ChangeRequestCreateValidation changeRequestCreateValidation;
    private ChangeRequestDetailsUpdateValidation changeRequestDetailsUpdateValidation;
    private ChangeRequestTypeValidation changeRequestTypeValidation;
    private CurrentStatusValidation currentStatusValidation;
    private DeveloperExistenceValidation developerExistenceValidation;
    private DeveloperActiveValidation developerActiveValidation;
    private ChangeRequestNotUpdatableDueToStatusValidation changeRequestNotUpdatableDueToStatusValidation;
    private ChangeRequestTypeInProcessValidation changeRequestTypeInProcessValidation;
    private CommentRequiredValidation commentRequiredValidation;
    private RoleAcbHasMultipleCertificationBodiesValidation roleAcbHasMultipleCertificationBodiesValidation;
    private WebsiteValidation websiteValidation;
    private SelfDeveloperValidation selfDeveloperValidation;
    private DemographicValidation addressValidation;
    private ContactValidation contactValidation;

    private Long websiteChangeRequestTypeId = 1L;
    private Long developerDetailsChangeRequestTypeId = 2L;
    private Long attestationChangeRequestTypeId = 3L;

    private ChangeRequestValidationService changeRequestValidationService;

    @Before
    public void setup() {
        changeRequestCreateValidation = Mockito.mock(ChangeRequestCreateValidation.class);
        Mockito.when(changeRequestCreateValidation.isValid(ArgumentMatchers.any(ChangeRequestValidationContext.class))).thenReturn(false);
        Mockito.when(changeRequestCreateValidation.getMessages()).thenReturn(Arrays.asList("Error message"));

        changeRequestDetailsUpdateValidation = Mockito.mock(ChangeRequestDetailsUpdateValidation.class);
        Mockito.when(changeRequestDetailsUpdateValidation.isValid(ArgumentMatchers.any(ChangeRequestValidationContext.class))).thenReturn(false);
        Mockito.when(changeRequestDetailsUpdateValidation.getMessages()).thenReturn(Arrays.asList("Error message"));

        changeRequestTypeValidation = Mockito.mock(ChangeRequestTypeValidation.class);
        Mockito.when(changeRequestTypeValidation.isValid(ArgumentMatchers.any(ChangeRequestValidationContext.class))).thenReturn(false);
        Mockito.when(changeRequestTypeValidation.getMessages()).thenReturn(Arrays.asList("Error message"));

        currentStatusValidation = Mockito.mock(CurrentStatusValidation.class);
        Mockito.when(currentStatusValidation.isValid(ArgumentMatchers.any(ChangeRequestValidationContext.class))).thenReturn(false);
        Mockito.when(currentStatusValidation.getMessages()).thenReturn(Arrays.asList("Error message"));

        developerExistenceValidation = Mockito.mock(DeveloperExistenceValidation.class);
        Mockito.when(developerExistenceValidation.isValid(ArgumentMatchers.any(ChangeRequestValidationContext.class))).thenReturn(false);
        Mockito.when(developerExistenceValidation.getMessages()).thenReturn(Arrays.asList("Error message"));

        developerActiveValidation = Mockito.mock(DeveloperActiveValidation.class);
        Mockito.when(developerActiveValidation.isValid(ArgumentMatchers.any(ChangeRequestValidationContext.class))).thenReturn(false);
        Mockito.when(developerActiveValidation.getMessages()).thenReturn(Arrays.asList("Error message"));

        changeRequestNotUpdatableDueToStatusValidation = Mockito.mock(ChangeRequestNotUpdatableDueToStatusValidation.class);
        Mockito.when(changeRequestNotUpdatableDueToStatusValidation.isValid(ArgumentMatchers.any(ChangeRequestValidationContext.class))).thenReturn(false);
        Mockito.when(changeRequestNotUpdatableDueToStatusValidation.getMessages()).thenReturn(Arrays.asList("Error message"));

        changeRequestTypeInProcessValidation = Mockito.mock(ChangeRequestTypeInProcessValidation.class);
        Mockito.when(changeRequestTypeInProcessValidation.isValid(ArgumentMatchers.any(ChangeRequestValidationContext.class))).thenReturn(false);
        Mockito.when(changeRequestTypeInProcessValidation.getMessages()).thenReturn(Arrays.asList("Error message"));

        commentRequiredValidation = Mockito.mock(CommentRequiredValidation.class);
        Mockito.when(commentRequiredValidation.isValid(ArgumentMatchers.any(ChangeRequestValidationContext.class))).thenReturn(false);
        Mockito.when(commentRequiredValidation.getMessages()).thenReturn(Arrays.asList("Error message"));

        roleAcbHasMultipleCertificationBodiesValidation = Mockito.mock(RoleAcbHasMultipleCertificationBodiesValidation.class);
        Mockito.when(roleAcbHasMultipleCertificationBodiesValidation.isValid(ArgumentMatchers.any(ChangeRequestValidationContext.class))).thenReturn(false);
        Mockito.when(roleAcbHasMultipleCertificationBodiesValidation.getMessages()).thenReturn(Arrays.asList("Error message"));

        websiteValidation = Mockito.mock(WebsiteValidation.class);
        Mockito.when(websiteValidation.isValid(ArgumentMatchers.any(ChangeRequestValidationContext.class))).thenReturn(false);
        Mockito.when(websiteValidation.getMessages()).thenReturn(Arrays.asList("Error message"));

        selfDeveloperValidation = Mockito.mock(SelfDeveloperValidation.class);
        Mockito.when(selfDeveloperValidation.isValid(ArgumentMatchers.any(ChangeRequestValidationContext.class))).thenReturn(false);
        Mockito.when(selfDeveloperValidation.getMessages()).thenReturn(Arrays.asList("Error message"));

        addressValidation = Mockito.mock(DemographicValidation.class);
        Mockito.when(addressValidation.isValid(ArgumentMatchers.any(ChangeRequestValidationContext.class))).thenReturn(false);
        Mockito.when(addressValidation.getMessages()).thenReturn(Arrays.asList("Error message"));

        contactValidation = Mockito.mock(ContactValidation.class);
        Mockito.when(contactValidation.isValid(ArgumentMatchers.any(ChangeRequestValidationContext.class))).thenReturn(false);
        Mockito.when(contactValidation.getMessages()).thenReturn(Arrays.asList("Error message"));

        changeRequestValidationService = new ChangeRequestValidationService(
                websiteChangeRequestTypeId,
                developerDetailsChangeRequestTypeId,
                attestationChangeRequestTypeId);
    }

//    @Test
//    public void validate_CreateWebsiteChangeRequest_AllValidatorsRun() {
//        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
//                .newChangeRequest(ChangeRequest.builder()
//                        .changeRequestType(ChangeRequestType.builder()
//                                .id(websiteChangeRequestTypeId)
//                                .build())
//                        .build())
//                .build();
//
//        List<String> errorMsgs = changeRequestValidationService.validate(context);
//
//        assertEquals(6, errorMsgs.size());
//    }
//
//    @Test
//    public void validate_CreateDeveloperDetailsChangeRequest_AllValidatorsRun() {
//        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
//                .newChangeRequest(ChangeRequest.builder()
//                        .changeRequestType(ChangeRequestType.builder()
//                                .id(developerDetailsChangeRequestTypeId)
//                                .build())
//                        .build())
//                .build();
//
//        List<String> errorMsgs = changeRequestValidationService.validate(context);
//
//        assertEquals(8, errorMsgs.size());
//    }
//
//    @Test
//    public void validate_CreateAttestationChangeRequest_AllValidatorsRun() {
//        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
//                .newChangeRequest(ChangeRequest.builder()
//                        .changeRequestType(ChangeRequestType.builder()
//                                .id(attestationChangeRequestTypeId)
//                                .build())
//                        .build())
//                .build();
//
//        List<String> errorMsgs = changeRequestValidationService.validate(context);
//
//        assertEquals(5, errorMsgs.size());
//    }
//
//    @Test
//    public void validate_UpdateWebsiteChangeRequest_AllValidatorsRun() {
//        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
//                .newChangeRequest(ChangeRequest.builder()
//                        .changeRequestType(ChangeRequestType.builder()
//                                .id(websiteChangeRequestTypeId)
//                                .build())
//                        .build())
//                .origChangeRequest(ChangeRequest.builder()
//                        .changeRequestType(ChangeRequestType.builder()
//                                .id(websiteChangeRequestTypeId)
//                                .build())
//                        .build())
//                .build();
//
//        List<String> errorMsgs = changeRequestValidationService.validate(context);
//
//        assertEquals(8, errorMsgs.size());
//    }
//
//    @Test
//    public void validate_UpdateDeveloperDetailsChangeRequest_AllValidatorsRun() {
//        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
//                .newChangeRequest(ChangeRequest.builder()
//                        .changeRequestType(ChangeRequestType.builder()
//                                .id(developerDetailsChangeRequestTypeId)
//                                .build())
//                        .build())
//                .origChangeRequest(ChangeRequest.builder()
//                        .changeRequestType(ChangeRequestType.builder()
//                                .id(developerDetailsChangeRequestTypeId)
//                                .build())
//                        .build())
//                .build();
//
//        List<String> errorMsgs = changeRequestValidationService.validate(context);
//
//        assertEquals(10, errorMsgs.size());
//    }
//
//    @Test
//    public void validate_UpdateAttestationChangeRequest_AllValidatorsRun() {
//        ChangeRequestValidationContext context = ChangeRequestValidationContext.builder()
//                .newChangeRequest(ChangeRequest.builder()
//                        .changeRequestType(ChangeRequestType.builder()
//                                .id(attestationChangeRequestTypeId)
//                                .build())
//                        .build())
//                .origChangeRequest(ChangeRequest.builder()
//                        .changeRequestType(ChangeRequestType.builder()
//                                .id(attestationChangeRequestTypeId)
//                                .build())
//                        .build())
//                .build();
//
//        List<String> errorMsgs = changeRequestValidationService.validate(context);
//
//        assertEquals(7, errorMsgs.size());
//    }

}
