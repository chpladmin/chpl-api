package gov.healthit.chpl.app.chartdata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is the starting point for populating statistics tables that will be used for the
 * charts.  As new tables need to be populated, they will be added here.
 * @author TYoung
 *
 */
public final class ChartData {
    private static final Logger LOGGER = LogManager.getLogger(ChartData.class);

    private ChartData() {
        //Default private constructor
    }

    /**
     * Entry point for populating statistics tables.
     * @param args there are no arguments for this method
     */
    public static void main(final String[] args) {
        ChartDataApplicationEnvironment appEnvironment = null;
        try {
            appEnvironment = new ChartDataApplicationEnvironment();
            SedParticipantsStatisticCount sedParticipantsStatisticCount = new SedParticipantsStatisticCount();
            sedParticipantsStatisticCount.run(appEnvironment);
        } catch (Exception e) {
            LOGGER.error("Fatal Error Running ChartData! " + e.getMessage(), e);
        } finally {
            appEnvironment.closeApplicationContext();
        }
    }
}
