package gov.healthit.chpl.activity;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.ProductActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;

@Component("productActivityMetadataBuilder")
public class ProductActivityMetadataBuilder extends ActivityMetadataBuilder {
    private static final Logger LOGGER = LogManager.getLogger(ProductActivityMetadataBuilder.class);
    private ObjectMapper jsonMapper;
    private DeveloperDAO developerDao;

    @Autowired
    public ProductActivityMetadataBuilder(final DeveloperDAO developerDao) {
        super();
        jsonMapper = new ObjectMapper();
        this.developerDao = developerDao;
    }

    protected void addConceptSpecificMetadata(final ActivityDTO dto, final ActivityMetadata metadata) {
        if (!(metadata instanceof ProductActivityMetadata)) {
            return;
        }
        ProductActivityMetadata productMetadata = (ProductActivityMetadata) metadata;

        //parse product specific metadata
        ProductDTO origProduct = null;
        if (dto.getOriginalData() != null) {
            try {
                origProduct =
                    jsonMapper.readValue(dto.getOriginalData(), ProductDTO.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity ID " + dto.getId() + " original data. "
                        + "JSON was: " + dto.getOriginalData(), ex);
            }
        }

        ProductDTO newProduct = null;
        if (dto.getNewData() != null) {
            try {
                newProduct =
                    jsonMapper.readValue(dto.getNewData(), ProductDTO.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity ID " + dto.getId() + " new data. "
                        + "JSON was: " + dto.getNewData(), ex);
            }
        }

        if (newProduct != null) {
            //if there is a new product that could mean the product
            //was updated and we want to fill in the metadata with the
            //latest product info
            parseProductMetadata(productMetadata, newProduct);
        } else if (origProduct != null) {
            //if there is an original product but no new product
            //then the product was deleted - pull its info from the orig object
            parseProductMetadata(productMetadata, origProduct);
        }

        productMetadata.getCategories().add(ActivityCategory.PRODUCT);
    }

    private void parseProductMetadata(
            final ProductActivityMetadata productMetadata, final ProductDTO product) {
        //Developer id is always filled in the activity object
        //but the name does not seem to be. If the name is available
        //use it but if not look up the developer by ID
        if (!StringUtils.isEmpty(product.getDeveloperName())) {
            productMetadata.setDeveloperName(product.getDeveloperName());
        } else if (product.getDeveloperId() != null) {
            try {
                DeveloperDTO developer = developerDao.getById(product.getDeveloperId());
                productMetadata.setDeveloperName(developer.getName());
            } catch (Exception ex) {
                LOGGER.error("Unable to find developer with ID " + product.getDeveloperId() + " referenced "
                        + "in activity for product " + product.getId());
            }
        }
        productMetadata.setProductName(product.getName());
    }
}
