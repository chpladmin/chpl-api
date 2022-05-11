package gov.healthit.chpl.upload.listing.normalizer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.dto.ProductVersionDTO;

@Component
public class ProductAndVersionNormalizer {
    private ProductDAO productDao;
    private ProductVersionDAO versionDao;

    @Autowired
    public ProductAndVersionNormalizer(ProductDAO productDao, ProductVersionDAO versionDao) {
        this.productDao = productDao;
        this.versionDao = versionDao;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (listing.getDeveloper() == null || listing.getDeveloper().getId() == null) {
            return;
        }

        if (listing.getProduct() != null && listing.getProduct().getId() == null
                && !StringUtils.isEmpty(listing.getProduct().getName())) {
            Product foundProduct = productDao.getByDeveloperAndName(listing.getDeveloper().getId(),
                    listing.getProduct().getName());
            if (foundProduct != null) {
                listing.setProduct(foundProduct);
            }
        }

        if (listing.getProduct() != null && listing.getProduct().getId() != null
                && listing.getVersion() != null && listing.getVersion().getId() == null
                && !StringUtils.isEmpty(listing.getVersion().getVersion())) {
            ProductVersionDTO foundVersion = versionDao.getByProductAndVersion(listing.getProduct().getId(),
                    listing.getVersion().getVersion());
            if (foundVersion != null) {
                listing.setVersion(new ProductVersion(foundVersion));
            }
        }
    }
}
