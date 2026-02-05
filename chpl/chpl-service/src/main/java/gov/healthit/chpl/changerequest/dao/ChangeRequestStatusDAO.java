package gov.healthit.chpl.changerequest.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.changerequest.domain.ChangeRequestConverter;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.entity.ChangeRequestStatusEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestStatusTypeEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.DateUtil;

@Repository("changeRequestStatusDAO")
public class ChangeRequestStatusDAO extends BaseDAOImpl {
    private ChangeRequestConverter changeRequestConverter;

    @Autowired
    public ChangeRequestStatusDAO(ChangeRequestConverter changeRequestConverter) {
        this.changeRequestConverter = changeRequestConverter;
    }

    public Long create(Long changeRequestId, ChangeRequestStatus crStatus)
            throws EntityRetrievalException {
        ChangeRequestStatusEntity entity = getNewEntity(changeRequestId, crStatus);
        create(entity);
        return entity.getId();
    }

    private ChangeRequestStatusEntity getEntityById(Long id) throws EntityRetrievalException {
        String hql = "FROM ChangeRequestStatusEntity crStatus "
                + "LEFT JOIN FETCH crStatus.certificationBody "
                + "WHERE crStatus.deleted = false "
                + "AND crStatus.id = :changeRequestStatusId";

        ChangeRequestStatusEntity entity = null;
        List<ChangeRequestStatusEntity> result = entityManager
                .createQuery(hql, ChangeRequestStatusEntity.class)
                .setParameter("changeRequestStatusId", id)
                .getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate change request id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private ChangeRequestStatusEntity getNewEntity(Long changeRequestId, ChangeRequestStatus crStatus) {
        ChangeRequestStatusEntity entity = new ChangeRequestStatusEntity();

        entity.setChangeRequestId(changeRequestId);
        entity.setChangeRequestStatusType(
                getSession().find(ChangeRequestStatusTypeEntity.class, crStatus.getChangeRequestStatusType().getId()));
        if (crStatus.getCertificationBody() != null && crStatus.getCertificationBody().getId() != null) {
            entity.setCertificationBody(
                    getSession().find(CertificationBodyEntity.class, crStatus.getCertificationBody().getId()));
        }
        entity.setUserGroupName(crStatus.getUserGroupName());
        entity.setComment(crStatus.getComment());
        entity.setStatusChangeDate(DateUtil.toDate(crStatus.getStatusChangeDateTime()));
        entity.setDeleted(false);
        return entity;
    }
}
