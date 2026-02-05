package gov.healthit.chpl.scheduler.job.chartdata;

import java.util.List;
import java.util.Map;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.search.ListingSearchManager;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "chartDataCreatorJobLogger")
@DisallowConcurrentExecution
public final class ChartDataCreatorJob extends QuartzJob {

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
            analyzeProducts(listings);
        } catch (Exception e) {
            LOGGER.error("Problem analyzing products " + e.getMessage(), e);
        }

        listings = null;
        LOGGER.info("*****Chart Data Generator is done running.*****");
    }

    private void analyzeProducts(List<ListingSearchResult> listings) throws NumberFormatException, EntityRetrievalException {
        CriterionProductDataFilter criterionProductDataFilter = new CriterionProductDataFilter();
        CriterionProductStatisticsCalculator criterionProductStatisticsCalculator = new CriterionProductStatisticsCalculator();
        List<ListingSearchResult> filteredListings = criterionProductDataFilter.filterData(listings);
        Map<Long, Long> productCounts = criterionProductStatisticsCalculator.getCounts(filteredListings);
        criterionProductStatisticsCalculator.logCounts(productCounts);
        criterionProductStatisticsCalculator.save(productCounts);
    }
}
