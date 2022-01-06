package gov.healthit.chpl.dao;

import static gov.healthit.chpl.util.LambdaExceptionUtil.rethrowConsumer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.dto.CQMResultCriteriaDTO;
import gov.healthit.chpl.dto.CQMResultDTO;
import gov.healthit.chpl.entity.listing.CQMResultCriteriaEntity;
import gov.healthit.chpl.entity.listing.CQMResultEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository(value = "cqmResultDAO")
public class CQMResultDAO extends BaseDAOImpl {

    private CQMCriterionDAO cqmDao;

    @Autowired
    public CQMResultDAO(CQMCriterionDAO cqmDao) {
        this.cqmDao = cqmDao;
    }

    public void create(Long listingId, CQMResultDetails cqmResult) throws EntityCreationException {
        if (CollectionUtils.isEmpty(cqmResult.getSuccessVersions())) {
            return;
        }
        List<Long> cqmsWithVersionIds = cqmResult.getSuccessVersions().stream()
                .map(cqmSuccessVersion -> cqmDao.getCMSByNumberAndVersion(cqmResult.getCmsId(), cqmSuccessVersion))
                .map(cqmWithVersion -> cqmWithVersion.getId())
                .collect(Collectors.toList());
        cqmsWithVersionIds.stream()
            .forEach(rethrowConsumer(cqmWithVersionId -> create(listingId, cqmWithVersionId, cqmResult.getCriteria())));
    }

