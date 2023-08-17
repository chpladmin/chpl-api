package gov.healthit.chpl.criteriaattribute.functionalitytested;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.criteriaattribute.CriteriaAttribute;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeCriteriaMap;
import gov.healthit.chpl.criteriaattribute.CriteriaAttributeDAO;
import gov.healthit.chpl.criteriaattribute.rule.RuleDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.PracticeTypeDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.PracticeTypeEntity;
import gov.healthit.chpl.entity.listing.CertificationResultEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository("functionalityTestedDAO")
public class FunctionalityTestedDAO extends BaseDAOImpl implements CriteriaAttributeDAO {

    private RuleDAO ruleDAO;
    private CertifiedProductDAO certifiedProductDAO;
    private PracticeTypeDAO practiceTypeDAO;

    @Autowired
    public FunctionalityTestedDAO(RuleDAO ruleDAO, CertifiedProductDAO certifiedProductDAO, PracticeTypeDAO practiceTypeDAO) {
        this.ruleDAO = ruleDAO;
        this.certifiedProductDAO = certifiedProductDAO;
        this.practiceTypeDAO = practiceTypeDAO;
    }

    public FunctionalityTested getById(Long id) {
        try {
            FunctionalityTestedEntity entity = getEntityById(id);
            if (entity != null) {
                return entity.toDomain();
            } else {
                return null;
            }
        } catch (EntityRetrievalException e) {
            LOGGER.error("Error retrieving Functionality Tested: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public CriteriaAttribute add(CriteriaAttribute criteriaAttribute) {
        FunctionalityTested ft = (FunctionalityTested) criteriaAttribute;

        FunctionalityTestedEntity entity = FunctionalityTestedEntity.builder()
                .name(criteriaAttribute.getValue()) //TODO: OCD-4288 - Remove when column is removed
                .number(criteriaAttribute.getRegulatoryTextCitation()) //TODO: OCD-4288 - Remove when column is removed
                .value(criteriaAttribute.getValue())
                .regulatoryTextCitation(criteriaAttribute.getRegulatoryTextCitation())
                .startDay(criteriaAttribute.getStartDay())
                .endDay(criteriaAttribute.getEndDay())
                .requiredDay(criteriaAttribute.getRequiredDay())
                .rule(criteriaAttribute.getRule() != null && criteriaAttribute.getRule().getId() != null
                        ? ruleDAO.getRuleEntityById(criteriaAttribute.getRule().getId())
                        : null)
                .practiceType(ft.getPracticeType() != null && ft.getPracticeType().getId() != null
                        ? getPracticeTypeEntity(ft.getPracticeType().getId())
                        : null)
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .lastModifiedUser(AuthUtil.getAuditId())
                .deleted(false)
                .build();

        create(entity);

        return getById(entity.getId());
    }

    @Override
    public CriteriaAttribute getCriteriaAttributeById(Long id) {
        return getById(id);
    }


    @Override
    public List<CriteriaAttributeCriteriaMap> getAllAssociatedCriteriaMaps() throws EntityRetrievalException {
        return getAllFunctionalityTestedCriteriaMap().stream()
                .map(map -> CriteriaAttributeCriteriaMap.builder()
                        .criterion(map.getCriterion())
                        .criteriaAttribute(map.getFunctionalityTested())
                        .build())
                .toList();
    }

    @Override
    public List<CertifiedProductDetailsDTO> getCertifiedProductsByCriteriaAttributeAndCriteria(CriteriaAttribute criteriaAttribute, CertificationCriterion criterion)
            throws EntityRetrievalException {
        List<Long> certifiedProductIds = getCertifiedProductIdsUsingFunctionalityTestedIdWithCriterion(criteriaAttribute.getId(), criterion.getId());
        return certifiedProductDAO.getDetailsByIds(certifiedProductIds);
    }

    @Override
    public void update(CriteriaAttribute criteriaAttribute) throws EntityRetrievalException {
        FunctionalityTestedEntity entity = getEntityById(criteriaAttribute.getId());

        entity.setValue(criteriaAttribute.getValue());
        entity.setRegulatoryTextCitation(criteriaAttribute.getRegulatoryTextCitation());
        entity.setStartDay(criteriaAttribute.getStartDay());
        entity.setEndDay(criteriaAttribute.getEndDay());
        entity.setRequiredDay(criteriaAttribute.getRequiredDay());
        if (criteriaAttribute.getRule() != null) {
            entity.setRule(ruleDAO.getRuleEntityById(criteriaAttribute.getRule().getId()));
        } else {
            entity.setRule(null);
        }

        FunctionalityTested ft = (FunctionalityTested) criteriaAttribute;
        if (ft.getPracticeType() != null && ft.getPracticeType().getId() != null) {
            entity.setPracticeType(practiceTypeDAO.getEntityById(ft.getPracticeType().getId()));
        } else {
            entity.setPracticeType(null);
        }

        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        update(entity);
    }

    //public void update

    @Override
    public void addCriteriaAttributeCriteriaMap(CriteriaAttribute criteriaAttribute, CertificationCriterion criterion) {
        FunctionalityTestedCriteriaMapEntity entity = FunctionalityTestedCriteriaMapEntity.builder()
                .certificationCriterionId(criterion.getId())
                .functionalityTestedId(criteriaAttribute.getId())
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .lastModifiedUser(AuthUtil.getAuditId())
                .deleted(false)
                .build();

        create(entity);
    }

    @Override
    public void removeCriteriaAttributeCriteriaMap(CriteriaAttribute criteriaAttribute, CertificationCriterion criterion) {
        try {
            FunctionalityTestedCriteriaMapEntity entity = getFunctionalityTestedCriteriaMapByTestToolAndCriterionEntity(criteriaAttribute.getId(), criterion.getId());
            entity.setDeleted(true);
            entity.setLastModifiedDate(new Date());
            entity.setLastModifiedUser(AuthUtil.getAuditId());

            update(entity);
        } catch (EntityRetrievalException e) {
            LOGGER.catching(e);
            return;
        }
    }

    @Override
    public List<CertifiedProductDetailsDTO> getCertifiedProductsByCriteriaAttribute(CriteriaAttribute criteriaAttribute) throws EntityRetrievalException {
        List<Long> certifiedProductIds = getCertifiedProductIdsUsingFunctionalityTestedId(criteriaAttribute.getId());
        return certifiedProductDAO.getDetailsByIds(certifiedProductIds);
    }

    @Override
    public void remove(CriteriaAttribute criteriaAttribute) throws EntityRetrievalException {
        FunctionalityTestedEntity entity = getEntityById(criteriaAttribute.getId());
        entity.setDeleted(true);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());
        update(entity);
    }

    public List<FunctionalityTested> findAll() {
        List<FunctionalityTestedEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    @Cacheable(CacheNames.FUNCTIONALITY_TESTED_MAPS)
    public Map<Long, List<FunctionalityTested>> getFunctionalitiesTestedCriteriaMaps() {
        List<FunctionalityTested> allFunctionalityTested = findAll();
        Map<Long, List<FunctionalityTested>> mapping = new HashMap<Long, List<FunctionalityTested>>();
        allFunctionalityTested.stream()
            .forEach(funcTest -> updateMapping(mapping, funcTest));
        return mapping;
    }

    @Transactional
    public List<FunctionalityTestedCriteriaMap> getAllFunctionalityTestedCriteriaMap() throws EntityRetrievalException {
        return getAllFunctionalityTestedCriteriaMapEntities().stream()
                .map(e -> e.toDomain())
                .collect(Collectors.toList());
    }


    private void updateMapping(Map<Long, List<FunctionalityTested>> mapping, FunctionalityTested functionalityTested) {
        functionalityTested.getCriteria().stream()
            .forEach(funcTestCriterion -> {
                if (!mapping.containsKey(funcTestCriterion.getId())) {
                    mapping.put(funcTestCriterion.getId(), new ArrayList<FunctionalityTested>());
                }
                mapping.get(funcTestCriterion.getId()).add(functionalityTested);
            });
    }

    private List<FunctionalityTestedEntity> getAllEntities() {
        return entityManager
                .createQuery("SELECT DISTINCT ft "
                            + "FROM FunctionalityTestedEntity ft "
                            + "LEFT OUTER JOIN FETCH ft.practiceType "
                            + "LEFT OUTER JOIN FETCH ft.mappedCriteria criteriaMapping "
                            + "LEFT OUTER JOIN FETCH criteriaMapping.criterion criterion "
                            + "LEFT OUTER JOIN FETCH criterion.certificationEdition "
                            + "WHERE (NOT ft.deleted = true) ", FunctionalityTestedEntity.class)
                .getResultList();
    }

    private FunctionalityTestedEntity getEntityById(Long id) throws EntityRetrievalException {
        FunctionalityTestedEntity entity = null;

        Query query = entityManager
                .createQuery("SELECT DISTINCT ft "
                        + "FROM FunctionalityTestedEntity ft "
                        + "LEFT OUTER JOIN FETCH ft.practiceType "
                        + "LEFT OUTER JOIN FETCH ft.mappedCriteria criteriaMapping "
                        + "LEFT OUTER JOIN FETCH criteriaMapping.criterion criterion "
                        + "LEFT OUTER JOIN FETCH criterion.certificationEdition "
                        + "WHERE (NOT ft.deleted = true) "
                        + "AND (ft.id = :entityid) ",
                        FunctionalityTestedEntity.class);
        query.setParameter("entityid", id);
        List<FunctionalityTestedEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate functionality tested id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private List<FunctionalityTestedCriteriaMapEntity> getAllFunctionalityTestedCriteriaMapEntities() throws EntityRetrievalException {
        return entityManager.createQuery("SELECT DISTINCT ftcm "
                        + "FROM FunctionalityTestedCriteriaMapEntity ftcm "
                        + "JOIN FETCH ftcm.criterion c "
                        + "JOIN FETCH c.certificationEdition "
                        + "JOIN FETCH ftcm.functionalityTested ft "
                        + "WHERE ftcm.deleted <> true "
                        + "AND ft.deleted <> true ",
                        FunctionalityTestedCriteriaMapEntity.class)
                .getResultList();
    }

    private List<Long> getCertifiedProductIdsUsingFunctionalityTestedIdWithCriterion(Long functionalityTestedId, Long criterionId) {
        List<CertificationResultEntity> certResultsWithTestTool =
                entityManager.createQuery("SELECT cr "
                        + "FROM CertificationResultFunctionalityTestedEntity crft, CertificationResultEntity cr "
                        + "WHERE crft.certificationResultId = cr.id "
                        + "AND crft.functionalityTested.id = :functionalityTestedId "
                        + "AND cr.certificationCriterionId= :criterionId "
                        + "AND crft.deleted <> true "
                        + "AND cr.deleted <> true ",
                        CertificationResultEntity.class)
                .setParameter("functionalityTestedId", functionalityTestedId)
                .setParameter("criterionId", criterionId)
                .getResultList();

        return certResultsWithTestTool.stream()
                .map(certResult -> certResult.getCertifiedProductId())
                .distinct()
                .collect(Collectors.toList());
    }

    private FunctionalityTestedCriteriaMapEntity getFunctionalityTestedCriteriaMapByTestToolAndCriterionEntity(Long functionalityTestedId, Long certificationCriterionId) throws EntityRetrievalException {
        List<FunctionalityTestedCriteriaMapEntity> result = entityManager.createQuery("SELECT DISTINCT ftcm "
                        + "FROM FunctionalityTestedCriteriaMapEntity ftcm "
                        + "JOIN FETCH ftcm.criterion c "
                        + "JOIN FETCH ftcm.functionalityTested ft "
                        + "WHERE c.id = :certificationCriterionId "
                        + "AND ft.id= :functionalityTestedId "
                        + "AND ftcm.deleted <> true "
                        + "AND ft.deleted <> true "
                        + "AND c.deleted <> true",
                        FunctionalityTestedCriteriaMapEntity.class)
                .setParameter("functionalityTestedId", functionalityTestedId)
                .setParameter("certificationCriterionId", certificationCriterionId)
                .getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate functionality tested criteria map id in database.");
        } else if (result.size() == 0) {
            throw new EntityRetrievalException("Data error. Could not locate functionality tested criteria map {" + functionalityTestedId + ", " + certificationCriterionId + "} in database.");
        }

        return result.get(0);
    }

    private List<Long> getCertifiedProductIdsUsingFunctionalityTestedId(Long functionalityTestedId) {
        List<CertificationResultEntity> certResultsWithTestTool =
                entityManager.createQuery("SELECT cr "
                        + "FROM CertificationResultFunctionalityTestedEntity crtf, CertificationResultEntity cr "
                        + "WHERE crtf.certificationResultId = cr.id "
                        + "AND crtf.functionalityTested.id = :testToolId "
                        + "AND crtf.deleted <> true "
                        + "AND cr.deleted <> true ",
                        CertificationResultEntity.class)
                .setParameter("testToolId", functionalityTestedId)
                .getResultList();

        return certResultsWithTestTool.stream()
                .map(certResult -> certResult.getCertifiedProductId())
                .distinct()
                .collect(Collectors.toList());
    }

    private PracticeTypeEntity getPracticeTypeEntity(Long practiceTypeId) {
        try {
            return practiceTypeDAO.getEntityById(practiceTypeId);
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve Practice Type: {}", practiceTypeId, e);
            return null;
        }
    }
}
