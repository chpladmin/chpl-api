package gov.healthit.chpl.changerequest.search;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.util.EasternToSystemLocalDateTimeDeserializer;
import gov.healthit.chpl.util.SystemToEasternLocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChangeRequestSearchResult implements Serializable {
    private static final long serialVersionUID = 216843916174697622L;

    private Long id;
    private IdNamePair changeRequestType;
    private IdNamePair developer;
    @Singular
    private List<IdNamePair> certificationBodies = new ArrayList<IdNamePair>();
    private CurrentStatusSearchResult currentStatus;
    @JsonDeserialize(using = EasternToSystemLocalDateTimeDeserializer.class)
    @JsonSerialize(using = SystemToEasternLocalDateTimeSerializer.class)
    private LocalDateTime submittedDateTime;

    public Boolean isAttestation() {
        return this.changeRequestType.getName().equalsIgnoreCase(ChangeRequestType.ATTESTATION_TYPE);
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentStatusSearchResult implements Serializable {
        private static final long serialVersionUID = -2377711036832863130L;
        private Long id;
        private String name;
        @JsonDeserialize(using = EasternToSystemLocalDateTimeDeserializer.class)
        @JsonSerialize(using = SystemToEasternLocalDateTimeSerializer.class)
        private LocalDateTime statusChangeDateTime;
    }
}
