package gov.healthit.chpl.svap.dao;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.listing.CertificationResultEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.svap.domain.SvapCriteriaMap;
import gov.healthit.chpl.svap.entity.SvapCriteriaMapEntity;
import gov.healthit.chpl.svap.entity.SvapEntity;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
public class SvapDAO extends BaseDAOImpl {
    private CertifiedProductDAO certifiedProductDao;
    private CertificationCriterionComparator criteriaComparator;

    @Autowired
    public SvapDAO(CertifiedProductDAO certifiedProductDao, CertificationCriterionComparator criteriaComparator) {
        this.certifiedProductDao = certifiedProductDao;
        this.criteriaComparator = criteriaComparator;
    }

    public Svap getById(Long id) throws EntityRetrievalException {
        SvapEntity entity = getSvapEntityById(id);
        if (entity != null) {
            return entity.toDomainWithCriteria();
        }
        return null;
    }

    public List<SvapCriteriaMap> getAllSvapCriteriaMap() throws EntityRetrievalException {
        List<SvapCriteriaMap> svapCriteriaMaps = getAllSvapCriteriaMapEntities().stream()
                .map(e -> e.toDomain())
                .collect(Collectors.toList());

        //sort criteria for each svap
        svapCriteriaMaps.stream()
            .map(mapping -> mapping.getSvap())
            .forEach(svap -> svap.setCriteria(
                    svap.getCriteria().stream().sorted(criteriaComparator).collect(Collectors.toList())));
        return svapCriteriaMaps;
    }

    public List<Svap> getAll() {
        return getAllEntities().stream()
                .map(entity -> entity.toDomainWithCriteria())
                .collect(Collectors.toList());
    }

    public Svap update(Svap svap) throws EntityRetrievalException {
        SvapEntity entity = getSvapEntityById(svap.getSvapId());

        entity.setApprovedStandardVersion(svap.getApprovedStandardVersion());
        entity.setRegulatoryTextCitation(svap.getRegulatoryTextCitation());
        entity.setReplaced(svap.isReplaced());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        update(entity);

        return getById(entity.getSvapId());
    }

    public Svap create(Svap svap) throws EntityRetrievalException {
        SvapEntity entity = SvapEntity.builder()
                .approvedStandardVersion(svap.getApprovedStandardVersion())
                .regulatoryTextCitation(svap.getRegulatoryTextCitation())
                .replaced(svap.isReplaced())
                .deleted(false)
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .lastModifiedUser(AuthUtil.getAuditId())
                .build();

        create(entity);

        return getById(entity.getSvapId());
    }

    public void remove(Svap svap) throws EntityRetrievalException {
        SvapEntity entity = getSvapEntityById(svap.getSvapId());

        entity.setDeleted(true);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        update(entity);
    }


    public void addSvapCriteriMap(Svap svap, CertificationCriterion criterion) {
        SvapCriteriaMapEntity entity = SvapCriteriaMapEntity.builder()
                .certificationCriterionId(criterion.getId())
                .svapId(svap.getSvapId())
                .creationDate(new Date())
                .lastModifiedDate(new Date())
                .lastModifiedUser(AuthUtil.getAuditId())
                .deleted(false)
                .build();

        create(entity);
    }

    public void removeSvapCriteriaMap(Svap svap, CertificationCriterion criterion) {
        try {
            SvapCriteriaMapEntity entity = getSvapCriteriaMapBySvapAndCriterionEntity(svap.getSvapId(), criterion.getId());
            entity.setDeleted(true);
            entity.setLastModifiedDate(new Date());
            entity.setLastModifiedUser(AuthUtil.getAuditId());

            update(entity);
        } catch (EntityRetrievalException e) {
            LOGGER.catching(e);
            return;
        }
    }

    public List<CertifiedProductDetailsDTO> getCertifiedProductsBySvap(Svap svap)
        throws EntityRetrievalException {
        List<Long> certifiedProductIds = getCertifiedProductIdsUsingSvapId(svap.getSvapId());
        return certifiedProductDao.getDetailsByIds(certifiedProductIds);
    }

    public List<CertifiedProductDetailsDTO> getCertifiedProductsBySvapAndCriteria(Svap svap, CertificationCriterion criterion)
            throws EntityRetrievalException {
        List<Long> certifiedProductIds = getCertifiedProductIdsUsingSvapIdWithCriterion(svap.getSvapId(), criterion.getId());
        return certifiedProductDao.getDetailsByIds(certifiedProductIds);
    }

