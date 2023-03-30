package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CQMResultDetails;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CQMResultComparator implements Comparator<CQMResultDetails> {

    @Override
    public int compare(CQMResultDetails cqm1, CQMResultDetails cqm2) {
        if (!StringUtils.isEmpty(cqm1.getCmsId()) && !StringUtils.isEmpty(cqm2.getCmsId())) {
            return cqm1.getCmsId().compareTo(cqm2.getCmsId());
        } else if (!StringUtils.isEmpty(cqm1.getNqfNumber()) && !StringUtils.isEmpty(cqm2.getNqfNumber())) {
            return cqm1.getNqfNumber().compareTo(cqm2.getNqfNumber());
        }
        return 0;
    }
}
