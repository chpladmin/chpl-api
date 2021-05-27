package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.entity.OptionalStandardEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OptionalStandard implements Serializable {
    private static final long serialVersionUID = 620315627813875501L;
    private Long id;
    private String name;
    private String description;

    public OptionalStandard(OptionalStandardEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.description = entity.getDescription();
    }
}
