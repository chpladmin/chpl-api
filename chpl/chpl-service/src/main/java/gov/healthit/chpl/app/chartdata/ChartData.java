package gov.healthit.chpl.app.chartdata;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;

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

            List<CertifiedProductFlatSearchResult> certifiedProducts = getCertifiedProducts(appEnvironment);
            LOGGER.info("Certified Product Count: " + certifiedProducts.size());

            analyzeSed(appEnvironment, certifiedProducts);
            analyzeProducts(appEnvironment, certifiedProducts);
            analyzeDevelopers(appEnvironment, certifiedProducts);
            analyzeActiveListings(appEnvironment, certifiedProducts);

        } catch (Exception e) {
            LOGGER.error("Fatal Error Running ChartData! " + e.getMessage(), e);
        } finally {
            appEnvironment.closeApplicationContext();
        }
    }

    private static void analyzeActiveListings(final ChartDataApplicationEnvironment appEnvironment,
            final List<CertifiedProductFlatSearchResult> listings) {
        ActiveListingsDataFilter activeListingsDataFilter = new ActiveListingsDataFilter();
        List<CertifiedProductFlatSearchResult> filteredListings = activeListingsDataFilter.filterData(listings);
        ActiveListingsStatisticsCalculator activeListingsStatisticsCalculator =
                new ActiveListingsStatisticsCalculator(appEnvironment);
        activeListingsStatisticsCalculator.run(filteredListings);

    }

    private static void analyzeDevelopers(final ChartDataApplicationEnvironment appEnvironment,
            final List<CertifiedProductFlatSearchResult> listings) {
        IncumbentDevelopersStatisticsCalculator incumbentDevelopersStatisticsCalculator =
                new IncumbentDevelopersStatisticsCalculator(appEnvironment);
        incumbentDevelopersStatisticsCalculator.run(listings);

    }

    private static void analyzeProducts(final ChartDataApplicationEnvironment appEnvironment,
            final List<CertifiedProductFlatSearchResult> listings) {
        CriterionProductDataFilter criterionProductDataFilter = new CriterionProductDataFilter();
        List<CertifiedProductFlatSearchResult> filteredListings = criterionProductDataFilter.filterData(listings);
        CriterionProductStatisticsCalculator criterionProductStatisticsCalculator =
                new CriterionProductStatisticsCalculator(appEnvironment);
        criterionProductStatisticsCalculator.run(filteredListings);

    }

    private static void analyzeSed(final ChartDataApplicationEnvironment appEnvironment,
            final List<CertifiedProductFlatSearchResult> listings) {
        // Get Certified Products
        SedDataCollector sedDataCollector = new SedDataCollector(appEnvironment);
        List<CertifiedProductSearchDetails> seds = sedDataCollector.retreiveData(listings);

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

    private static List<CertifiedProductFlatSearchResult> getCertifiedProducts(
            final ChartDataApplicationEnvironment appEnvironment) {
        CertifiedProductSearchDAO certifiedProductSearchDAO = (CertifiedProductSearchDAO) appEnvironment
                .getSpringManagedObject("certifiedProductSearchDAO");
        List<CertifiedProductFlatSearchResult> results = certifiedProductSearchDAO.getAllCertifiedProducts();
        return results;
    }
}
