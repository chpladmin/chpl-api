package gov.healthit.chpl.changerequest.dao;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestConverter;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.entity.ChangeRequestEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestStatusEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestStatusTypeEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.auth.UserPermissionEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("changeRequestStatusDAO")
public class ChangeRequestStatusDAOImpl extends BaseDAOImpl implements ChangeRequestStatusDAO {

    @Override
    public ChangeRequestStatus create(final ChangeRequest cr, final ChangeRequestStatus crStatus)
            throws EntityRetrievalException {
        ChangeRequestStatusEntity entity = getNewEntity(cr, crStatus);
        create(entity);
        return ChangeRequestConverter.convert(getEntityById(entity.getId()));
    }

    @Override
    public List<ChangeRequestStatus> getByChangeRequestId(final Long changeRequestId) {
        String hql = "SELECT crStatus "
                + "FROM ChangeRequestStatusEntity crStatus "
                + "JOIN FETCH crStatus.changeRequestStatusType "
                + "JOIN FETCH crStatus.userPermission "
                + "LEFT JOIN FETCH crStatus.certificationBody acb "
                + "LEFT JOIN FETCH acb.address "
                + "WHERE crStatus.deleted = false "
                + "AND crStatus.changeRequest.id = :changeRequestId";

        return entityManager
                .createQuery(hql, ChangeRequestStatusEntity.class)
                .setParameter("changeRequestId", changeRequestId)
                .getResultList().stream()
                .map(ChangeRequestConverter::convert)
                .collect(Collectors.<ChangeRequestStatus> toList());
    }

    private ChangeRequestStatusEntity getEntityById(final Long id) throws EntityRetrievalException {
        String hql = "FROM ChangeRequestStatusEntity crStatus "
                + "JOIN FETCH crStatus.userPermission "
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

    private ChangeRequestStatusEntity getNewEntity(final ChangeRequest cr, final ChangeRequestStatus crStatus) {
        ChangeRequestStatusEntity entity = new ChangeRequestStatusEntity();

        entity.setChangeRequest(getSession().load(ChangeRequestEntity.class, cr.getId()));
        entity.setChangeRequestStatusType(
                getSession().load(ChangeRequestStatusTypeEntity.class, crStatus.getChangeRequestStatusType().getId()));
        if (crStatus.getCertificationBody() != null && crStatus.getCertificationBody().getId() != null) {
            entity.setCertificationBody(
                    getSession().load(CertificationBodyEntity.class, crStatus.getCertificationBody().getId()));
        }
        entity.setUserPermission(getSession().load(UserPermissionEntity.class, crStatus.getUserPermission().getId()));
        entity.setComment(crStatus.getComment());
        entity.setStatusChangeDate(crStatus.getStatusChangeDate());
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());
        return entity;
    }
}
