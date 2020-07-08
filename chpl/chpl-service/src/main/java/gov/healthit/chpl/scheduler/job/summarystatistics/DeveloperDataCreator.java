package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.statistics.EmailCertificationBodyStatistic;
import gov.healthit.chpl.domain.statistics.EmailStatistic;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertificationStatusType;

@Component
public class DeveloperDataCreator extends StatisticsDataCreator{
    private static final Logger LOGGER = LogManager.getLogger("summaryStatisticsCreatorJobLogger");

    public EmailStatistic getUniqueDeveloperCount(List<CertifiedProductDetailsDTO> certifiedProducts,
            EditionCriteria listingsToInclude, List<CertificationStatusType> statuses) {
        EmailStatistic stat = new EmailStatistic();
        stat.setCount(getUniqueDeveloperCountTotal(certifiedProducts, listingsToInclude, statuses));
        stat.setAcbStatistics(getUniqueDeveloperCountTotalsByAcb(certifiedProducts, listingsToInclude, statuses));
        return stat;
    }

    public List<EmailCertificationBodyStatistic> getUniqueDeveloperCountTotalsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts,
            EditionCriteria listingsToInclude, List<CertificationStatusType> statuses) {

        return certifiedProducts.stream()
                .filter(cp -> includeListingBasedOnEdition(cp, listingsToInclude)
                        && includeListingBasedOnStatus(cp, statuses))
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    EmailCertificationBodyStatistic stat = new EmailCertificationBodyStatistic();
                    stat.setAcbName(entry.getKey());
                    stat.setCount(entry.getValue().stream()
                            .filter(distinctByKey(cp -> cp.getDeveloper().getId()))
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
    }

    public Long getUniqueDeveloperCountTotal(
            List<CertifiedProductDetailsDTO> certifiedProducts,
            EditionCriteria listingsToInclude, List<CertificationStatusType> statuses) {

        return certifiedProducts.stream()
                .filter(cp -> includeListingBasedOnEdition(cp, listingsToInclude)
                        && includeListingBasedOnStatus(cp, statuses))
                .filter(distinctByKey(cp -> cp.getDeveloper().getId()))
                .collect(Collectors.counting());

    }
}
