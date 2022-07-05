package gov.healthit.chpl.form;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Form implements Serializable {
    private static final long serialVersionUID = 2148616530869605769L;

    private Long id;
    private String description;
    private List<SectionHeading> sectionHeadings;
}
