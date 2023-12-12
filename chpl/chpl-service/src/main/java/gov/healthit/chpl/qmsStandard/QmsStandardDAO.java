package gov.healthit.chpl.qmsStandard;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Repository("qmsStandardDao")
@Log4j2
public class QmsStandardDAO extends BaseDAOImpl {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public QmsStandardDAO(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public QmsStandard create(QmsStandard qmsStandard) throws EntityCreationException {
        QmsStandardEntity entity = new QmsStandardEntity();
        entity.setDeleted(false);
        entity.setName(qmsStandard.getName());

        try {
            create(entity);
        } catch (Exception ex) {
            String msg = msgUtil.getMessage("listing.badQmsStandard", qmsStandard.getName());
            LOGGER.error(msg, ex);
            throw new EntityCreationException(msg);
        }
        return entity.toDomain();
    }

    public QmsStandard update(QmsStandard dto) throws EntityRetrievalException {
        QmsStandardEntity entity = this.getEntityById(dto.getId());
        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
        }
        entity.setName(dto.getName());
        update(entity);
        return entity.toDomain();
    }

    public void delete(Long id) throws EntityRetrievalException {
        QmsStandardEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            update(toDelete);
        }
    }

    public QmsStandard getById(Long id) {
        QmsStandardEntity entity = getEntityById(id);

        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public QmsStandard getByName(String name) {
        List<QmsStandardEntity> entities = getEntitiesByName(name);

        if (entities != null && entities.size() > 0) {
            return entities.get(0).toDomain();
        }
        return null;
    }

    public List<CertifiedProduct> getCertifiedProductsByQmsStandard(QmsStandard qmsStandard) {
        Query query = entityManager.createQuery("SELECT DISTINCT cpd "
                        + "FROM CertifiedProductQmsStandardEntity cpQms, CertifiedProductDetailsEntity cpd "
                        + "WHERE cpQms.certifiedProductId = cpd.id "
                        + "AND cpQms.qmsStandardId = :qmsStandardId "
                        + "AND cpQms.deleted <> true "
                        + "AND cpd.deleted <> true");
        query.setParameter("qmsStandardId", qmsStandard.getId());
        List<CertifiedProductDetailsEntity> results = query.getResultList();

        return results.stream()
            .map(entity -> entity.toCertifiedProduct())
            .toList();
    }

    public List<QmsStandard> getAll() {
        List<QmsStandardEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    private List<QmsStandardEntity> getAllEntities() {
        return entityManager.createQuery("SELECT qms "
                + "FROM QmsStandardEntity qms "
                + "WHERE (NOT deleted = true) ", QmsStandardEntity.class)
                .getResultList();
    }

    private QmsStandardEntity getEntityById(Long id) {
        QmsStandardEntity entity = null;

        Query query = entityManager.createQuery("SELECT qms "
                + "FROM QmsStandardEntity qms "
                + "WHERE (NOT deleted = true) "
                + "AND (id = :entityid) ", QmsStandardEntity.class);
        query.setParameter("entityid", id);
        List<QmsStandardEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private List<QmsStandardEntity> getEntitiesByName(String name) {
        Query query = entityManager.createQuery("SELECT qms "
                + "FROM QmsStandardEntity qms "
                + "WHERE (NOT deleted = true) "
                + "AND (UPPER(name) = :name) ",
                QmsStandardEntity.class);
        query.setParameter("name", name.toUpperCase().trim());
        List<QmsStandardEntity> result = query.getResultList();

        return result;
    }

}
