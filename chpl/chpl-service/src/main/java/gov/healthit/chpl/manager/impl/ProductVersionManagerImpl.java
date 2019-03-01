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
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.ProductVersionEntity;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.ProductVersionManager;

@Service
public class ProductVersionManagerImpl implements ProductVersionManager {
    private static final Logger LOGGER = LogManager.getLogger(ProductVersionManagerImpl.class);
    @Autowired
    private ProductVersionDAO dao;
    @Autowired
    private DeveloperDAO devDao;
    @Autowired
    private ProductDAO prodDao;
    @Autowired
    private CertifiedProductDAO cpDao;
    @Autowired
    private ActivityManager activityManager;

    @Override
    @Transactional(readOnly = true)
    public ProductVersionDTO getById(Long id) throws EntityRetrievalException {
        return dao.getById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVersionDTO> getAll() {
        return dao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVersionDTO> getByProduct(Long productId) {
        return dao.getByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVersionDTO> getByProducts(List<Long> productIds) {
        return dao.getByProductIds(productIds);
    }

    @Override
    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT_VERSION, "
            + "T(gov.healthit.chpl.permissions.domains.ProductVersionDomainPermissions).CREATE)")
    public ProductVersionDTO create(ProductVersionDTO dto)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        // check that the developer of this version is Active
        if (dto.getProductId() == null) {
            throw new EntityCreationException("Cannot create a version without a product ID.");
        }
        ProductDTO prod = prodDao.getById(dto.getProductId());
        if (prod == null) {
            throw new EntityRetrievalException("Cannot find product with id " + dto.getProductId());
        }
        DeveloperDTO dev = devDao.getById(prod.getDeveloperId());
        if (dev == null) {
            throw new EntityRetrievalException("Cannot find developer with id " + prod.getDeveloperId());
        }
        DeveloperStatusEventDTO currDevStatus = dev.getStatus();
        if (currDevStatus == null || currDevStatus.getStatus() == null) {
            String msg = "The version " + dto.getVersion() + " cannot be created since the status of developer "
                    + dev.getName() + " cannot be determined.";
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        } else if (!currDevStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString())) {
            String msg = "The version " + dto.getVersion() + " cannot be created since the developer " + dev.getName()
                    + " has a status of " + currDevStatus.getStatus().getStatusName();
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        }

        ProductVersionDTO created = dao.create(dto);
        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_VERSION, created.getId(),
                "Product Version " + dto.getVersion() + " added for product " + dto.getProductId(), null, created);
        return created;
    }

    @Override
    @Transactional(readOnly = false)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PRODUCT_VERSION, "
            + "T(gov.healthit.chpl.permissions.domains.ProductVersionDomainPermissions).UPDATE)")
    public ProductVersionDTO update(ProductVersionDTO dto)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {

        ProductVersionDTO before = dao.getById(dto.getId());
        // check that the developer of this version is Active
        DeveloperDTO dev = devDao.getByVersion(before.getId());
        if (dev == null) {
            throw new EntityRetrievalException("Cannot find developer of version id " + before.getId());
        }
        DeveloperStatusEventDTO currDevStatus = dev.getStatus();
        if (currDevStatus == null || currDevStatus.getStatus() == null) {
            String msg = "The version " + before.getVersion() + " cannot be updated since the status of developer "
                    + dev.getName() + " cannot be determined.";
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        } else if (!currDevStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString())) {
            String msg = "The version " + before.getVersion() + " cannot be updated since the developer "
                    + dev.getName() + " has a status of " + currDevStatus.getStatus().getStatusName();
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        }

        ProductVersionEntity result = dao.update(dto);
        ProductVersionDTO after = new ProductVersionDTO(result);
        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_VERSION, after.getId(),
                "Product Version " + dto.getVersion() + " updated for product " + dto.getProductId(), before, after);
        return after;
    }

    @Override
    @Transactional(rollbackFor = {
            EntityRetrievalException.class, EntityCreationException.class, JsonProcessingException.class,
            AccessDeniedException.class
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public ProductVersionDTO merge(List<Long> versionIdsToMerge, ProductVersionDTO toCreate)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {

        List<ProductVersionDTO> beforeVersions = new ArrayList<ProductVersionDTO>();
        for (Long versionId : versionIdsToMerge) {
            beforeVersions.add(dao.getById(versionId));
        }

        // make sure all versions come from an Active developer
        for (ProductVersionDTO version : beforeVersions) {
            // check that the developer of this version is Active
            DeveloperDTO dev = devDao.getByVersion(version.getId());
            if (dev == null) {
                throw new EntityRetrievalException("Cannot find developer of version id " + version.getId());
            }
            DeveloperStatusEventDTO currDevStatus = dev.getStatus();
            if (currDevStatus == null || currDevStatus.getStatus() == null) {
                String msg = "The version " + version.getVersion() + " cannot be merged since the status of developer "
                        + dev.getName() + " cannot be determined.";
                LOGGER.error(msg);
                throw new EntityCreationException(msg);
            } else if (!currDevStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString())) {
                String msg = "The version " + version.getVersion() + " cannot be merged since the developer "
                        + dev.getName() + " has a status of " + currDevStatus.getStatus().getStatusName();
                LOGGER.error(msg);
                throw new EntityCreationException(msg);
            }
        }

        ProductVersionDTO createdVersion = dao.create(toCreate);

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
            dao.delete(versionId);
        }

        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_VERSION, createdVersion.getId(),
                "Merged " + versionIdsToMerge.size() + " versions into '" + createdVersion.getVersion() + "'.",
                beforeVersions, createdVersion);

        return createdVersion;
    }
}
