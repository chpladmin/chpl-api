package gov.healthit.chpl.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.manager.rules.product.ProductValidationContext;
import gov.healthit.chpl.manager.rules.product.ProductValidationFactory;
import gov.healthit.chpl.service.DirectReviewUpdateEmailService;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil.ChplProductNumberParts;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class ProductManager extends SecuredManager {
    private ErrorMessageUtil msgUtil;
    private ProductDAO productDao;
    private ProductVersionDAO versionDao;
    private DeveloperDAO devDao;
    private CertifiedProductDAO cpDao;
    private CertifiedProductDetailsManager cpdManager;
    private ChplProductNumberUtil chplProductNumberUtil;
    private DirectReviewUpdateEmailService drEmailService;
    private ActivityManager activityManager;
    private ProductValidationFactory productValidationFactory;
    private ValidationUtils validationUtils;

    @Autowired
    @SuppressWarnings({"checkstyle:parameternumber"})
    public ProductManager(ErrorMessageUtil msgUtil, ProductDAO productDao,
            ProductVersionDAO versionDao, DeveloperDAO devDao, CertifiedProductDAO cpDao,
            CertifiedProductDetailsManager cpdManager, ChplProductNumberUtil chplProductNumberUtil,
            ActivityManager activityManager, DirectReviewUpdateEmailService drEmailService,
            ProductValidationFactory productValidationFactory,
            ValidationUtils validationUtils) {
        this.msgUtil = msgUtil;
        this.productDao = productDao;
        this.versionDao = versionDao;
        this.devDao = devDao;
        this.cpDao = cpDao;
        this.cpdManager = cpdManager;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.activityManager = activityManager;
        this.drEmailService = drEmailService;
        this.productValidationFactory = productValidationFactory;
        this.validationUtils = validationUtils;
    }


    @Transactional(readOnly = true)
    public Product getById(Long id, boolean allowDeleted) throws EntityRetrievalException {
        return productDao.getById(id, allowDeleted);
    }


    @Transactional(readOnly = true)
    public Product getById(Long id) throws EntityRetrievalException {
        return getById(id, false);
    }


    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return productDao.exists(id);
    }


    @Transactional(readOnly = true)
    public List<Product> getAll() {
        return productDao.findAll();
    }


    @Transactional(readOnly = true)
    public List<Product> getByDeveloper(Long developerId) {
        return productDao.getByDeveloper(developerId);
    }


    @Transactional(readOnly = true)
    public List<Product> getByDevelopers(List<Long> developerIds) {
        return productDao.getByDevelopers(developerIds);
    }

    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.ProductDomainPermissions).CREATE)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH, CacheNames.PRODUCT_NAMES
    }, allEntries = true)
    public Long create(Long developerId, Product product)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        product.setOwner(Developer.builder().id(developerId).build());
        Product createdProduct = null;
        try {
            createdProduct = createProduct(product);
        } catch (Exception ex) {
            LOGGER.error("Could not create product.", ex);
            throw new EntityCreationException(ex);
        }
        String activityMsg = "Product " + product.getName() + " was created.";
        activityManager.addActivity(ActivityConcept.PRODUCT, createdProduct.getId(), activityMsg, null, createdProduct);
        return createdProduct.getId();
    }

    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.ProductDomainPermissions).UPDATE_OWNERSHIP, #product)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH
    }, allEntries = true)
    @ListingStoreRemove(removeBy = RemoveBy.PRODUCT_ID, id = "#product.id")
    public Product updateProductOwnership(Product product)
            throws EntityRetrievalException, EntityCreationException, ValidationException, JsonProcessingException {
        Map<Long, CertifiedProductSearchDetails> preUpdateListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();
        Map<Long, CertifiedProductSearchDetails> postUpdateListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();

        Product currentProduct = productDao.getById(product.getId());
        Developer currentProductOwner = devDao.getById(currentProduct.getOwner().getId());
        List<CertifiedProductDetailsDTO> affectedListings = cpDao.findByProductId(currentProduct.getId());
        LOGGER.info("Getting details for " + affectedListings.size() + " listings with affected CHPL Product Numbers");
        for (CertifiedProductDetailsDTO affectedListing : affectedListings) {
            CertifiedProductSearchDetails details = cpdManager.getCertifiedProductDetails(affectedListing.getId());
            LOGGER.info("Complete retrieving details for id: " + details.getId());
            preUpdateListingDetails.put(details.getId(), details);
        }

        Product updatedProduct = updateProduct(product);
        Developer updatedProductOwner = devDao.getById(updatedProduct.getOwner().getId());
        LOGGER.info("Getting details for " + affectedListings.size() + " listings with affected CHPL Product Numbers");
        for (CertifiedProductDetailsDTO affectedListing : affectedListings) {
            CertifiedProductSearchDetails details = cpdManager.getCertifiedProductDetails(affectedListing.getId());
            LOGGER.info("Complete retrieving details for id: " + details.getId());
            postUpdateListingDetails.put(details.getId(), details);
        }

        for (Long id : preUpdateListingDetails.keySet()) {
            activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, id,
                    "Updated certified product " + postUpdateListingDetails.get(id).getChplProductNumber()
                    + ".", preUpdateListingDetails.get(id), postUpdateListingDetails.get(id));
        }

        drEmailService.sendEmail(Arrays.asList(currentProductOwner), Arrays.asList(updatedProductOwner),
                preUpdateListingDetails, postUpdateListingDetails, LOGGER);
        return updatedProduct;
    }

    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.ProductDomainPermissions).UPDATE, #product)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH, CacheNames.PRODUCT_NAMES
    }, allEntries = true)
    @ListingStoreRemove(removeBy = RemoveBy.PRODUCT_ID, id = "#product.id")
    public Product update(Product product)
            throws EntityRetrievalException, EntityCreationException, ValidationException, JsonProcessingException {
        return updateProduct(product);
    }

    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.ProductDomainPermissions).MERGE, #productIdsToMerge)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH, CacheNames.PRODUCT_NAMES
    }, allEntries = true)
    @ListingStoreRemove(removeBy = RemoveBy.PRODUCT_ID, id = "#toCreate.id")
    public Product merge(List<Long> productIdsToMerge, Product toCreate)
            throws EntityRetrievalException, ValidationException, EntityCreationException, JsonProcessingException {

        List<Product> beforeProducts = new ArrayList<Product>();
        for (Long productId : productIdsToMerge) {
            beforeProducts.add(productDao.getById(productId));
        }

        Product createdProduct = createProduct(toCreate);
        //must set the ID otherwise the "toCreate.id" passed into the shared store is null
        toCreate.setId(createdProduct.getId());

        // search for any versions assigned to the list of products passed in
        List<ProductVersionDTO> assignedVersions = versionDao.getByProductIds(productIdsToMerge);
        // reassign those versions to the new product
        for (ProductVersionDTO version : assignedVersions) {
            version.setProductId(createdProduct.getId());
            versionDao.update(version);
        }

        // - mark the passed in products as deleted
        for (Long productId : productIdsToMerge) {
            productDao.delete(productId);
        }

        String activityMsg = "Merged " + productIdsToMerge.size() + " products into new product '"
                + createdProduct.getName() + "'.";
        activityManager.addActivity(ActivityConcept.PRODUCT, createdProduct.getId(), activityMsg, beforeProducts,
                createdProduct);

        return createdProduct;
    }

    @Transactional(rollbackFor = {
            EntityRetrievalException.class, EntityCreationException.class, JsonProcessingException.class,
            AccessDeniedException.class
    })
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.ProductDomainPermissions).SPLIT, #oldProduct)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH, CacheNames.PRODUCT_NAMES
    }, allEntries = true)
    @ListingStoreRemove(removeBy = RemoveBy.PRODUCT_ID, id = "#productToCreate.id")
    public Product split(Product oldProduct, Product productToCreate, String newProductCode,
            List<ProductVersionDTO> newProductVersions)
            throws AccessDeniedException, ValidationException, EntityRetrievalException, EntityCreationException, JsonProcessingException {
        Product createdProduct = createProduct(productToCreate);
        //must set the ID otherwise the "productToCreate.id" passed into the shared store is null
        productToCreate.setId(createdProduct.getId());

        // re-assign versions to the new product and log activity for each
        List<Long> affectedVersionIds = new ArrayList<Long>();
        for (ProductVersionDTO affectedVersion : newProductVersions) {
            // get before and after for activity; update product owner
            ProductVersionDTO beforeVersion = versionDao.getById(affectedVersion.getId());
            affectedVersion.setProductId(createdProduct.getId());
            affectedVersion.setProductName(createdProduct.getName());
            versionDao.update(affectedVersion);
            ProductVersionDTO afterVersion = versionDao.getById(affectedVersion.getId());
            activityManager.addActivity(
                    ActivityConcept.VERSION, afterVersion.getId(), "Product Version " + afterVersion.getVersion()
                            + " product owner updated to " + afterVersion.getProductName(),
                    beforeVersion, afterVersion);
            affectedVersionIds.add(affectedVersion.getId());
        }

        // update product code on all associated certified products and log
        // activity for each
        List<CertifiedProductDTO> affectedCps = cpDao.getByVersionIds(affectedVersionIds);
        for (CertifiedProductDTO affectedCp : affectedCps) {
            // have to get the cpdetails for before and after code update
            // because that is object sent into activity reports
            CertifiedProductSearchDetails beforeListing = cpdManager.getCertifiedProductDetails(affectedCp.getId());

            // make sure the updated CHPL product number is unique and that the
            // new product code is valid
            String chplNumber = beforeListing.getChplProductNumber();
            if (!chplProductNumberUtil.isLegacyChplProductNumberStyle(chplNumber)) {
                ChplProductNumberParts parts = chplProductNumberUtil.parseChplProductNumber(chplNumber);
                String potentialChplNumber = chplProductNumberUtil.getChplProductNumber(parts.getEditionCode(),
                        parts.getAtlCode(), parts.getAcbCode(), parts.getDeveloperCode(), newProductCode,
                        parts.getVersionCode(), parts.getIcsCode(), parts.getAdditionalSoftwareCode(),
                        parts.getCertifiedDateCode());
                if (!chplProductNumberUtil.isUnique(potentialChplNumber)) {
                    throw new EntityCreationException("Cannot update certified product " + chplNumber + " to "
                            + potentialChplNumber + " because a certified product with that CHPL ID already exists.");
                }
                if (!validationUtils.chplNumberPartIsValid(potentialChplNumber,
                        ChplProductNumberUtil.PRODUCT_CODE_INDEX, ChplProductNumberUtil.PRODUCT_CODE_REGEX)) {
                    throw new EntityCreationException(msgUtil.getMessage("listing.badProductCodeChars",
                            ChplProductNumberUtil.PRODUCT_CODE_LENGTH));
                }
                affectedCp.setProductCode(newProductCode);
            }

            // do the update and add activity
            cpDao.update(affectedCp);
            CertifiedProductSearchDetails afterListing = cpdManager.getCertifiedProductDetailsNoCache(affectedCp.getId());
            activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, beforeListing.getId(),
                    "Updated certified product " + afterListing.getChplProductNumber() + ".", beforeListing,
                    afterListing);
        }

        Product afterProduct = null;
        //the split is complete - log split activity
        //getting the original product object from the db to make sure it's all filled in
        Product origProduct = getById(oldProduct.getId());
        afterProduct = getById(createdProduct.getId());
        List<Product> splitProducts = new ArrayList<Product>();
        splitProducts.add(origProduct);
        splitProducts.add(afterProduct);
        activityManager.addActivity(ActivityConcept.PRODUCT, afterProduct.getId(),
                "Split product " + origProduct.getName() + " into " + origProduct.getName() + " and " + afterProduct.getName(),
                origProduct, splitProducts);

        return afterProduct;
    }

    private Product updateProduct(Product product)
            throws EntityRetrievalException, EntityCreationException, ValidationException, JsonProcessingException {

        Product existingProduct = productDao.getById(product.getId());
        runExistingProductValidations(existingProduct);
        runNewProductValidations(product);

        productDao.update(product);
        Product productAfter = productDao.getById(product.getId());
        String activityMsg = "Product " + product.getName() + " was updated.";
        activityManager.addActivity(ActivityConcept.PRODUCT, productAfter.getId(), activityMsg, existingProduct, productAfter);
        return productAfter;
    }

    private Product createProduct(Product product)
            throws ValidationException, EntityRetrievalException, EntityCreationException, JsonProcessingException {
        runNewProductValidations(product);
        Long productId = productDao.create(product.getOwner().getId(), product);
        Product createdProduct = productDao.getById(productId);
        String activityMsg = "Product " + product.getName() + " was created.";
        activityManager.addActivity(ActivityConcept.PRODUCT, createdProduct.getId(), activityMsg, null, createdProduct);
        return createdProduct;
    }

    public void runNewProductValidations(Product product) throws ValidationException {
        List<ValidationRule<ProductValidationContext>> rules = new ArrayList<ValidationRule<ProductValidationContext>>();
        rules.add(productValidationFactory.getRule(ProductValidationFactory.NAME));
        rules.add(productValidationFactory.getRule(ProductValidationFactory.OWNER));
        rules.add(productValidationFactory.getRule(ProductValidationFactory.OWNER_HISTORY));
        Set<String> validationErrors = runValidations(rules, product);
        if (!CollectionUtils.isEmpty(validationErrors)) {
            throw new ValidationException(validationErrors);
        }
    }

    public void runExistingProductValidations(Product product) throws ValidationException {
        List<ValidationRule<ProductValidationContext>> rules = new ArrayList<ValidationRule<ProductValidationContext>>();
        rules.add(productValidationFactory.getRule(ProductValidationFactory.NAME));
        rules.add(productValidationFactory.getRule(ProductValidationFactory.OWNER));
        Set<String> validationErrors = runValidations(rules, product);
        if (!CollectionUtils.isEmpty(validationErrors)) {
            throw new ValidationException(validationErrors);
        }
    }

    private Set<String> runValidations(List<ValidationRule<ProductValidationContext>> rules,
            Product product) {
        Set<String> errorMessages = new HashSet<String>();
        ProductValidationContext context = new ProductValidationContext(product, devDao, msgUtil);

        for (ValidationRule<ProductValidationContext> rule : rules) {
            if (!rule.isValid(context)) {
                errorMessages.addAll(rule.getMessages());
            }
        }
        return errorMessages;
    }
}
