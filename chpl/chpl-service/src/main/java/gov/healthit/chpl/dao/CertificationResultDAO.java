package gov.healthit.chpl.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.TestParticipantDTO;
import gov.healthit.chpl.dto.TestTaskDTO;
import gov.healthit.chpl.entity.TestParticipantEntity;
import gov.healthit.chpl.entity.TestTaskEntity;
import gov.healthit.chpl.entity.listing.CertificationResultAdditionalSoftwareEntity;
import gov.healthit.chpl.entity.listing.CertificationResultConformanceMethodEntity;
import gov.healthit.chpl.entity.listing.CertificationResultEntity;
import gov.healthit.chpl.entity.listing.CertificationResultOptionalStandardEntity;
import gov.healthit.chpl.entity.listing.CertificationResultTestDataEntity;
import gov.healthit.chpl.entity.listing.CertificationResultTestFunctionalityEntity;
import gov.healthit.chpl.entity.listing.CertificationResultTestProcedureEntity;
import gov.healthit.chpl.entity.listing.CertificationResultTestStandardEntity;
import gov.healthit.chpl.entity.listing.CertificationResultTestTaskEntity;
import gov.healthit.chpl.entity.listing.CertificationResultTestToolEntity;
import gov.healthit.chpl.entity.listing.CertificationResultUcdProcessEntity;
import gov.healthit.chpl.entity.listing.TestTaskParticipantMapEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.svap.entity.CertificationResultSvapEntity;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Repository(value = "certificationResultDAO")
public class CertificationResultDAO extends BaseDAOImpl {
    private static final Logger LOGGER = LogManager.getLogger(CertificationResultDAO.class);

    private TestParticipantDAO participantDao;
    private TestTaskDAO testTaskDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public CertificationResultDAO(TestParticipantDAO participantDao, TestTaskDAO testTaskDao,
            ErrorMessageUtil msgUtil) {
        this.participantDao = participantDao;
        this.testTaskDao = testTaskDao;
        this.msgUtil = msgUtil;
    }

