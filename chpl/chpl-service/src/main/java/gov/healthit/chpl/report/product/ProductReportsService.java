package gov.healthit.chpl.report.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.statistics.SummaryStatisticsDAO;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.report.SummaryStatisticsReportBaseService;
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
public class ProductReportsService extends SummaryStatisticsReportBaseService {
    private CertificationStatusIdHelper statusIdHelper;
    private ListingSearchService listingSearchService;

    @Autowired
    public ProductReportsService(SummaryStatisticsDAO summaryStatisticsDAO, CertificationStatusDAO certificationStatusDao, ListingSearchService listingSearchService,
            CertificationBodyManager certificationBodyManager) {
        super(summaryStatisticsDAO, certificationBodyManager);
        this.statusIdHelper = new CertificationStatusIdHelper(certificationStatusDao);
        this.listingSearchService = listingSearchService;
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
        return stats.getProductCountForStatusesByAcb(statusIdHelper.getActiveAndSuspendedStatusIds()).stream()
                .map(stat -> stat.toBuilder()
                        .acbName(getGeneratedAcbName(stat.getAcbId()))
                        .build())
                .toList();
    }

    public List<CertificationBodyStatistic> getSuspendedProductCountsByAcb() {
        StatisticsSnapshot stats = getStatistics();
        return stats.getProductCountForStatusesByAcb(statusIdHelper.getSuspendedStatusIds()).stream()
                .map(stat -> stat.toBuilder()
                        .acbName(getGeneratedAcbName(stat.getAcbId()))
                        .build())
                .toList();
    }

    public List<CertificationBodyStatistic> getWithdrawnProductCountsByAcb() {
        StatisticsSnapshot stats = getStatistics();
        return stats.getProductCountForStatusesByAcb(statusIdHelper.getWithdrawnByDeveloperStatusIds()).stream()
                .map(stat -> stat.toBuilder()
                        .acbName(getGeneratedAcbName(stat.getAcbId()))
                        .build())
                .toList();
    }

    public List<ProductByAcb> getActiveProductsAndAcb() {
        return getProdutsAndAcbByStatuses(CertificationStatusUtil.getActiveStatusNames().stream().collect(Collectors.toSet())).stream()
                .map(result -> result.toBuilder()
                        .acb(updateAcbNameBasedOnRetired(result.getAcb()))
                        .build())
                .toList();
    }

    public List<ProductByAcb> getSuspendedProductsAndAcb() {
        return getProdutsAndAcbByStatuses(CertificationStatusUtil.getSuspendedStatuses().stream().map(status -> status.getName()).collect(Collectors.toSet())).stream()
                .map(result -> result.toBuilder()
                        .acb(updateAcbNameBasedOnRetired(result.getAcb()))
                        .build())
                .toList();
    }

    public List<ProductByAcb> getWithdrawnProductsAndAcb() {
        return getProdutsAndAcbByStatuses(CertificationStatusUtil.getWithdrawnStatuses().stream().map(status -> status.getName()).collect(Collectors.toSet())).stream()
                .map(result -> result.toBuilder()
                        .acb(updateAcbNameBasedOnRetired(result.getAcb()))
                        .build())
                .toList();
    }

    private List<ProductByAcb> getProdutsAndAcbByStatuses(Set<String> statusNames) {
        try {
            List<ListingSearchResult> results = listingSearchService.getAllPagesOfSearchResults(SearchRequest.builder()
                    .certificationStatuses(statusNames)
                    .build());

            Set<ProductByAcb> productsByAcbs =  results.stream()
                    .map(searchResult -> ProductByAcb.builder()
                            .product(searchResult.getProduct())
                            .acb(searchResult.getCertificationBody())
                            .developer(searchResult.getDeveloper())
                            .build())
                    .filter(distinctByKey(pba -> pba.getProduct().getId().toString() + "|" + pba.getAcb().getId().toString()))
                    .collect(Collectors.toSet());

            return new ArrayList<ProductByAcb>(productsByAcbs);
        } catch (ValidationException e) {
            LOGGER.error("Error validating SearchRequest: {}", e.getMessage(), e);
            return List.of();
        }
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
