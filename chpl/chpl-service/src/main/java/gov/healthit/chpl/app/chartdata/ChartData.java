package gov.healthit.chpl.app.chartdata;

import java.util.List;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

/**
 * This is the starting point for populating statistics tables that will be used for the
 * charts.  As new tables need to be populated, they will be added here.
 * @author TYoung
 *
 */
public final class ChartData {

    private ChartData() {
        //Default private constructor
    }

    /**
     * Entry point for populating statistics tables.
     * @param args there are no arguments for this method
     */
    public static void main(final String[] args) {
        ChartDataApplicationEnvironment appEnvironment;
        try {
            appEnvironment = new ChartDataApplicationEnvironment();
            
            SedDataCollector sedDataCollector = new SedDataCollector();
            
            List<CertifiedProductSearchDetails> seds = sedDataCollector.retreiveData(appEnvironment);
            
            SedParticipantsStatisticCountCalculator sedParticipantsStatisticCountCalculator = 
                    new SedParticipantsStatisticCountCalculator();
            sedParticipantsStatisticCountCalculator.run(seds, appEnvironment);
            
            ParticipantGenderStatisticsCalculator participantGenderStatisticsCalculator = 
                    new ParticipantGenderStatisticsCalculator();
            participantGenderStatisticsCalculator.run(seds, appEnvironment);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
