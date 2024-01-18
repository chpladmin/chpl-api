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
public class CertificationBodyStatusStatistic implements Serializable {
    private static final long serialVersionUID = 4312491254762293030L;

    private Set<Long> ids;
    private Long statusId;
    private String acbName;
    private Long acbId;
}
