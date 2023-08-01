package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductSummaryDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntitySimple;
import gov.healthit.chpl.entity.listing.CertifiedProductEntity;
import gov.healthit.chpl.entity.listing.CertifiedProductSummaryEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.urlStatus.data.UrlType;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor
@Repository(value = "certifiedProductDAO")
@Primary
public class CertifiedProductDAO extends BaseDAOImpl {
    private static final int CHPL_ID_LENGTH = 9;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public CertifiedProductDAO(ChplProductNumberUtil chplProductNumberUtil, ErrorMessageUtil msgUtil) {
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.msgUtil = msgUtil;
    }

    public Long create(CertifiedProductSearchDetails listing) throws EntityCreationException {
        try {
            CertifiedProductEntity entity = new CertifiedProductEntity();
            //foreign keys
            entity.setCertificationBodyId(MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY));
            entity.setProductVersionId(listing.getVersion().getId());
            entity.setCertificationEditionId(listing.getEdition().getId());
            //other listing fields
            entity.setAcbCertificationId(listing.getAcbCertificationId());
            entity.setProductCode(chplProductNumberUtil.getProductCode(listing.getChplProductNumber()));
            entity.setVersionCode(chplProductNumberUtil.getVersionCode(listing.getChplProductNumber()));
            entity.setAdditionalSoftwareCode(chplProductNumberUtil.getAdditionalSoftwareCode(listing.getChplProductNumber()));
            entity.setIcsCode(chplProductNumberUtil.getIcsCodeAsString(listing.getChplProductNumber()));
            entity.setCertifiedDateCode(chplProductNumberUtil.getCertificationDateCode(listing.getChplProductNumber()));
            entity.setReportFileLocation(listing.getReportFileLocation());
            entity.setSedIntendedUserDescription(listing.getSedIntendedUserDescription());
            entity.setSedTestingEnd(listing.getSedTestingEndDay());
            entity.setSedReportFileLocation(listing.getSedReportFileLocation());
            entity.setProductAdditionalSoftware(listing.getProductAdditionalSoftware());
            entity.setOtherAcb(listing.getOtherAcb());
            entity.setMandatoryDisclosures(listing.getMandatoryDisclosures());
            entity.setIcs(listing.getIcs() == null || listing.getIcs().getInherits() == null ? Boolean.FALSE : listing.getIcs().getInherits());
            entity.setAccessibilityCertified(listing.getAccessibilityCertified());
            entity.setSvapNoticeUrl(listing.getSvapNoticeUrl());
            entity.setChplProductNumber(null);
            entity.setLastModifiedUser(AuthUtil.getAuditId());

            //these fields are null for ALL current listings
            entity.setSedTesting(null);
            entity.setQmsTesting(null);

            create(entity);
            return entity.getId();
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    @Transactional(readOnly = false)
    public CertifiedProductDTO update(CertifiedProductDTO dto) throws EntityRetrievalException {
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
        entity.setMandatoryDisclosures(dto.getMandatoryDisclosures());
        entity.setCertificationBodyId(dto.getCertificationBodyId());
        entity.setCertificationEditionId(dto.getCertificationEditionId());
        entity.setProductVersionId(dto.getProductVersionId());
        entity.setRwtPlansUrl(dto.getRwtPlansUrl());
        entity.setRwtPlansCheckDate(dto.getRwtPlansCheckDate());
        entity.setRwtResultsUrl(dto.getRwtResultsUrl());
        entity.setRwtResultsCheckDate(dto.getRwtResultsCheckDate());
        entity.setSvapNoticeUrl(dto.getSvapNoticeUrl());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        try {
            update(entity);
        } catch (Exception ex) {
            String msg = msgUtil.getMessage("listing.badListingData", dto.getChplProductNumber(), ex.getMessage());
            LOGGER.error(msg, ex);
            throw new EntityRetrievalException(msg);
        }
        return new CertifiedProductDTO(entity);
    }

    @Transactional(readOnly = false)
    @SuppressWarnings({"checkstyle:todocomment"})
    public void delete(final Long productId) {
        // TODO: How to delete this without leaving orphans
        Query query = entityManager.createQuery(
                "UPDATE CertifiedProductEntity SET deleted = true WHERE certified_product_id = :productid");
        query.setParameter("productid", productId);
        query.executeUpdate();
    }

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

    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> findByDeveloperId(Long developerId) {
        Query query = entityManager.createQuery("SELECT cpd "
                + "FROM CertifiedProductDetailsEntity cpd "
                + "WHERE cpd.deleted = false "
                + "AND cpd.developerId = :developerId ",
                CertifiedProductDetailsEntity.class);
        query.setParameter("developerId", developerId);
        List<CertifiedProductDetailsEntity> entities = query.getResultList();
        List<CertifiedProductDetailsDTO> products = new ArrayList<CertifiedProductDetailsDTO>(entities.size());
        for (CertifiedProductDetailsEntity entity : entities) {
            CertifiedProductDetailsDTO product = new CertifiedProductDetailsDTO(entity);
            products.add(product);
        }
        return products;
    }

    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> findByProductId(Long productId) {
        Query query = entityManager.createQuery("SELECT cpd "
                + "FROM CertifiedProductDetailsEntity cpd "
                + "WHERE cpd.deleted = false "
                + "AND cpd.productId = :productId ",
                CertifiedProductDetailsEntity.class);
        query.setParameter("productId", productId);
        List<CertifiedProductDetailsEntity> entities = query.getResultList();
        List<CertifiedProductDetailsDTO> products = new ArrayList<CertifiedProductDetailsDTO>(entities.size());
        for (CertifiedProductDetailsEntity entity : entities) {
            CertifiedProductDetailsDTO product = new CertifiedProductDetailsDTO(entity);
            products.add(product);
        }
        return products;
    }

    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> findListingsByDeveloperId(Long developerId) {
        Query query = entityManager.createQuery("SELECT cpd "
                + "FROM CertifiedProductDetailsEntitySimple cpd "
                + "WHERE cpd.deleted = false "
                + "AND cpd.developerId = :developerId ",
                CertifiedProductDetailsEntitySimple.class);
        query.setParameter("developerId", developerId);
        List<CertifiedProductDetailsEntitySimple> entities = query.getResultList();
        return entities.stream()
            .map(entity -> new CertifiedProductDetailsDTO(entity))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getListingsByStatusForDeveloperAndAcb(Long developerId,
            List<CertificationStatusType> listingStatuses, List<Long> acbIds) {
        String hql = "FROM CertifiedProductDetailsEntity "
                + "WHERE developerId = :developerId "
                + "AND certificationStatusName IN (:listingStatusNames) "
                + "AND certificationBodyId IN (:acbIds) "
                + "AND deleted = false ";
        Query query = entityManager.createQuery(hql, CertifiedProductDetailsEntity.class);
        List<String> listingStatusNames = listingStatuses.stream()
                .map(CertificationStatusType::getName)
                .collect(Collectors.toList());
        query.setParameter("developerId", developerId);
        query.setParameter("listingStatusNames", listingStatusNames);
        query.setParameter("acbIds", acbIds);

        List<CertifiedProductDetailsEntity> queryResults = query.getResultList();
        if (queryResults == null || queryResults.size() == 0) {
            return new ArrayList<CertifiedProductDetailsDTO>();
        }
        return queryResults.stream()
                .map(entity -> new CertifiedProductDetailsDTO(entity))
                .collect(Collectors.toList());
    }

    public List<Long> getListingIdsAttestingToCriterion(Long criterionId, List<CertificationStatusType> statuses) {
        Query query = entityManager.createQuery("SELECT listing "
                + "FROM CertifiedProductDetailsEntitySimple listing, CertificationResultEntity cre "
                + "WHERE listing.id = cre.certifiedProductId "
                + "AND listing.certificationStatusName IN (:statusNames) "
                + "AND cre.deleted = false "
                + "AND cre.certificationCriterionId = :criterionId "
                + "AND cre.success = true "
                + "AND listing.deleted = false ",
                CertifiedProductDetailsEntitySimple.class);
        query.setParameter("statusNames", statuses.stream().map(status -> status.getName()).collect(Collectors.toList()));
        query.setParameter("criterionId", criterionId);
        List<CertifiedProductDetailsEntitySimple> results = query.getResultList();
        return results.stream()
                .map(result -> result.getId())
                .collect(Collectors.toList());
    }

    public List<CertifiedProductDetailsDTO> getListingsAttestingToCriterion(Long criterionId, List<CertificationStatusType> statuses) {
        Query query = entityManager.createQuery("SELECT listing "
                + "FROM CertifiedProductDetailsEntitySimple listing, CertificationResultEntity cre "
                + "WHERE listing.id = cre.certifiedProductId "
                + "AND listing.certificationStatusName IN (:statusNames) "
                + "AND cre.deleted = false "
                + "AND cre.certificationCriterionId = :criterionId "
                + "AND cre.success = true "
                + "AND listing.deleted = false ",
                CertifiedProductDetailsEntitySimple.class);
        query.setParameter("statusNames", statuses.stream().map(status -> status.getName()).collect(Collectors.toList()));
        query.setParameter("criterionId", criterionId);
        List<CertifiedProductDetailsEntitySimple> results = query.getResultList();
        return results.stream()
                .map(result -> new CertifiedProductDetailsDTO(result))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> findByEdition(final String edition) {
        Query query = entityManager.createQuery("SELECT cpd "
                + "FROM CertifiedProductDetailsEntity cpd "
                + "WHERE (NOT deleted = true) "
                + "AND cpd.year = :edition ", CertifiedProductDetailsEntity.class);
        query.setParameter("edition", edition.trim());
        List<CertifiedProductDetailsEntity> entities = query.getResultList();
        List<CertifiedProductDetailsDTO> products = new ArrayList<>(entities.size());

        for (CertifiedProductDetailsEntity entity : entities) {
            CertifiedProductDetailsDTO product = new CertifiedProductDetailsDTO(entity);
            products.add(product);
        }
        return products;
    }

    @Transactional(readOnly = true)
    public List<Long> findIdsByEdition(String edition) {
        Query query = entityManager.createQuery("SELECT cpd.id "
                + "FROM CertifiedProductDetailsEntity cpd "
                + "WHERE (NOT deleted = true) "
                + "AND cpd.year = :edition ", Long.class);
        query.setParameter("edition", edition.trim());
        List<Long> ids = query.getResultList();
        return ids;
    }

    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> findWithSurveillance() {

        List<CertifiedProductDetailsEntity> entities = entityManager.createQuery("SELECT DISTINCT cp "
            + "FROM CertifiedProductDetailsEntity cp, SurveillanceEntity surv "
            + "WHERE surv.certifiedProductId = cp.id "
            + "AND (NOT surv.deleted = true)",
            CertifiedProductDetailsEntity.class).getResultList();

        List<CertifiedProductDetailsDTO> products = new ArrayList<>();

        for (CertifiedProductDetailsEntity entity : entities) {
            CertifiedProductDetailsDTO product = new CertifiedProductDetailsDTO(entity);
            products.add(product);
        }
        return products;
    }

    @Transactional(readOnly = true)
    public List<Long> findListingIdsWithSvap() {
        List<Long> listingIds = entityManager.createQuery("SELECT DISTINCT cp.id "
                + "FROM CertifiedProductEntity cp, CertificationResultDetailsEntity cr "
                + "LEFT JOIN cr.certificationResultSvaps crSvaps "
                + "WHERE cr.certifiedProductId = cp.id "
                + "AND cp.deleted = false "
                + "AND cr.deleted = false "
                + "AND cr.success = true "
                + "AND ((cp.svapNoticeUrl IS NOT NULL AND cp.svapNoticeUrl != '') OR crSvaps.id IS NOT NULL)",
                Long.class).getResultList();

        return listingIds;
    }

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

    @Transactional(readOnly = true)
    public CertifiedProductDTO getById(final Long productId) throws EntityRetrievalException {
        CertifiedProductDTO dto = null;
        CertifiedProductEntity entity = getEntityById(productId);

        if (entity != null) {
            dto = new CertifiedProductDTO(entity);
        }
        return dto;
    }

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
            throw new EntityRetrievalException(msgUtil.getMessage("listing.notFound"));
        }
        return new CertifiedProductSummaryDTO(result.get(0));
    }

    @Transactional(readOnly = true)
    public CertifiedProductDTO getByChplNumber(final String chplProductNumber) {
        CertifiedProductDTO dto = null;
        CertifiedProductEntity entity = getEntityByChplNumber(chplProductNumber);

        if (entity != null) {
            dto = new CertifiedProductDTO(entity);
        }
        return dto;
    }

    @SuppressWarnings({"checkstyle:magicnumber"})
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

    @Transactional(readOnly = true)
    public CertifiedProductDetailsDTO getDetailsById(final Long cpId) throws EntityRetrievalException {
        Query query = entityManager.createQuery("from CertifiedProductDetailsEntity deets "
                + "LEFT OUTER JOIN FETCH deets.product "
                + "where deets.id = :cpId "
                + "and deets.deleted = false",
                CertifiedProductDetailsEntity.class);
        query.setParameter("cpId", cpId);
        List<CertifiedProductDetailsEntity> results = query.getResultList();

        if (results == null || results.size() == 0) {
            return null;
        }
        return new CertifiedProductDetailsDTO(results.get(0));
    }

    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getDetailsByIds(final List<Long> productIds)
            throws EntityRetrievalException {
        if ((null == productIds) || (productIds.size() == 0)) {
            return new ArrayList<CertifiedProductDetailsDTO>();
        }

        Query prodQuery = entityManager.createQuery("from CertifiedProductDetailsEntity deets "
                + "LEFT OUTER JOIN FETCH deets.product "
                + "WHERE deets.id in (:productIds) "
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

    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getDetailsByChplNumbers(final List<String> chplProductNumbers) {
        if ((null == chplProductNumbers) || (chplProductNumbers.size() == 0)) {
            return new ArrayList<CertifiedProductDetailsDTO>();
        }

        Query prodQuery = entityManager.createQuery(
                "from CertifiedProductDetailsEntity deets "
                        + "LEFT OUTER JOIN FETCH deets.product "
                        + "WHERE deets.chplProductNumber in (:chplProductNumbers) "
                        + "AND deets.deleted = false ",
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

    @Transactional(readOnly = true)
    public List<CertifiedProduct> getDetailsByVersionId(final Long versionId) {
        Query query = entityManager.createQuery("from CertifiedProductDetailsEntity deets "
                + "LEFT OUTER JOIN FETCH deets.product "
                + "WHERE deets.productVersionId = :versionId "
                + "AND deets.deleted = false",
                CertifiedProductDetailsEntity.class);
        query.setParameter("versionId", versionId);
        List<CertifiedProductDetailsEntity> entities = query.getResultList();

        List<CertifiedProduct> results = new ArrayList<CertifiedProduct>();
        for (CertifiedProductDetailsEntity entity : entities) {
            CertifiedProduct cp = new CertifiedProduct();
            cp.setCertificationDate(entity.getCertificationDate().getTime());
            cp.setCertificationStatus(entity.getCertificationStatusName());
            cp.setCuresUpdate(entity.getCuresUpdate());
            cp.setChplProductNumber(entity.getChplProductNumber());
            cp.setEdition(entity.getYear());
            cp.setId(entity.getId());
            cp.setLastModifiedDate(entity.getLastModifiedDate().getTime());
            results.add(cp);
        }
        return results;
    }

    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getDetailsByProductId(final Long productId) {
        Query query = entityManager.createQuery("from CertifiedProductDetailsEntity deets "
                + "LEFT OUTER JOIN FETCH deets.product "
                + "WHERE deets.productId = :productId "
                + "AND deets.deleted = false",
                CertifiedProductDetailsEntity.class);
        query.setParameter("productId", productId);
        List<CertifiedProductDetailsEntity> results = query.getResultList();

        List<CertifiedProductDetailsDTO> dtoResults = new ArrayList<CertifiedProductDetailsDTO>();
        for (CertifiedProductDetailsEntity result : results) {
            dtoResults.add(new CertifiedProductDetailsDTO(result));
        }
        return dtoResults;
    }

    @Transactional(readOnly = true)
    public List<CertifiedProductDetailsDTO> getDetailsByAcbIds(final List<Long> acbIds) {
        Query query = entityManager.createQuery(
                "from CertifiedProductDetailsEntity "
                        + "WHERE (NOT deleted = true) "
                        + "AND certification_body_id IN :idList",
                        CertifiedProductDetailsEntity.class);
        query.setParameter("idList", acbIds);
        List<CertifiedProductDetailsEntity> results = query.getResultList();

        List<CertifiedProductDetailsDTO> dtoResults = new ArrayList<CertifiedProductDetailsDTO>(results.size());
        for (CertifiedProductDetailsEntity result : results) {
            dtoResults.add(new CertifiedProductDetailsDTO(result));
        }
        return dtoResults;
    }

    @Transactional(readOnly = true)
    public List<CertifiedProductSummaryDTO> getSummaryByUrl(final String url, final UrlType urlType) {
        String queryStr = "SELECT cp "
                + "FROM CertifiedProductSummaryEntity cp "
                + "WHERE cp.deleted = false ";
        switch (urlType) {
        case MANDATORY_DISCLOSURE:
            queryStr += " AND cp.mandatoryDisclosures = :url ";
            break;
        case FULL_USABILITY_REPORT:
            queryStr += " AND cp.sedReportFileLocation = :url ";
            break;
        case TEST_RESULTS_SUMMARY:
            queryStr += " AND cp.reportFileLocation = :url ";
            break;
        case REAL_WORLD_TESTING_PLANS:
            queryStr += " AND cp.rwtPlansUrl = :url ";
            break;
        case REAL_WORLD_TESTING_RESULTS:
            queryStr += " AND cp.rwtResultsUrl = :url ";
            break;
        case STANDARDS_VERSION_ADVANCEMENT_PROCESS_NOTICE:
            queryStr += " AND cp.svapNoticeUrl = :url ";
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

    /**
     * This method has protected access because it is needed in the RealWorldTestingEligibilityJob.RwtEligibilityYearDAO class
     */
    @Transactional(readOnly = true)
    protected CertifiedProductEntity getEntityById(final Long entityId) throws EntityRetrievalException {

        CertifiedProductEntity entity = null;

        Query query = entityManager.createQuery("from CertifiedProductEntity where (certified_product_id = :entityid) ",
                CertifiedProductEntity.class);
        query.setParameter("entityid", entityId);
        List<CertifiedProductEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            throw new EntityRetrievalException(msgUtil.getMessage("listing.notFound"));
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

    @SuppressWarnings({"checkstyle:parameternumber"})
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
