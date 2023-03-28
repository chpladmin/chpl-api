package gov.healthit.chpl.util;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Component
public class CertificationCriterionComparator implements Comparator<CertificationCriterion> {
    private CertificationCriterionService criterionService;
    private boolean descending = false;

    @Autowired
    public CertificationCriterionComparator(CertificationCriterionService criterionService) {
        this.criterionService = criterionService;
    }

    @Override
    public int compare(CertificationCriterion criterion1, CertificationCriterion criterion2) {
        if (ObjectUtils.anyNull(criterion1, criterion2)) {
            return 0;
        }
        int sortFactor = descending ? -1 : 1;
        return (criterionService.sortCriteria(criterion1, criterion2)) * sortFactor;
    }
}
