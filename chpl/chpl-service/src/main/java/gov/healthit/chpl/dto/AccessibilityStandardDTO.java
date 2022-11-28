package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.accessibilityStandard.AccessibilityStandardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccessibilityStandardDTO implements Serializable {
    private static final long serialVersionUID = 4987850364061817190L;
    private Long id;
    private String name;

    public AccessibilityStandardDTO(AccessibilityStandardEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
    }
}
