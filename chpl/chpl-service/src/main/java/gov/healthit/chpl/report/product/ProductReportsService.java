package gov.healthit.chpl.report.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.entity.statistics.SummaryStatisticsEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.CertificationBodyStatistic;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import gov.healthit.chpl.scheduler.job.summarystatistics.email.CertificationStatusIdHelper;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.util.CertificationStatusUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ProductReportsService {
    private SummaryStatisticsDAO summaryStatisticsDAO;
    private CertificationStatusIdHelper statusIdHelper;
    private ListingSearchService listingSearchService;
    private ProductManager productManager;
    private CertificationBodyManager certificationBodyManager;

    @Autowired
    public ProductReportsService(SummaryStatisticsDAO summaryStatisticsDAO, CertificationStatusDAO certificationStatusDao, ListingSearchService listingSearchService,
            ProductManager productManager, CertificationBodyManager certificationBodyManager) {
        this.summaryStatisticsDAO = summaryStatisticsDAO;
        this.statusIdHelper = new CertificationStatusIdHelper(certificationStatusDao);
        this.listingSearchService = listingSearchService;
        this.productManager = productManager;
        this.certificationBodyManager = certificationBodyManager;
    }

    public UniqueProductCount getUniqueProductCount() {
        StatisticsSnapshot stats = getStatistics();
        return UniqueProductCount.builder()
                .totalCount(stats.getProductCountForStatuses(statusIdHelper.getNonRetiredStatusIds()))
                .activeCount(stats.getProductCountForStatuses(statusIdHelper.getActiveAndSuspendedStatusIds()))
                .suspendedCount(stats.getProductCountForStatuses(statusIdHelper.getSuspendedStatusIds()))
                .withdrawnCount(stats.getProductCountForStatuses(statusIdHelper.getWithdrawnByDeveloperStatusIds()))
                .build();
    }

    public List<CertificationBodyStatistic> getActiveProductCountsByAcb() {
        StatisticsSnapshot stats = getStatistics();
        return stats.getProductCountForStatusesByAcb(statusIdHelper.getActiveAndSuspendedStatusIds());
    }

    public List<CertificationBodyStatistic> getSuspendedProductCountsByAcb() {
        StatisticsSnapshot stats = getStatistics();
        return stats.getProductCountForStatusesByAcb(statusIdHelper.getSuspendedStatusIds());
    }

    public List<CertificationBodyStatistic> getWithdrawnProductCountsByAcb() {
        StatisticsSnapshot stats = getStatistics();
        return stats.getProductCountForStatusesByAcb(statusIdHelper.getWithdrawnByDeveloperStatusIds());
    }

    public List<ProductByAcb> getActiveProductsAndAcb() {
        return getProdutsAndAcbByStatuses(CertificationStatusUtil.getActiveStatusNames()
                .stream()
                .collect(Collectors.toSet()));
    }

    public List<ProductByAcb> getSuspendedProductsAndAcb() {
        return getProdutsAndAcbByStatuses(CertificationStatusUtil.getSuspendedStatuses()
                .stream()
                .map(status -> status.getName())
                .collect(Collectors.toSet()));
    }

    public List<ProductByAcb> getWithdrawnProductsAndAcb() {
        return getProdutsAndAcbByStatuses(CertificationStatusUtil.getWithdrawnStatuses()
                .stream()
                .map(status -> status.getName())
                .collect(Collectors.toSet()));
    }

    private List<ProductByAcb> getProdutsAndAcbByStatuses(Set<String> statusNames) {
        try {
            List<ListingSearchResult> results = listingSearchService.getAllPagesOfSearchResults(SearchRequest.builder()
                    .certificationStatuses(statusNames)
                    .build());

            Set<ProductByAcb> x =  results.stream()
                    .map(searchResult -> {
                        try {
                            Product product = productManager.getById(searchResult.getProduct().getId());
                            CertificationBody acb = certificationBodyManager.getById(searchResult.getCertificationBody().getId());
                            return ProductByAcb.builder()
                                    .product(product)
                                    .acb(acb)
                                    .build();
                        } catch (EntityRetrievalException e) {
                            LOGGER.error("Could not locate productId: {}", searchResult.getProduct().getId());
                            return null;
                        }
                    })
                    .collect(Collectors.toSet());

            return new ArrayList<ProductByAcb>(x);
        } catch (ValidationException e) {
            LOGGER.error("Error validating SearchRequest: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private StatisticsSnapshot getStatistics() {
        try {
            SummaryStatisticsEntity summaryStatistics = summaryStatisticsDAO.getCurrentSummaryStatistics();
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(summaryStatistics.getSummaryStatistics(), StatisticsSnapshot.class);
        } catch (Exception e) {
            LOGGER.error("Error retrieving summary statistics: {}", e.getMessage());
            return null;
        }
    }
}
