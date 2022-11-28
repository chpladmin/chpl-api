package gov.healthit.chpl.accessibilityStandard;

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

@Repository("accessibilityStandardDAO")
@Log4j2
public class AccessibilityStandardDAO extends BaseDAOImpl {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public AccessibilityStandardDAO(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public AccessibilityStandard create(AccessibilityStandard accessibilityStandard) throws EntityCreationException {
        AccessibilityStandardEntity entity = new AccessibilityStandardEntity();
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setName(accessibilityStandard.getName());

        try {
            create(entity);
        } catch (Exception ex) {
            String msg = msgUtil.getMessage("listing.badAccessibilityStandard", accessibilityStandard.getName());
            LOGGER.error(msg, ex);
            throw new EntityCreationException(msg);
        }
        return entity.toDomain();
    }

    public AccessibilityStandard update(AccessibilityStandard accessibilityStandard) throws EntityRetrievalException {
        AccessibilityStandardEntity entity = this.getEntityById(accessibilityStandard.getId());

        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + accessibilityStandard.getId() + " does not exist");
        }

        entity.setName(accessibilityStandard.getName());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        update(entity);
        return entity.toDomain();
    }

    public void delete(Long id) throws EntityRetrievalException {
        AccessibilityStandardEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            update(toDelete);
        }
    }

    public AccessibilityStandard getById(Long id) {
        AccessibilityStandardEntity entity = getEntityById(id);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public AccessibilityStandard getByName(String name) {
        List<AccessibilityStandardEntity> entities = getEntitiesByName(name);
        if (entities != null && entities.size() > 0) {
            return entities.get(0).toDomain();
        }
        return null;
    }

    public List<AccessibilityStandard> getAll() {
        List<AccessibilityStandardEntity> entities = getAllEntities();
        return entities.stream()
            .map(entity -> entity.toDomain())
            .collect(Collectors.toList());
    }

    public List<CertifiedProduct> getCertifiedProductsByAccessibilityStandard(AccessibilityStandard accessibilityStandard) {
        Query query = entityManager.createQuery("SELECT DISTINCT cpd "
                        + "FROM CertifiedProductAccessibilityStandardEntity cpAs "
                        + "CertifiedProductDetailsEntity cpd "
                        + "WHERE cpAs.accessibilityStandardId = :accessibilityStandardId "
                        + "AND cpAs.deleted <> true "
                        + "AND cpd.deleted <> true");
        query.setParameter("accessibilityStandardId", accessibilityStandard.getId());
        List<CertifiedProductDetailsEntity> results = query.getResultList();

        return results.stream()
            .map(entity -> entity.toCertifiedProduct())
            .toList();
    }

    private List<AccessibilityStandardEntity> getAllEntities() {
        return entityManager.createQuery("SELECT as "
                + "FROM AccessibilityStandardEntity as "
                + "WHERE (NOT deleted = true) ", AccessibilityStandardEntity.class).getResultList();
    }

    private AccessibilityStandardEntity getEntityById(Long id) {
        AccessibilityStandardEntity entity = null;
        Query query = entityManager.createQuery(
                "SELECT as "
                + "FROM AccessibilityStandardEntity as "
                + "WHERE (NOT deleted = true) "
                + "AND (as.id = :id) ",
                AccessibilityStandardEntity.class);
        query.setParameter("id", id);
        List<AccessibilityStandardEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<AccessibilityStandardEntity> getEntitiesByName(String name) {
        Query query = entityManager.createQuery(
                "SELECT as "
                + "FROM AccessibilityStandardEntity as "
                + "WHERE (NOT deleted = true) "
                + "AND (UPPER(name) = :name) ",
                AccessibilityStandardEntity.class);
        query.setParameter("name", name.toUpperCase());
        List<AccessibilityStandardEntity> result = query.getResultList();
        return result;
    }
}
