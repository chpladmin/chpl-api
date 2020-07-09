package gov.healthit.chpl.scheduler.job.summarystatistics.data;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmailStatistic implements Serializable {
    private static final long serialVersionUID = -7970681451007161298L;

    private Long count;
    private List<EmailCertificationBodyStatistic> acbStatistics;
}
