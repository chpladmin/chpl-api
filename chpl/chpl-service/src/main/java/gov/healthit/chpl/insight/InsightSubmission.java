package gov.healthit.chpl.insight;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsightSubmission implements Serializable {
    private static final long serialVersionUID = -6959841948370813370L;

    private Long productId;
    private String year;
    private String status;
}
