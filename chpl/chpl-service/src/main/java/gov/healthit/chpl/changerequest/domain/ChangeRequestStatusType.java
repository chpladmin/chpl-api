package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public @Data class ChangeRequestStatusType implements Serializable {
    private static final long serialVersionUID = -3309062067247912001L;

    private Long id;
    private String name;
}
