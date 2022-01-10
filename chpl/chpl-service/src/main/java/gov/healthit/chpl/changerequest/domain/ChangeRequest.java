package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
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
    private Date submittedDate;

}
