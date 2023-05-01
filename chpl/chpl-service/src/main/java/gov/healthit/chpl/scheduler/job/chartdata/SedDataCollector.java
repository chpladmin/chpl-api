package gov.healthit.chpl.scheduler.job.chartdata;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "chartDataCreatorJobLogger")
public class SedDataCollector {
    private static final String CRITERION_G_3 = "170.315 (g)(3)";

    @Autowired
    private CertificationCriterionDAO criteriaDao;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    List<Long> g3CriteriaIds;

    public SedDataCollector() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        g3CriteriaIds = criteriaDao.getAllByNumber(CRITERION_G_3).stream()
                .map(g3 -> g3.getId())
                .toList();
    }

    public List<CertifiedProductSearchDetails> retreiveData(List<ListingSearchResult> listings) {

        List<ListingSearchResult> certifiedProducts = filterData(listings);
        LOGGER.info("2015/SED Certified Product Count: " + certifiedProducts.size());

        List<CertifiedProductSearchDetails> certifiedProductsWithDetails = getCertifiedProductDetailsForAll(certifiedProducts);
        return certifiedProductsWithDetails;
    }

    private List<ListingSearchResult> filterData(List<ListingSearchResult> certifiedProducts) {
        return certifiedProducts.stream()
                .filter(listing -> listing.getEdition().getName().equalsIgnoreCase(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear())
                        && hasMetAtLeastOneG3Crteria(listing))
                .toList();
    }

    private boolean hasMetAtLeastOneG3Crteria(ListingSearchResult listing) {
        return listing.getCriteriaMet().stream()
                .map(metCriteria -> metCriteria.getId())
                .filter(i -> g3CriteriaIds.contains(i))
                .findAny()
                .isPresent();
    }

    private List<CertifiedProductSearchDetails> getCertifiedProductDetailsForAll(List<ListingSearchResult> certifiedProducts) {
        return certifiedProducts.stream()
                .map(cp -> {
                    try {
                        return certifiedProductDetailsManager.getCertifiedProductDetails(cp.getId());
                    } catch (EntityRetrievalException e) {
                        LOGGER.error("Could not retrieve listing detail for listing: {}", cp.getId(), e);
                        LOGGER.error("SED Chart statistics may not be correct");
                        return null;
                    }
                })
                .toList();
    }
}
