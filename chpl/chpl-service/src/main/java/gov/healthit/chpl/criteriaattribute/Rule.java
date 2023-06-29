package gov.healthit.chpl.criteriaattribute;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule implements Serializable {
    private static final long serialVersionUID = 1896885792278074234L;

    private Long id;
    private String name;
}
