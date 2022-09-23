package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.auth.UserPermission;
import gov.healthit.chpl.util.EasternToUtcLocalDateTimeDeserializer;
import gov.healthit.chpl.util.UtcToEasternLocalDateTimeSerializer;
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
    @JsonDeserialize(using = EasternToUtcLocalDateTimeDeserializer.class)
    @JsonSerialize(using = UtcToEasternLocalDateTimeSerializer.class)
    private LocalDateTime statusChangeDateTime;

    @DeprecatedResponseField(
            removalDate = "2023-03-15",
            message = "This field is deprecated and will be removed from the response data in a future release. Please replace usage of the 'statusChangeDate' field with 'statusChangeDateTime'.")
    @Deprecated
    private Date statusChangeDate;

    private String comment;
    private CertificationBody certificationBody;
    private UserPermission userPermission;

}
