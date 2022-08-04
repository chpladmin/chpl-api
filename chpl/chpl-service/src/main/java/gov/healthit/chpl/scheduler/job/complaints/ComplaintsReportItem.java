package gov.healthit.chpl.scheduler.job.complaints;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintsReportItem {
    private Complaint complaint;

    @Builder.Default
    private Set<Surveillance> relatedSurveillance = new LinkedHashSet<Surveillance>();
}
