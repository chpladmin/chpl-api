package gov.healthit.chpl.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SectionHeading {
    private Long id;
    private String name;
}
