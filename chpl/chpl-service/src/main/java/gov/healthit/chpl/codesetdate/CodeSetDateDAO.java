package gov.healthit.chpl.codesetdate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.listing.CertificationResultEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CodeSetDateDAO extends BaseDAOImpl  {

    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    public CodeSetDateDAO(CertifiedProductDAO certifiedProductDAO) {
        this.certifiedProductDAO = certifiedProductDAO;
    }

    public CodeSetDate getById(Long id) {
        try {
            CodeSetDateEntity entity = getEntityById(id);
            if (entity != null) {
                return entity.toDomainWithCriteria();
            } else {
                return null;
            }
        } catch (EntityRetrievalException e) {
            LOGGER.error("Error retrieving Code Set Date: {}", e.getMessage(), e);
            return null;
        }
    }

    public List<CodeSetDate> findAll() {
        List<CodeSetDateEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomainWithCriteria())
                .collect(Collectors.toList());
    }

    @CacheEvict(value = CacheNames.CODE_SET_DATES, allEntries = true)
    public CodeSetDate add(CodeSetDate codeSetDate) {
        CodeSetDateEntity entity = CodeSetDateEntity.builder()
                .requiredDay(codeSetDate.getRequiredDay())
                .build();

        create(entity);
        return getById(entity.getId());
    }

    @CacheEvict(value = CacheNames.CODE_SET_DATES, allEntries = true)
    public void update(CodeSetDate codeSetDate) throws EntityRetrievalException {
        CodeSetDateEntity entity = getEntityById(codeSetDate.getId());
        entity.setRequiredDay(codeSetDate.getRequiredDay());
        update(entity);
    }

    @CacheEvict(value = CacheNames.CODE_SET_DATES, allEntries = true)
    public void remove(CodeSetDate codeSetDate) throws EntityRetrievalException {
        CodeSetDateEntity entity = getEntityById(codeSetDate.getId());
        entity.setDeleted(true);
        update(entity);


    }

    public List<CertifiedProductDetailsDTO> getCertifiedProductsByCodeSetDateAndCriteria(CodeSetDate codeSetDate, CertificationCriterion criterion)
            throws EntityRetrievalException {
        List<Long> certifiedProductIds = getCertifiedProductIdsUsingCodeSetDateIdWithCriterion(codeSetDate.getId(), criterion.getId());
        return certifiedProductDAO.getDetailsByIds(certifiedProductIds);
    }

    @CacheEvict(value = CacheNames.CODE_SET_DATES, allEntries = true)
    public void addCodeSetDateCriteriaMap(CodeSetDate codeSetDate, CertificationCriterion criterion) {
        CodeSetDateCriteriaMapEntity entity = CodeSetDateCriteriaMapEntity.builder()
                .certificationCriterionId(criterion.getId())
                .codeSetDateId(codeSetDate.getId())
                .build();

        create(entity);
    }

    @CacheEvict(value = CacheNames.CODE_SET_DATES, allEntries = true)
    public void removeCodeSetDateCriteriaMap(CodeSetDate codeSetDate, CertificationCriterion criterion) {
        try {
            CodeSetDateCriteriaMapEntity entity = getCodeSetDateCriteriaMapByCodeSetDateAndCriterion(codeSetDate.getId(), criterion.getId());
            entity.setDeleted(true);

            update(entity);
        } catch (EntityRetrievalException e) {
            LOGGER.catching(e);
            return;
        }
    }

    public List<CertifiedProductDetailsDTO> getCertifiedProductsByCodeSetDate(CodeSetDate codeSetDate) throws EntityRetrievalException {
        List<Long> certifiedProductIds = getCertifiedProductIdsUsingCodeSetDateId(codeSetDate.getId());
        return certifiedProductDAO.getDetailsByIds(certifiedProductIds);
    }

    @Cacheable(CacheNames.CODE_SET_DATES)
    public Map<Long, List<CodeSetDate>> getCodeSetDateCriteriaMaps() {
        List<CodeSetDate> allCodeSetDates = findAll();
        Map<Long, List<CodeSetDate>> mapping = new HashMap<Long, List<CodeSetDate>>();
        allCodeSetDates.stream()
            .forEach(codeSetDate -> updateMapping(mapping, codeSetDate));
        return mapping;
    }

    @Transactional
    public List<CodeSetDateCriteriaMap> getAllCodeSetDateCriteriaMap() throws EntityRetrievalException {
        return getAllCodeSetDateCriteriaMapEntities().stream()
                .map(e -> e.toDomain())
                .collect(Collectors.toList());
    }

    private CodeSetDateEntity getEntityById(Long id) throws EntityRetrievalException {
        CodeSetDateEntity entity = null;

        Query query = entityManager
                .createQuery("SELECT DISTINCT csd "
                        + "FROM CodeSetDateEntity csd "
                        + "LEFT OUTER JOIN FETCH csd.mappedCriteria criteriaMapping "
                        + "LEFT OUTER JOIN FETCH criteriaMapping.criterion criterion "
                        + "LEFT OUTER JOIN FETCH criterion.certificationEdition "
                        + "LEFT JOIN FETCH criterion.rule "
                        + "WHERE (NOT csd.deleted = true) "
                        + "AND (csd.id = :entityid) ",
                        CodeSetDateEntity.class);
        query.setParameter("entityid", id);
        List<CodeSetDateEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate code set date id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private List<CodeSetDateEntity> getAllEntities() {
        return entityManager
                .createQuery("SELECT DISTINCT csd "
                            + "FROM CodeSetDateEntity csd "
                            + "LEFT OUTER JOIN FETCH csd.mappedCriteria criteriaMapping "
                            + "LEFT OUTER JOIN FETCH criteriaMapping.criterion criterion "
                            + "LEFT OUTER JOIN FETCH criterion.certificationEdition "
                            + "LEFT JOIN FETCH criterion.rule "
                            + "WHERE (NOT csd.deleted = true) ", CodeSetDateEntity.class)
                .getResultList();
    }

    private List<Long> getCertifiedProductIdsUsingCodeSetDateIdWithCriterion(Long codeSetDateId, Long criterionId) {
        List<CertificationResultEntity> certResultsWithCodeSetDate =
                entityManager.createQuery("SELECT cr "
                        + "FROM CertificationResultCodeSetDateEntity crcsd, CertificationResultEntity cr "
                        + "WHERE crcsd.certificationResultId = cr.id "
                        + "AND crcsd.codeSetDate.id = :codeSetDateId "
                        + "AND cr.certificationCriterionId= :criterionId "
                        + "AND crcsd.deleted <> true "
                        + "AND cr.deleted <> true ",
                        CertificationResultEntity.class)
                .setParameter("codeSetDateId", codeSetDateId)
                .setParameter("criterionId", criterionId)
                .getResultList();

        return certResultsWithCodeSetDate.stream()
                .map(certResult -> certResult.getCertifiedProductId())
                .distinct()
                .collect(Collectors.toList());
    }

    private CodeSetDateCriteriaMapEntity getCodeSetDateCriteriaMapByCodeSetDateAndCriterion(Long codeSetDateId, Long certificationCriterionId) throws EntityRetrievalException {
        List<CodeSetDateCriteriaMapEntity> result = entityManager.createQuery("SELECT DISTINCT csdcm "
                        + "FROM CodeSetDateCriteriaMapEntity csdcm "
                        + "JOIN FETCH csdcm.criterion c "
                        + "JOIN FETCH csdcm.codeSetDate csd "
                        + "WHERE c.id = :certificationCriterionId "
                        + "AND csd.id= :codeSetDateId "
                        + "AND csdcm.deleted <> true "
                        + "AND csd.deleted <> true "
                        + "AND c.deleted <> true",
                        CodeSetDateCriteriaMapEntity.class)
                .setParameter("codeSetDateId", codeSetDateId)
                .setParameter("certificationCriterionId", certificationCriterionId)
                .getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate code set date criteria map id in database.");
        } else if (result.size() == 0) {
            throw new EntityRetrievalException("Data error. Could not locate code set date criteria map {" + codeSetDateId + ", " + certificationCriterionId + "} in database.");
        }

        return result.get(0);
    }

    private List<Long> getCertifiedProductIdsUsingCodeSetDateId(Long codeSetId) {
        List<CertificationResultEntity> certResultsWithCodeSetDate =
                entityManager.createQuery("SELECT cr "
                        + "FROM CertificationResultCodeSetDateEntity crcsd, CertificationResultEntity cr "
                        + "WHERE crcsd.certificationResultId = cr.id "
                        + "AND crcsd.codeSetDate.id = :codeSetDateId "
                        + "AND crcsd.deleted <> true "
                        + "AND cr.deleted <> true ",
                        CertificationResultEntity.class)
                .setParameter("codeSetDateId", codeSetId)
                .getResultList();

        return certResultsWithCodeSetDate.stream()
                .map(certResult -> certResult.getCertifiedProductId())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<CodeSetDateCriteriaMapEntity> getAllCodeSetDateCriteriaMapEntities() throws EntityRetrievalException {
        return entityManager.createQuery("SELECT DISTINCT csdcm "
                        + "FROM CodeSetDateCriteriaMapEntity csdcm "
                        + "JOIN FETCH csdcm.criterion c "
                        + "JOIN FETCH c.certificationEdition "
                        + "JOIN FETCH c.rule "
                        + "JOIN FETCH csdcm.codeSetDate csd "
                        + "WHERE csdcm.deleted <> true "
                        + "AND csd.deleted <> true ",
                        CodeSetDateCriteriaMapEntity.class)
                .getResultList();
    }

    private void updateMapping(Map<Long, List<CodeSetDate>> mapping, CodeSetDate codeSetDate) {
        codeSetDate.getCriteria().stream()
            .forEach(codeSetDateCriterion -> {
                if (!mapping.containsKey(codeSetDateCriterion.getId())) {
                    mapping.put(codeSetDateCriterion.getId(), new ArrayList<CodeSetDate>());
                }
                mapping.get(codeSetDateCriterion.getId()).add(codeSetDate);
            });
    }

}
