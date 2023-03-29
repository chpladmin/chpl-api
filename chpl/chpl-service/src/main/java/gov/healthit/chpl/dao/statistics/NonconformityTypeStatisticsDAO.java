package gov.healthit.chpl.dao.statistics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.compliance.surveillance.entity.NonconformityTypeStatisticsEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("nonconformityTypeStatisticsDAO")
public class NonconformityTypeStatisticsDAO extends BaseDAOImpl {


    public List<NonconformityTypeStatisticsDTO> getAllNonconformityStatistics() {
        String hql = "SELECT data "
                + "FROM NonconformityTypeStatisticsEntity data "
                + "LEFT OUTER JOIN FETCH data.certificationCriterionEntity cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                + "WHERE data.deleted = false";
        Query query = entityManager.createQuery(hql, NonconformityTypeStatisticsEntity.class);

        List<NonconformityTypeStatisticsEntity> entities = query.getResultList();

        List<NonconformityTypeStatisticsDTO> dtos = new ArrayList<NonconformityTypeStatisticsDTO>();
        for (NonconformityTypeStatisticsEntity entity : entities) {
            NonconformityTypeStatisticsDTO dto = new NonconformityTypeStatisticsDTO(entity);
            dtos.add(dto);
        }

        return dtos;
    }


    @Transactional
    public void create(NonconformityTypeStatisticsDTO dto) {
        NonconformityTypeStatisticsEntity entity = new NonconformityTypeStatisticsEntity();
        entity.setNonconformityCount(dto.getNonconformityCount());
        entity.setNonconformityType(dto.getNonconformityType());
        if (dto.getCriterion() != null) {
            entity.setCertificationCriterionId(dto.getCriterion().getId());
        }
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


    @Transactional
    public void deleteAllOldNonConformityStatistics() throws EntityRetrievalException {
        String hql = "UPDATE NonconformityTypeStatisticsEntity SET deleted = true, lastModifiedUser = " + getUserId(User.SYSTEM_USER_ID) + " WHERE deleted = false";
        Query query = entityManager.createQuery(hql);
        query.executeUpdate();
    }
}
