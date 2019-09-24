package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductSummaryDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import gov.healthit.chpl.entity.listing.CertifiedProductEntity;
import gov.healthit.chpl.entity.listing.CertifiedProductSummaryEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.urlStatus.UrlType;
import gov.healthit.chpl.util.AuthUtil;

/**
 * Certified Product DAO.
 *
 * @author alarned
 *
 */
@Repository(value = "certifiedProductDAO")
public class CertifiedProductDAOImpl extends BaseDAOImpl implements CertifiedProductDAO {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductDAOImpl.class);
    private static final int CHPL_ID_LENGTH = 9;
    @Autowired
    private MessageSource messageSource;

    @Override
    @Transactional(readOnly = false)
    public CertifiedProductDTO create(final CertifiedProductDTO dto) throws EntityCreationException {

        CertifiedProductEntity entity = null;
        try {
            if (dto.getId() != null) {
                entity = this.getEntityById(dto.getId());
            }
        } catch (final EntityRetrievalException e) {
            throw new EntityCreationException(e);
        }

        if (entity != null) {
            throw new EntityCreationException("A product with this ID already exists.");
        } else {

            entity = new CertifiedProductEntity();
            entity.setPendingCertifiedProductId(dto.getPendingCertifiedProductId());
            entity.setAcbCertificationId(dto.getAcbCertificationId());
            // new products will always have null numbers
            entity.setChplProductNumber(null);
            entity.setProductCode(dto.getProductCode());
            entity.setVersionCode(dto.getVersionCode());
            entity.setAdditionalSoftwareCode(dto.getAdditionalSoftwareCode());
            entity.setIcsCode(dto.getIcsCode().toString());
            entity.setCertifiedDateCode(dto.getCertifiedDateCode());
            entity.setPracticeTypeId(dto.getPracticeTypeId());
            entity.setProductClassificationTypeId(dto.getProductClassificationTypeId());
            entity.setReportFileLocation(dto.getReportFileLocation());
            entity.setSedIntendedUserDescription(dto.getSedIntendedUserDescription());
            entity.setSedTestingEnd(dto.getSedTestingEnd());
            entity.setSedReportFileLocation(dto.getSedReportFileLocation());
            entity.setProductAdditionalSoftware(dto.getProductAdditionalSoftware());
            entity.setOtherAcb(dto.getOtherAcb());
            entity.setIcs(dto.getIcs());
            entity.setSedTesting(dto.getSedTesting());
            entity.setQmsTesting(dto.getQmsTesting());
            entity.setAccessibilityCertified(dto.getAccessibilityCertified());
            entity.setTransparencyAttestationUrl(dto.getTransparencyAttestationUrl());

            if (dto.getCertificationBodyId() != null) {
                entity.setCertificationBodyId(dto.getCertificationBodyId());
            }

            if (dto.getCertificationEditionId() != null) {
                entity.setCertificationEditionId(dto.getCertificationEditionId());
            }

            if (dto.getProductVersionId() != null) {
                entity.setProductVersionId(dto.getProductVersionId());
            }

            if (dto.getCreationDate() != null) {
                entity.setCreationDate(dto.getCreationDate());
            } else {
                entity.setCreationDate(new Date());
            }

            if (dto.getDeleted() != null) {
                entity.setDeleted(dto.getDeleted());
            } else {
                entity.setDeleted(false);
            }

            if (dto.getLastModifiedDate() != null) {
                entity.setLastModifiedDate(dto.getLastModifiedDate());
            } else {
                entity.setLastModifiedDate(new Date());
            }

            if (dto.getLastModifiedUser() != null) {
                entity.setLastModifiedUser(dto.getLastModifiedUser());
            } else {
                entity.setLastModifiedUser(AuthUtil.getAuditId());
            }

            try {
                create(entity);
            } catch (Exception ex) {
                String msg = String
                        .format(messageSource.getMessage(new DefaultMessageSourceResolvable("listing.badListingData"),
                                LocaleContextHolder.getLocale()), dto.getChplProductNumber(), ex.getMessage());
                LOGGER.error(msg, ex);
                throw new EntityCreationException(msg);
            }
            return new CertifiedProductDTO(entity);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public CertifiedProductDTO update(final CertifiedProductDTO dto) throws EntityRetrievalException {

        CertifiedProductEntity entity = getEntityById(dto.getId());
        entity.setAcbCertificationId(dto.getAcbCertificationId());
        entity.setProductCode(dto.getProductCode());
        entity.setVersionCode(dto.getVersionCode());
        entity.setIcsCode(dto.getIcsCode() == null ? null : dto.getIcsCode().toString());
        entity.setAdditionalSoftwareCode(dto.getAdditionalSoftwareCode());
        entity.setCertifiedDateCode(dto.getCertifiedDateCode());
        entity.setPracticeTypeId(dto.getPracticeTypeId());
        entity.setProductClassificationTypeId(dto.getProductClassificationTypeId());
        entity.setReportFileLocation(dto.getReportFileLocation());
        entity.setSedReportFileLocation(dto.getSedReportFileLocation());
        entity.setSedIntendedUserDescription(dto.getSedIntendedUserDescription());
        entity.setSedTestingEnd(dto.getSedTestingEnd());
        entity.setProductAdditionalSoftware(dto.getProductAdditionalSoftware());
        entity.setOtherAcb(dto.getOtherAcb());
        entity.setIcs(dto.getIcs());
        entity.setSedTesting(dto.getSedTesting());
        entity.setQmsTesting(dto.getQmsTesting());
        entity.setAccessibilityCertified(dto.getAccessibilityCertified());
        entity.setTransparencyAttestationUrl(dto.getTransparencyAttestationUrl());
        entity.setCertificationBodyId(dto.getCertificationBodyId());
        entity.setCertificationEditionId(dto.getCertificationEditionId());
        entity.setProductVersionId(dto.getProductVersionId());

        entity.setLastModifiedDate(new Date());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        try {
            update(entity);
        } catch (Exception ex) {
            String msg = String
                    .format(messageSource.getMessage(new DefaultMessageSourceResolvable("listing.badListingData"),
                            LocaleContextHolder.getLocale()), dto.getChplProductNumber(), ex.getMessage());
            LOGGER.error(msg, ex);
            throw new EntityRetrievalException(msg);
        }
        return new CertifiedProductDTO(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(final Long productId) {
        // TODO: How to delete this without leaving orphans
        Query query = entityManager.createQuery(
                "UPDATE CertifiedProductEntity SET deleted = true WHERE certified_product_id = :productid");
        query.setParameter("productid", productId);
        query.executeUpdate();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> findAll() {
        List<CertifiedProductDetailsEntity> entities = entityManager
                .createQuery("from CertifiedProductDetailsEntity where (NOT deleted = true) ",
                        CertifiedProductDetailsEntity.class)
                .getResultList();

        List<CertifiedProductDetailsDTO> products = new ArrayList<>();

        for (CertifiedProductDetailsEntity entity : entities) {
            CertifiedProductDetailsDTO product = new CertifiedProductDetailsDTO(entity);
            products.add(product);
        }
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> findByDeveloperId(final Long developerId) {
        Query query = entityManager.createQuery("SELECT cpd " + "FROM CertifiedProductDetailsEntity cpd "
                + "WHERE (NOT deleted = true) " + "AND cpd.developerId = :developerId ",
                CertifiedProductDetailsEntity.class);
        query.setParameter("developerId", developerId);
        List<CertifiedProductDetailsEntity> entities = query.getResultList();
        List<CertifiedProductDetailsDTO> products = new ArrayList<>(entities.size());

        for (CertifiedProductDetailsEntity entity : entities) {
            CertifiedProductDetailsDTO product = new CertifiedProductDetailsDTO(entity);
            products.add(product);
        }
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> findByEdition(final String edition) {
        Query query = entityManager.createQuery("SELECT cpd " + "FROM CertifiedProductDetailsEntity cpd "
                + "WHERE (NOT deleted = true) " + "AND cpd.year = :edition ", CertifiedProductDetailsEntity.class);
        query.setParameter("edition", edition.trim());
        List<CertifiedProductDetailsEntity> entities = query.getResultList();
        List<CertifiedProductDetailsDTO> products = new ArrayList<>(entities.size());

        for (CertifiedProductDetailsEntity entity : entities) {
            CertifiedProductDetailsDTO product = new CertifiedProductDetailsDTO(entity);
            products.add(product);
        }
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> findWithSurveillance() {

        List<CertifiedProductDetailsEntity> entities = entityManager.createQuery(
                "SELECT DISTINCT cp " + "FROM CertifiedProductDetailsEntity cp, SurveillanceEntity surv "
                        + "WHERE surv.certifiedProductId = cp.id " + "AND (NOT surv.deleted = true)",
                CertifiedProductDetailsEntity.class).getResultList();

        List<CertifiedProductDetailsDTO> products = new ArrayList<>();

        for (CertifiedProductDetailsEntity entity : entities) {
            CertifiedProductDetailsDTO product = new CertifiedProductDetailsDTO(entity);
            products.add(product);
        }
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> findWithInheritance() {

        List<CertifiedProductDetailsEntity> entities = entityManager.createQuery(
                "SELECT DISTINCT cp " + "FROM CertifiedProductDetailsEntity cp "
                        + "WHERE (icsCode != '0' OR ics = true)",
                CertifiedProductDetailsEntity.class).getResultList();

        List<CertifiedProductDetailsDTO> products = new ArrayList<>();
        for (CertifiedProductDetailsEntity entity : entities) {
            CertifiedProductDetailsDTO product = new CertifiedProductDetailsDTO(entity);
            products.add(product);
        }
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public CertifiedProductDTO getById(final Long productId) throws EntityRetrievalException {

        CertifiedProductDTO dto = null;
        CertifiedProductEntity entity = getEntityById(productId);

        if (entity != null) {
            dto = new CertifiedProductDTO(entity);
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public CertifiedProductSummaryDTO getSummaryById(final Long listingId) throws EntityRetrievalException {
        Query query = entityManager.createQuery("SELECT cp "
                + "FROM CertifiedProductSummaryEntity cp "
                + "WHERE id = :id "
                + "AND deleted = false",
                CertifiedProductSummaryEntity.class);
        query.setParameter("id", listingId);
        List<CertifiedProductSummaryEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("listing.notFound"),
                    LocaleContextHolder.getLocale()));
            throw new EntityRetrievalException(msg);
        }
        return new CertifiedProductSummaryDTO(result.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public CertifiedProductDTO getByChplNumber(final String chplProductNumber) {
        CertifiedProductDTO dto = null;
        CertifiedProductEntity entity = getEntityByChplNumber(chplProductNumber);

        if (entity != null) {
            dto = new CertifiedProductDTO(entity);
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public CertifiedProductDetailsDTO getByChplUniqueId(final String chplUniqueId) throws EntityRetrievalException {
        CertifiedProductDetailsDTO dto = null;
        String[] idParts = chplUniqueId.split("\\.");
        if (idParts.length < CHPL_ID_LENGTH) {
            throw new EntityRetrievalException("CHPL ID must have 9 parts separated by '.'");
        }
        CertifiedProductDetailsEntity entity = getEntityByUniqueIdParts(idParts[0], idParts[1], idParts[2], idParts[3],
                idParts[4], idParts[5], idParts[6], idParts[7], idParts[8]);

        if (entity != null) {
            dto = new CertifiedProductDetailsDTO(entity);
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDTO> getByVersionIds(final List<Long> versionIds) {
        Query query = entityManager.createQuery(
                "from CertifiedProductEntity where (NOT deleted = true) and product_version_id IN :idList",
                CertifiedProductEntity.class);
        query.setParameter("idList", versionIds);
        List<CertifiedProductEntity> results = query.getResultList();

        List<CertifiedProductDTO> dtoResults = new ArrayList<CertifiedProductDTO>(results.size());
        for (CertifiedProductEntity result : results) {
            dtoResults.add(new CertifiedProductDTO(result));
        }
        return dtoResults;
    }

    @Override
    @Transactional(readOnly = true)
    public Date getConfirmDate(final Long listingId) {
        Date confirmDate = null;
        CertifiedProductEntity entity = null;

        try {
            entity = getEntityById(listingId);
            if (entity != null) {
                confirmDate = entity.getCreationDate();
            }
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not get entity with ID " + listingId, ex);
        }

        return confirmDate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDTO> getCertifiedProductsForDeveloper(final Long developerId) {
        Query getCertifiedProductsQuery = entityManager.createQuery(
                "FROM CertifiedProductEntity cpe, ProductVersionEntity pve," + "ProductEntity pe, DeveloperEntity ve "
                        + "WHERE (NOT cpe.deleted = true) " + "AND cpe.productVersion = pve.id "
                        + "AND pve.productId = pe.id " + "AND ve.id = pe.developerId " + "AND ve.id = :developerId",
                CertifiedProductEntity.class);
        getCertifiedProductsQuery.setParameter("developerId", developerId);
        List<CertifiedProductEntity> results = getCertifiedProductsQuery.getResultList();

        List<CertifiedProductDTO> dtoResults = new ArrayList<CertifiedProductDTO>(results.size());
        for (CertifiedProductEntity result : results) {
            dtoResults.add(new CertifiedProductDTO(result));
        }
        return dtoResults;
    }

    @Override
    @Transactional(readOnly = true)
    public CertifiedProductDetailsDTO getDetailsById(final Long cpId) throws EntityRetrievalException {
        Query query = entityManager.createQuery("from CertifiedProductDetailsEntity deets "
                + "LEFT OUTER JOIN FETCH deets.product " + "where deets.id = :cpId",
                CertifiedProductDetailsEntity.class);
        query.setParameter("cpId", cpId);
        List<CertifiedProductDetailsEntity> results = query.getResultList();

        if (results == null || results.size() == 0) {
            return null;
        }
        return new CertifiedProductDetailsDTO(results.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getDetailsByIds(final List<Long> productIds)
            throws EntityRetrievalException {
        if ((null == productIds) || (productIds.size() == 0)) {
            return new ArrayList<CertifiedProductDetailsDTO>();
        }

        Query prodQuery = entityManager.createQuery("from CertifiedProductDetailsEntity deets "
                + "LEFT OUTER JOIN FETCH deets.product " + "WHERE deets.id in (:productIds) "
                + " AND deets.deleted = false",
                CertifiedProductDetailsEntity.class);
        prodQuery.setParameter("productIds", productIds);
        List<CertifiedProductDetailsEntity> results = prodQuery.getResultList();

        List<CertifiedProductDetailsDTO> dtos = null;
        if (null != results) {
            dtos = new ArrayList<CertifiedProductDetailsDTO>(results.size());
            for (CertifiedProductDetailsEntity entity : results) {
                CertifiedProductDetailsDTO dto = new CertifiedProductDetailsDTO(entity);
                dtos.add(dto);
            }
        }

        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getDetailsByChplNumbers(final List<String> chplProductNumbers) {
        if ((null == chplProductNumbers) || (chplProductNumbers.size() == 0)) {
            return new ArrayList<CertifiedProductDetailsDTO>();
        }

        Query prodQuery = entityManager.createQuery(
                "from CertifiedProductDetailsEntity deets " + "LEFT OUTER JOIN FETCH deets.product "
                        + "WHERE deets.chplProductNumber in (:chplProductNumbers) ",
                CertifiedProductDetailsEntity.class);
        prodQuery.setParameter("chplProductNumbers", chplProductNumbers);
        List<CertifiedProductDetailsEntity> results = prodQuery.getResultList();

        List<CertifiedProductDetailsDTO> dtos = null;
        if (null != results) {
            dtos = new ArrayList<CertifiedProductDetailsDTO>(results.size());
            for (CertifiedProductDetailsEntity entity : results) {
                CertifiedProductDetailsDTO dto = new CertifiedProductDetailsDTO(entity);
                dtos.add(dto);
            }
        }

        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getDetailsByVersionId(final Long versionId) {
        Query query = entityManager.createQuery("from CertifiedProductDetailsEntity deets "
                + "LEFT OUTER JOIN FETCH deets.product " + "WHERE deets.productVersionId = :versionId",
                CertifiedProductDetailsEntity.class);
        query.setParameter("versionId", versionId);
        List<CertifiedProductDetailsEntity> results = query.getResultList();

        List<CertifiedProductDetailsDTO> dtoResults = new ArrayList<CertifiedProductDetailsDTO>();
        for (CertifiedProductDetailsEntity result : results) {
            dtoResults.add(new CertifiedProductDetailsDTO(result));
        }
        return dtoResults;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getDetailsByProductId(final Long productId) {
        Query query = entityManager.createQuery("from CertifiedProductDetailsEntity deets "
                + "LEFT OUTER JOIN FETCH deets.product " + "WHERE deets.productId = :productId",
                CertifiedProductDetailsEntity.class);
        query.setParameter("productId", productId);
        List<CertifiedProductDetailsEntity> results = query.getResultList();

        List<CertifiedProductDetailsDTO> dtoResults = new ArrayList<CertifiedProductDetailsDTO>();
        for (CertifiedProductDetailsEntity result : results) {
            dtoResults.add(new CertifiedProductDetailsDTO(result));
        }
        return dtoResults;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getDetailsByAcbIds(final List<Long> acbIds) {
        Query query = entityManager.createQuery(
                "from CertifiedProductDetailsEntity where (NOT deleted = true) and certification_body_id IN :idList",
                CertifiedProductDetailsEntity.class);
        query.setParameter("idList", acbIds);
        List<CertifiedProductDetailsEntity> results = query.getResultList();

        List<CertifiedProductDetailsDTO> dtoResults = new ArrayList<CertifiedProductDetailsDTO>(results.size());
        for (CertifiedProductDetailsEntity result : results) {
            dtoResults.add(new CertifiedProductDetailsDTO(result));
        }
        return dtoResults;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getDetailsByVersionAndAcbIds(final Long versionId,
            final List<Long> acbIds) {
        Query query = entityManager.createQuery(
                "from CertifiedProductDetailsEntity where (NOT deleted = true) and "
                        + "certification_body_id IN :idList and product_version_id = :versionId",
                CertifiedProductDetailsEntity.class);
        query.setParameter("idList", acbIds);
        query.setParameter("versionId", versionId);
        List<CertifiedProductDetailsEntity> results = query.getResultList();

        List<CertifiedProductDetailsDTO> dtoResults = new ArrayList<CertifiedProductDetailsDTO>(results.size());
        for (CertifiedProductDetailsEntity result : results) {
            dtoResults.add(new CertifiedProductDetailsDTO(result));
        }
        return dtoResults;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertifiedProductSummaryDTO> getSummaryByUrl(final String url, final UrlType urlType) {
        String queryStr = "SELECT cp "
                + "FROM CertifiedProductSummaryEntity cp "
                + "WHERE cp.deleted = false ";
        switch (urlType) {
        case MANDATORY_DISCLOSURE_URL:
            queryStr += " AND cp.transparencyAttestationUrl = :url ";
            break;
        case FULL_USABILITY_REPORT:
            queryStr += " AND cp.sedReportFileLocation = :url ";
            break;
        case TEST_RESULTS_SUMMARY:
            queryStr += " AND cp.reportFileLocation = :url ";
            break;
        default:
                break;
        }

        Query query = entityManager.createQuery(queryStr, CertifiedProductSummaryEntity.class);
        query.setParameter("url", url);
        List<CertifiedProductSummaryEntity> entities = query.getResultList();
        List<CertifiedProductSummaryDTO> resultDtos = new ArrayList<CertifiedProductSummaryDTO>();
        for (CertifiedProductSummaryEntity entity : entities) {
            resultDtos.add(new CertifiedProductSummaryDTO(entity));
        }
        return resultDtos;
    }

    @Transactional(readOnly = false)
    private void create(final CertifiedProductEntity product) {

        entityManager.persist(product);
        entityManager.flush();
        entityManager.clear();
    }

    @Transactional(readOnly = false)
    private void update(final CertifiedProductEntity product) {

        entityManager.merge(product);
        entityManager.flush();
        entityManager.clear();
    }

    @Transactional(readOnly = true)
    private List<CertifiedProductEntity> getAllEntities() {

        List<CertifiedProductEntity> result = entityManager
                .createQuery("from CertifiedProductEntity where (NOT deleted = true) ", CertifiedProductEntity.class)
                .getResultList();
        return result;

    }

    @Transactional(readOnly = true)
    private CertifiedProductEntity getEntityById(final Long entityId) throws EntityRetrievalException {

        CertifiedProductEntity entity = null;

        Query query = entityManager.createQuery("from CertifiedProductEntity where (certified_product_id = :entityid) ",
                CertifiedProductEntity.class);
        query.setParameter("entityid", entityId);
        List<CertifiedProductEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("listing.notFound"),
                    LocaleContextHolder.getLocale()));
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate Certified Product id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    @Transactional(readOnly = true)
    private CertifiedProductEntity getEntityByChplNumber(final String chplProductNumber) {

        CertifiedProductEntity entity = null;

        Query query = entityManager.createQuery(
                "from CertifiedProductEntity where (chplProductNumber = :chplProductNumber) ",
                CertifiedProductEntity.class);
        query.setParameter("chplProductNumber", chplProductNumber);
        List<CertifiedProductEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    @Transactional(readOnly = true)
    private CertifiedProductDetailsEntity getEntityByUniqueIdParts(final String yearCode, final String atlCode,
            final String acbCode, final String developerCode, final String productCode, final String versionCode,
            final String icsCode, final String additionalSoftwareCode, final String certifiedDateCode) {

        CertifiedProductDetailsEntity entity = null;

        Query query = entityManager.createQuery("from CertifiedProductDetailsEntity deets "
                + "LEFT OUTER JOIN FETCH deets.product " + "where " + "deets.year = '20' || :yearCode AND "
                // + "deets.testingLabCode = :atlCode AND " +
                // "deets.certificationBodyCode = :acbCode AND "
                + "deets.certificationBodyCode = :acbCode AND "
                + "deets.developerCode = :developerCode AND " + "deets.productCode = :productCode AND "
                + "deets.versionCode = :versionCode AND " + "deets.icsCode = :icsCode AND "
                + "deets.additionalSoftwareCode = :additionalSoftwareCode AND "
                + "deets.deleted = false AND "
                + "deets.certifiedDateCode = :certifiedDateCode ", CertifiedProductDetailsEntity.class);

        query.setParameter("yearCode", yearCode);
        // query.setParameter("atlCode", atlCode);
        query.setParameter("acbCode", acbCode);
        query.setParameter("developerCode", developerCode);
        query.setParameter("productCode", productCode);
        query.setParameter("versionCode", versionCode);
        query.setParameter("icsCode", icsCode);
        query.setParameter("additionalSoftwareCode", additionalSoftwareCode);
        query.setParameter("certifiedDateCode", certifiedDateCode);

        List<CertifiedProductDetailsEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }
}