    private void create(Long listingId, Long cqmWithVersionId, List<CQMResultCertification> cqmCriteria)
            throws EntityCreationException {
        try {
            CQMResultEntity cqmResultEntity = new CQMResultEntity();
            cqmResultEntity.setCqmCriterionId(cqmWithVersionId);
            cqmResultEntity.setCertifiedProductId(listingId);
            cqmResultEntity.setSuccess(true);
            cqmResultEntity.setLastModifiedUser(AuthUtil.getAuditId());
            create(cqmResultEntity);
            if (!CollectionUtils.isEmpty(cqmCriteria)) {
                for (CQMResultCertification cqmCriterion : cqmCriteria) {
                    createCqmCriteronMapping(cqmResultEntity.getId(), cqmCriterion.getCriterion().getId());
                }
            }
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public Long createCqmCriteronMapping(Long cqmResultId, Long criterionId) throws EntityCreationException {
        try {
            CQMResultCriteriaEntity cqmCriterionMappingEntity = new CQMResultCriteriaEntity();
            cqmCriterionMappingEntity.setCertificationCriterionId(criterionId);
            cqmCriterionMappingEntity.setCqmResultId(cqmResultId);
            cqmCriterionMappingEntity.setLastModifiedUser(AuthUtil.getAuditId());
            create(cqmCriterionMappingEntity);
            return cqmCriterionMappingEntity.getId();
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public CQMResultDTO create(CQMResultDTO cqmResult) throws EntityCreationException {
        CQMResultEntity entity = null;
        try {
            if (cqmResult.getId() != null) {
                entity = this.getEntityById(cqmResult.getId());
            }
        } catch (final EntityRetrievalException e) {
            throw new EntityCreationException(e);
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {

            entity = new CQMResultEntity();
            entity.setCqmCriterionId(cqmResult.getCqmCriterionId());
            entity.setCertifiedProductId(cqmResult.getCertifiedProductId());
            entity.setSuccess(cqmResult.getSuccess());

            entity.setLastModifiedUser(AuthUtil.getAuditId());
            entity.setLastModifiedDate(new Date());
            entity.setCreationDate(new Date());
            entity.setDeleted(false);
            create(entity);

            if (cqmResult.getCriteria() != null) {
                for (CQMResultCriteriaDTO certDto : cqmResult.getCriteria()) {
                    certDto.setCqmResultId(entity.getId());
                    createCriteriaMapping(certDto);
                }
            }

            return new CQMResultDTO(entity);
        }
    }

    public CQMResultCriteriaDTO createCriteriaMapping(CQMResultCriteriaDTO criteria) {
        CQMResultCriteriaEntity newMapping = new CQMResultCriteriaEntity();
        newMapping.setCertificationCriterionId(criteria.getCriterionId());
        newMapping.setCqmResultId(criteria.getCqmResultId());
        newMapping.setCreationDate(new Date());
        newMapping.setDeleted(false);
        newMapping.setLastModifiedDate(new Date());
        newMapping.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.persist(newMapping);
        entityManager.flush();
        return new CQMResultCriteriaDTO(newMapping);
    }

    public void update(CQMResultDTO cqmResult) throws EntityRetrievalException {
        CQMResultEntity entity = this.getEntityById(cqmResult.getId());
        entity.setLastModifiedDate(new Date());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setSuccess(cqmResult.getSuccess());

        update(entity);

    }

    public CQMResultCriteriaDTO updateCriteriaMapping(CQMResultCriteriaDTO dto) {
        CQMResultCriteriaEntity toUpdate = getCqmCriteriaById(dto.getId());
        if (toUpdate == null) {
            return null;
        }
        toUpdate.setCqmResultId(dto.getCqmResultId());
        toUpdate.setCertificationCriterionId(dto.getCriterionId());
        toUpdate.setLastModifiedDate(new Date());
        toUpdate.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.merge(toUpdate);
        entityManager.flush();
        return new CQMResultCriteriaDTO(toUpdate);
    }

    public void delete(Long cqmResultId) {
        // TODO: How to delete this without leaving orphans
        deleteMappingsForCqmResult(cqmResultId);
        Query query = entityManager
                .createQuery("UPDATE CQMResultEntity SET deleted = true WHERE cqm_result_id = :resultid");
        query.setParameter("resultid", cqmResultId);
        query.executeUpdate();
    }

    public void deleteByCertifiedProductId(Long productId) {
        List<CQMResultDTO> cqmResults = findByCertifiedProductId(productId);
        for (CQMResultDTO cqmResult : cqmResults) {
            deleteMappingsForCqmResult(cqmResult.getId());
        }
        Query query = entityManager
                .createQuery("UPDATE CQMResultEntity SET deleted = true WHERE certified_product_id = :productId");
        query.setParameter("productId", productId);
        query.executeUpdate();
    }

    public void deleteCriteriaMapping(Long mappingId) {
        CQMResultCriteriaEntity toDelete = getCqmCriteriaById(mappingId);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.persist(toDelete);
            entityManager.flush();
        }
    }

    public void deleteMappingsForCqmResult(Long cqmResultId) {
        Query query = entityManager
                .createQuery("UPDATE CQMResultCriteriaEntity SET deleted = true WHERE cqm_result_id = :resultid");
        query.setParameter("resultid", cqmResultId);
        query.executeUpdate();
    }

    public List<CQMResultDTO> findAll() {
        List<CQMResultEntity> entities = getAllEntities();
        List<CQMResultDTO> cqmResults = new ArrayList<>();

        for (CQMResultEntity entity : entities) {
            CQMResultDTO cqmResult = new CQMResultDTO(entity);
            cqmResults.add(cqmResult);
        }
        return cqmResults;
    }

    public List<CQMResultDTO> findByCertifiedProductId(Long certifiedProductId) {
        List<CQMResultEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
        List<CQMResultDTO> cqmResults = new ArrayList<>();

        for (CQMResultEntity entity : entities) {
            CQMResultDTO cqmResult = new CQMResultDTO(entity);
            cqmResults.add(cqmResult);
        }
        return cqmResults;

    }

    public CQMResultDTO getById(Long cqmResultId) throws EntityRetrievalException {
        CQMResultDTO dto = null;
        CQMResultEntity entity = getEntityById(cqmResultId);
        if (entity != null) {
            dto = new CQMResultDTO(entity);
        }
        return dto;
    }

    public List<CQMResultCriteriaDTO> getCriteriaForCqmResult(Long cqmResultId) {
        List<CQMResultCriteriaEntity> entities = getCertCriteriaForCqmResult(cqmResultId);
        List<CQMResultCriteriaDTO> dtos = new ArrayList<CQMResultCriteriaDTO>();
        for (CQMResultCriteriaEntity entity : entities) {
            CQMResultCriteriaDTO dto = new CQMResultCriteriaDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    private CQMResultEntity getEntityById(Long id) throws EntityRetrievalException {
        CQMResultEntity entity = null;
        Query query = entityManager.createQuery(
                "from CQMResultEntity where (NOT deleted = true) AND (cqm_result_id = :entityid) ",
                CQMResultEntity.class);
        query.setParameter("entityid", id);
        List<CQMResultEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate CQM result id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private List<CQMResultEntity> getAllEntities() {
        List<CQMResultEntity> result = entityManager
                .createQuery("from CQMResultEntity where (NOT deleted = true) ", CQMResultEntity.class).getResultList();
        return result;
    }

    private List<CQMResultEntity> getEntitiesByCertifiedProductId(Long certifiedProductId) {
        Query query = entityManager.createQuery(
                "from CQMResultEntity where (NOT deleted = true) AND (certified_product_id = :entityid) ",
                CQMResultEntity.class);
        query.setParameter("entityid", certifiedProductId);
        List<CQMResultEntity> result = query.getResultList();
        return result;
    }

    private CQMResultCriteriaEntity getCqmCriteriaById(Long id) {
        CQMResultCriteriaEntity entity = null;
        Query query = entityManager.createQuery(
                "from CQMResultCriteriaEntity " + "where (NOT deleted = true) AND (id = :entityid) ",
                CQMResultCriteriaEntity.class);
        query.setParameter("entityid", id);
        List<CQMResultCriteriaEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<CQMResultCriteriaEntity> getCertCriteriaForCqmResult(Long cqmResultId) {
        Query query = entityManager.createQuery(
                "from CQMResultCriteriaEntity " + "where (NOT deleted = true) AND (cqm_result_id = :cqmResultId) ",
                CQMResultCriteriaEntity.class);
        query.setParameter("cqmResultId", cqmResultId);

        List<CQMResultCriteriaEntity> result = query.getResultList();
        if (result == null) {
            return null;
        }
        return result;
    }
}
