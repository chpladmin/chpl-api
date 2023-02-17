package gov.healthit.chpl.scheduler.job.onetime.participants;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public interface ParticipantUpdateStrategy {

    boolean updateParticipants(CertifiedProductSearchDetails listing);
}
