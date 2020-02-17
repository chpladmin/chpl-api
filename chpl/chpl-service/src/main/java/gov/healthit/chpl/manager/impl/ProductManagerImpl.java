package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil.ChplProductNumberParts;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

@Service
public class ProductManagerImpl extends SecuredManager implements ProductManager {
    private static final Logger LOGGER = LogManager.getLogger(ProductManagerImpl.class);

    private ErrorMessageUtil msgUtil;
    private ProductDAO productDao;
    private ProductVersionDAO versionDao;
    private DeveloperDAO devDao;
    private CertifiedProductDAO cpDao;
    private CertifiedProductDetailsManager cpdManager;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ActivityManager activityManager;
    private ResourcePermissions resourcePermissions;

    @Autowired
    public ProductManagerImpl(final ErrorMessageUtil msgUtil, final ProductDAO productDao,
            final ProductVersionDAO versionDao, final DeveloperDAO devDao, final CertifiedProductDAO cpDao,
            final CertifiedProductDetailsManager cpdManager,
            final ChplProductNumberUtil chplProductNumberUtil, final ActivityManager activityManager,
            final ResourcePermissions resourcePermissions) {
        this.msgUtil = msgUtil;
        this.productDao = productDao;
        this.versionDao = versionDao;
        this.devDao = devDao;
        this.cpDao = cpDao;
        this.cpdManager = cpdManager;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.activityManager = activityManager;
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getById(final Long id, final boolean allowDeleted) throws EntityRetrievalException {
        return productDao.getById(id, allowDeleted);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getById(final Long id) throws EntityRetrievalException {
        return getById(id, false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return productDao.exists(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAll() {
        return productDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getByDeveloper(final Long developerId) {
        return productDao.getByDeveloper(developerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getByDevelopers(final List<Long> developerIds) {
        return productDao.getByDevelopers(developerIds);
    }

    @Override
    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.ProductDomainPermissions).CREATE)")
    public ProductDTO create(final ProductDTO dto)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        // check that the developer of this product is Active
        if (dto.getDeveloperId() == null) {
            throw new EntityCreationException("Cannot create a product without a developer ID.");
        }

        return createProduct(dto);
    }

    @Override
    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.ProductDomainPermissions).UPDATE_OWNERSHIP, #dto)")
    public ProductDTO updateProductOwnership(final ProductDTO dto)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        // This method was created to provide different security than the update() method
        // even though it is the same functionality...
        return updateProduct(dto);
    }

    @Override
    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.ProductDomainPermissions).UPDATE, #dto)")
    public ProductDTO update(final ProductDTO dto)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        return updateProduct(dto);
    }

    @Override
    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.ProductDomainPermissions).MERGE, #productIdsToMerge)")
    public ProductDTO merge(final List<Long> productIdsToMerge, final ProductDTO toCreate)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {

        List<ProductDTO> beforeProducts = new ArrayList<ProductDTO>();
        for (Long productId : productIdsToMerge) {
            beforeProducts.add(productDao.getById(productId));
        }

        ProductDTO createdProduct = productDao.create(toCreate);

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

    @Override
    @Transactional(rollbackFor = {
            EntityRetrievalException.class, EntityCreationException.class, JsonProcessingException.class,
            AccessDeniedException.class
    })
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.ProductDomainPermissions).SPLIT, #oldProduct)")
    public ProductDTO split(final ProductDTO oldProduct, final ProductDTO productToCreate, final String newProductCode,
            final List<ProductVersionDTO> newProductVersions)
            throws AccessDeniedException, EntityRetrievalException, EntityCreationException, JsonProcessingException {
        // what ACB does the user have??
        List<CertificationBodyDTO> allowedAcbs = resourcePermissions.getAllAcbsForCurrentUser();

        // create the new product and log activity
        // this method checks that the related developer is Active and will
        // throw an exception if they aren't
        ProductDTO createdProduct = createProduct(productToCreate);

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
            // make sure each cp listing associated with the newProduct ->
            // version is owned by an ACB the user has access to
            boolean hasAccessToAcb = false;
            for (CertificationBodyDTO allowedAcb : allowedAcbs) {
                if (allowedAcb.getId().longValue() == affectedCp.getCertificationBodyId().longValue()) {
                    hasAccessToAcb = true;
                }
            }
            if (!hasAccessToAcb) {
                throw new AccessDeniedException(
                        msgUtil.getMessage("acb.accessDenied.listingUpdate", beforeListing.getChplProductNumber(),
                                beforeListing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY)));
            }