    private SvapEntity getSvapEntityById(Long id) throws EntityRetrievalException {
        List<SvapEntity> result = entityManager.createQuery("SELECT DISTINCT s "
                        + "FROM SvapEntity s "
                        + "LEFT JOIN FETCH s.criteria crit "
                        + "LEFT JOIN FETCH crit.certificationEdition "
                        + "LEFT JOIN FETCH crit.rule "
                        + "WHERE s.deleted <> true "
                        + "AND s.svapId = :entityid ",
                        SvapEntity.class)
                .setParameter("entityid", id)
                .getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate svap id in database.");
        } else if (result.size() == 0) {
            throw new EntityRetrievalException("Data error. Could not locate SVAP {" + id + "} in database.");
        }

        return result.get(0);
    }

    private List<SvapCriteriaMapEntity> getAllSvapCriteriaMapEntities() throws EntityRetrievalException {
        return entityManager.createQuery("SELECT DISTINCT scm "
                        + "FROM SvapCriteriaMapEntity scm "
                        + "JOIN FETCH scm.criteria c "
                        + "LEFT JOIN FETCH c.certificationEdition "
                        + "LEFT JOIN FETCH c.rule "
                        + "JOIN FETCH scm.svap s "
                        + "LEFT JOIN FETCH s.criteria c "
                        + "WHERE scm.deleted <> true "
                        + "AND s.deleted <> true "
                        + "AND c.deleted <> true",
                        SvapCriteriaMapEntity.class)
                .getResultList();
    }

    private List<SvapEntity> getAllEntities() {
        return entityManager.createQuery("SELECT DISTINCT svap "
                + "FROM SvapEntity svap "
                + "LEFT JOIN FETCH svap.criteria crit "
                + "LEFT JOIN FETCH crit.certificationEdition "
                + "LEFT JOIN FETCH crit.rule "
                + "WHERE svap.deleted <> true ",
                SvapEntity.class)
        .getResultList();
    }

    private SvapCriteriaMapEntity getSvapCriteriaMapBySvapAndCriterionEntity(Long svapId, Long certificationCriterionId) throws EntityRetrievalException {
        List<SvapCriteriaMapEntity> result = entityManager.createQuery("SELECT DISTINCT scm "
                        + "FROM SvapCriteriaMapEntity scm "
                        + "JOIN FETCH scm.criteria c "
                        + "LEFT JOIN FETCH c.certificationEdition "
                        + "LEFT JOIN FETCH c.rule "
                        + "JOIN FETCH scm.svap s "
                        + "LEFT JOIN FETCH s.criteria c "
                        + "WHERE scm.svapId = :svapId "
                        + "AND scm.certificationCriterionId = :certificationCriterionId "
                        + "AND scm.deleted <> true "
                        + "AND s.deleted <> true "
                        + "AND c.deleted <> true",
                        SvapCriteriaMapEntity.class)
                .setParameter("svapId", svapId)
                .setParameter("certificationCriterionId", certificationCriterionId)
                .getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate svap criteria map id in database.");
        } else if (result.size() == 0) {
            throw new EntityRetrievalException("Data error. Could not locate svap criteria map {" + svapId + ", " + certificationCriterionId + "} in database.");
        }

        return result.get(0);
    }

    private List<Long> getCertifiedProductIdsUsingSvapId(Long svapId) {
        List<CertificationResultEntity> certResultsWithSvap =
                entityManager.createQuery("SELECT cr "
                        + "FROM CertificationResultSvapEntity crs, CertificationResultEntity cr "
                        + "WHERE crs.certificationResultId = cr.id "
                        + "AND crs.svapId = :svapId "
                        + "AND crs.deleted <> true "
                        + "AND cr.deleted <> true ",
                        CertificationResultEntity.class)
                .setParameter("svapId", svapId)
                .getResultList();

        return certResultsWithSvap.stream()
                .map(certResult -> certResult.getCertifiedProductId())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Long> getCertifiedProductIdsUsingSvapIdWithCriterion(Long svapId, Long criterionId) {
        List<CertificationResultEntity> certResultsWithSvap =
                entityManager.createQuery("SELECT cr "
                        + "FROM CertificationResultSvapEntity crs, CertificationResultEntity cr "
                        + "WHERE crs.certificationResultId = cr.id "
                        + "AND crs.svapId = :svapId "
                        + "AND cr.certificationCriterionId= :criterionId "
                        + "AND crs.deleted <> true "
                        + "AND cr.deleted <> true ",
                        CertificationResultEntity.class)
                .setParameter("svapId", svapId)
                .setParameter("criterionId", criterionId)
                .getResultList();

        return certResultsWithSvap.stream()
                .map(certResult -> certResult.getCertifiedProductId())
                .distinct()
                .collect(Collectors.toList());
    }

 }
