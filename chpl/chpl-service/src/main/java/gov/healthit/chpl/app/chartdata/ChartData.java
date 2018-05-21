package gov.healthit.chpl.app.chartdata;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

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

            analyzeSed(appEnvironment);
            analyzeProducts(appEnvironment);

        } catch (Exception e) {
            LOGGER.error("Fatal Error Running ChartData! " + e.getMessage(), e);
        } finally {
            appEnvironment.closeApplicationContext();
        }
    }

    private static void analyzeProducts(final ChartDataApplicationEnvironment appEnvironment) {
        ProductDataCollector productDataCollector = new ProductDataCollector(appEnvironment);
        List<CertifiedProductSearchDetails> products = productDataCollector.retreiveData();
        CriterionProductStatisticsCalculator criterionProductStatisticsCalculator =
                new CriterionProductStatisticsCalculator(appEnvironment);
        criterionProductStatisticsCalculator.run(products);

    }

    private static void analyzeSed(final ChartDataApplicationEnvironment appEnvironment) {
        // Get Certified Products
        SedDataCollector sedDataCollector = new SedDataCollector();
        List<CertifiedProductSearchDetails> seds = sedDataCollector.retreiveData(appEnvironment);

        // Extract SED Statistics
        SedParticipantsStatisticCountCalculator sedParticipantsStatisticCountCalculator =
                new SedParticipantsStatisticCountCalculator();
        sedParticipantsStatisticCountCalculator.run(seds, appEnvironment);

        ParticipantGenderStatisticsCalculator participantGenderStatisticsCalculator =
                new ParticipantGenderStatisticsCalculator();
        participantGenderStatisticsCalculator.run(seds, appEnvironment);

        ParticipantAgeStatisticsCalculator participantAgeStatisticsCalculator =
                new ParticipantAgeStatisticsCalculator();
        participantAgeStatisticsCalculator.run(seds, appEnvironment);

        ParticipantEducationStatisticsCalculator participantEducationStatisticsCalculator =
                new ParticipantEducationStatisticsCalculator();
        participantEducationStatisticsCalculator.run(seds, appEnvironment);

        ParticipantExperienceStatisticsCalculator participantProfExperienceStatisticsCalculator =
                new ParticipantExperienceStatisticsCalculator();

        participantProfExperienceStatisticsCalculator.run(seds, ExperienceType.COMPUTER_EXPERIENCE, appEnvironment);
        participantProfExperienceStatisticsCalculator.run(seds, ExperienceType.PRODUCT_EXPERIENCE, appEnvironment);
        participantProfExperienceStatisticsCalculator.run(seds,
                ExperienceType.PROFESSIONAL_EXPERIENCE, appEnvironment);
    }
}
