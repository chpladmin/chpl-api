package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.fuzzyMatching.FuzzyChoicesManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("productReviewer")
public class ProductReviewer implements Reviewer {
    private FuzzyChoicesManager fuzzyChoicesManager;
    private ProductDAO productDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ProductReviewer(FuzzyChoicesManager fuzzyChoicesManager,
            ProductDAO productDao,
            ErrorMessageUtil msgUtil) {
        this.fuzzyChoicesManager = fuzzyChoicesManager;
        this.productDao = productDao;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getProduct() == null) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.missingProduct"));
        } else {
            addFuzzyMatchWarnings(listing);
        }
    }

    private void addFuzzyMatchWarnings(CertifiedProductSearchDetails listing) {
        if (listing.getProduct() != null && listing.getProduct().getId() == null
                && listing.getDeveloper() != null && listing.getDeveloper().getId() != null) {
            String fuzzyMatchedProductName = findFuzzyMatchForUnknownProduct(listing);
            if (!StringUtils.isEmpty(fuzzyMatchedProductName)) {
                String warningMsg = msgUtil.getMessage("listing.product.fuzzyMatch", listing.getProduct().getName(),
                        listing.getDeveloper().getName(), fuzzyMatchedProductName);
                listing.addWarningMessage(warningMsg);
            }
        }
    }

    private String findFuzzyMatchForUnknownProduct(CertifiedProductSearchDetails listing) {
        List<Product> productsOnDeveloper = productDao.getByDeveloper(listing.getDeveloper().getId());
        List<String> productNames = productsOnDeveloper.stream()
                .map(prod -> prod.getName())
                .collect(Collectors.toList());
        return fuzzyChoicesManager.getTopFuzzyChoice(listing.getProduct().getName(), productNames);
    }
}
