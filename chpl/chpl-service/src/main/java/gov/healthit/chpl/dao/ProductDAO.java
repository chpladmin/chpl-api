package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;
import gov.healthit.chpl.entity.ContactEntity;
import gov.healthit.chpl.entity.ProductActiveOwnerEntity;
import gov.healthit.chpl.entity.ProductEntity;
import gov.healthit.chpl.entity.ProductEntitySimple;
import gov.healthit.chpl.entity.ProductInsertableOwnerEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("productDAO")
public class ProductDAO extends BaseDAOImpl {
    private static final Logger LOGGER = LogManager.getLogger(ProductDAO.class);

    @Autowired
    private ContactDAO contactDao;

    public ProductDTO create(ProductDTO dto) throws EntityCreationException, EntityRetrievalException {
        ProductEntity entity = null;
        try {
            if (dto.getId() != null) {
                entity = this.getEntityById(dto.getId());
            }
        } catch (final EntityRetrievalException e) {
            throw new EntityCreationException(e);
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {

            entity = new ProductEntity();
            entity.setName(dto.getName());
            entity.setReportFileLocation(dto.getReportFileLocation());
            entity.setDeveloperId(dto.getOwner().getId());
            entity.setDeleted(false);
            entity.setLastModifiedUser(AuthUtil.getAuditId());

            if (dto.getContact() != null) {
                if (dto.getContact().getId() != null) {
                    ContactEntity contact = contactDao.getEntityById(dto.getContact().getId());
                    if (contact != null && contact.getId() != null) {
                        entity.setContactId(contact.getId());
                        entity.setContact(contact);
                    }
                } else {
                    ContactEntity contact = contactDao.create(dto.getContact());
                    if (contact != null) {
                        entity.setContactId(contact.getId());
                        entity.setContact(contact);
                    }
                }
            }

            create(entity);

            ProductDTO result = new ProductDTO(entity);
            if (dto.getOwnerHistory() != null && dto.getOwnerHistory().size() > 0) {
                for (ProductOwnerDTO prevOwner : dto.getOwnerHistory()) {
                    prevOwner.setProductId(entity.getId());
                    ProductOwnerDTO prevOwnerDto = addOwnershipHistory(prevOwner);
                    result.getOwnerHistory().add(prevOwnerDto);
                }
            }
            return result;
        }

    }

    public ProductDTO update(ProductDTO dto) throws EntityRetrievalException, EntityCreationException {
        ProductEntity entity = this.getEntityById(dto.getId());
        // update product data
        entity.setReportFileLocation(dto.getReportFileLocation());
        entity.setName(dto.getName());
        entity.setDeveloperId(dto.getOwner().getId());
        entity.setDeleted(dto.getDeleted() == null ?  Boolean.FALSE : dto.getDeleted());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        if (dto.getContact() != null) {
            if (dto.getContact().getId() == null) {
                // if there is not contact id then it must not exist - create it
                ContactEntity contact = contactDao.create(dto.getContact());
                if (contact != null && contact.getId() != null) {
                    entity.setContactId(contact.getId());
                    entity.setContact(contact);
                }
            } else {
                // if there is a contact id then set that on the object
                ContactEntity contact = contactDao.update(dto.getContact());
                if (contact != null) {
                    entity.setContactId(dto.getContact().getId());
                    entity.setContact(contact);
                }
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
        if (dto.getOwnerHistory() == null || dto.getOwnerHistory().size() == 0) {
            if (entity.getOwnerHistory() != null && entity.getOwnerHistory().size() > 0) {
                for (ProductActiveOwnerEntity existingPrevOwner : entity.getOwnerHistory()) {
                    existingPrevOwner.setDeleted(true);
                    existingPrevOwner.setLastModifiedDate(new Date());
                    existingPrevOwner.setLastModifiedUser(AuthUtil.getAuditId());
                    entityManager.merge(existingPrevOwner);
                    entityManager.flush();
                }
            }
        } else {
            // Look for new entries in ownership history that aren't already
            // in the list of previous owners.
            for (ProductOwnerDTO updatedProductPrevOwner : dto.getOwnerHistory()) {
                boolean alreadyExists = false;

                Iterator<ProductActiveOwnerEntity> ownerHistoryIter = entity.getOwnerHistory().iterator();
                while (ownerHistoryIter.hasNext()) {
                    ProductActiveOwnerEntity existingProductPreviousOwner = ownerHistoryIter.next();
                    if (existingProductPreviousOwner.getDeveloper() != null
                            && updatedProductPrevOwner.getDeveloper() != null
                            && existingProductPreviousOwner.getDeveloper().getId()
                            .longValue() == updatedProductPrevOwner.getDeveloper().getId().longValue()) {
                        alreadyExists = true;
                    }
                }

                if (!alreadyExists) {
                    addOwnershipHistory(updatedProductPrevOwner);
                }
            }

            // Look for entries in the existing ownership history that are
            // not in the passed-in history for the updated product
            for (ProductActiveOwnerEntity existingPrevOwner : entity.getOwnerHistory()) {
                boolean isInUpdate = false;
                for (int i = 0; i < dto.getOwnerHistory().size() && !isInUpdate; i++) {
                    ProductOwnerDTO updatedProductPreviousOwner = dto.getOwnerHistory().get(i);
                    if (existingPrevOwner.getDeveloper() != null && updatedProductPreviousOwner.getDeveloper() != null
                            && existingPrevOwner.getDeveloper().getId().longValue() == updatedProductPreviousOwner
                            .getDeveloper().getId().longValue()) {
                        isInUpdate = true;
                    }
                }
                if (!isInUpdate) {
                    existingPrevOwner.setDeleted(true);
                    existingPrevOwner.setLastModifiedDate(new Date());
                    existingPrevOwner.setLastModifiedUser(AuthUtil.getAuditId());
                    entityManager.merge(existingPrevOwner);
                    entityManager.flush();
                }
            }

            // Look for entries in the existing ownership history
            // and a matching entry in the new ownership history to see if
            // anything changed (transfer date)
            for (ProductActiveOwnerEntity existingPrevOwner : entity.getOwnerHistory()) {
                boolean isInUpdate = false;
                for (int i = 0; i < dto.getOwnerHistory().size() && !isInUpdate; i++) {
                    ProductOwnerDTO updatedProductPreviousOwner = dto.getOwnerHistory().get(i);
                    if (existingPrevOwner.getDeveloper() != null && updatedProductPreviousOwner.getDeveloper() != null
                            && existingPrevOwner.getDeveloper().getId().longValue() == updatedProductPreviousOwner
                            .getDeveloper().getId().longValue()) {

                        if (existingPrevOwner.getTransferDate().getTime()
                                != updatedProductPreviousOwner.getTransferDate().longValue()) {
                            existingPrevOwner.setTransferDate(new Date(updatedProductPreviousOwner.getTransferDate()));
                            entityManager.merge(existingPrevOwner);
                            entityManager.flush();
                        }
                    }
                }
            }
        }

        entityManager.clear();
        return getById(dto.getId());
    }

    @Transactional
    public void delete(final Long id) throws EntityRetrievalException {
        ProductEntity toDelete = getEntityById(id);
        if (toDelete == null) {
            throw new EntityRetrievalException("Could not find product with id " + id + " for deletion.");
        }
        // delete owner history
        if (toDelete.getOwnerHistory() != null) {
            for (ProductActiveOwnerEntity prevOwner : toDelete.getOwnerHistory()) {
                prevOwner.setDeleted(true);
                prevOwner.setLastModifiedDate(new Date());
                prevOwner.setLastModifiedUser(AuthUtil.getAuditId());
                entityManager.merge(prevOwner);
                entityManager.flush();
            }
        }
        toDelete.setDeleted(true);
        toDelete.setLastModifiedDate(new Date());
        toDelete.setLastModifiedUser(AuthUtil.getAuditId());
        update(toDelete);
    }

    public ProductOwnerDTO addOwnershipHistory(ProductOwnerDTO toAdd) {
        ProductInsertableOwnerEntity entityToAdd = new ProductInsertableOwnerEntity();
        entityToAdd.setProductId(toAdd.getProductId());
        entityToAdd.setCreationDate(new Date());
        entityToAdd.setLastModifiedDate(new Date());
        entityToAdd.setDeleted(false);
        entityToAdd.setLastModifiedUser(AuthUtil.getAuditId());
        if (toAdd.getDeveloper() != null) {
            entityToAdd.setDeveloperId(toAdd.getDeveloper().getId());
        }
        entityToAdd.setTransferDate(new Date(toAdd.getTransferDate()));
        entityManager.persist(entityToAdd);
        entityManager.flush();

        return new ProductOwnerDTO(entityToAdd);
    }

    public void deletePreviousOwner(Long previousOwnershipId) throws EntityRetrievalException {
        ProductActiveOwnerEntity toDelete = getProductPreviousOwner(previousOwnershipId);
        if (toDelete == null) {
            throw new EntityRetrievalException("Could not find previous ownership with id " + previousOwnershipId);
        }
        toDelete.setDeleted(true);
        toDelete.setLastModifiedDate(new Date());
        toDelete.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.merge(toDelete);
        entityManager.flush();
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findAllIdsAndNames() {

        List<ProductEntitySimple> entities = entityManager.createQuery(
                "SELECT prod "
                + "FROM ProductEntitySimple prod "
                + "WHERE prod.deleted = false").getResultList();
        List<ProductDTO> dtos = new ArrayList<ProductDTO>();
        for (ProductEntitySimple entity : entities) {
            ProductDTO dto = new ProductDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findAll() {

        List<ProductEntity> entities = getAllEntities();
        List<ProductDTO> dtos = new ArrayList<>();

        for (ProductEntity entity : entities) {
            ProductDTO dto = new ProductDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findAllIncludingDeleted() {
        List<ProductEntity> entities = getAllEntitiesIncludingDeleted();
        List<ProductDTO> dtos = new ArrayList<>();

        for (ProductEntity entity : entities) {
            ProductDTO dto = new ProductDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    @Transactional(readOnly = true)
    public ProductDTO getById(final Long id) throws EntityRetrievalException {
        return getById(id, false);
    }

    @Transactional(readOnly = true)
    public ProductDTO getSimpleProductById(Long id, boolean includeDeleted) throws EntityRetrievalException {
        ProductDTO result = null;
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
            result = new ProductDTO(entities.get(0));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public ProductDTO getById(final Long id, final boolean includeDeleted) throws EntityRetrievalException {

        ProductEntity entity = getEntityById(id, includeDeleted);
        if (entity == null) {
            return null;
        }
        ProductDTO dto = new ProductDTO(entity);
        return dto;

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

    @Transactional(readOnly = true)
    public List<ProductDTO> getByDeveloper(Long developerId) {
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

        List<ProductDTO> dtoResults = new ArrayList<ProductDTO>();
        for (ProductEntity result : results) {
            dtoResults.add(new ProductDTO(result));
        }
        return dtoResults;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getByDevelopers(List<Long> developerIds) {
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

        List<ProductDTO> dtoResults = new ArrayList<ProductDTO>();
        for (ProductEntity result : results) {
            dtoResults.add(new ProductDTO(result));
        }
        return dtoResults;
    }

    @Transactional(readOnly = true)
    public ProductDTO getByDeveloperAndName(Long developerId, String name) {
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

        ProductDTO result = null;
        if (results != null && results.size() > 0) {
            result = new ProductDTO(results.get(0));
        }
        return result;
    }

    private void create(ProductEntity entity) {

        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();
    }

    private void update(ProductEntity entity) {

        entityManager.merge(entity);
        entityManager.flush();
        entityManager.clear();
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

    private ProductActiveOwnerEntity getProductPreviousOwner(final Long ppoId) {
        ProductActiveOwnerEntity result = null;
        Query query = entityManager.createQuery("SELECT po " + "FROM ProductActiveOwnerEntity po "
                + "LEFT OUTER JOIN FETCH po.developer " + "WHERE (po.id = :ppoId)", ProductActiveOwnerEntity.class);
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

    private ProductEntity getEntityById(final Long id) throws EntityRetrievalException {
        return getEntityById(id, false);
    }

    private ProductEntity getEntityById(final Long id, final boolean includeDeleted) throws EntityRetrievalException {
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
