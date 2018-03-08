package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.SedParticipantStatisticsCount;

/**
 * Represents a list of SedParticipantStatisticsCount domain objects.
 * @author TYoung
 *
 */
public class SedParticipantStatisticsCountResults implements Serializable {
    private static final long serialVersionUID = -466094988910938560L;
    private List<SedParticipantStatisticsCount> sedParticipantStatisticsCounts =
            new ArrayList<SedParticipantStatisticsCount>();

    public List<SedParticipantStatisticsCount> getSedParticipantStatisticsCounts() {
        return sedParticipantStatisticsCounts;
    }

    public void setSedParticipantStatisticsCounts(
            final List<SedParticipantStatisticsCount> sedParticipantStatisticsCounts) {
        this.sedParticipantStatisticsCounts = sedParticipantStatisticsCounts;
    }
}
