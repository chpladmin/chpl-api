package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceBasedUrl {
    private String url;
    private Long developerId;
    private List<String> chplProductNumbers;
}
