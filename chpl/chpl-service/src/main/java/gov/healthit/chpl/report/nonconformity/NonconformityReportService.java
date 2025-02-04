package gov.healthit.chpl.report.nonconformity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CriterionStatus;
import gov.healthit.chpl.domain.NonconformityType;

@Component
public class NonconformityReportService {

    private NonconformityReportDao nonconformityReportDao;

    @Autowired
    public NonconformityReportService(NonconformityReportDao nonconformityReportDao) {
        this.nonconformityReportDao = nonconformityReportDao;
    }

    public List<NonconformityTypeCount> getNonconformityCounts() {
        return nonconformityReportDao.getNonconformityCounts().stream()
                .filter(count -> !isNonconformityTypeRetired(count.getNonconformityType()))
                .toList();
    }

    private Boolean isNonconformityTypeRetired(NonconformityType nonconformityType) {
        return nonconformityType.getStatus() == CriterionStatus.RETIRED;
    }
}
