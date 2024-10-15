package gov.healthit.chpl.report.product;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Product;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductByAcb {
    private Product product;
    private CertificationBody acb;
}
