package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.domain.statistics.EmailCertificationBodyStatistic;
import gov.healthit.chpl.domain.statistics.EmailStatistic;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertificationStatusType;

@Component
public class ListingDataCreator extends StatisticsDataCreator {
    private static final String ONC_TEST_METHOD = "ONC Test Method";

    private CertificationResultDAO certificationResultsDAO;

    @Autowired
    public ListingDataCreator(CertificationResultDAO certificationResultsDAO) {
        this.certificationResultsDAO = certificationResultsDAO;
    }

    public EmailStatistic getUniqueListingCount(List<CertifiedProductDetailsDTO> certifiedProducts,
            EditionCriteria listingsToInclude, List<CertificationStatusType> statuses,
            boolean onlyIncludeAlternativeTestMethods) {

        EmailStatistic stat = new EmailStatistic();
        stat.setCount(getUniqueListingCountTotal(
                certifiedProducts, listingsToInclude, statuses, onlyIncludeAlternativeTestMethods));
        stat.setAcbStatistics(getUniqueListingCountTotalsByAcb(
                certifiedProducts, listingsToInclude, statuses, onlyIncludeAlternativeTestMethods));
        return stat;
    }

    public List<EmailCertificationBodyStatistic> getUniqueListingCountTotalsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts,
            EditionCriteria listingsToInclude, List<CertificationStatusType> statuses,
            boolean onlyIncludeAlternativeTestMethods) {

        return certifiedProducts.stream()
                .filter(cp -> includeListingBasedOnEdition(cp, listingsToInclude)
                        && includeListingBasedOnStatus(cp, statuses)
                        && (onlyIncludeAlternativeTestMethods ? doesListingHaveAlternativeTestMethod(cp.getId()) : true))
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    EmailCertificationBodyStatistic stat = new EmailCertificationBodyStatistic();
                    stat.setAcbName(entry.getKey());
                    stat.setCount(entry.getValue().stream()
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
    }

    public Long getUniqueListingCountTotal(
            List<CertifiedProductDetailsDTO> certifiedProducts,
            EditionCriteria listingsToInclude, List<CertificationStatusType> statuses,
            boolean onlyIncludeAlternativeTestMethods) {

        return certifiedProducts.stream()
                .filter(cp -> includeListingBasedOnEdition(cp, listingsToInclude)
                        && includeListingBasedOnStatus(cp, statuses)
                        && (onlyIncludeAlternativeTestMethods ? doesListingHaveAlternativeTestMethod(cp.getId()) : true))
                .collect(Collectors.counting());
    }

    private boolean doesListingHaveAlternativeTestMethod(Long listingId) {
        return certificationResultsDAO.getTestProceduresForListing(listingId).stream()
                .filter(crtp -> !crtp.getTestProcedure().getName().equals(ONC_TEST_METHOD))
                .findAny()
                .isPresent();
    }


}
