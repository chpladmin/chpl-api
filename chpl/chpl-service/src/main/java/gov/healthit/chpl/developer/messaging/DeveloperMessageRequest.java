package gov.healthit.chpl.developer.messaging;

import java.io.Serializable;

import gov.healthit.chpl.developer.search.DeveloperSearchRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeveloperMessageRequest implements Serializable {
    private static final long serialVersionUID = -1504729742772709807L;
    private DeveloperSearchRequest query;
    private String subject;
    private String body;
}
