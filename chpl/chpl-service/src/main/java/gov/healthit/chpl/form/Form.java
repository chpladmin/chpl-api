package gov.healthit.chpl.form;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder
@AllArgsConstructor
public class Form {
    private Long id;
    private String description;

    @Singular
    private List<FormItem> formItems;
}
