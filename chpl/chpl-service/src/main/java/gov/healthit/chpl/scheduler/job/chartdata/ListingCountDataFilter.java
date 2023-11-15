package gov.healthit.chpl.scheduler.job.chartdata;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.search.domain.ListingSearchResult;

/**
 * Filters Listings to those needed for Listing Count chart.
 *
 * @author alarned
 *
 */
public class ListingCountDataFilter {
    private static final Logger LOGGER = LogManager.getLogger("chartDataCreatorJobLogger");
    private static final String BAD_STATUS = "Retired";

    public ListingCountDataFilter() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    public List<ListingSearchResult> filterData(List<ListingSearchResult> certifiedProducts) {
        List<ListingSearchResult> results = new ArrayList<ListingSearchResult>();
        for (ListingSearchResult result : certifiedProducts) {
            if ((result.getEdition() == null
                    || result.getEdition().getName().equalsIgnoreCase(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear()))
                    && !result.getCertificationStatus().getName().equalsIgnoreCase(BAD_STATUS)) {
                results.add(result);
            }
        }
        LOGGER.info("Number of filtered Listings: " + results.size());
        return results;
    }
}
