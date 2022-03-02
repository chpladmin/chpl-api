package gov.healthit.chpl.manager;

import java.util.ArrayList;
import java.util.List;

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
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.ProductVersionEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
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
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ValidationUtils validationUtils;

    @Autowired
    @SuppressWarnings({"checkstyle:parameternumber"})
    public ProductVersionManager(ProductVersionDAO versionDao, DeveloperDAO devDao,
            ProductDAO prodDao, CertifiedProductDAO cpDao, ActivityManager activityManager,
            CertifiedProductDetailsManager cpdManager, ResourcePermissions resourcePermissions,
            ErrorMessageUtil msgUtil, ChplProductNumberUtil chplProductNumberUtil,
            ValidationUtils validationUtils) {
        this.versionDao = versionDao;
        this.devDao = devDao;
        this.prodDao = prodDao;
        this.cpDao = cpDao;
        this.activityManager = activityManager;
        this.cpdManager = cpdManager;
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.validationUtils = validationUtils;

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
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH
    }, allEntries = true)
    public Long create(Long productId, ProductVersion version)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
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
        DeveloperStatus currDevStatus = dev.getStatus();
        if (currDevStatus == null || currDevStatus.getStatus() == null) {
            String msg = "The version " + version.getVersion() + " cannot be created since the status of developer "
                    + dev.getName() + " cannot be determined.";
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        }

        Long versionId = versionDao.create(productId, version);
        version.setVersionId(versionId);
        ProductVersionDTO createdVersionDto = versionDao.getById(versionId);
        activityManager.addActivity(ActivityConcept.VERSION, versionId,
                "Product Version " + version.getVersion() + " added for product " + productId, null, createdVersionDto);
        return versionId;
    }

    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT_VERSION, "
            + "T(gov.healthit.chpl.permissions.domains.ProductVersionDomainPermissions).CREATE)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH
    }, allEntries = true)
    public ProductVersionDTO create(ProductVersionDTO dto)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
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
        DeveloperStatus currDevStatus = dev.getStatus();
        if (currDevStatus == null || currDevStatus.getStatus() == null) {
            String msg = "The version " + dto.getVersion() + " cannot be created since the status of developer "
                    + dev.getName() + " cannot be determined.";
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        }

        ProductVersionDTO created = versionDao.create(dto);
        activityManager.addActivity(ActivityConcept.VERSION, created.getId(),
                "Product Version " + dto.getVersion() + " added for product " + dto.getProductId(), null, created);
        return created;
    }

    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT_VERSION, "
            + "T(gov.healthit.chpl.permissions.domains.ProductVersionDomainPermissions).UPDATE, #dto)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH
    }, allEntries = true)
    public ProductVersionDTO update(ProductVersionDTO dto)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {

        ProductVersionDTO before = versionDao.getById(dto.getId());
        // check that the developer of this version is Active
        Developer dev = devDao.getByVersion(before.getId());
        if (dev == null) {
            throw new EntityRetrievalException("Cannot find developer of version id " + before.getId());
        }
        DeveloperStatus currDevStatus = dev.getStatus();
        if (currDevStatus == null || currDevStatus.getStatus() == null) {
            String msg = "The version " + before.getVersion() + " cannot be updated since the status of developer "
                    + dev.getName() + " cannot be determined.";
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        }

        ProductVersionEntity result = versionDao.update(dto);
        ProductVersionDTO after = new ProductVersionDTO(result);
        activityManager.addActivity(ActivityConcept.VERSION, after.getId(),
                "Product Version " + dto.getVersion() + " updated for product " + dto.getProductId(), before, after);
        return after;
    }


    @Transactional(rollbackFor = {
            EntityRetrievalException.class, EntityCreationException.class, JsonProcessingException.class,
            AccessDeniedException.class
    })
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT_VERSION, "
            + "T(gov.healthit.chpl.permissions.domains.ProductVersionDomainPermissions).MERGE, #versionIdsToMerge)")
    @CacheEvict(value = {
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH
    }, allEntries = true)
    public ProductVersionDTO merge(List<Long> versionIdsToMerge, ProductVersionDTO toCreate)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {

        List<ProductVersionDTO> beforeVersions = new ArrayList<ProductVersionDTO>();
        for (Long versionId : versionIdsToMerge) {
            beforeVersions.add(versionDao.getById(versionId));
        }

        ProductVersionDTO createdVersion = versionDao.create(toCreate);

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
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH
    }, allEntries = true)
    public ProductVersionDTO split(ProductVersionDTO oldVersion, ProductVersionDTO newVersion,
            String newVersionCode, List<Long> newVersionListingIds)
            throws AccessDeniedException, EntityRetrievalException, EntityCreationException, JsonProcessingException {
        // what ACB does the user have??
        List<CertificationBodyDTO> allowedAcbs = resourcePermissions.getAllAcbsForCurrentUser();

        // create the new version and log activity
        // this method checks that the related developer is Active and will
        // throw an exception if they aren't
        ProductVersionDTO createdVersion = create(newVersion);

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
            CertifiedProductSearchDetails afterListing = cpdManager.getCertifiedProductDetails(affectedListingId);
            activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, beforeListing.getId(),
                    "Updated certified product " + afterListing.getChplProductNumber() + ".", beforeListing,
                    afterListing);
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
}
