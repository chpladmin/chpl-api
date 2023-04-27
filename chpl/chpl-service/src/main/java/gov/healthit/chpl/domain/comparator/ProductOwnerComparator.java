package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import gov.healthit.chpl.domain.ProductOwner;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProductOwnerComparator implements Comparator<ProductOwner> {
    @Override
    public int compare(ProductOwner o1, ProductOwner o2) {
        if (o1.getTransferDay() == null || o2.getTransferDay() == null) {
            return 0;
        }
        return o1.getTransferDay().compareTo(o2.getTransferDay());
    }
}
