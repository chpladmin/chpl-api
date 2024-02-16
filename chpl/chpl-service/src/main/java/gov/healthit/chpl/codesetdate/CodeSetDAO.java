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
public class CodeSetDAO extends BaseDAOImpl  {

    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    public CodeSetDAO(CertifiedProductDAO certifiedProductDAO) {
        this.certifiedProductDAO = certifiedProductDAO;
    }

    public CodeSet getById(Long id) {
        try {
            CodeSetEntity entity = getEntityById(id);
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

    public List<CodeSet> findAll() {
        List<CodeSetEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomainWithCriteria())
                .collect(Collectors.toList());
    }

    @CacheEvict(value = CacheNames.CODE_SET, allEntries = true)
    public CodeSet add(CodeSet codeSet) {
        CodeSetEntity entity = CodeSetEntity.builder()
                .requiredDay(codeSet.getRequiredDay())
                .startDay(codeSet.getStartDay())
                .build();

        create(entity);
        return getById(entity.getId());
    }

    @CacheEvict(value = CacheNames.CODE_SET, allEntries = true)
    public void update(CodeSet codeSet) throws EntityRetrievalException {
        CodeSetEntity entity = getEntityById(codeSet.getId());
        entity.setRequiredDay(codeSet.getRequiredDay());
        entity.setStartDay(codeSet.getStartDay());
        update(entity);
    }

    @CacheEvict(value = CacheNames.CODE_SET, allEntries = true)
    public void remove(CodeSet codeSet) throws EntityRetrievalException {
        CodeSetEntity entity = getEntityById(codeSet.getId());
        entity.setDeleted(true);
        update(entity);


    }

    public List<CertifiedProductDetailsDTO> getCertifiedProductsByCodeSetAndCriteria(CodeSet codeSet, CertificationCriterion criterion)
            throws EntityRetrievalException {
        List<Long> certifiedProductIds = getCertifiedProductIdsUsingCodeSetIdWithCriterion(codeSet.getId(), criterion.getId());
        return certifiedProductDAO.getDetailsByIds(certifiedProductIds);
    }

    @CacheEvict(value = CacheNames.CODE_SET, allEntries = true)
    public void addCodeSetCriteriaMap(CodeSet codeSet, CertificationCriterion criterion) {
        CodeSetCriteriaMapEntity entity = CodeSetCriteriaMapEntity.builder()
                .certificationCriterionId(criterion.getId())
                .codeSetId(codeSet.getId())
                .build();

        create(entity);
    }

    @CacheEvict(value = CacheNames.CODE_SET, allEntries = true)
    public void removeCodeSetCriteriaMap(CodeSet codeSet, CertificationCriterion criterion) {
        try {
            CodeSetCriteriaMapEntity entity = getCodeSetCriteriaMapByCodeSetAndCriterion(codeSet.getId(), criterion.getId());
            entity.setDeleted(true);

            update(entity);
        } catch (EntityRetrievalException e) {
            LOGGER.catching(e);
            return;
        }
    }

    public List<CertifiedProductDetailsDTO> getCertifiedProductsByCodeSet(CodeSet codeSet) throws EntityRetrievalException {
        List<Long> certifiedProductIds = getCertifiedProductIdsUsingCodeSetId(codeSet.getId());
        return certifiedProductDAO.getDetailsByIds(certifiedProductIds);
    }

    @Cacheable(CacheNames.CODE_SET)
    public Map<Long, List<CodeSet>> getCodeSetCriteriaMaps() {
        List<CodeSet> allCodeSets = findAll();
        Map<Long, List<CodeSet>> mapping = new HashMap<Long, List<CodeSet>>();
        allCodeSets.stream()
            .forEach(codeSet -> updateMapping(mapping, codeSet));
        return mapping;
    }

    @Transactional
    public List<CodeSetCriteriaMap> getAllCodeSetCriteriaMap() throws EntityRetrievalException {
        return getAllCodeSetCriteriaMapEntities().stream()
                .map(e -> e.toDomain())
                .collect(Collectors.toList());
    }

    private CodeSetEntity getEntityById(Long id) throws EntityRetrievalException {
        CodeSetEntity entity = null;

        Query query = entityManager
                .createQuery("SELECT DISTINCT cs "
                        + "FROM CodeSetEntity cs "
                        + "LEFT OUTER JOIN FETCH cs.mappedCriteria criteriaMapping "
                        + "LEFT OUTER JOIN FETCH criteriaMapping.criterion criterion "
                        + "LEFT OUTER JOIN FETCH criterion.certificationEdition "
                        + "LEFT JOIN FETCH criterion.rule "
                        + "WHERE (NOT cs.deleted = true) "
                        + "AND (csd.id = :entityid) ",
                        CodeSetEntity.class);
        query.setParameter("entityid", id);
        List<CodeSetEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate code set id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private List<CodeSetEntity> getAllEntities() {
        return entityManager
                .createQuery("SELECT DISTINCT cs "
                            + "FROM CodeSetEntity cs "
                            + "LEFT OUTER JOIN FETCH cs.mappedCriteria criteriaMapping "
                            + "LEFT OUTER JOIN FETCH criteriaMapping.criterion criterion "
                            + "LEFT OUTER JOIN FETCH criterion.certificationEdition "
                            + "LEFT JOIN FETCH criterion.rule "
                            + "WHERE (NOT cs.deleted = true) ", CodeSetEntity.class)
                .getResultList();
    }

    private List<Long> getCertifiedProductIdsUsingCodeSetIdWithCriterion(Long codeSetId, Long criterionId) {
        List<CertificationResultEntity> certResultsWithCodeSet =
                entityManager.createQuery("SELECT cr "
                        + "FROM CertificationResultCodeSetEntity crcs, CertificationResultEntity cr "
                        + "WHERE crcs.certificationResultId = cr.id "
                        + "AND crcs.codeSet.id = :codeSetId "
                        + "AND cr.certificationCriterionId= :criterionId "
                        + "AND crcs.deleted <> true "
                        + "AND cr.deleted <> true ",
                        CertificationResultEntity.class)
                .setParameter("codeSetId", codeSetId)
                .setParameter("criterionId", criterionId)
                .getResultList();

        return certResultsWithCodeSet.stream()
                .map(certResult -> certResult.getCertifiedProductId())
                .distinct()
                .collect(Collectors.toList());
    }

    private CodeSetCriteriaMapEntity getCodeSetCriteriaMapByCodeSetAndCriterion(Long codeSetId, Long certificationCriterionId) throws EntityRetrievalException {
        List<CodeSetCriteriaMapEntity> result = entityManager.createQuery("SELECT DISTINCT cscm "
                        + "FROM CodeSetCriteriaMapEntity cscm "
                        + "JOIN FETCH cscm.criterion c "
                        + "JOIN FETCH cscm.codeSet cs "
                        + "WHERE c.id = :certificationCriterionId "
                        + "AND cs.id= :codeSetId "
                        + "AND cscm.deleted <> true "
                        + "AND cs.deleted <> true "
                        + "AND c.deleted <> true",
                        CodeSetCriteriaMapEntity.class)
                .setParameter("codeSetId", codeSetId)
                .setParameter("certificationCriterionId", certificationCriterionId)
                .getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate code set criteria map id in database.");
        } else if (result.size() == 0) {
            throw new EntityRetrievalException("Data error. Could not locate code set criteria map {" + codeSetId + ", " + certificationCriterionId + "} in database.");
        }

        return result.get(0);
    }

    private List<Long> getCertifiedProductIdsUsingCodeSetId(Long codeSetId) {
        List<CertificationResultEntity> certResultsWithCodeSet =
                entityManager.createQuery("SELECT cr "
                        + "FROM CertificationResultCodeSetEntity crcs, CertificationResultEntity cr "
                        + "WHERE crcs.certificationResultId = cr.id "
                        + "AND crcs.codeSet.id = :codeSetId "
                        + "AND crcs.deleted <> true "
                        + "AND cr.deleted <> true ",
                        CertificationResultEntity.class)
                .setParameter("codeSetId", codeSetId)
                .getResultList();

        return certResultsWithCodeSet.stream()
                .map(certResult -> certResult.getCertifiedProductId())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<CodeSetCriteriaMapEntity> getAllCodeSetCriteriaMapEntities() throws EntityRetrievalException {
        return entityManager.createQuery("SELECT DISTINCT cscm "
                        + "FROM CodeSetCriteriaMapEntity cscm "
                        + "JOIN FETCH cscm.criterion c "
                        + "JOIN FETCH c.certificationEdition "
                        + "JOIN FETCH c.rule "
                        + "JOIN FETCH cscm.codeSet cs "
                        + "WHERE cscm.deleted <> true "
                        + "AND cs.deleted <> true ",
                        CodeSetCriteriaMapEntity.class)
                .getResultList();
    }

    private void updateMapping(Map<Long, List<CodeSet>> mapping, CodeSet codeSet) {
        codeSet.getCriteria().stream()
            .forEach(codeSetCriterion -> {
                if (!mapping.containsKey(codeSetCriterion.getId())) {
                    mapping.put(codeSetCriterion.getId(), new ArrayList<CodeSet>());
                }
                mapping.get(codeSetCriterion.getId()).add(codeSet);
            });
    }

}