    public CertificationResultDTO create(CertificationResultDTO result) throws EntityCreationException {
        CertificationResultEntity entity = null;
        try {
            if (result.getId() != null) {
                entity = this.getEntityById(result.getId());
            }
        } catch (final EntityRetrievalException e) {
            throw new EntityCreationException(e);
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {

            entity = new CertificationResultEntity();
            entity.setCertificationCriterionId(result.getCertificationCriterionId());
            entity.setCertifiedProductId(result.getCertifiedProductId());
            entity.setGap(result.getGap());
            entity.setSed(result.getSed());
            entity.setG1Success(result.getG1Success());
            entity.setG2Success(result.getG2Success());
            entity.setAttestationAnswer(result.getAttestationAnswer());
            entity.setSuccess(result.getSuccessful());
            entity.setApiDocumentation(result.getApiDocumentation());
            entity.setExportDocumentation(result.getExportDocumentation());
            entity.setDocumentationUrl(result.getDocumentationUrl());
            entity.setUseCases(result.getUseCases());
            entity.setServiceBaseUrlList(result.getServiceBaseUrlList());
            entity.setPrivacySecurityFramework(result.getPrivacySecurityFramework());
            entity.setLastModifiedUser(AuthUtil.getAuditId());

            try {
                create(entity);
            } catch (Exception ex) {
                String msg = msgUtil.getMessage("listing.badCriteriaData",
                        result.getCertificationCriterionId(), ex.getMessage());
                LOGGER.error(msg, ex);
                throw new EntityCreationException(msg);
            }
        }
        return new CertificationResultDTO(entity);

    }

    public CertificationResultDTO update(CertificationResultDTO toUpdate) throws EntityRetrievalException {
        CertificationResultEntity entity = getEntityById(toUpdate.getId());
        entity.setCertificationCriterionId(toUpdate.getCertificationCriterionId());
        entity.setCertifiedProductId(toUpdate.getCertifiedProductId());
        entity.setGap(toUpdate.getGap());
        entity.setSed(toUpdate.getSed());
        entity.setG1Success(toUpdate.getG1Success());
        entity.setG2Success(toUpdate.getG2Success());
        entity.setAttestationAnswer(toUpdate.getAttestationAnswer());
        entity.setSuccess(toUpdate.getSuccessful());
        entity.setApiDocumentation(toUpdate.getApiDocumentation());
        entity.setExportDocumentation(toUpdate.getExportDocumentation());
        entity.setDocumentationUrl(toUpdate.getDocumentationUrl());
        entity.setUseCases(toUpdate.getUseCases());
        entity.setServiceBaseUrlList(toUpdate.getServiceBaseUrlList());
        entity.setPrivacySecurityFramework(toUpdate.getPrivacySecurityFramework());
        entity.setLastModifiedUser(AuthUtil.getAuditId());

        try {
            entityManager.merge(entity);
            entityManager.flush();
        } catch (Exception ex) {
            String msg = msgUtil.getMessage("listing.badCriteriaData", toUpdate.getCertificationCriterionId(), ex.getMessage());
            LOGGER.error(msg, ex);
            throw new EntityRetrievalException(msg);
        }
        return new CertificationResultDTO(entity);
    }

    public void delete(Long resultId) {

        // TODO: How to delete this without leaving orphans
        Query query = entityManager.createQuery(
                "UPDATE CertificationResultEntity SET deleted = true WHERE certification_result_id = :resultid");
        query.setParameter("resultid", resultId);
        query.executeUpdate();

    }

    public void deleteByCertifiedProductId(Long certifiedProductId) {

        // TODO: How to delete this without leaving orphans
        Query query = entityManager.createQuery(
                "UPDATE CertificationResultEntity SET deleted = true WHERE certified_product_id = :certifiedProductId");
        query.setParameter("certifiedProductId", certifiedProductId);
        query.executeUpdate();

    }

    public CertificationResultDTO getById(Long resultId) throws EntityRetrievalException {

        CertificationResultDTO dto = null;
        CertificationResultEntity entity = getEntityById(resultId);

        if (entity != null) {
            dto = new CertificationResultDTO(entity);
        }
        return dto;
    }

    private CertificationResultEntity getEntityById(Long id) throws EntityRetrievalException {

        CertificationResultEntity entity = null;

        Query query = entityManager.createQuery(
                "from CertificationResultEntity where (NOT deleted = true) AND (certification_result_id = :entityid) ",
                CertificationResultEntity.class);
        query.setParameter("entityid", id);
        List<CertificationResultEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate result id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    public List<Long> getCpIdsByCriterionId(Long criterionId) throws EntityRetrievalException {

        CertificationResultEntity entity = null;

        Query query = entityManager.createQuery(
                "select certifiedProductId from CertificationResultEntity where (NOT deleted = true) AND (certification_criterion_id = :entityid) AND (success = true)");
        query.setParameter("entityid", criterionId);
        List<Long> result = query.getResultList();

        return result;
    }

    public List<CertificationResultDTO> findByCertifiedProductId(Long certifiedProductId) {
        List<CertificationResultEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
        List<CertificationResultDTO> cqmResults = new ArrayList<>();

        for (CertificationResultEntity entity : entities) {
            CertificationResultDTO cqmResult = new CertificationResultDTO(entity);
            cqmResults.add(cqmResult);
        }
        return cqmResults;
    }

    private List<CertificationResultEntity> getEntitiesByCertifiedProductId(Long certifiedProductId) {

        Query query = entityManager.createQuery(
                "from CertificationResultEntity where (NOT deleted = true) AND (certified_product_id = :entityid) ",
                CertificationResultEntity.class);
        query.setParameter("entityid", certifiedProductId);
        List<CertificationResultEntity> result = query.getResultList();
        return result;
    }

    private List<CertificationResultEntity> getEntitiesByCertifiedProductIdSED(Long certifiedProductId) {

        Query query = entityManager.createQuery("from CertificationResultEntity "
                + "where (NOT deleted = true) AND (certified_product_id = :entityid) AND sed = true and success = true ",
                CertificationResultEntity.class);
        query.setParameter("entityid", certifiedProductId);
        List<CertificationResultEntity> result = query.getResultList();
        return result;
    }

    /******************************************************
     * UCD Details for Certification Results
     *
     *******************************************************/

    public List<CertificationResultUcdProcessDTO> getUcdProcessesForCertificationResult(Long certificationResultId) {

        List<CertificationResultUcdProcessEntity> entities = getUcdProcessesForCertification(certificationResultId);
        List<CertificationResultUcdProcessDTO> dtos = new ArrayList<CertificationResultUcdProcessDTO>();

        for (CertificationResultUcdProcessEntity entity : entities) {
            dtos.add(new CertificationResultUcdProcessDTO(entity));
        }
        return dtos;
    }

    public CertificationResultUcdProcessDTO lookupUcdProcessMapping(Long certificationResultId, Long ucdProcessId) {
        Query query = entityManager.createQuery("SELECT up " + "FROM CertificationResultUcdProcessEntity up "
                + "LEFT OUTER JOIN FETCH up.ucdProcess " + "where (NOT up.deleted = true) "
                + "AND (certification_result_id = :certificationResultId) " + "AND up.ucdProcessId = :ucdProcessId",
                CertificationResultUcdProcessEntity.class);
        query.setParameter("certificationResultId", certificationResultId);
        query.setParameter("ucdProcessId", ucdProcessId);
        List<CertificationResultUcdProcessEntity> entities = query.getResultList();

        CertificationResultUcdProcessDTO result = null;
        if (entities != null && entities.size() > 0) {
            result = new CertificationResultUcdProcessDTO(entities.get(0));
        }
        return result;
    }

    public CertificationResultUcdProcessDTO addUcdProcessMapping(CertificationResultUcdProcessDTO dto)
            throws EntityCreationException {
        CertificationResultUcdProcessEntity mapping = new CertificationResultUcdProcessEntity();
        mapping.setCertificationResultId(dto.getCertificationResultId());
        mapping.setUcdProcessId(dto.getUcdProcessId());
        mapping.setUcdProcessDetails(dto.getUcdProcessDetails());
        mapping.setCreationDate(new Date());
        mapping.setDeleted(false);
        mapping.setLastModifiedDate(new Date());
        mapping.setLastModifiedUser(AuthUtil.getAuditId());
        try {
            entityManager.persist(mapping);
            entityManager.flush();
        } catch (Exception ex) {
            String msg = msgUtil.getMessage("listing.criteria.badUcdProcess", dto.getUcdProcessName());
            LOGGER.error(msg, ex);
            throw new EntityCreationException(msg);
        }

        return new CertificationResultUcdProcessDTO(mapping);
    }

    public void deleteUcdProcessMapping(Long certResultId, Long ucdProcessId) {
        CertificationResultUcdProcessEntity toDelete = getCertificationResultUcdProcessById(certResultId, ucdProcessId);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.persist(toDelete);
            entityManager.flush();
        }
    }

    public void updateUcdProcessMapping(CertificationResultUcdProcessDTO dto) throws EntityRetrievalException {
        CertificationResultUcdProcessEntity toUpdate = getCertificationResultUcdProcessById(
                dto.getCertificationResultId(), dto.getUcdProcessId());
        if (toUpdate == null) {
            throw new EntityRetrievalException("Could not find UCD process mapping with id " + dto.getId());
        }
        toUpdate.setUcdProcessDetails(dto.getUcdProcessDetails());
        toUpdate.setUcdProcessId(dto.getUcdProcessId());
        toUpdate.setLastModifiedDate(new Date());
        toUpdate.setLastModifiedUser(AuthUtil.getAuditId());
        try {
            entityManager.persist(toUpdate);
            entityManager.flush();
        } catch (Exception ex) {
            String msg = msgUtil.getMessage("listing.criteria.badUcdProcess", dto.getUcdProcessName());
            LOGGER.error(msg, ex);
            throw new EntityRetrievalException(msg);
        }
    }

    private CertificationResultUcdProcessEntity getCertificationResultUcdProcessById(Long certResultId,
            Long ucdProcessId) {
        CertificationResultUcdProcessEntity entity = null;

        Query query = entityManager.createQuery(
                "SELECT certUcd " + "FROM CertificationResultUcdProcessEntity certUcd "
                        + "LEFT OUTER JOIN FETCH certUcd.ucdProcess ucd " + "WHERE (NOT certUcd.deleted = true) "
                        + "AND (ucd.id = :ucdProcessId) " + "AND certUcd.certificationResultId = :certResultId ",
                CertificationResultUcdProcessEntity.class);
        query.setParameter("ucdProcessId", ucdProcessId);
        query.setParameter("certResultId", certResultId);
        List<CertificationResultUcdProcessEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<CertificationResultUcdProcessEntity> getUcdProcessesForCertification(Long certificationResultId) {
        Query query = entityManager.createQuery(
                "SELECT up " + "FROM CertificationResultUcdProcessEntity up " + "LEFT OUTER JOIN FETCH up.ucdProcess "
                        + "where (NOT up.deleted = true) AND (certification_result_id = :certificationResultId) ",
                CertificationResultUcdProcessEntity.class);
        query.setParameter("certificationResultId", certificationResultId);

        return query.getResultList();
    }

    /******************************************************
     * Additional Software for Certification Results
     *
     *******************************************************/

    public List<CertificationResultAdditionalSoftwareDTO> getAdditionalSoftwareForCertificationResult(
            Long certificationResultId) {

        List<CertificationResultAdditionalSoftwareEntity> entities = getAdditionalSoftwareForCertification(
                certificationResultId);
        List<CertificationResultAdditionalSoftwareDTO> dtos = new ArrayList<CertificationResultAdditionalSoftwareDTO>();

        for (CertificationResultAdditionalSoftwareEntity entity : entities) {
            CertificationResultAdditionalSoftwareDTO dto = new CertificationResultAdditionalSoftwareDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    public CertificationResultAdditionalSoftwareDTO addAdditionalSoftwareMapping(
            CertificationResultAdditionalSoftwareDTO dto) throws EntityCreationException {
        CertificationResultAdditionalSoftwareEntity mapping = new CertificationResultAdditionalSoftwareEntity();
        mapping = new CertificationResultAdditionalSoftwareEntity();
        mapping.setCertificationResultId(dto.getCertificationResultId());
        mapping.setCertifiedProductId(dto.getCertifiedProductId());
        mapping.setName(dto.getName());
        mapping.setVersion(dto.getVersion());
        mapping.setJustification(dto.getJustification());
        mapping.setGrouping(dto.getGrouping());
        mapping.setCreationDate(new Date());
        mapping.setDeleted(false);
        mapping.setLastModifiedDate(new Date());
        mapping.setLastModifiedUser(AuthUtil.getAuditId());

        try {
            entityManager.persist(mapping);
            entityManager.flush();
        } catch (Exception ex) {
            String msg = msgUtil.getMessage("listing.criteria.badAdditionalSoftware",
                    (StringUtils.isEmpty(dto.getName()) ? dto.getCertifiedProductNumber() : dto.getName()));
            LOGGER.error(msg, ex);
            throw new EntityCreationException(msg);
        }

        return new CertificationResultAdditionalSoftwareDTO(mapping);
    }

    public void deleteAdditionalSoftwareMapping(Long mappingId) {
        CertificationResultAdditionalSoftwareEntity toDelete = getCertificationResultAdditionalSoftwareById(mappingId);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.persist(toDelete);
            entityManager.flush();
        }
    }

    public CertificationResultAdditionalSoftwareDTO updateAdditionalSoftwareMapping(
            CertificationResultAdditionalSoftwareDTO toUpdate) throws EntityRetrievalException {

        CertificationResultAdditionalSoftwareEntity curr = getCertificationResultAdditionalSoftwareById(
                toUpdate.getId());
        if (curr == null) {
            throw new EntityRetrievalException("Could not find mapping with id " + toUpdate.getId());
        }
        curr.setCertifiedProductId(toUpdate.getCertifiedProductId());
        curr.setCertificationResultId(toUpdate.getCertificationResultId());
        curr.setGrouping(toUpdate.getGrouping());
        curr.setJustification(toUpdate.getJustification());
        curr.setName(toUpdate.getName());
        curr.setVersion(toUpdate.getVersion());
        curr.setLastModifiedDate(new Date());
        curr.setLastModifiedUser(AuthUtil.getAuditId());

        try {
            entityManager.merge(curr);
            entityManager.flush();
        } catch (Exception ex) {
            String msg = msgUtil.getMessage("listing.criteria.badAdditionalSoftware",
                    (StringUtils.isEmpty(toUpdate.getName()) ? toUpdate.getCertifiedProductNumber()
                            : toUpdate.getName()));
            LOGGER.error(msg, ex);
            throw new EntityRetrievalException(msg);
        }
        return new CertificationResultAdditionalSoftwareDTO(curr);
    }

    private CertificationResultAdditionalSoftwareEntity getCertificationResultAdditionalSoftwareById(Long id) {
        CertificationResultAdditionalSoftwareEntity entity = null;

        Query query = entityManager.createQuery(
                "from CertificationResultAdditionalSoftwareEntity "
                        + "where (NOT deleted = true) AND (id = :entityid) ",
                CertificationResultAdditionalSoftwareEntity.class);
        query.setParameter("entityid", id);
        List<CertificationResultAdditionalSoftwareEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<CertificationResultAdditionalSoftwareEntity> getAdditionalSoftwareForCertification(
            Long certificationResultId) {
        Query query = entityManager.createQuery(
                "from CertificationResultAdditionalSoftwareEntity "
                        + "where (NOT deleted = true) AND (certification_result_id = :certificationResultId) ",
                CertificationResultAdditionalSoftwareEntity.class);
        query.setParameter("certificationResultId", certificationResultId);

        List<CertificationResultAdditionalSoftwareEntity> result = query.getResultList();
        if (result == null) {
            return null;
        }
        return result;
    }

    public boolean getCertifiedProductHasAdditionalSoftware(Long certifiedProductId) {
        // This needs to be a native query since the is no relationship between
        // CertificationResultEntity
        // and CertificationResultAdditionalSoftwareEntity defined.
        Query query = entityManager.createNativeQuery(
                "select count(cr.certification_result_id) "
                        + "from " + SCHEMA_NAME + ".certification_result cr "
                        + "    inner join " + SCHEMA_NAME + ".certification_result_additional_software cras"
                        + "        on cr.certification_result_id = cras.certification_result_id "
                        + "where cr.certified_product_id = :certifiedProductId "
                        + "and NOT cras.deleted");

        query.setParameter("certifiedProductId", certifiedProductId);
        BigInteger count = (BigInteger) query.getSingleResult();
        return count.intValue() > 0;
    }

    /******************************************************
     * Conformance Method methods.
     *
     *******************************************************/

    /*
    public List<CertificationResultOptionalStandard> getOptionalStandardsForCertificationResult(Long certificationResultId) {

        List<CertificationResultOptionalStandardEntity> entities = getOptionalStandardsForCertification(certificationResultId);
        List<CertificationResultOptionalStandard> domains = new ArrayList<CertificationResultOptionalStandard>();

        for (CertificationResultOptionalStandardEntity entity : entities) {
            CertificationResultOptionalStandard domain = new CertificationResultOptionalStandard(entity);
            domains.add(domain);
        }
        return domains;
    }
*/
    public CertificationResultConformanceMethod addConformanceMethodMapping(CertificationResultConformanceMethodEntity entity)
            throws EntityCreationException {
        CertificationResultConformanceMethodEntity mapping = new CertificationResultConformanceMethodEntity();
        mapping.setCertificationResultId(entity.getCertificationResultId());
        mapping.setConformanceMethodId(entity.getConformanceMethodId());
        mapping.setVersion(entity.getVersion());
        mapping.setCreationDate(new Date());
        mapping.setDeleted(false);
        mapping.setLastModifiedDate(new Date());
        mapping.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.persist(mapping);
        entityManager.flush();

        return new CertificationResultConformanceMethod(mapping);
    }

    public void deleteConformanceMethodMapping(Long mappingId) {
        CertificationResultConformanceMethodEntity toDelete = getCertificationResultConformanceMethodById(mappingId);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.persist(toDelete);
            entityManager.flush();
        }
    }

    /*
    public CertificationResultOptionalStandard lookupOptionalStandardMapping(Long certificationResultId,
            Long optionalStandardId) {
        Query query = entityManager.createQuery("SELECT os " + "FROM CertificationResultOptionalStandardEntity os "
                + "LEFT OUTER JOIN FETCH os.optionalStandard " + "where (NOT os.deleted = true) "
                + "AND (os.certificationResultId = :certificationResultId) "
                + "AND (os.optionalStandardId = :optionalStandardId)", CertificationResultOptionalStandardEntity.class);
        query.setParameter("certificationResultId", certificationResultId);
        query.setParameter("optionalStandardId", optionalStandardId);
        List<CertificationResultOptionalStandardEntity> entities = query.getResultList();

        CertificationResultOptionalStandard result = null;
        if (entities != null && entities.size() > 0) {
            result = new CertificationResultOptionalStandard(entities.get(0));
        }

        return result;
    }
*/
    private CertificationResultConformanceMethodEntity getCertificationResultConformanceMethodById(Long id) {
        CertificationResultConformanceMethodEntity entity = null;

        Query query = entityManager.createQuery(
                "SELECT os " + "FROM CertificationResultConformanceMethodEntity cm "
                        + "LEFT OUTER JOIN FETCH cm.conformanceMethod " + "where (NOT cm.deleted = true) AND (cm.id = :id) ",
                        CertificationResultConformanceMethodEntity.class);
        query.setParameter("id", id);
        List<CertificationResultConformanceMethodEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

  /*  private List<CertificationResultOptionalStandardEntity> getOptionalStandardsForCertification(Long certificationResultId) {
        Query query = entityManager.createQuery(
                "SELECT os " + "FROM CertificationResultOptionalStandardEntity os "
                        + "LEFT OUTER JOIN FETCH os.optionalStandard "
                        + "where (NOT os.deleted = true) AND (certification_result_id = :certificationResultId) ",
                CertificationResultOptionalStandardEntity.class);
        query.setParameter("certificationResultId", certificationResultId);

        List<CertificationResultOptionalStandardEntity> result = query.getResultList();
        if (result == null) {
            return null;
        }
        return result;
    }
*/
    /******************************************************
     * Optional Standard methods.
     *
     *******************************************************/

    public List<CertificationResultOptionalStandard> getOptionalStandardsForCertificationResult(Long certificationResultId) {

        List<CertificationResultOptionalStandardEntity> entities = getOptionalStandardsForCertification(certificationResultId);
        List<CertificationResultOptionalStandard> domains = new ArrayList<CertificationResultOptionalStandard>();

        for (CertificationResultOptionalStandardEntity entity : entities) {
            CertificationResultOptionalStandard domain = new CertificationResultOptionalStandard(entity);
            domains.add(domain);
        }
        return domains;
    }

    public CertificationResultOptionalStandard addOptionalStandardMapping(CertificationResultOptionalStandardEntity entity)
            throws EntityCreationException {
        CertificationResultOptionalStandardEntity mapping = new CertificationResultOptionalStandardEntity();
        mapping.setCertificationResultId(entity.getCertificationResultId());
        mapping.setOptionalStandardId(entity.getOptionalStandardId());
        mapping.setCreationDate(new Date());
        mapping.setDeleted(false);
        mapping.setLastModifiedDate(new Date());
        mapping.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.persist(mapping);
        entityManager.flush();

        return new CertificationResultOptionalStandard(mapping);
    }

    public void deleteOptionalStandardMapping(Long mappingId) {
        CertificationResultOptionalStandardEntity toDelete = getCertificationResultOptionalStandardById(mappingId);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.persist(toDelete);
            entityManager.flush();
        }
    }

    public CertificationResultOptionalStandard lookupOptionalStandardMapping(Long certificationResultId,
            Long optionalStandardId) {
        Query query = entityManager.createQuery("SELECT os " + "FROM CertificationResultOptionalStandardEntity os "
                + "LEFT OUTER JOIN FETCH os.optionalStandard " + "where (NOT os.deleted = true) "
                + "AND (os.certificationResultId = :certificationResultId) "
                + "AND (os.optionalStandardId = :optionalStandardId)", CertificationResultOptionalStandardEntity.class);
        query.setParameter("certificationResultId", certificationResultId);
        query.setParameter("optionalStandardId", optionalStandardId);
        List<CertificationResultOptionalStandardEntity> entities = query.getResultList();

        CertificationResultOptionalStandard result = null;
        if (entities != null && entities.size() > 0) {
            result = new CertificationResultOptionalStandard(entities.get(0));
        }

        return result;
    }

    private CertificationResultOptionalStandardEntity getCertificationResultOptionalStandardById(Long id) {
        CertificationResultOptionalStandardEntity entity = null;

        Query query = entityManager.createQuery(
                "SELECT os " + "FROM CertificationResultOptionalStandardEntity os "
                        + "LEFT OUTER JOIN FETCH os.optionalStandard " + "where (NOT os.deleted = true) AND (os.id = :id) ",
                CertificationResultOptionalStandardEntity.class);
        query.setParameter("id", id);
        List<CertificationResultOptionalStandardEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<CertificationResultOptionalStandardEntity> getOptionalStandardsForCertification(Long certificationResultId) {
        Query query = entityManager.createQuery(
                "SELECT os " + "FROM CertificationResultOptionalStandardEntity os "
                        + "LEFT OUTER JOIN FETCH os.optionalStandard "
                        + "where (NOT os.deleted = true) AND (certification_result_id = :certificationResultId) ",
                CertificationResultOptionalStandardEntity.class);
        query.setParameter("certificationResultId", certificationResultId);

        List<CertificationResultOptionalStandardEntity> result = query.getResultList();
        if (result == null) {
            return null;
        }
        return result;
    }

    /******************************************************
     * Test Standard methods.
     *
     *******************************************************/

    public List<CertificationResultTestStandardDTO> getTestStandardsForCertificationResult(Long certificationResultId) {

        List<CertificationResultTestStandardEntity> entities = getTestStandardsForCertification(certificationResultId);
        List<CertificationResultTestStandardDTO> dtos = new ArrayList<CertificationResultTestStandardDTO>();

        for (CertificationResultTestStandardEntity entity : entities) {
            CertificationResultTestStandardDTO dto = new CertificationResultTestStandardDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    public CertificationResultTestStandardDTO addTestStandardMapping(CertificationResultTestStandardDTO dto)
            throws EntityCreationException {
        CertificationResultTestStandardEntity mapping = new CertificationResultTestStandardEntity();
        mapping.setCertificationResultId(dto.getCertificationResultId());
        mapping.setTestStandardId(dto.getTestStandardId());
        mapping.setCreationDate(new Date());
        mapping.setDeleted(false);
        mapping.setLastModifiedDate(new Date());
        mapping.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.persist(mapping);
        entityManager.flush();

        return new CertificationResultTestStandardDTO(mapping);
    }

    public void deleteTestStandardMapping(Long mappingId) {
        CertificationResultTestStandardEntity toDelete = getCertificationResultTestStandardById(mappingId);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.persist(toDelete);
            entityManager.flush();
        }
    }

    public CertificationResultTestStandardDTO lookupTestStandardMapping(Long certificationResultId,
            Long testStandardId) {
        Query query = entityManager.createQuery("SELECT ts " + "FROM CertificationResultTestStandardEntity ts "
                + "LEFT OUTER JOIN FETCH ts.testStandard " + "where (NOT ts.deleted = true) "
                + "AND (ts.certificationResultId = :certificationResultId) "
                + "AND (ts.testStandardId = :testStandardId)", CertificationResultTestStandardEntity.class);
        query.setParameter("certificationResultId", certificationResultId);
        query.setParameter("testStandardId", testStandardId);
        List<CertificationResultTestStandardEntity> entities = query.getResultList();

        CertificationResultTestStandardDTO result = null;
        if (entities != null && entities.size() > 0) {
            result = new CertificationResultTestStandardDTO(entities.get(0));
        }

        return result;
    }

    private CertificationResultTestStandardEntity getCertificationResultTestStandardById(Long id) {
        CertificationResultTestStandardEntity entity = null;

        Query query = entityManager.createQuery(
                "SELECT ts " + "FROM CertificationResultTestStandardEntity ts "
                        + "LEFT OUTER JOIN FETCH ts.testStandard " + "where (NOT ts.deleted = true) AND (ts.id = :id) ",
                CertificationResultTestStandardEntity.class);
        query.setParameter("id", id);
        List<CertificationResultTestStandardEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<CertificationResultTestStandardEntity> getTestStandardsForCertification(Long certificationResultId) {
        Query query = entityManager.createQuery(
                "SELECT ts " + "FROM CertificationResultTestStandardEntity ts "
                        + "LEFT OUTER JOIN FETCH ts.testStandard "
                        + "where (NOT ts.deleted = true) AND (certification_result_id = :certificationResultId) ",
                CertificationResultTestStandardEntity.class);
        query.setParameter("certificationResultId", certificationResultId);

        List<CertificationResultTestStandardEntity> result = query.getResultList();
        if (result == null) {
            return null;
        }
        return result;
    }

    /******************************************************
     * Test Tool methods
     *
     *******************************************************/

    public List<CertificationResultTestToolDTO> getTestToolsForCertificationResult(Long certificationResultId) {

        List<CertificationResultTestToolEntity> entities = getTestToolsForCertification(certificationResultId);
        List<CertificationResultTestToolDTO> dtos = new ArrayList<CertificationResultTestToolDTO>();

        for (CertificationResultTestToolEntity entity : entities) {
            CertificationResultTestToolDTO dto = new CertificationResultTestToolDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    public CertificationResultTestToolDTO addTestToolMapping(CertificationResultTestToolDTO dto)
            throws EntityCreationException {
        CertificationResultTestToolEntity mapping = new CertificationResultTestToolEntity();
        mapping.setCertificationResultId(dto.getCertificationResultId());
        mapping.setTestToolId(dto.getTestToolId());
        mapping.setVersion(dto.getTestToolVersion());
        mapping.setCreationDate(new Date());
        mapping.setDeleted(false);
        mapping.setLastModifiedDate(new Date());
        mapping.setLastModifiedUser(AuthUtil.getAuditId());
        try {
            entityManager.persist(mapping);
            entityManager.flush();
        } catch (Exception ex) {
            String msg = msgUtil.getMessage("listing.criteria.badTestTool", dto.getTestToolName());
            LOGGER.error(msg, ex);
            throw new EntityCreationException(msg);
        }

        return new CertificationResultTestToolDTO(mapping);
    }

    public void deleteTestToolMapping(Long mappingId) {
        CertificationResultTestToolEntity toDelete = getCertificationResultTestToolById(mappingId);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.persist(toDelete);
            entityManager.flush();
        }
    }

    private CertificationResultTestToolEntity getCertificationResultTestToolById(Long id) {
        CertificationResultTestToolEntity entity = null;

        Query query = entityManager.createQuery("SELECT tt " + "FROM CertificationResultTestToolEntity tt "
                + "LEFT OUTER JOIN FETCH tt.testTool " + "where (NOT tt.deleted = true) AND (tt.id = :id) ",
                CertificationResultTestToolEntity.class);
        query.setParameter("id", id);
        List<CertificationResultTestToolEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<CertificationResultTestToolEntity> getTestToolsForCertification(Long certificationResultId) {
        Query query = entityManager.createQuery(
                "SELECT tt " + "FROM CertificationResultTestToolEntity tt " + "LEFT OUTER JOIN FETCH tt.testTool "
                        + "where (NOT tt.deleted = true) AND (certification_result_id = :certificationResultId) ",
                CertificationResultTestToolEntity.class);
        query.setParameter("certificationResultId", certificationResultId);

        List<CertificationResultTestToolEntity> result = query.getResultList();
        if (result == null) {
            return null;
        }
        return result;
    }

    /******************************************************
     * Test Data for Certification Results
     *
     *******************************************************/

    public List<CertificationResultTestDataDTO> getTestDataForCertificationResult(Long certificationResultId) {

        List<CertificationResultTestDataEntity> entities = getTestDataForCertification(certificationResultId);
        List<CertificationResultTestDataDTO> dtos = new ArrayList<CertificationResultTestDataDTO>();

        for (CertificationResultTestDataEntity entity : entities) {
            CertificationResultTestDataDTO dto = new CertificationResultTestDataDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    public CertificationResultTestDataDTO addTestDataMapping(CertificationResultTestDataDTO dto)
            throws EntityCreationException {
        CertificationResultTestDataEntity mapping = new CertificationResultTestDataEntity();
        mapping = new CertificationResultTestDataEntity();
        mapping.setCertificationResultId(dto.getCertificationResultId());
        mapping.setTestDataId(dto.getTestDataId());
        mapping.setTestDataVersion(dto.getVersion());
        mapping.setAlterationDescription(dto.getAlteration());
        mapping.setCreationDate(new Date());
        mapping.setDeleted(false);
        mapping.setLastModifiedDate(new Date());
        mapping.setLastModifiedUser(AuthUtil.getAuditId());
        try {
            entityManager.persist(mapping);
            entityManager.flush();
        } catch (Exception ex) {
            String msg = msgUtil.getMessage("listing.criteria.badTestData", dto.getVersion());
            LOGGER.error(msg, ex);
            throw new EntityCreationException(msg);
        }

        return new CertificationResultTestDataDTO(mapping);
    }

    public void deleteTestDataMapping(Long mappingId) {
        CertificationResultTestDataEntity toDelete = getCertificationResultTestDataById(mappingId);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.persist(toDelete);
            entityManager.flush();
        }
    }

    public void updateTestDataMapping(CertificationResultTestDataDTO dto) throws EntityRetrievalException {
        CertificationResultTestDataEntity toUpdate = getCertificationResultTestDataById(dto.getId());
        if (toUpdate == null) {
            throw new EntityRetrievalException("Could not find test data mapping with id " + dto.getId());
        }
        toUpdate.setTestDataId(dto.getTestDataId());
        toUpdate.setAlterationDescription(dto.getAlteration());
        toUpdate.setTestDataVersion(dto.getVersion());
        toUpdate.setLastModifiedDate(new Date());
        toUpdate.setLastModifiedUser(AuthUtil.getAuditId());
        try {
            entityManager.persist(toUpdate);
            entityManager.flush();
        } catch (Exception ex) {
            String msg = msgUtil.getMessage("listing.criteria.badTestData", dto.getVersion());
            LOGGER.error(msg, ex);
            throw new EntityRetrievalException(msg);
        }
    }

    private CertificationResultTestDataEntity getCertificationResultTestDataById(Long id) {
        CertificationResultTestDataEntity entity = null;

        Query query = entityManager.createQuery(
                "SELECT td " + "FROM CertificationResultTestDataEntity td " + "LEFT JOIN FETCH td.testData "
                        + "WHERE (NOT td.deleted = true) " + "AND (td.id = :entityid) ",
                CertificationResultTestDataEntity.class);
        query.setParameter("entityid", id);
        List<CertificationResultTestDataEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<CertificationResultTestDataEntity> getTestDataForCertification(Long certificationResultId) {
        Query query = entityManager.createQuery(
                "SELECT td " + "FROM CertificationResultTestDataEntity td " + "LEFT JOIN FETCH td.testData "
                        + "WHERE (NOT td.deleted = true) " + "AND (td.certificationResultId = :certificationResultId) ",
                CertificationResultTestDataEntity.class);
        query.setParameter("certificationResultId", certificationResultId);

        List<CertificationResultTestDataEntity> result = query.getResultList();
        if (result == null) {
            return null;
        }
        return result;
    }

    /******************************************************
     * Test Procedure methods.
     *
     *******************************************************/

    public List<CertificationResultTestProcedureDTO> getTestProceduresForCertificationResult(
            Long certificationResultId) {

        List<CertificationResultTestProcedureEntity> entities = getTestProceduresForCertification(
                certificationResultId);
        List<CertificationResultTestProcedureDTO> dtos = new ArrayList<CertificationResultTestProcedureDTO>();

        for (CertificationResultTestProcedureEntity entity : entities) {
            CertificationResultTestProcedureDTO dto = new CertificationResultTestProcedureDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    public List<CertificationResultTestProcedureDTO> getTestProceduresForListing(Long listingId) {
        return getTestProcedureEntitiesForListing(listingId).stream()
                .map(tp -> new CertificationResultTestProcedureDTO(tp))
                .collect(Collectors.toList());
    }

    public CertificationResultTestProcedureDTO addTestProcedureMapping(CertificationResultTestProcedureDTO dto)
            throws EntityCreationException {
        CertificationResultTestProcedureEntity mapping = new CertificationResultTestProcedureEntity();
        mapping.setCertificationResultId(dto.getCertificationResultId());
        mapping.setTestProcedureId(dto.getTestProcedureId());
        mapping.setVersion(dto.getVersion());
        mapping.setCreationDate(new Date());
        mapping.setDeleted(false);
        mapping.setLastModifiedDate(new Date());
        mapping.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.persist(mapping);
        entityManager.flush();

        return new CertificationResultTestProcedureDTO(mapping);
    }

    public void deleteTestProcedureMapping(Long mappingId) {
        CertificationResultTestProcedureEntity toDelete = getCertificationResultTestProcedureById(mappingId);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.persist(toDelete);
            entityManager.flush();
        }
    }

    private CertificationResultTestProcedureEntity getCertificationResultTestProcedureById(Long id) {
        CertificationResultTestProcedureEntity entity = null;

        Query query = entityManager.createQuery("SELECT tp " + "FROM CertificationResultTestProcedureEntity tp "
                + "LEFT OUTER JOIN FETCH tp.testProcedure " + "WHERE (NOT tp.deleted = true) "
                + "AND (tp.id = :entityid) ", CertificationResultTestProcedureEntity.class);
        query.setParameter("entityid", id);
        List<CertificationResultTestProcedureEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<CertificationResultTestProcedureEntity> getTestProceduresForCertification(
            Long certificationResultId) {
        Query query = entityManager.createQuery(
                "SELECT tp " + "FROM CertificationResultTestProcedureEntity tp "
                        + "LEFT OUTER JOIN FETCH tp.testProcedure " + "WHERE (NOT tp.deleted = true) "
                        + "AND (certification_result_id = :certificationResultId) ",
                CertificationResultTestProcedureEntity.class);
        query.setParameter("certificationResultId", certificationResultId);

        List<CertificationResultTestProcedureEntity> result = query.getResultList();
        if (result == null) {
            return null;
        }
        return result;
    }

    private List<CertificationResultTestProcedureEntity> getTestProcedureEntitiesForListing(Long listingId) {

        String hql = "SELECT tp "
                + "FROM CertificationResultTestProcedureEntity tp "
                + "LEFT OUTER JOIN FETCH tp.testProcedure "
                + "WHERE (NOT tp.deleted = true) "
                + "AND tp.certificationResultId IN "
                + "(SELECT cr.id "
                + "FROM CertificationResultEntity cr "
                + "WHERE cr.certifiedProductId = :listingId "
                + "AND cr.deleted = false)";

        return entityManager.createQuery(hql, CertificationResultTestProcedureEntity.class)
                .setParameter("listingId", listingId)
                .getResultList();
    }

    /******************************************************
     * Test Functionality for Certification Results
     *
     *******************************************************/

    public List<CertificationResultTestFunctionalityDTO> getTestFunctionalityForCertificationResult(
            Long certificationResultId) {

        List<CertificationResultTestFunctionalityEntity> entities = getTestFunctionalityForCertification(
                certificationResultId);
        List<CertificationResultTestFunctionalityDTO> dtos = new ArrayList<CertificationResultTestFunctionalityDTO>();

        for (CertificationResultTestFunctionalityEntity entity : entities) {
            CertificationResultTestFunctionalityDTO dto = new CertificationResultTestFunctionalityDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    public CertificationResultTestFunctionalityDTO addTestFunctionalityMapping(
            CertificationResultTestFunctionalityDTO dto) throws EntityCreationException {
        CertificationResultTestFunctionalityEntity mapping = new CertificationResultTestFunctionalityEntity();
        mapping.setCertificationResultId(dto.getCertificationResultId());
        mapping.setTestFunctionalityId(dto.getTestFunctionalityId());
        mapping.setCreationDate(new Date());
        mapping.setDeleted(false);
        mapping.setLastModifiedDate(new Date());
        mapping.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.persist(mapping);
        entityManager.flush();

        return new CertificationResultTestFunctionalityDTO(mapping);
    }

    public void deleteTestFunctionalityMapping(Long mappingId) {
        CertificationResultTestFunctionalityEntity toDelete = getCertificationResultTestFunctionalityById(mappingId);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.persist(toDelete);
            entityManager.flush();
        }
    }

    private CertificationResultTestFunctionalityEntity getCertificationResultTestFunctionalityById(Long id) {
        CertificationResultTestFunctionalityEntity entity = null;

        Query query = entityManager.createQuery("SELECT crtf " + "FROM CertificationResultTestFunctionalityEntity crtf "
                + "LEFT OUTER JOIN FETCH crtf.testFunctionality tf " + "JOIN FETCH tf.certificationEdition edition "
                + "WHERE (NOT crtf.deleted = true) " + "AND (crtf.id = :entityid) ",
                CertificationResultTestFunctionalityEntity.class);
        query.setParameter("entityid", id);
        List<CertificationResultTestFunctionalityEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<CertificationResultTestFunctionalityEntity> getTestFunctionalityForCertification(
            Long certificationResultId) {
        Query query = entityManager.createQuery("SELECT crtf " + "FROM CertificationResultTestFunctionalityEntity crtf "
                + "LEFT OUTER JOIN FETCH crtf.testFunctionality tf " + "JOIN FETCH tf.certificationEdition edition "
                + "WHERE (NOT crtf.deleted = true) " + "AND (crtf.certificationResultId = :certificationResultId) ",
                CertificationResultTestFunctionalityEntity.class);
        query.setParameter("certificationResultId", certificationResultId);

        List<CertificationResultTestFunctionalityEntity> result = query.getResultList();
        if (result == null) {
            return null;
        }
        return result;
    }

    /******************************************************
     * Test Task
     *
     *******************************************************/

    public List<CertificationResultTestTaskDTO> getTestTasksForCertificationResult(Long certificationResultId) {

        List<CertificationResultTestTaskEntity> entities = getTestTasksForCertification(certificationResultId);
        List<CertificationResultTestTaskDTO> dtos = new ArrayList<CertificationResultTestTaskDTO>();

        for (CertificationResultTestTaskEntity entity : entities) {
            CertificationResultTestTaskDTO dto = new CertificationResultTestTaskDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    public CertificationResultTestTaskDTO addTestTaskMapping(CertificationResultTestTaskDTO dto)
            throws EntityCreationException {
        CertificationResultTestTaskEntity mapping = new CertificationResultTestTaskEntity();
        mapping.setCertificationResultId(dto.getCertificationResultId());
        mapping.setTestTaskId(dto.getTestTaskId());
        mapping.setCreationDate(new Date());
        mapping.setDeleted(false);
        mapping.setLastModifiedDate(new Date());
        mapping.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.persist(mapping);
        entityManager.flush();

        if (dto.getTestTask() != null && dto.getTestTask().getParticipants() != null) {
            for (TestParticipantDTO participant : dto.getTestTask().getParticipants()) {
                addTestParticipantMapping(dto.getTestTask(), participant);
            }
        }
        return new CertificationResultTestTaskDTO(mapping);
    }

    public void deleteTestTaskMapping(Long certResultId, Long taskId) {
        Query query = entityManager.createQuery(
                "SELECT ttMapping " + "FROM CertificationResultTestTaskEntity ttMapping "
                        + "LEFT OUTER JOIN FETCH ttMapping.testTask task "
                        + "LEFT OUTER JOIN FETCH task.testParticipants participantMappings "
                        + "LEFT OUTER JOIN FETCH participantMappings.testParticipant participant "
                        + "WHERE (ttMapping.deleted <> true) " + "AND (task.id = :taskId) "
                        + "AND ttMapping.certificationResultId = :certResultId ",
                CertificationResultTestTaskEntity.class);
        query.setParameter("taskId", taskId);
        query.setParameter("certResultId", certResultId);

        List<CertificationResultTestTaskEntity> toDeleteList = query.getResultList();
        if (toDeleteList != null && toDeleteList.size() > 0) {
            for (CertificationResultTestTaskEntity toDelete : toDeleteList) {
                toDelete.setDeleted(true);
                toDelete.setLastModifiedDate(new Date());
                toDelete.setLastModifiedUser(AuthUtil.getAuditId());
                entityManager.persist(toDelete);
            }
            entityManager.flush();
        }

        // is this test task used anywhere else? if not, delete it and all of
        // its participants
        query = entityManager.createQuery(
                "SELECT ttMapping " + "FROM CertificationResultTestTaskEntity ttMapping "
                        + "LEFT OUTER JOIN FETCH ttMapping.testTask task "
                        + "LEFT OUTER JOIN FETCH task.testParticipants participantMappings "
                        + "LEFT OUTER JOIN FETCH participantMappings.testParticipant participant "
                        + "WHERE (ttMapping.deleted <> true) " + "AND (task.id = :taskId) ",
                CertificationResultTestTaskEntity.class);
        query.setParameter("taskId", taskId);

        List<CertificationResultTestTaskEntity> otherMappingsForTestTask = query.getResultList();
        if (otherMappingsForTestTask == null || otherMappingsForTestTask.size() == 0) {
            TestTaskEntity taskToDelete = entityManager.find(TestTaskEntity.class, taskId);
            testTaskDao.delete(taskId);

            // check each participant
            if (taskToDelete.getTestParticipants() != null && taskToDelete.getTestParticipants().size() > 0) {
                for (TestTaskParticipantMapEntity participantMap : taskToDelete.getTestParticipants()) {
                    deleteTestParticipantMapping(taskId, participantMap.getTestParticipantId());
                }
            }
        }
    }

    private List<CertificationResultTestTaskEntity> getTestTasksForCertification(Long certificationResultId) {
        Query query = entityManager.createQuery(
                "SELECT tp " + "FROM CertificationResultTestTaskEntity tp " + "LEFT OUTER JOIN FETCH tp.testTask task "
                        + "LEFT OUTER JOIN FETCH task.testParticipants participantMappings "
                        + "LEFT OUTER JOIN FETCH participantMappings.testParticipant participant "
                        + "LEFT OUTER JOIN FETCH participant.education education "
                        + "LEFT OUTER JOIN FETCH participant.ageRange ageRange "
                        + "where (NOT tp.deleted = true) AND (certification_result_id = :certificationResultId) ",
                CertificationResultTestTaskEntity.class);
        query.setParameter("certificationResultId", certificationResultId);

        List<CertificationResultTestTaskEntity> result = query.getResultList();
        if (result == null) {
            return null;
        }
        return result;
    }

    public TestParticipantDTO addTestParticipantMapping(TestTaskDTO task, TestParticipantDTO participant)
            throws EntityCreationException {
        boolean createMapping = false;
        if (participant.getId() == null) {
            participant = participantDao.create(participant);
            createMapping = true;
        } else {
            Query query = entityManager.createQuery(
                    "SELECT participantMap " + "FROM TestTaskParticipantMapEntity participantMap "
                            + "WHERE participantMap.deleted <> true " + "AND participantMap.testTaskId = :testTaskId "
                            + "AND participantMap.testParticipantId = :testParticipantId",
                    TestTaskParticipantMapEntity.class);
            query.setParameter("testTaskId", task.getId());
            query.setParameter("testParticipantId", participant.getId());
            List<TestTaskParticipantMapEntity> existingMappings = query.getResultList();
            if (existingMappings == null || existingMappings.size() == 0) {
                createMapping = true;
            }
        }

        if (createMapping) {
            TestTaskParticipantMapEntity mapping = new TestTaskParticipantMapEntity();
            mapping.setTestParticipantId(participant.getId());
            mapping.setTestTaskId(task.getId());
            mapping.setCreationDate(new Date());
            mapping.setDeleted(false);
            mapping.setLastModifiedDate(new Date());
            mapping.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.persist(mapping);
            entityManager.flush();
        }

        return participant;
    }

    public void deleteTestParticipantMapping(Long testTaskId, Long testParticipantId) {
        Query query = entityManager.createQuery("SELECT mapping " + "FROM TestTaskParticipantMapEntity mapping "
                + "WHERE (NOT mapping.deleted = true) " + "AND mapping.testTaskId = :testTaskId "
                + "AND mapping.testParticipantId = :testParticipantId ", TestTaskParticipantMapEntity.class);
        query.setParameter("testTaskId", testTaskId);
        query.setParameter("testParticipantId", testParticipantId);

        List<TestTaskParticipantMapEntity> toDeleteList = query.getResultList();
        if (toDeleteList != null && toDeleteList.size() > 0) {
            for (TestTaskParticipantMapEntity toDelete : toDeleteList) {
                toDelete.setDeleted(true);
                toDelete.setLastModifiedDate(new Date());
                toDelete.setLastModifiedUser(AuthUtil.getAuditId());
                entityManager.persist(toDelete);
            }
            entityManager.flush();
        }

        // does the test participant have mappings to any other tasks?
        // if not, mark them deleted too
        Query otherParticipantMappingsQuery = entityManager.createQuery(
                "SELECT mapping " + "FROM TestTaskParticipantMapEntity mapping " + "WHERE (NOT mapping.deleted = true) "
                        + "AND mapping.testParticipantId = :testParticipantId ",
                TestTaskParticipantMapEntity.class);
        otherParticipantMappingsQuery.setParameter("testParticipantId", testParticipantId);
        List<TestTaskParticipantMapEntity> otherParticipantMappingsResults = otherParticipantMappingsQuery
                .getResultList();
        if (otherParticipantMappingsResults == null || otherParticipantMappingsResults.size() == 0) {
            TestParticipantEntity participantEntity = entityManager.find(TestParticipantEntity.class,
                    testParticipantId);
            participantEntity.setDeleted(true);
            participantEntity.setLastModifiedDate(new Date());
            participantEntity.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.persist(participantEntity);
            entityManager.flush();
        }
    }

    /******************************************************
     * SVAP
     *
     *******************************************************/
    public List<CertificationResultSvap> getSvapForCertificationResult(Long certificationResultId) {
        List<CertificationResultSvapEntity> entities = getSvapForCertification(certificationResultId);
        return entities.stream()
                .map(e -> new CertificationResultSvap(e))
                .collect(Collectors.toList());
    }


    public CertificationResultSvap addCertificationResultSvap(CertificationResultSvap certificationResultSvapToAdd,
            Long certificationResultId) {

        CertificationResultSvapEntity entity = new CertificationResultSvapEntity();
        entity.setCertificationResultId(certificationResultId);
        entity.setSvapId(certificationResultSvapToAdd.getSvapId());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());
        entity.setCreationDate(new Date());
        entity.setDeleted(false);
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.refresh(entity);

        return new CertificationResultSvap(getCertificationResultSvapEntityById(entity.getId()));
    }

    public void deleteCertificationResultSvap(CertificationResultSvap certificationResultSvapToDelete) {
        CertificationResultSvapEntity entity = getCertificationResultSvapEntityById(certificationResultSvapToDelete.getId());
        entity.setDeleted(true);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.refresh(entity);
    }

    private CertificationResultSvapEntity getCertificationResultSvapEntityById(Long certificationResultSvapId) {
        Query query = entityManager.createQuery(
                "SELECT s "
                        + "FROM CertificationResultSvapEntity s "
                        + "JOIN FETCH s.svap "
                        + "WHERE s.deleted <> true "
                        + "AND s.id = :certificationResultSvapId ",
                CertificationResultSvapEntity.class);
        query.setParameter("certificationResultSvapId", certificationResultSvapId);

        List<CertificationResultSvapEntity> result = query.getResultList();

        if (result.size() > 0) {
            return result.get(0);
        } else {
            return null;
        }
    }

    private List<CertificationResultSvapEntity> getSvapForCertification(Long certificationResultId) {
        Query query = entityManager.createQuery(
                "SELECT s "
                        + "FROM CertificationResultSvapEntity s "
                        + "JOIN FETCH s.svap "
                        + "WHERE s.deleted <> true "
                        + "AND s.certificationResultId = :certificationResultId ",
                CertificationResultSvapEntity.class);
        query.setParameter("certificationResultId", certificationResultId);

        List<CertificationResultSvapEntity> result = query.getResultList();
        if (result == null) {
            return null;
        }
        return result;
    }
}
