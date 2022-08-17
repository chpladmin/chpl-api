package gov.healthit.chpl.form;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.Test;

public class FormTest {

    @Test
    public void extractFormItems_FormHasNoFormItems_Returns0FormItems() {
        List<FormItem> formItems = Form.builder().build().extractFormItems();

        assertNotNull(formItems);
        assertEquals(0, formItems.size());
    }

    @Test
    public void extractFormItems_FormHas2TopLevelFormItems_Returns2FormItems() {
        List<FormItem> formItems = getFormWithChildItems().extractFormItems();

        assertNotNull(formItems);
        assertEquals(2, formItems.size());
    }

    @Test
    public void extractFlatFormItems_FormHasNoFormItems_Returns0FormItems() {
        List<FormItem> formItems = Form.builder().build().extractFlatFormItems();

        assertNotNull(formItems);
        assertEquals(0, formItems.size());
    }

    @Test
    public void extractFlatFormItems_FormHas3FormItems_Returns3FormItems() {
        List<FormItem> formItems = getFormWithChildItems().extractFlatFormItems();

        assertNotNull(formItems);
        assertEquals(3, formItems.size());
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
                                        .question("Question #3")
                                        .allowedResponse(AllowedResponse.builder()
                                                .id(1L)
                                                .response("Yes")
                                                .build())
                                        .allowedResponse(AllowedResponse.builder()
                                                .id(2L)
                                                .response("No")
                                                .build())
                                        .build())
                                .required(true)
                                .sortOrder(1)
                                .build())
                        .build())
                .build();
    }

}
