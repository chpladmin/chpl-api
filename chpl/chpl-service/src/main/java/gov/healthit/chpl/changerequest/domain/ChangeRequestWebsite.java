package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public @Data class ChangeRequestWebsite implements Serializable {
    private static final long serialVersionUID = -5572794875424284955L;

    private Long id;
    private String website;

}
