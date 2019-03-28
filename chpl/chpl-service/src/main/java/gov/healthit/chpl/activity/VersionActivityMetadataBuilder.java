package gov.healthit.chpl.activity;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.VersionActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;

@Component("versionActivityMetadataBuilder")
public class VersionActivityMetadataBuilder extends ActivityMetadataBuilder {
    private static final Logger LOGGER = LogManager.getLogger(VersionActivityMetadataBuilder.class);
    private ObjectMapper jsonMapper;
    private ProductDAO productDao;

    @Autowired
    public VersionActivityMetadataBuilder(final ProductDAO productDao) {
        super();
        jsonMapper = new ObjectMapper();
        this.productDao = productDao;
    }

    protected void addConceptSpecificMetadata(final ActivityDTO dto, final ActivityMetadata metadata) {
        if (!(metadata instanceof VersionActivityMetadata)) {
            return;
        }
        VersionActivityMetadata versionMetadata = (VersionActivityMetadata) metadata;

        //parse version specific metadata
        ProductVersionDTO origVersion = null;
        if (dto.getOriginalData() != null) {
            try {
                origVersion =
                    jsonMapper.readValue(dto.getOriginalData(), ProductVersionDTO.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity ID " + dto.getId() + " original data. "
                        + "JSON was: " + dto.getOriginalData(), ex);
            }
        }

        ProductVersionDTO newVersion = null;
        if (dto.getNewData() != null) {
            try {
                newVersion =
                    jsonMapper.readValue(dto.getNewData(), ProductVersionDTO.class);
            } catch (final Exception ex) {
                LOGGER.error("Could not parse activity ID " + dto.getId() + " new data. "
                        + "JSON was: " + dto.getNewData(), ex);
            }
        }

        if (newVersion != null) {
            //if there is a new version that could mean the version
            //was version and we want to fill in the metadata with the
            //latest product info
            parseVersionMetadata(versionMetadata, newVersion);
        } else if (origVersion != null) {
            //if there is an original version but no new version
            //then the version was deleted - pull its info from the orig object
            parseVersionMetadata(versionMetadata, origVersion);
        }

        versionMetadata.getCategories().add(ActivityCategory.VERSION);
    }

    private void parseVersionMetadata(
            final VersionActivityMetadata versionMetadata, final ProductVersionDTO version) {
        //Product ID or Name may or may not be filled in.
        //Try to get the product name if either is available.
        if (!StringUtils.isEmpty(version.getProductName())) {
            versionMetadata.setProductName(version.getProductName());
        } else if (version.getProductId() != null) {
            try {
                ProductDTO product = productDao.getById(version.getProductId());
                versionMetadata.setProductName(product.getName());
            } catch (Exception ex) {
                LOGGER.error("Unable to find product with ID " + version.getProductId() + " referenced "
                        + "in activity for version " + version.getId());
            }
        }
        versionMetadata.setVersion(version.getVersion());
    }
}
