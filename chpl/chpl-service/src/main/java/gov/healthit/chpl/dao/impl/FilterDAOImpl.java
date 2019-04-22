package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.entity.UserEntity;
import gov.healthit.chpl.dao.FilterDAO;
import gov.healthit.chpl.dto.FilterDTO;
import gov.healthit.chpl.dto.FilterTypeDTO;
import gov.healthit.chpl.entity.FilterEntity;
import gov.healthit.chpl.entity.FilterTypeEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("filterDAO")
public class FilterDAOImpl extends BaseDAOImpl implements FilterDAO {

    @Override
    public FilterDTO update(final FilterDTO dto) throws EntityRetrievalException {
        FilterEntity entity = new FilterEntity();
        entity.setFilter(dto.getFilter());
        entity.setLastModifiedUser(Util.getAuditId());
        entity.setFilterType(new FilterTypeEntity());
        entity.getFilterType().setId(dto.getFilterType().getId());
        entity.getFilterType().setName(dto.getFilterType().getName());
        entity.setUser(getUserEntityById(dto.getUser().getId()));
        entity.setDeleted(false);
        entity.setLastModifiedUser(Util.getAuditId());

        entityManager.merge(entity);
        entityManager.flush();

        return new FilterDTO(entity);
    }

    @Override
    public FilterDTO create(final FilterDTO dto) throws EntityRetrievalException {
        FilterEntity entity = new FilterEntity();
        entity.setFilter(dto.getFilter());
        entity.setName(dto.getName());
        entity.setFilterType(new FilterTypeEntity());
        entity.getFilterType().setId(dto.getFilterType().getId());
        entity.getFilterType().setName(dto.getFilterType().getName());
        entity.setUser(getUserEntityById(dto.getUser().getId()));
        entity.setDeleted(false);
        entity.setLastModifiedUser(Util.getAuditId());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());

        entityManager.persist(entity);
        entityManager.flush();
        return new FilterDTO(entity);
    }

    @Override
    public List<FilterDTO> getByFilterType(final FilterTypeDTO filterType) {
        Query query = entityManager
                .createQuery(
                        "FROM FilterEntity f " + "JOIN FETCH f.filterType " + "JOIN FETCH f.user "
                                + "WHERE f.deleted = false " + "AND f.filterType.id = :filterTypeId",
                        FilterEntity.class);
        query.setParameter("filterTypeId", filterType.getId());
        List<FilterEntity> result = query.getResultList();

        List<FilterDTO> filterDTOs = new ArrayList<FilterDTO>();
        for (FilterEntity entity : result) {
            filterDTOs.add(new FilterDTO(entity));
        }
        return filterDTOs;
    }

    @Override
    public void delete(final FilterDTO dto) throws EntityRetrievalException {
        FilterEntity entity = getEntityById(dto.getId());
        if (entity != null) {
            entity.setDeleted(true);
            entity.setLastModifiedUser(Util.getAuditId());
            entityManager.merge(entity);
            entityManager.flush();
        }

    }

    @Override
    public FilterDTO getById(Long id) throws EntityRetrievalException {
        return new FilterDTO(getEntityById(id));
    }

    @Override
    public FilterTypeDTO getFilterTypeById(Long filterTypeId) throws EntityRetrievalException {
        return new FilterTypeDTO(getFilterTypeEntityById(filterTypeId));
    }

    private FilterEntity getEntityById(Long id) throws EntityRetrievalException {
        FilterEntity entity = null;

        Query query = entityManager.createQuery("FROM FilterEntity f " + "JOIN FETCH f.filterType "
                + "JOIN FETCH f.user " + "WHERE f.deleted = false " + "AND f.id = :entityid", FilterEntity.class);
        query.setParameter("entityid", id);
        List<FilterEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate filter id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private UserEntity getUserEntityById(final Long userId) throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "from UserEntity where (NOT deleted = true) " + "AND (user_id = :userid) ", UserEntity.class);
        query.setParameter("userid", userId);
        List<UserEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("user.notFound"),
                    LocaleContextHolder.getLocale()));
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate user id in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private FilterTypeEntity getFilterTypeEntityById(final Long filterTypeId) throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "from FilterTypeEntity where (NOT deleted = true) " + "AND (id = :filterTypeId) ",
                FilterTypeEntity.class);
        query.setParameter("filterTypeId", filterTypeId);
        List<FilterTypeEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("user.notFound"),
                    LocaleContextHolder.getLocale()));
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate filter type in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

}
