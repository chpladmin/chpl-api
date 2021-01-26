package gov.healthit.chpl.svap.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.svap.domain.SvapCriteriaMap;
import gov.healthit.chpl.svap.entity.SvapCriteriaMapEntity;
import gov.healthit.chpl.svap.entity.SvapEntity;
import gov.healthit.chpl.util.AuthUtil;

@Repository
public class SvapDAO extends BaseDAOImpl {

    public Svap getById(Long id) throws EntityRetrievalException {
        SvapEntity entity = getSvapEntityById(id);
        if (entity != null) {
            return new Svap(entity);
        }
        return null;
    }

    @Transactional
    public List<SvapCriteriaMap> getAllSvapCriteriaMap() throws EntityRetrievalException {
        return getAllSvapCriteriaMapEntities().stream()
                .map(e -> new SvapCriteriaMap(e))
                .collect(Collectors.toList());
    }

    @Transactional
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

        update(entity);

        return new Svap(entity);
    }

    private SvapEntity getSvapEntityById(Long id) throws EntityRetrievalException {
        List<SvapEntity> result = entityManager.createQuery("SELECT s "
                        + "FROM SvapEntity s "
                        + "WHERE (NOT s.deleted = true) "
                        + "AND (s.id = :entityid) ",
                        SvapEntity.class)
                .setParameter("entityid", id)
                .getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate svap id in database.");
        }

        return result.get(0);
    }

    private List<SvapCriteriaMapEntity> getAllSvapCriteriaMapEntities() throws EntityRetrievalException {
        return entityManager.createQuery("SELECT scm "
                        + "FROM SvapCriteriaMapEntity scm "
                        + "JOIN FETCH scm.criteria c "
                        + "JOIN FETCH c.certificationEdition "
                        + "JOIN FETCH scm.svap "
                        + "WHERE scm.deleted <> true ",
                        SvapCriteriaMapEntity.class)
                .getResultList();
    }

    private List<SvapEntity> getAllEntities() {
        return entityManager.createQuery("SELECT svap "
                + "FROM SvapEntity svap "
                + "JOIN FETCH svap.criteria "
                + "WHERE svap.deleted <> true ",
                SvapEntity.class)
        .getResultList();
    }
}
