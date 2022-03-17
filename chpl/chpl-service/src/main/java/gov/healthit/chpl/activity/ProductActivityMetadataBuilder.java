package gov.healthit.chpl.activity;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.ProductActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("productActivityMetadataBuilder")
public class ProductActivityMetadataBuilder extends ActivityMetadataBuilder {
    private ObjectMapper jsonMapper;
    private DeveloperDAO developerDao;

    @Autowired
    public ProductActivityMetadataBuilder(DeveloperDAO developerDao) {
        super();
        jsonMapper = new ObjectMapper();
        this.developerDao = developerDao;
    }

    protected void addConceptSpecificMetadata(ActivityDTO activity, ActivityMetadata metadata) {
        if (!(metadata instanceof ProductActivityMetadata)) {
            return;
        }
        ProductActivityMetadata productMetadata = (ProductActivityMetadata) metadata;

        //parse product specific metadata
        //for merges, original data is a list of products
        //for splits, new data is a list of products
        //otherwise we expect orig/new data to be a single product
        Product origProduct = null;
        List<Product> origProducts = null;
        if (activity.getOriginalData() != null) {
            try {
                origProduct =
                    jsonMapper.readValue(activity.getOriginalData(), Product.class);
            } catch (Exception ignore) { }

            if (origProduct == null) {
                try {
                    origProducts = jsonMapper.readValue(activity.getOriginalData(),
                            jsonMapper.getTypeFactory().constructCollectionType(List.class, Product.class));
                } catch (Exception ignore) { }
            }

            if (origProduct == null && origProducts == null) {
                LOGGER.error("Could not parse activity ID " + activity.getId() + " original data "
                        + " as ProductDTO or List<ProductDTO>. JSON was: " + activity.getOriginalData());
            }
        }

        Product newProduct = null;
        List<Product> newProducts = null;
        if (activity.getNewData() != null) {
            try {
                newProduct =
                    jsonMapper.readValue(activity.getNewData(), Product.class);
            } catch (Exception ignore) { }

            if (newProduct == null) {
                try {
                    newProducts = jsonMapper.readValue(activity.getNewData(),
                            jsonMapper.getTypeFactory().constructCollectionType(List.class, Product.class));
                } catch (Exception ignore) { }
            }

            if (newProduct == null && newProducts == null) {
                LOGGER.error("Could not parse activity ID " + activity.getId() + " new data "
                    + "as ProductDTO or List<ProductDTO>. JSON was: " + activity.getNewData());
            }
        }

        if (newProduct != null && origProduct != null
                && newProducts == null && origProducts == null) {
            //if there is a single new product and single original product
            //that means the activity was editing the product
            parseProductMetadata(productMetadata, newProduct);
        } else if (origProduct != null && newProduct == null
                && newProducts == null && origProducts == null) {
            //if there is an original product but no new product
            //then the product was deleted - pull its info from the orig object
            parseProductMetadata(productMetadata, origProduct);
        } else if (newProduct != null && origProduct == null
                && newProducts == null && origProducts == null) {
            //if there is a new product but no original product
            //then the product was just created
            parseProductMetadata(productMetadata, newProduct);
        } else if (newProducts != null && origProduct != null
                && newProduct == null && origProducts == null) {
            //multiple new products and a single original product
            //means the activity was a split
            parseProductMetadata(productMetadata, activity, newProducts);
        } else if (origProducts != null && newProduct != null
                && origProduct == null && newProducts == null) {
            //multiple original products and a single new product
            //means the activity was a merge
            parseProductMetadata(productMetadata, newProduct);
        }

        productMetadata.getCategories().add(ActivityCategory.PRODUCT);
    }

    private void parseProductMetadata(
            ProductActivityMetadata productMetadata, Product product) {
        //Developer id is always filled in the activity object
        //but the name does not seem to be. If the name is available
        //use it but if not look up the developer by ID
        if (product.getOwner() != null && !StringUtils.isEmpty(product.getOwner().getName())) {
            productMetadata.setDeveloperName(product.getOwner().getName());
        } else if (product.getOwner() != null && product.getOwner().getDeveloperId() != null) {
            try {
                Developer developer = developerDao.getSimpleDeveloperById(product.getOwner().getDeveloperId(), true);
                productMetadata.setDeveloperName(developer.getName());
            } catch (Exception ex) {
                LOGGER.error("Unable to find developer with ID " + product.getDeveloperId() + " referenced "
                        + "in activity for product " + product.getProductId());
            }
        } else if (!StringUtils.isEmpty(product.getDeveloperName())) {
            productMetadata.setDeveloperName(product.getDeveloperName());
        } else if (product.getDeveloperId() != null) {
            try {
                Developer developer = developerDao.getSimpleDeveloperById(product.getDeveloperId(), true);
                productMetadata.setDeveloperName(developer.getName());
            } catch (Exception ex) {
                LOGGER.error("Unable to find developer with ID " + product.getDeveloperId() + " referenced "
                        + "in activity for product " + product.getProductId());
            }
        }
        productMetadata.setProductName(product.getName());
    }

    private void parseProductMetadata(ProductActivityMetadata productMetadata, ActivityDTO activity,
            List<Product> products) {
        Long idToFind = activity.getActivityObjectId();
        for (Product currProduct : products) {
            if (currProduct != null && idsMatch(currProduct, idToFind)) {
                parseProductMetadata(productMetadata, currProduct);
                break;
            }
        }
    }

    private boolean idsMatch(Product product, Long id) {
        if (product.getId() != null) {
            return product.getId().equals(id);
        } else if (product.getProductId() != null) {
            return product.getProductId().equals(id);
        }
        return false;
    }
}
