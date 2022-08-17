package gov.healthit.chpl.attestation.manager;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.attestation.dao.AttestationDAO;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationSubmission;
import gov.healthit.chpl.attestation.entity.AttestationSubmissionResponseEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.form.AllowedResponse;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.form.FormItem;
import gov.healthit.chpl.form.FormService;
import gov.healthit.chpl.form.Question;
import gov.healthit.chpl.form.ResponseCardinalityType;
import gov.healthit.chpl.form.SectionHeading;
import gov.healthit.chpl.form.entity.AllowedResponseEntity;
import gov.healthit.chpl.form.entity.FormItemEntity;

public class AttestationSubmissionServiceTest {

    private AttestationDAO attestationDAO;
    private FormService formService;

    private AttestationSubmissionService service;

    @Before
    public void setup() {
        attestationDAO = Mockito.mock(AttestationDAO.class);
        formService = Mockito.mock(FormService.class);

        service = new AttestationSubmissionService(attestationDAO, formService);
    }

    @Test
    public void getAttestationSubmissions_ResponsesExistForAttesttion_ReturnsAttestationSubmissionWithResponses() throws EntityRetrievalException {
        Mockito.when(attestationDAO.getAttestationSubmissionsByDeveloper(ArgumentMatchers.anyLong())).thenReturn(
                List.of(AttestationSubmission.builder()
                        .id(1L)
                        .developerId(1L)
                        .attestationPeriod(getSecondAttestationPeriod())
                        .form(getFormWithChildItems())
                        .signature("Joe Smith")
                        .signature("smith@company.com")
                        .build()));

        Mockito.when(formService.getForm(ArgumentMatchers.anyLong())).thenReturn(getFormWithChildItems());

        Mockito.when(attestationDAO.getAttestationSubmissionResponseEntities(ArgumentMatchers.anyLong())).thenReturn(
                List.of(AttestationSubmissionResponseEntity.builder()
                            .id(1L)
                            .attestationSubmissionId(1L)
                            .formItem(FormItemEntity.builder().id(1L).build())
                            .response(AllowedResponseEntity.builder().id(1L).build())
                            .build(),
                        AttestationSubmissionResponseEntity.builder()
                            .id(2L)
                            .attestationSubmissionId(1L)
                            .formItem(FormItemEntity.builder().id(2L).build())
                            .response(AllowedResponseEntity.builder().id(3L).build())
                            .build(),
                        AttestationSubmissionResponseEntity.builder()
                            .id(3L)
                            .attestationSubmissionId(1L)
                            .formItem(FormItemEntity.builder().id(2L).build())
                            .response(AllowedResponseEntity.builder().id(4L).build())
                            .build()));

        List<AttestationSubmission> attestationSubmission = service.getAttestationSubmissions(1L);

        assertNotNull(attestationSubmission);
        //Top Level Question
        assertEquals(attestationSubmission.get(0).getForm()
                .getSectionHeadings().get(0)
                .getFormItems().get(0)
                .getSubmittedResponses().size(), 1);

        //Subordinate Question
        assertEquals(attestationSubmission.get(0).getForm()
                .getSectionHeadings().get(0)
                .getFormItems().get(0)
                .getChildFormItems().get(0)
                .getSubmittedResponses().size(), 2);
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

    private Form getFormWithChildItems() {
        return Form.builder()
                .sectionHeading(SectionHeading.builder()
                        .id(1L)
                        .name("Section 1")
                        .sortOrder(1)
                        .formItem(FormItem.builder()
                                .id(1L)
                                .question(Question.builder()
                                        .id(1L)
                                        .responseCardinalityType(ResponseCardinalityType.builder()
                                                .id(1L)
                                                .description("Single")
                                                .build())
                                        .question("Question #1")
                                        .allowedResponse(AllowedResponse.builder()
                                                .id(1L)
                                                .response("Yes")
                                                .build())
                                        .allowedResponse(AllowedResponse.builder()
                                                .id(2L)
                                                .response("No")
                                                .build())
                                        .build())
                                .parentResponse(AllowedResponse.builder()
                                        .id(1L)
                                        .response("Yes")
                                        .build())
                                .required(true)
                                .sortOrder(1)
                                .childFormItem(FormItem.builder()
                                        .id(2L)
                                        .question(Question.builder()
                                                .id(2L)
                                                .responseCardinalityType(ResponseCardinalityType.builder()
                                                        .id(2L)
                                                        .description("Multiple")
                                                        .build())
                                                .question("Question #2")
                                                .allowedResponse(AllowedResponse.builder()
                                                        .id(3L)
                                                        .response("More than 10")
                                                        .build())
                                                .allowedResponse(AllowedResponse.builder()
                                                        .id(4L)
                                                        .response("Less Than 20")
                                                        .build())
                                                .build())
                                        .required(false)
                                        .sortOrder(1)
                                        .build())
                                .build()
                                )
                        .build())
                .build();
    }

}
