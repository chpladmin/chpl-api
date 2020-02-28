package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.auth.UserPermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public @Data class ChangeRequestStatus implements Serializable {
    private static final long serialVersionUID = 3333749014276324377L;

    private Long id;
    private ChangeRequestStatusType changeRequestStatusType;
    private Date statusChangeDate;
    private String comment;
    private CertificationBody certificationBody;
    private UserPermission userPermission;

}
