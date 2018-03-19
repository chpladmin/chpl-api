package gov.healthit.chpl.web.controller.results;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.ParticipantEducationStatistics;

/**
 * Represents a list of ParticipantEducationStatistics domain objects.
 * @author TYoung
 *
 */
public class ParticipantEducationStatisticsResult {
    private List<ParticipantEducationStatistics> participantEducationStatistics =
            new ArrayList<ParticipantEducationStatistics>();

    public List<ParticipantEducationStatistics> getParticipantEducationStatistics() {
        return participantEducationStatistics;
    }

    public void setParticipantEducationStatistics(
            final List<ParticipantEducationStatistics> participantEducationStatistics) {
        this.participantEducationStatistics = participantEducationStatistics;
    }

}
