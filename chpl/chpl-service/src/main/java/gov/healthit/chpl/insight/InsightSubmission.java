package gov.healthit.chpl.insight;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonAlias;

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

    @JsonAlias({"insight_year"})
    private String year;

    @JsonAlias({"submission_status"})
    private String status;
}
