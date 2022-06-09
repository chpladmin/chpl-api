package gov.healthit.chpl.changerequest.search;

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

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChangeRequestSearchResult implements Serializable {
    private static final long serialVersionUID = 216843916174697622L;

    private Long id;
    private IdNamePairSearchResult changeRequestType;
    private IdNamePairSearchResult developer;
    @Singular
    private List<IdNamePairSearchResult> certificationBodies = new ArrayList<IdNamePairSearchResult>();
    private CurrentStatusSearchResult currentStatus;
    private Date submittedDate;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdNamePairSearchResult implements Serializable {
        private static final long serialVersionUID = -237707803683286810L;
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentStatusSearchResult implements Serializable {
        private static final long serialVersionUID = -2377711036832863130L;
        private Long id;
        private String name;
        private Date statusChangeDate;
    }
}