            // make sure the updated CHPL product number is unique and that the
            // new product code is valid
            String chplNumber = beforeListing.getChplProductNumber();
            if (!chplProductNumberUtil.isLegacy(chplNumber)) {
                ChplProductNumberParts parts = chplProductNumberUtil.parseChplProductNumber(chplNumber);
                String potentialChplNumber = chplProductNumberUtil.getChplProductNumber(parts.getEditionCode(),
                        parts.getAtlCode(), parts.getAcbCode(), parts.getDeveloperCode(), newProductCode,
                        parts.getVersionCode(), parts.getIcsCode(), parts.getAdditionalSoftwareCode(),
                        parts.getCertifiedDateCode());
                if (!chplProductNumberUtil.isUnique(potentialChplNumber)) {
                    throw new EntityCreationException("Cannot update certified product " + chplNumber + " to "
                            + potentialChplNumber + " because a certified product with that CHPL ID already exists.");
                }
                if (!ValidationUtils.chplNumberPartIsValid(potentialChplNumber,
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

        ProductDTO afterProduct = null;
        //the split is complete - log split activity
        //getting the original product object from the db to make sure it's all filled in
        ProductDTO origProduct = getById(oldProduct.getId());
        afterProduct = getById(createdProduct.getId());
        List<ProductDTO> splitProducts = new ArrayList<ProductDTO>();
        splitProducts.add(origProduct);
        splitProducts.add(afterProduct);
        activityManager.addActivity(ActivityConcept.PRODUCT, afterProduct.getId(),
                "Split product " + origProduct.getName() + " into " + origProduct.getName() + " and " + afterProduct.getName(),
                origProduct, splitProducts);

        return afterProduct;
    }

    private ProductDTO updateProduct(final ProductDTO dto)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {

        ProductDTO beforeDTO = productDao.getById(dto.getId());

        // check that the developer of this product is Active
        if (beforeDTO.getDeveloperId() == null) {
            throw new EntityCreationException("Cannot update a product without a developer ID.");
        }

        DeveloperDTO dev = devDao.getById(beforeDTO.getDeveloperId());
        if (dev == null) {
            throw new EntityRetrievalException("Cannot find developer with id " + beforeDTO.getDeveloperId());
        }
        DeveloperStatusEventDTO currDevStatus = dev.getStatus();
        if (currDevStatus == null || currDevStatus.getStatus() == null) {
            String msg = "The product " + dto.getName() + " cannot be updated since the status of developer "
                    + dev.getName() + " cannot be determined.";
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        } else if (!currDevStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString())
                && !resourcePermissions.isUserRoleAdmin() && !resourcePermissions.isUserRoleOnc()) {
            String msg = "The product " + dto.getName() + " cannot be updated since the developer " + dev.getName()
                    + " has a status of " + currDevStatus.getStatus().getStatusName();
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        }

        ProductDTO result = productDao.update(dto);
        // the developer name is not updated at this point until after
        // transaction commit so we have to set it
        DeveloperDTO devDto = devDao.getById(result.getDeveloperId());
        result.setDeveloperName(devDto.getName());

        String activityMsg = "Product " + dto.getName() + " was updated.";
        activityManager.addActivity(ActivityConcept.PRODUCT, result.getId(), activityMsg, beforeDTO, result);
        return result;
    }

    private ProductDTO createProduct(final ProductDTO dto)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        DeveloperDTO dev = devDao.getById(dto.getDeveloperId());
        if (dev == null) {
            throw new EntityRetrievalException("Cannot find developer with id " + dto.getDeveloperId());
        }
        DeveloperStatusEventDTO currDevStatus = dev.getStatus();
        if (currDevStatus == null || currDevStatus.getStatus() == null) {
            String msg = "The product " + dto.getName() + " cannot be created since the status of developer "
                    + dev.getName() + " cannot be determined.";
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        }

        ProductDTO result = productDao.create(dto);
        String activityMsg = "Product " + dto.getName() + " was created.";
        activityManager.addActivity(ActivityConcept.PRODUCT, result.getId(), activityMsg, null, result);
        return getById(result.getId());
    }
}
