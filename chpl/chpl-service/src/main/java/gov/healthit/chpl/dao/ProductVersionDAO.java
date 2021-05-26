package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.ProductVersionEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("productVersionDAO")
public class ProductVersionDAO extends BaseDAOImpl {


    public ProductVersionDTO create(ProductVersionDTO dto) throws EntityCreationException, EntityRetrievalException {

        ProductVersionEntity entity = null;
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

            entity = new ProductVersionEntity();
            entity.setProductId(dto.getProductId());
            entity.setVersion(dto.getVersion());

            if (dto.getCreationDate() != null) {
                entity.setCreationDate(dto.getCreationDate());
            } else {
                entity.setCreationDate(new Date());
            }

            if (dto.getDeleted() != null) {
                entity.setDeleted(dto.getDeleted());
            } else {
                entity.setDeleted(false);
            }

            if (dto.getLastModifiedDate() != null) {
                entity.setLastModifiedDate(dto.getLastModifiedDate());
            } else {
                entity.setLastModifiedDate(new Date());
            }

            if (dto.getLastModifiedUser() != null) {
                entity.setLastModifiedUser(dto.getLastModifiedUser());
            } else {
                entity.setLastModifiedUser(AuthUtil.getAuditId());
            }
            create(entity);
        }

        return getById(entity.getId());
    }


    public ProductVersionEntity update(ProductVersionDTO dto) throws EntityRetrievalException {

        ProductVersionEntity entity = this.getEntityById(dto.getId());

        entity.setVersion(dto.getVersion()); // version can be null

        if (dto.getProductId() != null) {
            entity.setProductId(dto.getProductId());
        }

        if (dto.getCreationDate() != null) {
            entity.setCreationDate(dto.getCreationDate());
        } else {
            entity.setCreationDate(new Date());
        }

        if (dto.getDeleted() != null) {
            entity.setDeleted(dto.getDeleted());
        } else {
            entity.setDeleted(false);
        }

        if (dto.getLastModifiedDate() != null) {
            entity.setLastModifiedDate(dto.getLastModifiedDate());
        } else {
            entity.setLastModifiedDate(new Date());
        }

        if (dto.getLastModifiedUser() != null) {
            entity.setLastModifiedUser(dto.getLastModifiedUser());
        } else {
            entity.setLastModifiedUser(AuthUtil.getAuditId());
        }

        update(entity);
        return entity;
    }


    public void delete(Long id) throws EntityRetrievalException {
        ProductVersionEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            update(toDelete);
        }
    }


    public List<ProductVersionDTO> findAll() {

        List<ProductVersionEntity> entities = getAllEntities();
        List<ProductVersionDTO> dtos = new ArrayList<>();

        for (ProductVersionEntity entity : entities) {
            ProductVersionDTO dto = new ProductVersionDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }


    public ProductVersionDTO getById(final Long id) throws EntityRetrievalException {
        return getById(id, false);
    }


    public ProductVersionDTO getById(final Long id, final boolean includeDeleted)
            throws EntityRetrievalException {
        ProductVersionDTO dto = null;
        ProductVersionEntity entity = getEntityById(id, includeDeleted);

        if (entity != null) {
            dto = new ProductVersionDTO(entity);
        }
        return dto;
    }

    public List<ProductVersionDTO> getByDeveloper(Long developerId) {
        Query query = entityManager.createQuery("SELECT pve "
                        + " FROM ProductVersionEntity pve "
                        + " JOIN FETCH pve.product product "
                        + "JOIN FETCH product.developer dev "
                        + "WHERE pve.deleted = false "
                        + "AND dev.id = :developerId",
                ProductVersionEntity.class);

        query.setParameter("developerId", developerId);
        List<ProductVersionEntity> results = query.getResultList();

        List<ProductVersionDTO> dtoResults = new ArrayList<ProductVersionDTO>();
        for (ProductVersionEntity result : results) {
            dtoResults.add(new ProductVersionDTO(result));
        }
        return dtoResults;
    }

    public List<ProductVersionDTO> getByProductId(final Long productId) {
        Query query = entityManager.createQuery("SELECT pve "
                + "FROM ProductVersionEntity pve "
                + "JOIN FETCH pve.product product "
                + "JOIN FETCH product.developer dev "
                + "WHERE pve.deleted = false "
                + "AND (pve.productId = :productId)",
                ProductVersionEntity.class);
        query.setParameter("productId", productId);
        List<ProductVersionEntity> results = query.getResultList();

        List<ProductVersionDTO> dtoResults = new ArrayList<ProductVersionDTO>();
        for (ProductVersionEntity result : results) {
            dtoResults.add(new ProductVersionDTO(result));
        }
        return dtoResults;
    }

    public List<ProductVersionDTO> getByProductIds(final List<Long> productIds) {
        Query query = entityManager.createQuery("SELECT pve "
                        + " FROM ProductVersionEntity pve "
                        + " LEFT OUTER JOIN FETCH pve.product product "
                        + "LEFT OUTER JOIN FETCH product.developer dev "
                        + "WHERE (NOT pve.deleted = true) "
                        + "AND (pve.productId IN (:idList))",
                ProductVersionEntity.class);

        query.setParameter("idList", productIds);
        List<ProductVersionEntity> results = query.getResultList();

        List<ProductVersionDTO> dtoResults = new ArrayList<ProductVersionDTO>();
        for (ProductVersionEntity result : results) {
            dtoResults.add(new ProductVersionDTO(result));
        }
        return dtoResults;
    }

    public ProductVersionDTO getByProductAndVersion(final Long productId, final String version) {
        Query query = entityManager.createQuery("SELECT pve "
                + "FROM ProductVersionEntity pve "
                + "LEFT OUTER JOIN FETCH pve.product product "
                + "LEFT OUTER JOIN FETCH product.developer "
                + "where (NOT pve.deleted = true) "
                + "AND (pve.productId = :productId) "
                + "AND (pve.version = :version) ", ProductVersionEntity.class);

        query.setParameter("productId", productId);
        query.setParameter("version", version);
        List<ProductVersionEntity> results = query.getResultList();

        ProductVersionDTO result = null;
        if (results != null && results.size() > 0) {
            result = new ProductVersionDTO(results.get(0));
        }
        return result;
    }

    private void create(final ProductVersionEntity entity) {

        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();
    }

    private void update(final ProductVersionEntity entity) {

        entityManager.merge(entity);
        entityManager.flush();
        entityManager.clear();
    }

    private List<ProductVersionEntity> getAllEntities() {
        Query query = entityManager.createQuery("SELECT pve "
                + " FROM ProductVersionEntity pve "
                + " LEFT OUTER JOIN FETCH pve.product product "
                + "LEFT OUTER JOIN FETCH product.developer "
                + "WHERE (NOT pve.deleted = true)", ProductVersionEntity.class);

        List<ProductVersionEntity> result = query.getResultList();
        return result;

    }

    private ProductVersionEntity getEntityById(final Long id, final boolean includeDeleted)
            throws EntityRetrievalException {

        String queryStr = "SELECT pve "
                + " FROM ProductVersionEntity pve "
                + " LEFT OUTER JOIN FETCH pve.product product "
                + "LEFT OUTER JOIN FETCH product.developer "
                + "WHERE (product_version_id = :entityid)";
        if (!includeDeleted) {
            queryStr += " AND pve.deleted = false";
        }

        Query query = entityManager.createQuery(queryStr, ProductVersionEntity.class);
        query.setParameter("entityid", id);
        List<ProductVersionEntity> result = query.getResultList();

        ProductVersionEntity entity = null;
        if (result == null || result.size() == 0) {
            String msg = msgUtil.getMessage("version.notFound");
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate product version id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }

        return entity;
    }

    private ProductVersionEntity getEntityById(final Long id) throws EntityRetrievalException {
        return getEntityById(id, false);
    }

}
