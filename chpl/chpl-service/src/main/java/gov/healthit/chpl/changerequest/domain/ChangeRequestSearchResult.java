package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChangeRequestSearchResult implements Serializable {
    private static final long serialVersionUID = 216843916174697622L;

    private Long id;
    private ChangeRequestType changeRequestType;
    private IdNamePairSearchResult developer;
    @Singular
    private List<IdNamePairSearchResult> certificationBodies = new ArrayList<IdNamePairSearchResult>();
    private ChangeRequestStatus currentStatus;
    private Date submittedDate;

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdNamePairSearchResult implements Serializable {
        private static final long serialVersionUID = -2377078036832863130L;
        private Long id;
        private String name;
    }
}
