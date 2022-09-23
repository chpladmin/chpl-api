package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.util.EasternToUtcLocalDateTimeDeserializer;
import gov.healthit.chpl.util.UtcToEasternLocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChangeRequest implements Serializable {
    private static final long serialVersionUID = 216843913133697622L;

    private Long id;
    private ChangeRequestType changeRequestType;
    private Developer developer;
    @Singular
    private List<CertificationBody> certificationBodies = new ArrayList<CertificationBody>();
    private ChangeRequestStatus currentStatus;
    @Singular
    private List<ChangeRequestStatus> statuses = new ArrayList<ChangeRequestStatus>();
    private Object details;

    @JsonDeserialize(using = EasternToUtcLocalDateTimeDeserializer.class)
    @JsonSerialize(using = UtcToEasternLocalDateTimeSerializer.class)
    private LocalDateTime submittedDateTime;

    @DeprecatedResponseField(
            removalDate = "2023-03-15",
            message = "This field is deprecated and will be removed from the response data in a future release. Please replace usage of the 'submittedDate' field with 'submittedDateTime'.")
    @Deprecated
    private Date submittedDate;

}
