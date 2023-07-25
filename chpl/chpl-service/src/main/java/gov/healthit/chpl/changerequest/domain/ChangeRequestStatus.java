package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.auth.UserPermission;
import gov.healthit.chpl.util.EasternToSystemLocalDateTimeDeserializer;
import gov.healthit.chpl.util.SystemToEasternLocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChangeRequestStatus implements Serializable {
    private static final long serialVersionUID = 3333749014276324377L;

    private Long id;
    private ChangeRequestStatusType changeRequestStatusType;
    @JsonDeserialize(using = EasternToSystemLocalDateTimeDeserializer.class)
    @JsonSerialize(using = SystemToEasternLocalDateTimeSerializer.class)
    private LocalDateTime statusChangeDateTime;

    private String comment;
    private CertificationBody certificationBody;
    private UserPermission userPermission;

}
