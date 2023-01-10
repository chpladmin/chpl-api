package gov.healthit.chpl.scheduler.job.developer.attestation.email;

import gov.healthit.chpl.domain.Developer;

public interface DeveloperEmailGenerator {
    DeveloperEmail getDeveloperEmail(Developer developer);
}
