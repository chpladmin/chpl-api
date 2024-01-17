package gov.healthit.chpl.scheduler.job.summarystatistics.data;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Listing implements Serializable {
    private static final long serialVersionUID = -3968710463806250210L;

    private Long id;
    private Long acbId;
    private Long developerId;
    private Long productId;
    private Long statusId;
}
