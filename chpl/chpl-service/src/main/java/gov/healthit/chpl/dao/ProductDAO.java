package gov.healthit.chpl.dao;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.KeyValueModelStatuses;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductOwner;
import gov.healthit.chpl.domain.Statuses;
import gov.healthit.chpl.entity.ProductActiveOwnerEntity;
import gov.healthit.chpl.entity.ProductEntity;
import gov.healthit.chpl.entity.ProductEntitySimple;
import gov.healthit.chpl.entity.ProductInsertableOwnerEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository("productDAO")
public class ProductDAO extends BaseDAOImpl {

    @Autowired
    private ContactDAO contactDao;

    public Long create(Long developerId, Product product) throws EntityCreationException {
        try {
            ProductEntity entity = new ProductEntity();
            entity.setName(product.getName());
            entity.setReportFileLocation(product.getReportFileLocation());
            entity.setDeveloperId(developerId);
            entity.setLastModifiedUser(AuthUtil.getAuditId());

            if (product.getContact() != null) {
                Long contactId = contactDao.create(product.getContact());
                if (contactId != null) {
                    entity.setContactId(contactId);
                }
            }
            create(entity);
            if (!CollectionUtils.isEmpty(product.getOwnerHistory())) {
                for (ProductOwner prevOwner : product.getOwnerHistory()) {
                    createOwnerHistory(product.getProductId(), prevOwner);
                }
            }
            return entity.getId();
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public void createOwnerHistory(Long productId, ProductOwner owner) throws EntityCreationException {
        ProductInsertableOwnerEntity entityToAdd = new ProductInsertableOwnerEntity();
        entityToAdd.setProductId(productId);
        entityToAdd.setDeleted(false);
        entityToAdd.setLastModifiedUser(AuthUtil.getAuditId());
        if (owner.getDeveloper() != null) {
            entityToAdd.setDeveloperId(owner.getDeveloper().getDeveloperId());
        }
        entityToAdd.setTransferDate(new Date(owner.getTransferDate()));
        create(entityToAdd);
    }

    public void update(Product product) throws EntityRetrievalException, EntityCreationException {
        ProductEntity entity = this.getEntityById(product.getProductId());
        // update product data
        entity.setReportFileLocation(product.getReportFileLocation());
        entity.setName(product.getName());
        entity.setDeveloperId(product.getOwner().getDeveloperId());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        if (product.getContact() != null) {
            if (product.getContact().getContactId() == null) {
                // if there is not contact id then it must not exist - create it
                Long contactId = contactDao.create(product.getContact());
                if (contactId != null) {
                    entity.setContactId(contactId);
                    entity.setContact(contactDao.getEntityById(contactId));
                }
            } else {
                // if there is a contact id then set that on the object
                contactDao.update(product.getContact());
                entity.setContactId(product.getContact().getContactId());
                entity.setContact(contactDao.getEntityById(product.getContact().getContactId()));
            }
        } else {
            // if there's no contact at all, set the id to null
            entity.setContactId(null);
            entity.setContact(null);
        }

        update(entity);

        // update ownership history
        // there used to be owners but aren't anymore so delete the existing
        // ones
        if (product.getOwnerHistory() == null || product.getOwnerHistory().size() == 0) {
            if (entity.getOwnerHistory() != null && entity.getOwnerHistory().size() > 0) {
                for (ProductActiveOwnerEntity existingPrevOwner : entity.getOwnerHistory()) {
                    existingPrevOwner.setDeleted(true);
                    existingPrevOwner.setLastModifiedUser(AuthUtil.getAuditId());
                    update(existingPrevOwner);
                }
            }
        } else {
            // Look for new entries in ownership history that aren't already
            // in the list of previous owners.
            for (ProductOwner updatedProductPrevOwner : product.getOwnerHistory()) {
                boolean alreadyExists = false;

                Iterator<ProductActiveOwnerEntity> ownerHistoryIter = entity.getOwnerHistory().iterator();
                while (ownerHistoryIter.hasNext()) {
                    ProductActiveOwnerEntity existingProductPreviousOwner = ownerHistoryIter.next();
                    if (existingProductPreviousOwner.getDeveloper() != null
                            && updatedProductPrevOwner.getDeveloper() != null
                            && existingProductPreviousOwner.getDeveloper().getId()
                            .longValue() == updatedProductPrevOwner.getDeveloper().getDeveloperId().longValue()) {
                        alreadyExists = true;
                    }
                }

                if (!alreadyExists) {
                    createOwnerHistory(product.getProductId(), updatedProductPrevOwner);
                }
            }

            // Look for entries in the existing ownership history that are
            // not in the passed-in history for the updated product
            for (ProductActiveOwnerEntity existingPrevOwner : entity.getOwnerHistory()) {
                boolean isInUpdate = false;
                for (int i = 0; i < product.getOwnerHistory().size() && !isInUpdate; i++) {
                    ProductOwner updatedProductPreviousOwner = product.getOwnerHistory().get(i);
                    if (existingPrevOwner.getDeveloper() != null && updatedProductPreviousOwner.getDeveloper() != null
                            && existingPrevOwner.getDeveloper().getId().longValue() == updatedProductPreviousOwner
                            .getDeveloper().getDeveloperId().longValue()) {
                        isInUpdate = true;
                    }
                }
                if (!isInUpdate) {
                    existingPrevOwner.setDeleted(true);
                    existingPrevOwner.setLastModifiedUser(AuthUtil.getAuditId());
                    update(existingPrevOwner);
                }
            }

            // Look for entries in the existing ownership history
            // and a matching entry in the new ownership history to see if
            // anything changed (transfer date)
            for (ProductActiveOwnerEntity existingPrevOwner : entity.getOwnerHistory()) {
                boolean isInUpdate = false;
                for (int i = 0; i < product.getOwnerHistory().size() && !isInUpdate; i++) {
                    ProductOwner updatedProductPreviousOwner = product.getOwnerHistory().get(i);
                    if (existingPrevOwner.getDeveloper() != null && updatedProductPreviousOwner.getDeveloper() != null
                            && existingPrevOwner.getDeveloper().getId().longValue() == updatedProductPreviousOwner
                            .getDeveloper().getDeveloperId().longValue()) {

                        if (existingPrevOwner.getTransferDate().getTime()
                                != updatedProductPreviousOwner.getTransferDate().longValue()) {
                            existingPrevOwner.setTransferDate(new Date(updatedProductPreviousOwner.getTransferDate()));
                            update(existingPrevOwner);
                        }
                    }
                }
            }
        }
        entityManager.clear();
    }

    public void delete(Long id) throws EntityRetrievalException {
        ProductEntity toDelete = getEntityById(id);
        if (toDelete == null) {
            throw new EntityRetrievalException("Could not find product with id " + id + " for deletion.");
        }
        // delete owner history
        if (toDelete.getOwnerHistory() != null) {
            for (ProductActiveOwnerEntity prevOwner : toDelete.getOwnerHistory()) {
                prevOwner.setDeleted(true);
                prevOwner.setLastModifiedUser(AuthUtil.getAuditId());
                update(prevOwner);
            }
        }
        toDelete.setDeleted(true);
        toDelete.setLastModifiedUser(AuthUtil.getAuditId());
        update(toDelete);
    }

    public void deletePreviousOwner(Long previousOwnershipId) throws EntityRetrievalException {
        ProductActiveOwnerEntity toDelete = getProductPreviousOwner(previousOwnershipId);
        if (toDelete == null) {
            throw new EntityRetrievalException("Could not find previous ownership with id " + previousOwnershipId);
        }
        toDelete.setDeleted(true);
        toDelete.setLastModifiedUser(AuthUtil.getAuditId());
        update(toDelete);
    }

    @Transactional(readOnly = true)
    public List<Product> findAllIdsAndNames() {

        List<ProductEntitySimple> entities = entityManager.createQuery(
                "SELECT prod "
                + "FROM ProductEntitySimple prod "
                + "WHERE prod.deleted = false").getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public List<Product> findAll() {
        List<ProductEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public List<Product> findAllIncludingDeleted() {
        List<ProductEntity> entities = getAllEntitiesIncludingDeleted();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public Product getById(Long id) throws EntityRetrievalException {
        return getById(id, false);
    }

    public Product getSimpleProductById(Long id, boolean includeDeleted) throws EntityRetrievalException {
        Product result = null;
        String queryStr = "SELECT DISTINCT pe "
                + "FROM ProductEntitySimple pe "
                + "WHERE pe.id = :entityid ";
        if (!includeDeleted) {
            queryStr += " AND pe.deleted = false";
        }

        Query query = entityManager.createQuery(queryStr, ProductEntitySimple.class);
        query.setParameter("entityid", id);
        List<ProductEntitySimple> entities = query.getResultList();

        if (entities == null || entities.size() == 0) {
            String msg = msgUtil.getMessage("product.notFound");
            throw new EntityRetrievalException(msg);
        } else if (entities.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate product id in database.");
        } else if (entities.size() == 1) {
            result = entities.get(0).toDomain();
        }

        return result;
    }

    public Product getById(Long id, boolean includeDeleted) throws EntityRetrievalException {
        ProductEntity entity = getEntityById(id, includeDeleted);
        if (entity == null) {
            return null;
        }
        return entity.toDomain();
    }

    public boolean exists(Long id) {
        String queryStr = "SELECT COUNT(pe) "
                + "FROM ProductEntity pe "
                + "WHERE pe.id = :productId "
                + "AND pe.deleted = false";

        Query query = entityManager.createQuery(queryStr, Long.class);
        query.setParameter("productId", id);
        Object result = query.getSingleResult();
        int count = 0;
        if (result != null && result instanceof Long) {
            count = ((Long) result).intValue();
        }
        return count > 0;
    }

    public List<Product> getByDeveloper(Long developerId) {
        Query query = entityManager.createQuery("SELECT DISTINCT pe "
                + "FROM ProductEntity pe "
                + "JOIN FETCH pe.developer "
                + "LEFT JOIN FETCH pe.contact "
                + "LEFT JOIN FETCH pe.ownerHistory oh "
                + "LEFT JOIN FETCH pe.productVersions pv "
                + "LEFT JOIN FETCH pe.productCertificationStatuses "
                + "WHERE (pe.developerId = :devId) "
                + "AND (pe.deleted = false) ", ProductEntity.class);
        query.setParameter("devId", developerId);
        List<ProductEntity> results = query.getResultList();

        return results.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public List<Product> getByDevelopers(List<Long> developerIds) {
        Query query = entityManager.createQuery("SELECT DISTINCT pe "
                + "FROM ProductEntity pe "
                + "JOIN FETCH pe.developer "
                + "LEFT JOIN FETCH pe.contact "
                + "LEFT JOIN FETCH pe.ownerHistory "
                + "LEFT JOIN FETCH pe.productVersions "
                + "LEFT JOIN FETCH pe.productCertificationStatuses "
                + "where pe.deleted = false "
                + "AND pe.developerId IN (:idList) ", ProductEntity.class);
        query.setParameter("idList", developerIds);
        List<ProductEntity> results = query.getResultList();

        return results.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public Product getByDeveloperAndName(Long developerId, String name) {
        Query query = entityManager.createQuery("SELECT distinct pe "
                + "FROM ProductEntity pe "
                + "JOIN FETCH pe.developer "
                + "LEFT JOIN FETCH pe.contact "
                + "LEFT JOIN FETCH pe.ownerHistory "
                + "LEFT JOIN FETCH pe.productVersions "
                + "LEFT JOIN FETCH pe.productCertificationStatuses "
                + "WHERE pe.deleted = false "
                + "AND (pe.developerId = :developerId) "
                + "AND (pe.name = :name)", ProductEntity.class);
        query.setParameter("developerId", developerId);
        query.setParameter("name", name);
        List<ProductEntity> results = query.getResultList();
        if (CollectionUtils.isEmpty(results)) {
            return null;
        }
        return results.get(0).toDomain();
    }

    public Set<KeyValueModelStatuses> findAllWithStatuses() {
        List<ProductEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> new KeyValueModelStatuses(entity.getId(), entity.getName(), createStatuses(entity)))
                .collect(Collectors.toSet());
    }

    private Statuses createStatuses(ProductEntity entity) {
        return new Statuses(entity.getProductCertificationStatuses().getActive(),
                entity.getProductCertificationStatuses().getRetired(),
                entity.getProductCertificationStatuses().getWithdrawnByDeveloper(),
                entity.getProductCertificationStatuses().getWithdrawnByAcb(),
                entity.getProductCertificationStatuses().getSuspendedByAcb(),
                entity.getProductCertificationStatuses().getSuspendedByOnc(),
                entity.getProductCertificationStatuses().getTerminatedByOnc());
    }

    private List<ProductEntity> getAllEntities() {
        List<ProductEntity> result = entityManager
                .createQuery(
                        "SELECT distinct pe "
                                + "FROM ProductEntity pe "
                                + "LEFT JOIN FETCH pe.developer "
                                + "LEFT JOIN FETCH pe.contact "
                                + "LEFT JOIN FETCH pe.ownerHistory "
                                + "LEFT JOIN FETCH pe.productVersions "
                                + "LEFT JOIN FETCH pe.productCertificationStatuses "
                                + "where (NOT pe.deleted = true) ",
                                ProductEntity.class)
                .getResultList();

        LOGGER.debug("SQL call: List<ProductEntity> getAllEntities()");
        return result;

    }

    private ProductActiveOwnerEntity getProductPreviousOwner(Long ppoId) {
        ProductActiveOwnerEntity result = null;
        Query query = entityManager.createQuery("SELECT po "
                + "FROM ProductActiveOwnerEntity po "
                + "LEFT OUTER JOIN FETCH po.developer "
                + "WHERE (po.id = :ppoId)", ProductActiveOwnerEntity.class);
        query.setParameter("ppoId", ppoId);
        List<ProductActiveOwnerEntity> results = query.getResultList();
        if (results != null && results.size() > 0) {
            result = results.get(0);
        }
        return result;

    }

    private List<ProductEntity> getAllEntitiesIncludingDeleted() {
        List<ProductEntity> result = entityManager.createQuery("SELECT DISTINCT pe "
                + "FROM ProductEntity pe "
                + "JOIN FETCH pe.developer "
                + "LEFT JOIN FETCH pe.contact "
                + "LEFT JOIN FETCH pe.ownerHistory "
                + "LEFT JOIN FETCH pe.productVersions "
                + "LEFT JOIN FETCH pe.productCertificationStatuses ", ProductEntity.class).getResultList();
        LOGGER.debug("SQL call: List<ProductEntity> getAllEntities()");
        return result;
    }

    private ProductEntity getEntityById(Long id) throws EntityRetrievalException {
        return getEntityById(id, false);
    }

    private ProductEntity getEntityById(Long id, boolean includeDeleted) throws EntityRetrievalException {
        ProductEntity entity = null;
        String queryStr = "SELECT DISTINCT pe "
                + "FROM ProductEntity pe "
                + "LEFT JOIN FETCH pe.developer "
                + "LEFT JOIN FETCH pe.contact "
                + "LEFT JOIN FETCH pe.ownerHistory "
                + "LEFT JOIN FETCH pe.productVersions "
                + "WHERE pe.id = :entityid ";
        if (!includeDeleted) {
            queryStr += " AND pe.deleted = false";
        }

        Query query = entityManager.createQuery(queryStr, ProductEntity.class);
        query.setParameter("entityid", id);
        List<ProductEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = msgUtil.getMessage("product.notFound");
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate product id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }

        return entity;
    }
}
