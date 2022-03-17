package gov.healthit.chpl.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.DirectReviewUpdateEmailService;
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
    private ResourcePermissions resourcePermissions;
    private ValidationUtils validationUtils;

    @Autowired
    @SuppressWarnings({"checkstyle:parameternumber"})
    public ProductManager(ErrorMessageUtil msgUtil, ProductDAO productDao,
            ProductVersionDAO versionDao, DeveloperDAO devDao, CertifiedProductDAO cpDao,
            CertifiedProductDetailsManager cpdManager, ChplProductNumberUtil chplProductNumberUtil,
            ActivityManager activityManager, DirectReviewUpdateEmailService drEmailService,
            ResourcePermissions resourcePermissions, ValidationUtils validationUtils) {
        this.msgUtil = msgUtil;
        this.productDao = productDao;
        this.versionDao = versionDao;
        this.devDao = devDao;
        this.cpDao = cpDao;
        this.cpdManager = cpdManager;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.activityManager = activityManager;
        this.drEmailService = drEmailService;
        this.resourcePermissions = resourcePermissions;
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
    public Product create(Product product)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        // check that the developer of this product is Active
        if (product.getOwner() == null || product.getOwner().getDeveloperId() == null) {
            throw new EntityCreationException("Cannot create a product without a developer ID.");
        }

        return createProduct(product);
    }

    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.ProductDomainPermissions).CREATE)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH, CacheNames.PRODUCT_NAMES
    }, allEntries = true)
    public Long create(Long developerId, Product product)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        Developer dev = devDao.getById(developerId);
        if (dev == null) {
            throw new EntityRetrievalException("Cannot find developer with id " + developerId);
        }
        DeveloperStatus currDevStatus = dev.getStatus();
        if (currDevStatus == null || currDevStatus.getStatus() == null) {
            String msg = "The product " + product.getName() + " cannot be created since the status of developer "
                    + dev.getName() + " cannot be determined.";
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        }

        Long productId = productDao.create(developerId, product);
        product.setProductId(productId);
        Product createdProduct = productDao.getById(productId);
        String activityMsg = "Product " + product.getName() + " was created.";
        activityManager.addActivity(ActivityConcept.PRODUCT, productId, activityMsg, null, createdProduct);
        return productId;
    }

    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.ProductDomainPermissions).UPDATE_OWNERSHIP, #product)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH
    }, allEntries = true)
    public Product updateProductOwnership(Product product)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        Map<Long, CertifiedProductSearchDetails> preUpdateListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();
        Map<Long, CertifiedProductSearchDetails> postUpdateListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();

        Product currentProduct = productDao.getById(product.getProductId());
        Developer currentProductOwner = devDao.getById(currentProduct.getOwner().getDeveloperId());
        List<CertifiedProductDetailsDTO> affectedListings = cpDao.findByProductId(currentProduct.getOwner().getDeveloperId());
        LOGGER.info("Getting details for " + affectedListings.size() + " listings with affected CHPL Product Numbers");
        for (CertifiedProductDetailsDTO affectedListing : affectedListings) {
            CertifiedProductSearchDetails details = cpdManager.getCertifiedProductDetails(affectedListing.getId());
            LOGGER.info("Complete retrieving details for id: " + details.getId());
            preUpdateListingDetails.put(details.getId(), details);
        }

        Product updatedProduct = updateProduct(product);
        Developer updatedProductOwner = devDao.getById(updatedProduct.getOwner().getDeveloperId());
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
                preUpdateListingDetails, postUpdateListingDetails);
        return updatedProduct;
    }

    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.ProductDomainPermissions).UPDATE, #product)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH, CacheNames.PRODUCT_NAMES
    }, allEntries = true)
    public Product update(Product product)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        return updateProduct(product);
    }

    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.ProductDomainPermissions).MERGE, #productIdsToMerge)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH, CacheNames.PRODUCT_NAMES
    }, allEntries = true)
    public Product merge(List<Long> productIdsToMerge, Product toCreate)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {

        List<Product> beforeProducts = new ArrayList<Product>();
        for (Long productId : productIdsToMerge) {
            beforeProducts.add(productDao.getById(productId));
        }

        Long createdProductId = productDao.create(toCreate.getOwner().getDeveloperId(), toCreate);

        // search for any versions assigned to the list of products passed in
        List<ProductVersionDTO> assignedVersions = versionDao.getByProductIds(productIdsToMerge);
        // reassign those versions to the new product
        for (ProductVersionDTO version : assignedVersions) {
            version.setProductId(createdProductId);
            versionDao.update(version);
        }

        // - mark the passed in products as deleted
        for (Long productId : productIdsToMerge) {
            productDao.delete(productId);
        }

        Product createdProduct = productDao.getById(createdProductId);
        String activityMsg = "Merged " + productIdsToMerge.size() + " products into new product '"
                + createdProduct.getName() + "'.";
        activityManager.addActivity(ActivityConcept.PRODUCT, createdProduct.getProductId(), activityMsg, beforeProducts,
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
    public Product split(Product oldProduct, Product productToCreate, String newProductCode,
            List<ProductVersionDTO> newProductVersions)
            throws AccessDeniedException, EntityRetrievalException, EntityCreationException, JsonProcessingException {
        // create the new product and log activity
        // this method checks that the related developer is Active and will
        // throw an exception if they aren't
        Product createdProduct = createProduct(productToCreate);

        // re-assign versions to the new product and log activity for each
        List<Long> affectedVersionIds = new ArrayList<Long>();
        for (ProductVersionDTO affectedVersion : newProductVersions) {
            // get before and after for activity; update product owner
            ProductVersionDTO beforeVersion = versionDao.getById(affectedVersion.getId());
            affectedVersion.setProductId(createdProduct.getProductId());
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
            CertifiedProductSearchDetails afterListing = cpdManager.getCertifiedProductDetails(affectedCp.getId());
            activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, beforeListing.getId(),
                    "Updated certified product " + afterListing.getChplProductNumber() + ".", beforeListing,
                    afterListing);
        }

        Product afterProduct = null;
        //the split is complete - log split activity
        //getting the original product object from the db to make sure it's all filled in
        Product origProduct = getById(oldProduct.getProductId());
        afterProduct = getById(createdProduct.getProductId());
        List<Product> splitProducts = new ArrayList<Product>();
        splitProducts.add(origProduct);
        splitProducts.add(afterProduct);
        activityManager.addActivity(ActivityConcept.PRODUCT, afterProduct.getProductId(),
                "Split product " + origProduct.getName() + " into " + origProduct.getName() + " and " + afterProduct.getName(),
                origProduct, splitProducts);

        return afterProduct;
    }

    private Product updateProduct(Product product)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {

        Product productBefore = productDao.getById(product.getProductId());
        // check that the developer of this product is Active
        if (productBefore.getOwner() == null || productBefore.getOwner().getDeveloperId() == null) {
            throw new EntityCreationException("Cannot update a product without a developer ID.");
        }

        Developer currentProductOwner = devDao.getById(productBefore.getOwner().getDeveloperId());
        if (currentProductOwner == null) {
            throw new EntityRetrievalException("Cannot find developer with id " + productBefore.getOwner().getDeveloperId());
        }
        DeveloperStatus currDevStatus = currentProductOwner.getStatus();
        if (currDevStatus == null || currDevStatus.getStatus() == null) {
            String msg = "The product " + product.getName() + " cannot be updated since the status of developer "
                    + currentProductOwner.getName() + " cannot be determined.";
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        } else if (!currDevStatus.getStatus().equals(DeveloperStatusType.Active.toString())
                && !resourcePermissions.isUserRoleAdmin() && !resourcePermissions.isUserRoleOnc()) {
            String msg = "The product " + product.getName() + " cannot be updated since the developer " + currentProductOwner.getName()
                    + " has a status of " + currDevStatus.getStatus();
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        }

        productDao.update(product);
        Product productAfter = productDao.getById(product.getProductId());
        String activityMsg = "Product " + product.getName() + " was updated.";
        activityManager.addActivity(ActivityConcept.PRODUCT, productAfter.getProductId(), activityMsg, productBefore, productAfter);
        return productAfter;
    }

    private Product createProduct(Product product)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        Developer dev = devDao.getById(product.getOwner().getDeveloperId());
        if (dev == null) {
            throw new EntityRetrievalException("Cannot find developer with id " + product.getOwner().getDeveloperId());
        }
        DeveloperStatus currDevStatus = dev.getStatus();
        if (currDevStatus == null || currDevStatus.getStatus() == null) {
            String msg = "The product " + product.getName() + " cannot be created since the status of developer "
                    + dev.getName() + " cannot be determined.";
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        }

        Long productId = productDao.create(product.getOwner().getDeveloperId(), product);
        Product createdProduct = productDao.getById(productId);
        String activityMsg = "Product " + product.getName() + " was created.";
        activityManager.addActivity(ActivityConcept.PRODUCT, createdProduct.getProductId(), activityMsg, null, createdProduct);
        return createdProduct;
    }
}
