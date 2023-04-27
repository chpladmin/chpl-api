package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Component
public class CertificationResultComparator implements Comparator<CertificationResult> {
    private CertificationCriterionService criterionService;

    @Autowired
    public CertificationResultComparator(CertificationCriterionService criterionService) {
        this.criterionService = criterionService;
    }

    @Override
    public int compare(CertificationResult certResult1, CertificationResult certResult2) {
        if (ObjectUtils.anyNull(certResult1.getCriterion(), certResult2.getCriterion())
                || StringUtils.isAnyEmpty(certResult1.getCriterion().getNumber(), certResult2.getCriterion().getNumber())) {
            return 0;
        }
        return criterionService.sortCriteria(certResult1.getCriterion(), certResult2.getCriterion());
    }
}
