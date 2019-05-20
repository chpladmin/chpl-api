package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.ProductVersionEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("productVersionDAO")
public class ProductVersionDAOImpl extends BaseDAOImpl implements ProductVersionDAO {

    @Override
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

        return new ProductVersionDTO(entity);
    }

    @Override
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

    @Override
    public void delete(Long id) throws EntityRetrievalException {
        ProductVersionEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            update(toDelete);
        }
    }

    @Override
    public List<ProductVersionDTO> findAll() {

        List<ProductVersionEntity> entities = getAllEntities();
        List<ProductVersionDTO> dtos = new ArrayList<>();

        for (ProductVersionEntity entity : entities) {
            ProductVersionDTO dto = new ProductVersionDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public ProductVersionDTO getById(Long id) throws EntityRetrievalException {

        ProductVersionDTO dto = null;
        ProductVersionEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new ProductVersionDTO(entity);
        }
        return dto;
    }

    @Override
    public List<ProductVersionDTO> getByProductId(Long productId) {
        Query query = entityManager.createQuery("SELECT pve "
                + " FROM ProductVersionEntity pve "
                + " LEFT OUTER JOIN FETCH pve.product product "
                + "LEFT OUTER JOIN FETCH product.developer "
                + "WHERE (NOT pve.deleted = true) "
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

    public List<ProductVersionDTO> getByProductIds(List<Long> productIds) {
        Query query = entityManager.createQuery("SELECT pve "
                        + " FROM ProductVersionEntity pve "
                        + " LEFT OUTER JOIN FETCH pve.product product "
                        + "LEFT OUTER JOIN FETCH product.developer "
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

    public ProductVersionDTO getByProductAndVersion(Long productId, String version) {
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

    private void create(ProductVersionEntity entity) {

        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();
    }

    private void update(ProductVersionEntity entity) {

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

    private ProductVersionEntity getEntityById(final Long id) throws EntityRetrievalException {

        ProductVersionEntity entity = null;
        Query query = entityManager.createQuery("SELECT pve "
                + " FROM ProductVersionEntity pve "
                + " LEFT OUTER JOIN FETCH pve.product product "
                + "LEFT OUTER JOIN FETCH product.developer "
                + "WHERE (NOT pve.deleted = true) "
                + "AND (product_version_id = :entityid)",
                ProductVersionEntity.class);

        query.setParameter("entityid", id);
        List<ProductVersionEntity> result = query.getResultList();

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

}
