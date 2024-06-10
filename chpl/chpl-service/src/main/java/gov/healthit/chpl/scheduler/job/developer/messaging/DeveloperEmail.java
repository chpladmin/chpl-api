package gov.healthit.chpl.scheduler.job.developer.messaging;

import java.io.File;
import java.util.List;

import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperEmail {
    private DeveloperSearchResult developer;
    private List<String> recipients;
    private String subject;
    private List<File> files;
    private String message;
}
