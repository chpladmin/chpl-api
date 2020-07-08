package gov.healthit.chpl.domain.statistics;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmailCertificationBodyStatistic implements Serializable {
    private static final long serialVersionUID = 4312495564762293030L;

    private Long count;
    private String acbName;
    private Long acbId;

}
