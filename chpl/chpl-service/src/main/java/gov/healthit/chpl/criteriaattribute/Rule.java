package gov.healthit.chpl.criteriaattribute;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Rule implements Serializable {
    private static final long serialVersionUID = 1896885792278074234L;

    private Long id;
    private String name;
}
