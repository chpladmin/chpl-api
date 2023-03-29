package gov.healthit.chpl.certifiedproduct.service.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.ListingMeasure;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ListingMeasureComparator implements Comparator<ListingMeasure> {

    @Override
    public int compare(ListingMeasure measure1, ListingMeasure measure2) {
        if (ObjectUtils.anyNull(measure1.getMeasure(), measure2.getMeasure())
                || StringUtils.isAnyEmpty(measure1.getMeasure().getAbbreviation(),
                        measure2.getMeasure().getAbbreviation())) {
            return 0;
        }
        return measure1.getMeasure().getAbbreviation().compareTo(measure2.getMeasure().getAbbreviation());
    }
}
