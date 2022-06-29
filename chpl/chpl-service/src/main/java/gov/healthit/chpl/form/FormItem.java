package gov.healthit.chpl.form;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class FormItem {
    private Long id;
    private Form form;
    private Question question;
    private AllowedResponse parentResponse;
    private List<FormItem> childFormItems;
    private Integer sortOrder;
    private Boolean required;
}
