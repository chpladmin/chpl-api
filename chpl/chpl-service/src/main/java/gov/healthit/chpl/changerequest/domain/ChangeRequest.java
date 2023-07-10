package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.util.EasternToSystemLocalDateTimeDeserializer;
import gov.healthit.chpl.util.SystemToEasternLocalDateTimeSerializer;
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

    @JsonDeserialize(using = EasternToSystemLocalDateTimeDeserializer.class)
    @JsonSerialize(using = SystemToEasternLocalDateTimeSerializer.class)
    private LocalDateTime submittedDateTime;
}
