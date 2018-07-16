package gov.healthit.chpl.web.controller.results;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.ParticipantExperienceStatistics;

/**
 * Represents a list of ParticipantExperienceStatistics domain objects.
 * @author TYoung
 *
 */
public class ParticipantExperienceStatisticsResult {
    private List<ParticipantExperienceStatistics> participantExperienceStatistics =
            new ArrayList<ParticipantExperienceStatistics>();

    public List<ParticipantExperienceStatistics> getParticipantExperienceStatistics() {
        return participantExperienceStatistics;
    }

    public void setParticipantExperienceStatistics(
            final List<ParticipantExperienceStatistics> participantExperienceStatistics) {
        this.participantExperienceStatistics = participantExperienceStatistics;
    }
}
