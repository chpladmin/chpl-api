package gov.healthit.chpl.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.developer.DeveloperStatusEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeveloperStatusDTO implements Serializable {
    private static final long serialVersionUID = 6227999632663396485L;
    private Long id;
    private String statusName;

    public DeveloperStatusDTO(DeveloperStatusEntity entity) {
        this.id = entity.getId();
        this.statusName = entity.getName().toString();
    }
}
