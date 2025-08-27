package gov.healthit.chpl.report.questionableurl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionableUrlReport {
    private String urlType;
    private Long count;
}
