package gov.healthit.chpl.web.controller.results;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.ParticipantAgeStatistics;

/**
 * Represents a list of ParticipantAgeStatistics domain objects.
 * @author TYoung
 *
 */
public class ParticipantAgeStatisticsResult {
    private List<ParticipantAgeStatistics> participantAgeStatistics = new ArrayList<ParticipantAgeStatistics>();

    public List<ParticipantAgeStatistics> getParticipantAgeStatistics() {
        return participantAgeStatistics;
    }

    public void setParticipantAgeStatistics(final List<ParticipantAgeStatistics> participantAgeStatistics) {
        this.participantAgeStatistics = participantAgeStatistics;
    }

}
