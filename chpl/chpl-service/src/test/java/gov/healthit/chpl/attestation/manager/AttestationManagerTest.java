package gov.healthit.chpl.attestation.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.attestation.dao.AttestationDAO;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationPeriodDeveloperException;
import gov.healthit.chpl.attestation.domain.AttestationPeriodForm;
import gov.healthit.chpl.attestation.domain.AttestationSubmission;
import gov.healthit.chpl.attestation.service.AttestationResponseValidationService;
import gov.healthit.chpl.attestation.service.AttestationSubmissionService;
import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.form.FormService;
import gov.healthit.chpl.form.SectionHeading;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class AttestationManagerTest {
    private AttestationManager manager;
    private AttestationDAO attestationDAO;
    private AttestationPeriodService attestationPeriodService;
    private FormService formService;
    private AttestationSubmissionService attestationSubmissionService;
    private ChangeRequestDAO changeRequestDAO;
    private AttestationExceptionEmail exceptionEmail;
    private ErrorMessageUtil errorMessageUtil;
    private static final Integer DEFAULT_EXCEPTION_WINDOW = 3;

    @BeforeEach
    public void setup() throws EntityRetrievalException {
        attestationDAO = Mockito.mock(AttestationDAO.class);
        attestationPeriodService = Mockito.mock(AttestationPeriodService.class);
        formService = Mockito.mock(FormService.class);
        attestationSubmissionService = Mockito.mock(AttestationSubmissionService.class);
        changeRequestDAO = Mockito.mock(ChangeRequestDAO.class);
        exceptionEmail = Mockito.mock(AttestationExceptionEmail.class);
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(formService.getForm(ArgumentMatchers.anyLong())).thenReturn(getFirstPeriodForm());

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any(Object[].class)))
            .thenReturn("This is an error message.");

        Mockito.when(attestationPeriodService.getAllPeriods()).thenReturn(
                List.of(getFirstAttestationPeriod(), getSecondAttestationPeriod()));

        manager = new AttestationManager(attestationDAO, attestationPeriodService, formService,
                attestationSubmissionService, Mockito.mock(AttestationResponseValidationService.class),
                Mockito.mock(ListingSearchService.class),
                changeRequestDAO, exceptionEmail, errorMessageUtil, DEFAULT_EXCEPTION_WINDOW);
    }

    @Test
    public void getAllPeriods_Success_Returns2Periods() {
        List<AttestationPeriod> periods = manager.getAllPeriods();

        assertEquals(2, periods.size());
    }

    @Test
    public void getAttestationForm_Success_ReturnsAttestationPeriodForm() throws EntityRetrievalException {
        AttestationPeriodForm attestationPeriodForm = manager.getAttestationForm(1L, null);

        assertNotNull(attestationPeriodForm.getForm());
        assertNotNull(attestationPeriodForm.getPeriod());
    }

    @Test
    public void getAttestationForm_FormDoesNotExistForPeriod_ThrowsException() throws EntityRetrievalException {
        Mockito.when(formService.getForm(ArgumentMatchers.anyLong())).thenThrow(EntityRetrievalException.class);

        Exception exception = assertThrows(EntityRetrievalException.class, () -> {
            manager.getAttestationForm(1L, null);
        });
        assertNotNull(exception);
    }

    @Test
    public void getAttestationForm_PeriodDoesNotExist_ReturnsNull() throws EntityRetrievalException {
        AttestationPeriodForm attestationPeriodForm = manager.getAttestationForm(3L, null);

        assertNull(attestationPeriodForm);
    }

    @Test
    public void saveDeveloperAttestation_Success_ReturnSavedObject() throws EntityRetrievalException {
        AttestationSubmission submittedAttestationSubmission = AttestationSubmission.builder()
                .developerId(1L)
                .attestationPeriod(getFirstAttestationPeriod())
                .form(getFirstPeriodForm())
                .signature("Person A")
                .signatureEmail("persion_a@company.com")
                .build();

        AttestationSubmission returnedAttestationSubmission = AttestationSubmission.builder()
                .id(1L)
                .developerId(1L)
                .attestationPeriod(getFirstAttestationPeriod())
                .form(getFirstPeriodForm())
                .signature("Person A")
                .signatureEmail("persion_a@company.com")
                .build();

        Mockito.when(attestationDAO.saveAttestationSubmssion(ArgumentMatchers.anyLong(), ArgumentMatchers.any(AttestationSubmission.class)))
                .thenReturn(returnedAttestationSubmission);

        AttestationSubmission as = manager.saveDeveloperAttestation(1L, submittedAttestationSubmission);

        assertNotNull(as);
        assertNotNull(as.getId());
        assertEquals(1L, as.getId());
    }

    @Test
    public void getDeveloperAttestations_Success_AllAttestationSubmissions() {
        Mockito.when(attestationSubmissionService.getAttestationSubmissions(ArgumentMatchers.anyLong())).thenReturn(
                List.of(AttestationSubmission.builder()
                    .id(1L)
                    .developerId(1L)
                    .attestationPeriod(getFirstAttestationPeriod())
                    .form(getFirstPeriodForm())
                    .signature("Person A")
                    .signatureEmail("persion_a@company.com")
                    .build(),
                AttestationSubmission.builder()
                    .id(2L)
                    .developerId(1L)
                    .attestationPeriod(getSecondAttestationPeriod())
                    .form(getSecondPeriodForm())
                    .signature("Person A")
                    .signatureEmail("persion_a@company.com")
                    .build()));

        List<AttestationSubmission> submissions = manager.getDeveloperAttestations(1L);

        assertNotNull(submissions);
        assertEquals(2, submissions.size());
    }

    @Test
    public void canDeveloperSubmitChangeRequest_PendingCrDoesNotExistAndNoExceptionExistsAndCurrentDateNotWithinSubmissionPeriod_ReturnFalse() throws EntityRetrievalException {
        Mockito.when(attestationPeriodService.getMostRecentPastAttestationPeriod()).thenReturn(getFirstAttestationPeriod());

        // No pending Change Requests
        Mockito.when(changeRequestDAO.getByDeveloper(ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
            .thenReturn(
                List.of(ChangeRequest.builder()
                        .developer(Developer.builder()
                                .id(1L)
                                .build())
                        .changeRequestType(ChangeRequestType.builder()
                                .id(1l)
                                .name("Developer Attestation Change Request")
                                .build())
                        .currentStatus(ChangeRequestStatus.builder()
                                .id(1L)
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(3L)
                                        .name("Accepted")
                                        .build())
                                .build())
                        .build()));

        //No Exception Exists
        Mockito.when(attestationPeriodService.getCurrentExceptionEndDateForDeveloper(ArgumentMatchers.anyLong())).thenReturn(null);

        //Current Date not within submission period
        Mockito.when(attestationPeriodService.isDateWithinSubmissionPeriodForDeveloper(ArgumentMatchers.anyLong(), ArgumentMatchers.any(LocalDate.class))).thenReturn(false);

        assertFalse(manager.canDeveloperSubmitChangeRequest(1L));

    }

    @Test
    public void canDeveloperSubmitChangeRequest_PendingCrDoesExistAndNoExceptionExistsAndCurrentDateNotWithinSubmissionPeriod_ReturnFalse() throws EntityRetrievalException {
        Mockito.when(attestationPeriodService.getMostRecentPastAttestationPeriod()).thenReturn(getFirstAttestationPeriod());

        // ending Change Requests
        Mockito.when(changeRequestDAO.getByDeveloper(ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
            .thenReturn(
                List.of(ChangeRequest.builder()
                        .developer(Developer.builder()
                                .id(1L)
                                .build())
                        .changeRequestType(ChangeRequestType.builder()
                                .id(1L)
                                .name("Developer Attestation Change Request")
                                .build())
                        .currentStatus(ChangeRequestStatus.builder()
                                .id(1L)
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(1L)
                                        .name("Pending ONC-ACB Action")
                                        .build())
                                .build())
                        .build()));

        //No Exception Exists
        Mockito.when(attestationPeriodService.getCurrentExceptionEndDateForDeveloper(ArgumentMatchers.anyLong())).thenReturn(null);

        //Current Date not within submission period
        Mockito.when(attestationPeriodService.isDateWithinSubmissionPeriodForDeveloper(ArgumentMatchers.anyLong(), ArgumentMatchers.any(LocalDate.class))).thenReturn(false);

        assertFalse(manager.canDeveloperSubmitChangeRequest(1L));

    }

    @Test
    public void canDeveloperSubmitChangeRequest_PendingCrDoesNotExistAndNoExceptionExistsAndCurrentDateWithinSubmissionPeriodAndAttestationDoesNotExistForDveloper_ReturnTrue() throws EntityRetrievalException {
        Mockito.when(attestationPeriodService.getMostRecentPastAttestationPeriod()).thenReturn(getFirstAttestationPeriod());

        //Pending Change Requests
        Mockito.when(changeRequestDAO.getByDeveloper(ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
            .thenReturn(
                List.of(ChangeRequest.builder()
                        .developer(Developer.builder()
                                .id(1L)
                                .build())
                        .changeRequestType(ChangeRequestType.builder()
                                .id(1L)
                                .name("Developer Attestation Change Request")
                                .build())
                        .currentStatus(ChangeRequestStatus.builder()
                                .id(1L)
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(3L)
                                        .name("Accepted")
                                        .build())
                                .build())
                        .build()));

        //No Exception Exists
        Mockito.when(attestationPeriodService.getCurrentExceptionEndDateForDeveloper(ArgumentMatchers.anyLong())).thenReturn(null);

        //Current Date not within submission period
        Mockito.when(attestationPeriodService.isDateWithinSubmissionPeriodForDeveloper(ArgumentMatchers.anyLong(), ArgumentMatchers.any(LocalDate.class))).thenReturn(true);

        //Attestation does not exist for developer
        Mockito.when(attestationDAO.getAttestationSubmissionsByDeveloper(ArgumentMatchers.anyLong())).thenReturn(new ArrayList<AttestationSubmission>());

        assertTrue(manager.canDeveloperSubmitChangeRequest(1L));
    }

    @Test
    public void canDeveloperSubmitChangeRequest_PendingCrDoesNotExistAndNoExceptionExistsAndCurrentDateWithinSubmissionPeriodAndAttestationDoesExistForDveloper_ReturnFalse() throws EntityRetrievalException {
        Mockito.when(attestationPeriodService.getMostRecentPastAttestationPeriod()).thenReturn(getFirstAttestationPeriod());

        // No pending Change Requests
        Mockito.when(changeRequestDAO.getByDeveloper(ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
            .thenReturn(
                List.of(ChangeRequest.builder()
                        .developer(Developer.builder()
                                .id(1L)
                                .build())
                        .changeRequestType(ChangeRequestType.builder()
                                .id(1L)
                                .name("Developer Attestation Change Request")
                                .build())
                        .currentStatus(ChangeRequestStatus.builder()
                                .id(1L)
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(3L)
                                        .name("Accepted")
                                        .build())
                                .build())
                        .build()));

        //No Exception Exists
        Mockito.when(attestationPeriodService.getCurrentExceptionEndDateForDeveloper(ArgumentMatchers.anyLong())).thenReturn(null);

        //Current Date not within submission period
        Mockito.when(attestationPeriodService.isDateWithinSubmissionPeriodForDeveloper(ArgumentMatchers.anyLong(), ArgumentMatchers.any(LocalDate.class))).thenReturn(true);

        //Attestation does exist for developer
        Mockito.when(attestationDAO.getAttestationSubmissionsByDeveloper(ArgumentMatchers.anyLong())).thenReturn(
                List.of(AttestationSubmission.builder()
                        .id(1L)
                        .developerId(1L)
                        .attestationPeriod(getFirstAttestationPeriod())
                        .build()));

        assertFalse(manager.canDeveloperSubmitChangeRequest(1L));
    }

    @Test
    public void getSubmittablePeriod_CanDeveloperSubmitCrIsFalse_ReturnNull() throws EntityRetrievalException {
        //This is so we don't have to mock a bunch of stuff, when just spy on canDeveloperSubmitChangeRequest()
        AttestationManager spyManager = Mockito.spy(
                new AttestationManager(attestationDAO, attestationPeriodService, formService,
                        attestationSubmissionService, Mockito.mock(AttestationResponseValidationService.class),
                        Mockito.mock(ListingSearchService.class),
                        changeRequestDAO, exceptionEmail, errorMessageUtil, DEFAULT_EXCEPTION_WINDOW));
        Mockito.doReturn(false).when(spyManager).canDeveloperSubmitChangeRequest(ArgumentMatchers.anyLong());

        AttestationPeriod period = spyManager.getSubmittablePeriod(1L);

        assertNull(period);
    }

    @Test
    public void getSubmittablePeriod_CanDeveloperSubmitCrIsTrue_ReturnPeriod() throws EntityRetrievalException {
        //This is so we don't have to mock a bunch of stuff, when just spy on canDeveloperSubmitChangeRequest()
        AttestationManager spyManager = Mockito.spy(
                new AttestationManager(attestationDAO, attestationPeriodService, formService,
                        attestationSubmissionService,  Mockito.mock(AttestationResponseValidationService.class),
                        Mockito.mock(ListingSearchService.class),
                        changeRequestDAO, exceptionEmail, errorMessageUtil, DEFAULT_EXCEPTION_WINDOW));
        Mockito.doReturn(true).when(spyManager).canDeveloperSubmitChangeRequest(ArgumentMatchers.anyLong());
        Mockito.when(attestationPeriodService.getSubmittableAttestationPeriod(ArgumentMatchers.anyLong())).thenReturn(getFirstAttestationPeriod());

        AttestationPeriod period = spyManager.getSubmittablePeriod(1L);

        assertNotNull(period);
    }

    @Test
    public void canCreateException_PendingCrExists_ReturnFalse() throws EntityRetrievalException {
        //Pending Change Requests
        Mockito.when(changeRequestDAO.getByDeveloper(ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
            .thenReturn(
                List.of(ChangeRequest.builder()
                        .developer(Developer.builder()
                                .id(1L)
                                .build())
                        .changeRequestType(ChangeRequestType.builder()
                                .id(1L)
                                .name("Developer Attestation Change Request")
                                .build())
                        .currentStatus(ChangeRequestStatus.builder()
                                .id(1L)
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(1L)
                                        .name("Pending ONC-ACB Action")
                                        .build())
                                .build())
                        .build()));

        assertFalse(manager.canCreateException(1L));
    }

    @Test
    public void canCreateException_PendingCrNotExistsAndSubmittablePeriodExists_ReturnTrue() throws EntityRetrievalException {

        /////use a spy to mock getSubmittablePeriod()
        AttestationManager spyManager = Mockito.spy(
                new AttestationManager(attestationDAO, attestationPeriodService, formService,
                        attestationSubmissionService,  Mockito.mock(AttestationResponseValidationService.class),
                        Mockito.mock(ListingSearchService.class),

                        changeRequestDAO, exceptionEmail, errorMessageUtil, DEFAULT_EXCEPTION_WINDOW));

        Mockito.doReturn(null).when(spyManager).getSubmittablePeriod(ArgumentMatchers.anyLong());

        //Pending Change Requests
        Mockito.when(changeRequestDAO.getByDeveloper(ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
            .thenReturn(
                List.of(ChangeRequest.builder()
                        .developer(Developer.builder()
                                .id(1L)
                                .build())
                        .changeRequestType(ChangeRequestType.builder()
                                .id(1L)
                                .name("Developer Attestation Change Request")
                                .build())
                        .currentStatus(ChangeRequestStatus.builder()
                                .id(1L)
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(3L)
                                        .name("Accepted")
                                        .build())
                                .build())
                        .build()));

        assertTrue(spyManager.canCreateException(1L));
    }

    @Test
    public void canCreateException_PendingCrNotExistsAndSubmittablePeriodNotExists_ReturnFalse() throws EntityRetrievalException {

        /////use a spy to mock getSubmittablePeriod()
        AttestationManager spyManager = Mockito.spy(
                new AttestationManager(attestationDAO, attestationPeriodService, formService,
                        attestationSubmissionService,  Mockito.mock(AttestationResponseValidationService.class),
                        Mockito.mock(ListingSearchService.class),
                        changeRequestDAO, exceptionEmail, errorMessageUtil, DEFAULT_EXCEPTION_WINDOW));

        Mockito.doReturn(getFirstAttestationPeriod()).when(spyManager).getSubmittablePeriod(ArgumentMatchers.anyLong());

        //Pending Change Requests
        Mockito.when(changeRequestDAO.getByDeveloper(ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean()))
            .thenReturn(
                List.of(ChangeRequest.builder()
                        .developer(Developer.builder()
                                .id(1L)
                                .build())
                        .changeRequestType(ChangeRequestType.builder()
                                .id(1L)
                                .name("Developer Attestation Change Request")
                                .build())
                        .currentStatus(ChangeRequestStatus.builder()
                                .id(1L)
                                .changeRequestStatusType(ChangeRequestStatusType.builder()
                                        .id(3L)
                                        .name("Accepted")
                                        .build())
                                .build())
                        .build()));

        assertFalse(spyManager.canCreateException(1L));
    }

    @Test
    public void createAttestationPeriodDeveloperException_InvalidPeriodId_ThrowsValidationException() throws EntityRetrievalException, ValidationException {
        Mockito.when(attestationPeriodService.getAllPeriods()).thenReturn(
                List.of(getFirstAttestationPeriod(), getSecondAttestationPeriod()));

        Exception exception = assertThrows(ValidationException.class, () -> {
            //3L should not exist in list of periods
            manager.createAttestationPeriodDeveloperException(1L,  3L);
        });
        assertNotNull(exception);
    }

    @Test
    public void createAttestationPeriodDeveloperException_CanCreateExceptionIsFalse_ThrowsValidationException() throws EntityRetrievalException, ValidationException {
        /////use a spy to mock canCreateException()
        AttestationManager spyManager = Mockito.spy(
                new AttestationManager(attestationDAO, attestationPeriodService, formService,
                        attestationSubmissionService,  Mockito.mock(AttestationResponseValidationService.class),
                        Mockito.mock(ListingSearchService.class),
                        changeRequestDAO, exceptionEmail, errorMessageUtil, DEFAULT_EXCEPTION_WINDOW));

        Mockito.doReturn(false).when(spyManager).canCreateException(ArgumentMatchers.anyLong());

        Mockito.when(attestationPeriodService.getAllPeriods()).thenReturn(
                List.of(getFirstAttestationPeriod(), getSecondAttestationPeriod()));

        Mockito.when(attestationDAO.createAttestationPeriodDeveloperException(ArgumentMatchers.any(AttestationPeriodDeveloperException.class))).thenReturn(
                AttestationPeriodDeveloperException.builder().build());

        Exception exception = assertThrows(ValidationException.class, () -> {
            assertNotNull(spyManager.createAttestationPeriodDeveloperException(1L,  1L));

        });
        assertNotNull(exception);
    }

    private AttestationPeriod getFirstAttestationPeriod() {
        return AttestationPeriod.builder()
                .id(1L)
                .periodStart(LocalDate.of(2021, 1, 1))
                .periodEnd(LocalDate.of(2022, 1, 31))
                .submissionStart(LocalDate.of(2022, 2, 1))
                .submissionEnd(LocalDate.of(2022, 2, 28))
                .description("First Period")
                .form(Form.builder()
                        .id(1L)
                        .build())
                .build();
    }

    private AttestationPeriod getSecondAttestationPeriod() {
        return AttestationPeriod.builder()
                .id(2L)
                .periodStart(LocalDate.of(2022, 2, 1))
                .periodEnd(LocalDate.of(2022, 9, 30))
                .submissionStart(LocalDate.of(2022, 10, 1))
                .submissionEnd(LocalDate.of(2022, 10, 30))
                .description("Second Period")
                .form(Form.builder()
                        .id(2L)
                        .build())
                .build();
    }

    private Form getFirstPeriodForm() {
        return Form.builder()
                .id(1L)
                .description("First Attestation Period Form")
                .sectionHeadings(List.of(SectionHeading.builder().build()))
                .build();
    }

    private Form getSecondPeriodForm() {
        return Form.builder()
                .id(2L)
                .description("Second Attestation Period Form")
                .sectionHeadings(List.of(SectionHeading.builder().build()))
                .build();
    }


}
