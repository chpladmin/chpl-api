package gov.healthit.chpl.scheduler.job.summarystatistics.data;

import java.io.Serializable;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AttestedCriterionStatistic implements Serializable {
    private static final long serialVersionUID = 4312410254762293030L;

    private Long certificationCriterionId;
    private Set<Long> listingIds;
    private Long listingStatusId;
    private String acbName;
    private Long acbId;
}
