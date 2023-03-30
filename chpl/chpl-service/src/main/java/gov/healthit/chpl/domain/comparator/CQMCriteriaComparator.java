package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Component
public class CQMCriteriaComparator implements Comparator<CQMResultCertification> {
    private CertificationCriterionService criterionService;

    @Autowired
    public CQMCriteriaComparator(CertificationCriterionService criterionService) {
        this.criterionService = criterionService;
    }

    @Override
    public int compare(CQMResultCertification cqmCriterion1, CQMResultCertification cqmCriterion2) {
        if (ObjectUtils.anyNull(cqmCriterion1.getCriterion(), cqmCriterion2.getCriterion())
                || StringUtils.isAnyEmpty(cqmCriterion1.getCriterion().getNumber(), cqmCriterion2.getCriterion().getNumber())) {
            return 0;
        }
        return criterionService.sortCriteria(cqmCriterion1.getCriterion(), cqmCriterion2.getCriterion());
    }
}
