package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
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
import gov.healthit.chpl.domain.concept.ActivityConcept;
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
import gov.healthit.chpl.validation.listing.reviewer.ChplNumberReviewer;

@Service
public class ProductManagerImpl implements ProductManager {
    private static final Logger LOGGER = LogManager.getLogger(ProductManagerImpl.class);

    @Autowired
    private MessageSource messageSource;
    @Autowired
    private ProductDAO productDao;
    @Autowired
    private ProductVersionDAO versionDao;
    @Autowired
    private DeveloperDAO devDao;
    @Autowired
    private CertifiedProductDAO cpDao;
    @Autowired
    private CertifiedProductDetailsManager cpdManager;
    @Autowired
    private ChplNumberReviewer chplNumberReviewer;
    @Autowired
    private ResourcePermissions resourcePermissions;
    @Autowired
    private ActivityManager activityManager;

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getById(Long id) throws EntityRetrievalException {
        return productDao.getById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAll() {
        return productDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getByDeveloper(Long developerId) {
        return productDao.getByDeveloper(developerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getByDevelopers(List<Long> developerIds) {
        return productDao.getByDevelopers(developerIds);
    }

    @Override
    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.ProductDomainPermissions).CREATE)")
    public ProductDTO create(ProductDTO dto)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        // check that the developer of this product is Active
        if (dto.getDeveloperId() == null) {
            throw new EntityCreationException("Cannot create a product without a developer ID.");
        }

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
        } else if (!currDevStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString())) {
            String msg = "The product " + dto.getName() + " cannot be created since the developer " + dev.getName()
                    + " has a status of " + currDevStatus.getStatus().getStatusName();
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        }

        ProductDTO result = productDao.create(dto);
        String activityMsg = "Product " + dto.getName() + " was created.";
        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PRODUCT, result.getId(), activityMsg, null,
                result);
        return getById(result.getId());
    }

    @Override
    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.ProductDomainPermissions).UPDATE)")
    public ProductDTO update(ProductDTO dto)
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
        } else if (!currDevStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString())) {
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
        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PRODUCT, result.getId(), activityMsg, beforeDTO,
                result);
        return result;

    }

    @Override
    @Transactional(readOnly = false)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public ProductDTO merge(List<Long> productIdsToMerge, ProductDTO toCreate)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {

        List<ProductDTO> beforeProducts = new ArrayList<ProductDTO>();
        for (Long productId : productIdsToMerge) {
            beforeProducts.add(productDao.getById(productId));
        }

        for (ProductDTO beforeProduct : beforeProducts) {
            Long devId = beforeProduct.getDeveloperId();
            DeveloperDTO dev = devDao.getById(devId);
            DeveloperStatusEventDTO currDevStatus = dev.getStatus();
            if (currDevStatus == null || currDevStatus.getStatus() == null) {
                String msg = "The product " + beforeProduct.getName()
                        + " cannot be merged since the status of developer " + dev.getName() + " cannot be determined.";
                LOGGER.error(msg);
                throw new EntityCreationException(msg);
            } else if (!currDevStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString())) {
                String msg = "The product " + beforeProduct.getName() + " cannot be merged since the developer "
                        + dev.getName() + " has a status of " + currDevStatus.getStatus().getStatusName();
                LOGGER.error(msg);
                throw new EntityCreationException(msg);
            }
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
        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PRODUCT, createdProduct.getId(), activityMsg,
                beforeProducts, createdProduct);

        return createdProduct;
    }

    @Override
    @Transactional(rollbackFor = {
            EntityRetrievalException.class, EntityCreationException.class, JsonProcessingException.class,
            AccessDeniedException.class
    })
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.ProductDomainPermissions).SPLIT)")
    public ProductDTO split(ProductDTO oldProduct, ProductDTO newProduct, String newProductCode,
            List<ProductVersionDTO> newProductVersions)
            throws AccessDeniedException, EntityRetrievalException, EntityCreationException, JsonProcessingException {
        // what ACB does the user have??
        List<CertificationBodyDTO> allowedAcbs = resourcePermissions.getAllAcbsForCurrentUser();

        // create the new product and log activity
        // this method checks that the related developer is Active and will
        // throw an exception if they aren't
        newProduct = create(newProduct);

        // re-assign versions to the new product and log activity for each
        List<Long> affectedVersionIds = new ArrayList<Long>();
        for (ProductVersionDTO affectedVersion : newProductVersions) {
            // get before and after for activity; update product owner
            ProductVersionDTO beforeVersion = versionDao.getById(affectedVersion.getId());
            affectedVersion.setProductId(newProduct.getId());
            affectedVersion.setProductName(newProduct.getName());
            versionDao.update(affectedVersion);
            ProductVersionDTO afterVersion = versionDao.getById(affectedVersion.getId());
            activityManager.addActivity(
                    ActivityConcept.ACTIVITY_CONCEPT_VERSION, afterVersion.getId(), "Product Version "
                            + afterVersion.getVersion() + " product owner updated to " + afterVersion.getProductName(),
                    beforeVersion, afterVersion);
            affectedVersionIds.add(affectedVersion.getId());
        }

        // update product code on all associated certified products and log
        // activity for each
        List<CertifiedProductDTO> affectedCps = cpDao.getByVersionIds(affectedVersionIds);
        for (CertifiedProductDTO affectedCp : affectedCps) {
            // have to get the cpdetails for before and after code update
            // because that is object sent into activity reports
            CertifiedProductSearchDetails beforeProduct = cpdManager.getCertifiedProductDetails(affectedCp.getId());
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
                        "Access is denied to update certified product " + beforeProduct.getChplProductNumber()
                                + " because it is owned by " + beforeProduct.getCertifyingBody().get("name") + ".");
            }

            // make sure the updated CHPL product number is valid
            String chplNumber = beforeProduct.getChplProductNumber();
            String[] splitChplNumber = chplNumber.split("\\.");
            if (splitChplNumber.length > 1) {
                String potentialChplNumber = splitChplNumber[0] + "." + splitChplNumber[1] + "." + splitChplNumber[2]
                        + "." + splitChplNumber[3] + "." + newProductCode + "." + splitChplNumber[5] + "."
                        + splitChplNumber[6] + "." + splitChplNumber[7] + "." + splitChplNumber[8];
                if (!chplNumberReviewer.validateUniqueId(potentialChplNumber)) {
                    throw new EntityCreationException("Cannot update certified product " + chplNumber + " to "
                            + potentialChplNumber + " because a certified product with that CHPL ID already exists.");
                }
                if (!chplNumberReviewer.validateProductCodeCharacters(potentialChplNumber)) {
                    throw new EntityCreationException(String.format(
                            messageSource.getMessage(new DefaultMessageSourceResolvable("listing.badProductCodeChars"),
                                    LocaleContextHolder.getLocale()),
                            CertifiedProductDTO.PRODUCT_CODE_LENGTH));
                }

                affectedCp.setProductCode(newProductCode);
            }

            // do the update and add activity
            cpDao.update(affectedCp);
            CertifiedProductSearchDetails afterProduct = cpdManager.getCertifiedProductDetails(affectedCp.getId());
            activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, beforeProduct.getId(),
                    "Updated certified product " + afterProduct.getChplProductNumber() + ".", beforeProduct,
                    afterProduct);
        }

        return getById(newProduct.getId());
    }
}
