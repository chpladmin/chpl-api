package gov.healthit.chpl.manager.rules.product;

import java.util.List;

import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductValidationContext {
    private ProductDAO productDao;
    private Product product;
    private Long developerId;
    private boolean isOwnerJoiningAnotherDeveloper;
    private List<Long> productsBeingMerged;
    private ErrorMessageUtil errorMessageUtil;
}
