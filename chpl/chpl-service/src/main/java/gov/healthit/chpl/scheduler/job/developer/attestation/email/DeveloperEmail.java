package gov.healthit.chpl.scheduler.job.developer.attestation.email;

import java.io.File;
import java.util.List;

import gov.healthit.chpl.domain.Developer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperEmail {
    private Developer developer;
    private List<String> recipients;
    private String subject;
    private List<File> files;
    private String message;
}
