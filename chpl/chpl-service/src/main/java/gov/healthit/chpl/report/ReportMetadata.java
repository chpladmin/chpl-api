package gov.healthit.chpl.report;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportMetadata {
    private Long id;
    private String environment;
    private String title;
    private String reportKey;
    private String reportGroup;
    private String url;
    private String height;
    private String displayOrder;
}
