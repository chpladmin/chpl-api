package gov.healthit.chpl.scheduler.job.developer.messaging;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.auth.User;

public interface DeveloperEmailGenerator {
    DeveloperEmail getDeveloperEmail(Developer developer, User submittedUser);
}
