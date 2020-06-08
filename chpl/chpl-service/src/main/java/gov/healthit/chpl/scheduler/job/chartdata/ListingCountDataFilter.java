package gov.healthit.chpl.scheduler.job.chartdata;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;

/**
 * Filters Listings to those needed for Listing Count chart.
 *
 * @author alarned
 *
 */
public class ListingCountDataFilter {
    private static final Logger LOGGER = LogManager.getLogger("chartDataCreatorJobLogger");
    private static final String EDITION_2014 = "2014";
    private static final String EDITION_2015 = "2015";
    private static final String BAD_STATUS = "Retired";

    @Autowired
    private FF4j ff4j;

    public ListingCountDataFilter() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    public List<CertifiedProductFlatSearchResult> filterData(
            final List<CertifiedProductFlatSearchResult> certifiedProducts) {
        List<CertifiedProductFlatSearchResult> results = new ArrayList<CertifiedProductFlatSearchResult>();
        for (CertifiedProductFlatSearchResult result : certifiedProducts) {
            if (((result.getEdition().equalsIgnoreCase(EDITION_2014)
                    && !ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_THREE_MONTHS))
                    || result.getEdition().equalsIgnoreCase(EDITION_2015))
                    && !result.getCertificationStatus().equalsIgnoreCase(BAD_STATUS)) {
                results.add(result);
            }
        }
        LOGGER.info("Number of filtered Listings: " + results.size());
        return results;
    }
}
