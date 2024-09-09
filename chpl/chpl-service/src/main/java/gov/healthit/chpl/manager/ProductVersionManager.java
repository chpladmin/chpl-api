package gov.healthit.chpl.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.ListingSearchCacheRefresh;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.ProductVersionEntity;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.manager.rules.version.VersionValidationContext;
import gov.healthit.chpl.manager.rules.version.VersionValidationFactory;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil.ChplProductNumberParts;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ProductVersionManager extends SecuredManager {
    private ProductVersionDAO versionDao;
    private DeveloperDAO devDao;
    private ProductDAO prodDao;
    private CertifiedProductDAO cpDao;
    private ActivityManager activityManager;
    private CertifiedProductDetailsManager cpdManager;
    private ErrorMessageUtil msgUtil;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ValidationUtils validationUtils;
    private VersionValidationFactory versionValidationFactory;

    @Autowired
    @SuppressWarnings({"checkstyle:parameternumber"})
    public ProductVersionManager(ProductVersionDAO versionDao, DeveloperDAO devDao,
            ProductDAO prodDao, CertifiedProductDAO cpDao, ActivityManager activityManager,
            CertifiedProductDetailsManager cpdManager,
            ErrorMessageUtil msgUtil, ChplProductNumberUtil chplProductNumberUtil,
            ValidationUtils validationUtils,
            VersionValidationFactory versionValidationFactory) {
        this.versionDao = versionDao;
        this.devDao = devDao;
        this.prodDao = prodDao;
        this.cpDao = cpDao;
        this.activityManager = activityManager;
        this.cpdManager = cpdManager;
        this.msgUtil = msgUtil;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.validationUtils = validationUtils;
        this.versionValidationFactory = versionValidationFactory;
    }

    @Transactional(readOnly = true)
    public ProductVersionDTO getById(Long id, boolean allowDeleted)
            throws EntityRetrievalException {
        return versionDao.getById(id, allowDeleted);
    }

    @Transactional(readOnly = true)
    public ProductVersionDTO getById(Long id) throws EntityRetrievalException {
        return getById(id, false);
    }

    @Transactional(readOnly = true)
    public List<ProductVersionDTO> getAll() {
        return versionDao.findAll();
    }

    @Transactional(readOnly = true)
    public List<ProductVersionDTO> getByProduct(Long productId) {
        return versionDao.getByProductId(productId);
    }

    @Transactional(readOnly = true)
    public List<ProductVersionDTO> getByProducts(List<Long> productIds) {
        return versionDao.getByProductIds(productIds);
    }

    @Transactional(readOnly = true)
    public List<ProductVersionDTO> getByDeveloper(Long developerId) {
        return versionDao.getByDeveloper(developerId);
    }

    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT_VERSION, "
            + "T(gov.healthit.chpl.permissions.domains.ProductVersionDomainPermissions).CREATE)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS
    }, allEntries = true)
    @ListingSearchCacheRefresh
    public Long create(Long productId, ProductVersion version)
            throws EntityCreationException, EntityRetrievalException,
            ValidationException, ActivityException {
        // check that the developer of this version is Active
        if (productId == null) {
            throw new EntityCreationException("Cannot create a version without a product ID.");
        }
        Product prod = prodDao.getById(productId);
        if (prod == null) {
            throw new EntityRetrievalException("Cannot find product with id " + productId);
        }
        Developer dev = devDao.getById(prod.getOwner().getId());
        if (dev == null) {
            throw new EntityRetrievalException("Cannot find developer with id " + prod.getOwner().getId());
        }
        if (!dev.isNotBannedOrSuspended()) {
            String msg = "The version " + version.getVersion() + " cannot be created due to the current status of the developer";
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        }

        runNewVersionValidations(version, productId, null);

        Long versionId = versionDao.create(productId, version);
        version.setId(versionId);
        ProductVersionDTO createdVersionDto = versionDao.getById(versionId);
        activityManager.addActivity(ActivityConcept.VERSION, versionId,
                "Product Version " + version.getVersion() + " added for product " + productId, null, createdVersionDto);
        return versionId;
    }

    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT_VERSION, "
            + "T(gov.healthit.chpl.permissions.domains.ProductVersionDomainPermissions).CREATE)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS
    }, allEntries = true)
    @ListingSearchCacheRefresh
    public ProductVersionDTO create(ProductVersionDTO dto, List<Long> versionsBeingMerged) throws EntityCreationException,
        ValidationException, EntityRetrievalException, ActivityException {
        // check that the developer of this version is Active
        if (dto.getProductId() == null) {
            throw new EntityCreationException("Cannot create a version without a product ID.");
        }
        Product prod = prodDao.getById(dto.getProductId());
        if (prod == null) {
            throw new EntityRetrievalException("Cannot find product with id " + dto.getProductId());
        }
        Developer dev = devDao.getById(prod.getOwner().getId());
        if (dev == null) {
            throw new EntityRetrievalException("Cannot find developer with id " + prod.getOwner().getId());
        }
        if (!dev.isNotBannedOrSuspended()) {
            String msg = "The version " + dto.getVersion() + " cannot be created due to the current status of the developer";
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        }

        runNewVersionValidations(new ProductVersion(dto), dto.getProductId(), versionsBeingMerged);

        ProductVersionDTO created = versionDao.create(dto);
        activityManager.addActivity(ActivityConcept.VERSION, created.getId(),
                "Product Version " + dto.getVersion() + " added for product " + dto.getProductId(), null, created);
        return created;
    }

    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT_VERSION, "
            + "T(gov.healthit.chpl.permissions.domains.ProductVersionDomainPermissions).UPDATE, #version)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.QUESTIONABLE_ACTIVITIES
    }, allEntries = true)
    @ListingSearchCacheRefresh
    @ListingStoreRemove(removeBy = RemoveBy.VERSION_ID, id = "#version.id")
    public ProductVersionDTO update(ProductVersionDTO version) throws EntityRetrievalException,
        ValidationException, EntityCreationException, ActivityException {

        ProductVersionDTO beforeVersion = versionDao.getById(version.getId());

        //Needs more work in the future to remove the ProductVersionDTO.
        ProductVersion versionDomain = new ProductVersion(version);
        ProductVersion beforeVersionDomain = new ProductVersion(beforeVersion);
        if (versionDomain.equals(beforeVersionDomain)) {
            LOGGER.info("Version did not change - not saving");
            LOGGER.info(versionDomain.toString());
            return beforeVersion;
        }

        runExistingVersionValidations(beforeVersionDomain, beforeVersion.getProductId());
        runNewVersionValidations(versionDomain, beforeVersion.getProductId(), null);

        // check that the developer of this version is Active
        Developer dev = devDao.getByVersion(beforeVersion.getId());
        if (dev == null) {
            throw new EntityRetrievalException("Cannot find developer of version id " + beforeVersion.getId());
        }
        if (!dev.isNotBannedOrSuspended()) {
            String msg = "The version " + beforeVersion.getVersion() + " cannot be created due to the current status of the developer";
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        }

        ProductVersionEntity result = versionDao.update(version);
        ProductVersionDTO after = new ProductVersionDTO(result);
        activityManager.addActivity(ActivityConcept.VERSION, after.getId(),
                "Product Version " + version.getVersion() + " updated for product " + version.getProductId(), beforeVersion, after);
        return after;
    }


    @Transactional(rollbackFor = {
            EntityRetrievalException.class, EntityCreationException.class, JsonProcessingException.class,
            AccessDeniedException.class
    })
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT_VERSION, "
            + "T(gov.healthit.chpl.permissions.domains.ProductVersionDomainPermissions).MERGE, #versionIdsToMerge)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.QUESTIONABLE_ACTIVITIES
    }, allEntries = true)
    @ListingSearchCacheRefresh
    @ListingStoreRemove(removeBy = RemoveBy.VERSION_ID, id = "#toCreate.id")
    public ProductVersionDTO merge(List<Long> versionIdsToMerge, ProductVersionDTO toCreate)
            throws EntityRetrievalException, EntityCreationException, ValidationException, ActivityException {

        List<ProductVersionDTO> beforeVersions = new ArrayList<ProductVersionDTO>();
        for (Long versionId : versionIdsToMerge) {
            beforeVersions.add(versionDao.getById(versionId));
        }

        ProductVersionDTO createdVersion = create(toCreate, versionIdsToMerge);
        //must set the ID otherwise the "toCreate.id" passed into the shared store is null
        toCreate.setId(createdVersion.getId());

        // search for any certified products assigned to the list of versions
        // passed in
        List<CertifiedProductDTO> assignedCps = cpDao.getByVersionIds(versionIdsToMerge);

        // reassign those certified products to the new version
        for (CertifiedProductDTO certifiedProduct : assignedCps) {
            certifiedProduct.setProductVersionId(createdVersion.getId());
            cpDao.update(certifiedProduct);
        }

        // - mark the passed in versions as deleted
        for (Long versionId : versionIdsToMerge) {
            versionDao.delete(versionId);
        }

        activityManager.addActivity(ActivityConcept.VERSION, createdVersion.getId(),
                "Merged " + versionIdsToMerge.size() + " versions into '" + createdVersion.getVersion() + "'.",
                beforeVersions, createdVersion);

        return createdVersion;
    }


    @Transactional(rollbackFor = {
            EntityRetrievalException.class, EntityCreationException.class, JsonProcessingException.class,
            AccessDeniedException.class
    })
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT_VERSION, "
            + "T(gov.healthit.chpl.permissions.domains.ProductVersionDomainPermissions).SPLIT, #oldVersion)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.QUESTIONABLE_ACTIVITIES
    }, allEntries = true)
    @ListingSearchCacheRefresh
    @ListingStoreRemove(removeBy = RemoveBy.VERSION_ID, id = "#newVersion.id")
    public ProductVersionDTO split(ProductVersionDTO oldVersion, ProductVersionDTO newVersion, String newVersionCode, List<Long> newVersionListingIds)
            throws EntityCreationException, EntityRetrievalException, ValidationException,
            ActivityException {

        // create the new version and log activity
        // this method checks that the related developer is Active and will
        // throw an exception if they aren't
        ProductVersionDTO createdVersion = create(newVersion, null);
        //must set the ID otherwise the "newVersion.id" passed into the shared store is null
        newVersion.setId(createdVersion.getId());

        // re-assign listings to the new version and
        //update their version codes and log activity for each
        for (Long affectedListingId : newVersionListingIds) {
            //get listing by id so all info is filled in prior to update
            CertifiedProductDTO affectedListing = cpDao.getById(affectedListingId);

            // have to get the cpdetails for before and after code update
            // because that is object sent into activity reports
            CertifiedProductSearchDetails beforeListing = cpdManager.getCertifiedProductDetails(affectedListingId);

            //make sure the affected listing belongs to the old version id
            if (!affectedListing.getProductVersionId().equals(oldVersion.getId().longValue())) {
                throw new EntityCreationException(msgUtil.getMessage("version.split.listingVersionMismatch",
                        affectedListing.getChplProductNumber(), affectedListing.getProductVersionId(),
                        oldVersion.getId()));
            }

            // make sure the updated CHPL product number is unique and that the
            // new product code is valid
            String chplNumber = beforeListing.getChplProductNumber();
            if (!chplProductNumberUtil.isLegacyChplProductNumberStyle(chplNumber)) {
                ChplProductNumberParts parts = chplProductNumberUtil.parseChplProductNumber(chplNumber);
                String potentialChplNumber = chplProductNumberUtil.getChplProductNumber(parts.getEditionCode(),
                        parts.getAtlCode(), parts.getAcbCode(), parts.getDeveloperCode(), parts.getProductCode(),
                        newVersionCode, parts.getIcsCode(), parts.getAdditionalSoftwareCode(),
                        parts.getCertifiedDateCode());
                if (!chplProductNumberUtil.isUnique(potentialChplNumber)) {
                    throw new EntityCreationException(msgUtil.getMessage("version.split.duplicateChplId",
                            chplNumber, potentialChplNumber));
                }
                if (!validationUtils.chplNumberPartIsValid(potentialChplNumber,
                        ChplProductNumberUtil.VERSION_CODE_INDEX, ChplProductNumberUtil.VERSION_CODE_REGEX)) {
                    throw new EntityCreationException(msgUtil.getMessage("listing.badVersionCodeChars",
                            ChplProductNumberUtil.VERSION_CODE_LENGTH));
                }
                affectedListing.setVersionCode(newVersionCode);
            }
            affectedListing.setProductVersionId(createdVersion.getId());

            // do the update and add activity
            cpDao.update(affectedListing);
            CertifiedProductSearchDetails afterListing = cpdManager.getCertifiedProductDetailsNoCache(affectedListingId);
            if (!StringUtils.equals(beforeListing.getChplProductNumber(), afterListing.getChplProductNumber())) {
                activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, beforeListing.getId(),
                    "Updated certified product " + afterListing.getChplProductNumber() + ".", beforeListing,
                    afterListing);
            }
        }

        //the split is complete - log split activity
        //getting the original version object from the db to make sure it's all filled in
        ProductVersionDTO origVersion = getById(oldVersion.getId());
        ProductVersionDTO afterVersion = getById(createdVersion.getId());
        List<ProductVersionDTO> splitVersions = new ArrayList<ProductVersionDTO>();
        splitVersions.add(origVersion);
        splitVersions.add(afterVersion);
        activityManager.addActivity(ActivityConcept.VERSION, afterVersion.getId(),
                "Split version " + origVersion.getVersion() + " into "
                        + origVersion.getVersion() + " and " + afterVersion.getVersion(),
                origVersion, splitVersions);

        return afterVersion;
    }

    private void runNewVersionValidations(ProductVersion version, Long productId, List<Long> versionsBeingMerged) throws ValidationException {
        List<ValidationRule<VersionValidationContext>> rules = new ArrayList<ValidationRule<VersionValidationContext>>();
        rules.add(versionValidationFactory.getRule(VersionValidationFactory.NAME));
        Set<String> validationErrors = runValidations(rules, version, productId, versionsBeingMerged);
        if (!CollectionUtils.isEmpty(validationErrors)) {
            LOGGER.error("New version validation errors: \n" + validationErrors);
            throw new ValidationException(validationErrors);
        }
    }

    private void runExistingVersionValidations(ProductVersion version, Long productId) throws ValidationException {
        List<ValidationRule<VersionValidationContext>> rules = new ArrayList<ValidationRule<VersionValidationContext>>();
        rules.add(versionValidationFactory.getRule(VersionValidationFactory.NAME));
        Set<String> validationErrors = runValidations(rules, version, productId, null);
        if (!CollectionUtils.isEmpty(validationErrors)) {
            LOGGER.error("Existing version validation errors: \n" + validationErrors);
            throw new ValidationException(validationErrors);
        }
    }

    private Set<String> runValidations(List<ValidationRule<VersionValidationContext>> rules,
            ProductVersion version, Long productId, List<Long> versionsBeingMerged) {
        Set<String> errorMessages = new HashSet<String>();
        VersionValidationContext context
            = new VersionValidationContext(versionDao, version, productId, versionsBeingMerged, msgUtil);

        for (ValidationRule<VersionValidationContext> rule : rules) {
            if (!rule.isValid(context)) {
                errorMessages.addAll(rule.getMessages());
            }
        }
        return errorMessages;
    }
}
