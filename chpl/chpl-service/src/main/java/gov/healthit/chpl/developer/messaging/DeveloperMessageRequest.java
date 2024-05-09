package gov.healthit.chpl.developer.messaging;

import gov.healthit.chpl.developer.search.DeveloperSearchRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeveloperMessageRequest {
    private DeveloperSearchRequest query;
    private String subject;
    private String body;
}
