package gov.healthit.chpl.scheduler.job.chartdata;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;

/**
 * Filters Listings to those needed for Criterion Product chart.
 * 
 * @author alarned
 *
 */
public class CriterionProductDataFilter {
    private static final Logger LOGGER = LogManager.getLogger("chartDataCreatorJobLogger");
    private static final String EDITION_2014 = "2014";
    private static final String EDITION_2015 = "2015";

    /**
     * Filter listings down to those needed for charts.
     * 
     * @param certifiedProducts
     *            initial set of listings
     * @return filtered list (2014 & 2015 edition only)
     */
    public List<CertifiedProductFlatSearchResult> filterData(
            final List<CertifiedProductFlatSearchResult> certifiedProducts) {
        List<CertifiedProductFlatSearchResult> results = new ArrayList<CertifiedProductFlatSearchResult>();
        for (CertifiedProductFlatSearchResult result : certifiedProducts) {
            if (result.getEdition().equalsIgnoreCase(EDITION_2014)
                    || result.getEdition().equalsIgnoreCase(EDITION_2015)) {
                results.add(result);
            }
        }
        LOGGER.info("Number of filtered Listings: " + results.size());
        return results;
    }
}
