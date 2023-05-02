package gov.healthit.chpl.scheduler.job.chartdata;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dto.IncumbentDevelopersStatisticsDTO;
import gov.healthit.chpl.dto.ListingCountStatisticsDTO;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.search.ListingSearchManager;
import gov.healthit.chpl.search.domain.ListingSearchResult;

/**
 * This is the starting point for populating statistics tables that will be used for the charts. As new tables need to
 * be populated, they will be added here.
 *
 * @author TYoung
 *
 */
@DisallowConcurrentExecution
public final class ChartDataCreatorJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("chartDataCreatorJobLogger");

    @Autowired
    private ListingSearchManager listingSearchManager;

    public ChartDataCreatorJob() throws Exception {
        super();
    }

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        LOGGER.info("*****Chart Data Generator is starting now.*****");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        List<ListingSearchResult> listings = listingSearchManager.getAllListings();
        LOGGER.info("Certified Product Count: " + listings.size());

        try {
            analyzeSed(listings);
        } catch (Exception e) {
            LOGGER.error("Problem analyzing sed " + e.getMessage());
        }

        try {
            analyzeProducts(listings);
        } catch (Exception e) {
            LOGGER.error("Problem analyzing products " + e.getMessage());
        }

        try {
            analyzeDevelopers(listings);
        } catch (Exception e) {
            LOGGER.error("Problem analyzing developers " + e.getMessage());
        }

        try {
            analyzeListingCounts(listings);
        } catch (Exception e) {
            LOGGER.error("Problem analyzing listing counts " + e.getMessage());
        }

        try {
            analyzeNonconformity();
        } catch (Exception e) {
            LOGGER.error("Problem analyzing nonconformities " + e.getMessage());
        }
        listings = null;
        LOGGER.info("*****Chart Data Generator is done running.*****");
    }

    private void analyzeDevelopers(List<ListingSearchResult> listings) {
        IncumbentDevelopersStatisticsCalculator incumbentDevelopersStatisticsCalculator = new IncumbentDevelopersStatisticsCalculator();
        List<IncumbentDevelopersStatisticsDTO> dtos = incumbentDevelopersStatisticsCalculator.getCounts(listings);
        incumbentDevelopersStatisticsCalculator.logCounts(dtos);
        incumbentDevelopersStatisticsCalculator.save(dtos);
    }

    private void analyzeListingCounts(List<ListingSearchResult> listings) {
        ListingCountDataFilter listingCountDataFilter = new ListingCountDataFilter();
        List<ListingSearchResult> filteredListings = listingCountDataFilter.filterData(listings);
        ListingCountStatisticsCalculator listingCountStatisticsCalculator = new ListingCountStatisticsCalculator();
        List<ListingCountStatisticsDTO> dtos = listingCountStatisticsCalculator.getCounts(filteredListings);
        listingCountStatisticsCalculator.logCounts(dtos);
        listingCountStatisticsCalculator.save(dtos);
    }

    private void analyzeNonconformity() {
        NonconformityTypeChartCalculator nonconformityStatisticsCalculator = new NonconformityTypeChartCalculator();
        List<NonconformityTypeStatisticsDTO> dtos = nonconformityStatisticsCalculator.getCounts();
        nonconformityStatisticsCalculator.logCounts(dtos);
        nonconformityStatisticsCalculator.saveCounts(dtos);
    }

    private void analyzeProducts(List<ListingSearchResult> listings) throws NumberFormatException, EntityRetrievalException {
        CriterionProductDataFilter criterionProductDataFilter = new CriterionProductDataFilter();
        CriterionProductStatisticsCalculator criterionProductStatisticsCalculator = new CriterionProductStatisticsCalculator();
        List<ListingSearchResult> filteredListings = criterionProductDataFilter.filterData(listings);
        Map<Long, Long> productCounts = criterionProductStatisticsCalculator.getCounts(filteredListings);
        criterionProductStatisticsCalculator.logCounts(productCounts);
        criterionProductStatisticsCalculator.save(productCounts);
    }

    private void analyzeSed(List<ListingSearchResult> listings) {
        // Get Certified Products
        SedDataCollector sedDataCollector = new SedDataCollector();
        List<ListingSearchResult> seds = sedDataCollector.retreiveData(listings);

        LOGGER.info("Collected SED Data");

        // Extract SED Statistics
        SedParticipantsStatisticCountCalculator sedParticipantsStatisticCountCalculator = new SedParticipantsStatisticCountCalculator();
        sedParticipantsStatisticCountCalculator.run(seds);

        ParticipantGenderStatisticsCalculator participantGenderStatisticsCalculator = new ParticipantGenderStatisticsCalculator();
        participantGenderStatisticsCalculator.run(seds);

        ParticipantAgeStatisticsCalculator participantAgeStatisticsCalculator = new ParticipantAgeStatisticsCalculator();
        participantAgeStatisticsCalculator.run(seds);

        ParticipantEducationStatisticsCalculator participantEducationStatisticsCalculator = new ParticipantEducationStatisticsCalculator();
        participantEducationStatisticsCalculator.run(seds);

        ParticipantExperienceStatisticsCalculator participantProfExperienceStatisticsCalculator = new ParticipantExperienceStatisticsCalculator();

        participantProfExperienceStatisticsCalculator.run(seds, ExperienceType.COMPUTER_EXPERIENCE);
        participantProfExperienceStatisticsCalculator.run(seds, ExperienceType.PRODUCT_EXPERIENCE);
        participantProfExperienceStatisticsCalculator.run(seds, ExperienceType.PROFESSIONAL_EXPERIENCE);
    }

}
