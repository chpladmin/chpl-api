package gov.healthit.chpl.scheduler.job.chartdata;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.search.domain.CertifiedProductBasicSearchResult;

/**
 * Filters Listings to those needed for Listing Count chart.
 *
 * @author alarned
 *
 */
public class ListingCountDataFilter {
    private static final Logger LOGGER = LogManager.getLogger("chartDataCreatorJobLogger");
    private static final String EDITION_2015 = "2015";
    private static final String BAD_STATUS = "Retired";

    public ListingCountDataFilter() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    public List<CertifiedProductBasicSearchResult> filterData(List<CertifiedProductBasicSearchResult> certifiedProducts) {
        List<CertifiedProductBasicSearchResult> results = new ArrayList<CertifiedProductBasicSearchResult>();
        for (CertifiedProductBasicSearchResult result : certifiedProducts) {
            if (result.getEdition().equalsIgnoreCase(EDITION_2015)
                    && !result.getCertificationStatus().equalsIgnoreCase(BAD_STATUS)) {
                results.add(result);
            }
        }
        LOGGER.info("Number of filtered Listings: " + results.size());
        return results;
    }
}
