package gov.healthit.chpl.report.questionableurl;

import java.util.Date;

import gov.healthit.chpl.scheduler.job.urlStatus.data.UrlType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionableUrlDetailReport {
    private UrlType urlType;
    private String url;
    private Date lastChecked;
    private Integer responseCode;
    private String responseMessage;

    /**
     * The CHPL Product Number, developer name, ONC-ACB name, etc depending on the "type" of the url
     */
    private String relatedItem;
}
