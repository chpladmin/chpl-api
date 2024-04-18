package gov.healthit.chpl.domain.activity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserMaintenanceActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = -3518832572761720950L;

    private String email;
    private String subjectName;

}
