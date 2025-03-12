package gov.healthit.chpl.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.ProductActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.util.ChplUserToCognitoUserUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("productActivityMetadataBuilder")
public class ProductActivityMetadataBuilder extends ActivityMetadataBuilder {

    private ProductDAO productDao;

    @Autowired
    public ProductActivityMetadataBuilder(ChplUserToCognitoUserUtil chplUserToCognitoUserUtil,
            ProductDAO productDao) {
        super(chplUserToCognitoUserUtil);
        this.productDao = productDao;
    }

    @Override
    protected void addConceptSpecificMetadata(ActivityDTO activity, ActivityMetadata metadata) {
        if (!(metadata instanceof ProductActivityMetadata)) {
            return;
        }
        ProductActivityMetadata productMetadata = (ProductActivityMetadata) metadata;
        productMetadata.getCategories().add(ActivityCategory.PRODUCT);

        if (metadata.getObject() != null && metadata.getObject().getId() != null) {
            Product product = null;
            try {
                product = productDao.getById(metadata.getObject().getId(), true);
                metadata.getObject().setName(product.getName());
            } catch (Exception ex) {
                LOGGER.error("Could not find product " + metadata.getObject().getId() + " for activity metadata.", ex);
            }
        }
    }
}
