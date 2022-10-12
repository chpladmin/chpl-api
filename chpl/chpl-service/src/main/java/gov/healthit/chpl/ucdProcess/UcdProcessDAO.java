package gov.healthit.chpl.ucdProcess;

import java.util.Date;
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
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Repository("ucdProcessDAO")
@Log4j2
public class UcdProcessDAO extends BaseDAOImpl {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public UcdProcessDAO(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public UcdProcess create(UcdProcess ucdProcess) throws EntityCreationException {
        UcdProcessEntity entity = new UcdProcessEntity();
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setName(ucdProcess.getName());

        try {
            create(entity);
        } catch (Exception ex) {
            String msg = msgUtil.getMessage("listing.criteria.badUcdProcess", ucdProcess.getName());
            LOGGER.error(msg, ex);
            throw new EntityCreationException(msg);
        }
        return entity.toDomain();
    }

    public UcdProcess update(UcdProcess ucdProcess) throws EntityRetrievalException {
        UcdProcessEntity entity = this.getEntityById(ucdProcess.getId());

        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + ucdProcess.getId() + " does not exist");
        }

        entity.setName(ucdProcess.getName());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        update(entity);
        return entity.toDomain();
    }

    public void delete(Long id) throws EntityRetrievalException {
        UcdProcessEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            update(toDelete);
        }
    }

    public UcdProcess getById(Long id) {
        UcdProcessEntity entity = getEntityById(id);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public UcdProcess getByName(String name) {
        List<UcdProcessEntity> entities = getEntitiesByName(name);
        if (entities != null && entities.size() > 0) {
            return entities.get(0).toDomain();
        }
        return null;
    }

    public List<UcdProcess> getAll() {
        List<UcdProcessEntity> entities = getAllEntities();
        return entities.stream()
            .map(entity -> entity.toDomain())
            .collect(Collectors.toList());
    }

    public List<CertifiedProduct> getCertifiedProductsByUcdProcess(UcdProcess ucdProcess) {
        Query query = entityManager.createQuery("SELECT DISTINCT cpd "
                        + "FROM CertificationResultUcdProcessEntity crUcd, CertificationResultEntity cr, "
                        + "CertifiedProductDetailsEntity cpd "
                        + "WHERE cr.certifiedProductId = cpd.id "
                        + "AND crUcd.certificationResultId = cr.id "
                        + "AND crUcd.ucdProcessId = :ucdProcessId "
                        + "AND crUcd.deleted <> true "
                        + "AND cr.deleted <> true "
                        + "AND cpd.deleted <> true");
        query.setParameter("ucdProcessId", ucdProcess.getId());
        List<CertifiedProductDetailsEntity> results = query.getResultList();

        return results.stream()
            .map(entity -> entity.toCertifiedProduct())
            .toList();
    }

    private List<UcdProcessEntity> getAllEntities() {
        return entityManager.createQuery("SELECT ucd "
                + "FROM UcdProcessEntity ucd "
                + "WHERE (NOT deleted = true) ", UcdProcessEntity.class).getResultList();
    }

    private UcdProcessEntity getEntityById(Long id) {
        UcdProcessEntity entity = null;
        Query query = entityManager.createQuery(
                "SELECT ucd "
                + "FROM UcdProcessEntity ucd "
                + "WHERE (NOT deleted = true) "
                + "AND (ucd.id = :id) ",
                UcdProcessEntity.class);
        query.setParameter("id", id);
        List<UcdProcessEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<UcdProcessEntity> getEntitiesByName(String name) {
        Query query = entityManager.createQuery(
                "SELECT ucd "
                + "FROM UcdProcessEntity ucd "
                + "WHERE (NOT deleted = true) "
                + "AND (UPPER(name) = :name) ",
                UcdProcessEntity.class);
        query.setParameter("name", name.toUpperCase());
        List<UcdProcessEntity> result = query.getResultList();
        return result;
    }
}
