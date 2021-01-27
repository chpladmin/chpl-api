package gov.healthit.chpl.svap.dao;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationCriterion;
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

    public Svap getById(Long id) throws EntityRetrievalException {
        SvapEntity entity = getSvapEntityById(id);
        if (entity != null) {
            return new Svap(entity);
        }
        return null;
    }

    public List<SvapCriteriaMap> getAllSvapCriteriaMap() throws EntityRetrievalException {
        return getAllSvapCriteriaMapEntities().stream()
                .map(e -> new SvapCriteriaMap(e))
                .collect(Collectors.toList());
    }

    public List<Svap> getAll() {
        return getAllEntities().stream()
                .map(entity -> new Svap(entity))
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
            SvapCriteriaMapEntity entity = getAllSvapCriteriaMapBySvapAndCriterionEntity(svap.getSvapId(), criterion.getId());
            entity.setDeleted(true);
            entity.setLastModifiedDate(new Date());
            entity.setLastModifiedUser(AuthUtil.getAuditId());

            update(entity);
        } catch (EntityRetrievalException e) {
            LOGGER.catching(e);
            return;
        }
    }

    private SvapEntity getSvapEntityById(Long id) throws EntityRetrievalException {
        List<SvapEntity> result = entityManager.createQuery("SELECT DISTINCT s "
                        + "FROM SvapEntity s "
                        + "LEFT JOIN FETCH s.criteria "
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
                        + "JOIN FETCH c.certificationEdition "
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
                + "LEFT JOIN FETCH svap.criteria "
                + "WHERE svap.deleted <> true ",
                SvapEntity.class)
        .getResultList();
    }

    private SvapCriteriaMapEntity getAllSvapCriteriaMapBySvapAndCriterionEntity(Long svapId, Long certificationCriterionId) throws EntityRetrievalException {
        List<SvapCriteriaMapEntity> result = entityManager.createQuery("SELECT DISTINCT scm "
                        + "FROM SvapCriteriaMapEntity scm "
                        + "JOIN FETCH scm.criteria c "
                        + "JOIN FETCH c.certificationEdition "
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
}
