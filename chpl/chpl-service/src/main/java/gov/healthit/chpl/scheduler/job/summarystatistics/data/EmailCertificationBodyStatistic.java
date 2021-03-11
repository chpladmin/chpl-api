package gov.healthit.chpl.scheduler.job.summarystatistics.data;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class EmailCertificationBodyStatistic implements Serializable {
    private static final long serialVersionUID = 4312495564762293030L;

    private Long count;
    private String acbName;
    private Long acbId;
}
