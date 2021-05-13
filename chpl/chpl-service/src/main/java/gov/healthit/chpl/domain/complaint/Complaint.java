package gov.healthit.chpl.domain.complaint;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.ComplaintSurveillanceMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Complaint implements Serializable {
    private static final long serialVersionUID = -7018474294841580851L;

    public static final String COMPLAINT_OPEN = "Open";
    public static final String COMPLAINT_CLOSED = "Closed";
    public static final int NUMBER_OF_STATES = 2;

    private Long id;
    private CertificationBody certificationBody;
    private ComplainantType complainantType;
    private String complainantTypeOther;
    private String oncComplaintId;
    private String acbComplaintId;
    private Date receivedDate;
    private String summary;
    private String actions;
    private boolean complainantContacted;
    private boolean developerContacted;
    private boolean oncAtlContacted;
    private boolean flagForOncReview;
    private Date closedDate;
    @Builder.Default
    private Set<ComplaintListingMap> listings = new HashSet<ComplaintListingMap>();
    @Builder.Default
    private Set<ComplaintCriterionMap> criteria = new HashSet<ComplaintCriterionMap>();
    @Builder.Default
    private Set<ComplaintSurveillanceMap> surveillances = new HashSet<ComplaintSurveillanceMap>();

    @Override
    public boolean equals(Object another) {
        if (another == null) {
            return false;
        }
        if (!(another instanceof Complaint)) {
            return false;
        }
        Complaint anotherComplaint = (Complaint) another;
        if (this.getId() != null && anotherComplaint.getId() == null
                || this.getId() == null && anotherComplaint.getId() != null) {
            return false;
        }
        return this.getId().equals(anotherComplaint.getId());
    }

    @Override
    public int hashCode() {
        if (this.getId() == null) {
            return -1;
        }
        return this.getId().hashCode();
    }
}
