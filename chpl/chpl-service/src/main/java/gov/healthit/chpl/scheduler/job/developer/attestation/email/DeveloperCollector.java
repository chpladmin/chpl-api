package gov.healthit.chpl.scheduler.job.developer.attestation.email;

import java.util.List;

import gov.healthit.chpl.domain.Developer;

public interface DeveloperCollector {
    List<Developer> getDevelopers();
}
