package gov.healthit.chpl.form;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class SectionHeading {
    private Long id;
    private String name;
    private Integer sortOrder;

    @Singular
    private List<FormItem> formItems;
}
