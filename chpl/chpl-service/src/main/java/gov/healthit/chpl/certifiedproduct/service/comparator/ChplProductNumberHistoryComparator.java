package gov.healthit.chpl.certifiedproduct.service.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertifiedProductChplProductNumberHistory;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ChplProductNumberHistoryComparator implements Comparator<CertifiedProductChplProductNumberHistory> {

    @Override
    public int compare(CertifiedProductChplProductNumberHistory history1,
            CertifiedProductChplProductNumberHistory history2) {
        if (history1.getEndDateTime() != null && history2.getEndDateTime() != null) {
            return history1.getEndDateTime().compareTo(history2.getEndDateTime());
        } else if (!StringUtils.isEmpty(history1.getChplProductNumber()) && !StringUtils.isEmpty(history2.getChplProductNumber())) {
            return history1.getChplProductNumber().compareTo(history2.getChplProductNumber());
        } else if (history1.getId() != null && history2.getId() != null) {
            return history1.getId().compareTo(history2.getId());
        }
        return 0;
    }
}
