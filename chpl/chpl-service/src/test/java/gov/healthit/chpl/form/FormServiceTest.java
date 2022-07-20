package gov.healthit.chpl.form;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.form.dao.FormDAO;
import gov.healthit.chpl.form.entity.AllowedResponseEntity;
import gov.healthit.chpl.form.entity.FormEntity;
import gov.healthit.chpl.form.entity.FormItemEntity;
import gov.healthit.chpl.form.entity.QuestionEntity;
import gov.healthit.chpl.form.entity.ResponseCardinalityTypeEntity;
import gov.healthit.chpl.form.entity.SectionHeadingEntity;

public class FormServiceTest {
    private FormDAO formDAO;

    private FormService formService;

    @Before
    public void setup() {
        formDAO = Mockito.mock(FormDAO.class);

        formService = new FormService(formDAO);
    }

    @Test(expected = EntityRetrievalException.class)
    public void getForm_FormIdDoesNotExist_ThrowsEntityRetrievalException() throws EntityRetrievalException {
        Mockito.when(formDAO.getForm(ArgumentMatchers.anyLong())).thenThrow(EntityRetrievalException.class);

        formService.getForm(1L);
    }

    @Test
    public void getForm_1Section2FormItems_ReturnForm() throws EntityRetrievalException {
        Mockito.when(formDAO.getForm(ArgumentMatchers.anyLong())).thenReturn(
                FormEntity.builder()
                    .id(1L)
                    .description("Form X")
                    .build());

        Mockito.when(formDAO.getFormItems(ArgumentMatchers.anyLong(), ArgumentMatchers.isNull())).thenReturn(
                List.of(getFormItemEntity(1L, 1L, "Question #1", 1L, "Section #1"),
                        getFormItemEntity(2L, 2L, "Question #2", 1L, "Section #1")));

        Form form = formService.getForm(1L);

        assertNotNull(form);
        assertNotNull(form.getSectionHeadings());
        assertEquals(1, form.getSectionHeadings().size());
        assertNotNull(form.getSectionHeadings().get(0).getFormItems());
        assertEquals(2, form.getSectionHeadings().get(0).getFormItems().size());
    }

    @Test
    public void getForm_2Section2FormItems_ReturnForm() throws EntityRetrievalException {
        Mockito.when(formDAO.getForm(ArgumentMatchers.anyLong())).thenReturn(
                FormEntity.builder()
                    .id(1L)
                    .description("Form X")
                    .build());

        Mockito.when(formDAO.getFormItems(ArgumentMatchers.anyLong(), ArgumentMatchers.isNull())).thenReturn(
                List.of(getFormItemEntity(1L, 1L, "Question #1", 1L, "Section #1"),
                        getFormItemEntity(2L, 2L, "Question #2", 2L, "Section #2")));

        Form form = formService.getForm(1L);

        assertNotNull(form);
        assertNotNull(form.getSectionHeadings());
        assertEquals(2, form.getSectionHeadings().size());
        assertNotNull(form.getSectionHeadings().get(0).getFormItems());
        assertEquals(1, form.getSectionHeadings().get(0).getFormItems().size());
        assertNotNull(form.getSectionHeadings().get(1).getFormItems());
        assertEquals(1, form.getSectionHeadings().get(1).getFormItems().size());
    }

    public FormItemEntity getFormItemEntity(Long formItemId, Long questionId, String question, Long sectionHeadingId, String section) {
        return FormItemEntity.builder()
                .id(formItemId)
                .sortOrder((int) (Math.random() * 100))
                .required(true)
                .form(FormEntity.builder()
                        .id(1L)
                        .description("Form X")
                        .build())
                .question(QuestionEntity.builder()
                        .id(questionId)
                        .question(question)
                        .responseCardinalityType(ResponseCardinalityTypeEntity.builder()
                                .id(1L)
                                .description("Single")
                                .build())
                        .allowedResponse(AllowedResponseEntity.builder()
                                .id(1l)
                                .response("Yes")
                                .build())
                        .sectionHeading(SectionHeadingEntity.builder()
                                .id(sectionHeadingId)
                                .name(section)
                                .build())

                        .build())
                .build();
    }
 }
