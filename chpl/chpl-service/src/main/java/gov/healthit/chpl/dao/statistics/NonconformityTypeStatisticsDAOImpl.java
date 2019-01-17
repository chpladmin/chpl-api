package gov.healthit.chpl.dao.statistics;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;
import gov.healthit.chpl.entity.surveillance.NonconformityTypeStatisticsEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("nonconformityTypeStatisticsDAO")
public class NonconformityTypeStatisticsDAOImpl extends BaseDAOImpl implements NonconformityTypeStatisticsDAO {

    @Override
    public List<NonconformityTypeStatisticsDTO> getAllNonconformityStatistics() {
        String hql = "FROM NonconformityTypeStatisticsEntity WHERE deleted = false";
        Query query = entityManager.createQuery(hql);

        List<NonconformityTypeStatisticsEntity> entities = query.getResultList();

        List<NonconformityTypeStatisticsDTO> dtos = new ArrayList<NonconformityTypeStatisticsDTO>();
        for (NonconformityTypeStatisticsEntity entity : entities) {
            NonconformityTypeStatisticsDTO dto = new NonconformityTypeStatisticsDTO(entity);
            dtos.add(dto);
        }

        return dtos;
    }

    @Override
    @Transactional
    public void create(NonconformityTypeStatisticsDTO dto) {
        NonconformityTypeStatisticsEntity entity = new NonconformityTypeStatisticsEntity();
        entity.setNonconformityCount(dto.getNonconformityCount());
        entity.setNonconformityType(dto.getNonconformityType());
        if (dto.getLastModifiedDate() == null) {
            entity.setLastModifiedDate(new Date());
        } else {
            entity.setLastModifiedDate(dto.getLastModifiedDate());
        }

        if (dto.getLastModifiedUser() == null) {
            entity.setLastModifiedUser(-2L);
        } else {
            entity.setLastModifiedUser(dto.getLastModifiedUser());
        }

        if (dto.getDeleted() == null) {
            entity.setDeleted(false);
        } else {
            entity.setDeleted(dto.getDeleted());
        }
        entityManager.persist(entity);
        entityManager.flush();
    }

    @Override
    @Transactional
    public void deleteAllOldNonConformityStatistics() throws EntityRetrievalException {
        String hql = "UPDATE NonconformityTypeStatisticsEntity SET deleted = true, lastModifiedUser = " + getUserId(User.SYSTEM_USER_ID) + " WHERE deleted = false";
        Query query = entityManager.createQuery(hql);
        query.executeUpdate();
    }
}
