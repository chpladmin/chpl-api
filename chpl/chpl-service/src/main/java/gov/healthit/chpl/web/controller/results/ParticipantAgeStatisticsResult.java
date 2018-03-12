package gov.healthit.chpl.web.controller.results;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.ParticipantAgeStatistics;

public class ParticipantAgeStatisticsResult {
    private List<ParticipantAgeStatistics> participantAgeStatistics = new ArrayList<ParticipantAgeStatistics>();

    public List<ParticipantAgeStatistics> getParticipantAgeStatistics() {
        return participantAgeStatistics;
    }

    public void setParticipantAgeStatistics(List<ParticipantAgeStatistics> participantAgeStatistics) {
        this.participantAgeStatistics = participantAgeStatistics;
    }
            
}
