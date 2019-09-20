package gov.healthit.chpl.changerequest.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestConverter;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatus;
import gov.healthit.chpl.changerequest.entity.ChangeRequestEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestStatusEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestTypeEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.AuthUtil;

@Repository("changeRequestDAO")
public class ChangeRequestDAOImpl extends BaseDAOImpl implements ChangeRequestDAO {

    private ResourcePermissions resourcePermissions;

    @Value("${changerequest.status.pendingacbaction}")
    private Long pendingAcbAction;

    @Value("${changerequest.status.pendingdeveloperaction}")
    private Long pendingDeveloperAction;


    @Autowired
    public ChangeRequestDAOImpl(final ResourcePermissions resourcePermissions) {
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    public ChangeRequest create(final ChangeRequest cr) throws EntityRetrievalException {
        ChangeRequestEntity entity = getNewEntity(cr);
        create(entity);
        return ChangeRequestConverter.convert(getEntityById(entity.getId()));
    }

    @Override
    public ChangeRequest get(final Long changeRequestId) throws EntityRetrievalException {
        ChangeRequest cr = ChangeRequestConverter.convert(getEntityById(changeRequestId));
        cr.setCurrentStatus(getCurrentStatus(cr.getId()));
        return cr;
    }

    @Override
    public List<ChangeRequest> getAllForCurrentUser() throws EntityRetrievalException {
        List<Long> developers = resourcePermissions.getAllDevelopersForCurrentUser().stream()
                .map(dev -> dev.getId())
                .collect(Collectors.<Long>toList());

        return getEntitiesByDevelopers(developers).stream()
                .map(entity -> ChangeRequestConverter.convert(entity))
                .map(cr -> {
                    cr.setCurrentStatus(getCurrentStatus(cr.getId()));
                    return cr;
                })
                .collect(Collectors.<ChangeRequest>toList());
    }

    @Override
    public List<ChangeRequest> getAllPending() throws EntityRetrievalException {
         return getAllEntities().stream()
                .map(entity -> ChangeRequestConverter.convert(entity))
                .map(cr -> {
                    cr.setCurrentStatus(getCurrentStatus(cr.getId()));
                    return cr;
                })
                .filter(cr -> getUpdatableStatuses().contains(cr.getCurrentStatus().getChangeRequestStatusType().getId()))
                .collect(Collectors.<ChangeRequest>toList());
    }

    public List<ChangeRequest> getByDeveloper(final Long developerId) throws EntityRetrievalException {
        List<Long> developers = new ArrayList<Long>(Arrays.asList(developerId));

        return getEntitiesByDevelopers(developers).stream()
                .map(entity -> ChangeRequestConverter.convert(entity))
                .map(cr -> {
                    cr.setCurrentStatus(getCurrentStatus(cr.getId()));
                    return cr;
                })
                .collect(Collectors.<ChangeRequest>toList());
    }

    private ChangeRequestEntity getEntityById(final Long id) throws EntityRetrievalException {
        String hql = "SELECT DISTINCT cr "
                + "FROM ChangeRequestEntity cr "
                + "JOIN FETCH cr.changeRequestType "
                + "JOIN FETCH cr.developer "
                + "WHERE cr.deleted = false "
                + "AND cr.id = :changeRequestId";

        ChangeRequestEntity entity = null;
        List<ChangeRequestEntity> result = entityManager
                .createQuery(hql, ChangeRequestEntity.class)
                .setParameter("changeRequestId", id)
                .getResultList();

        if (result.size() == 0) {
            throw new EntityRetrievalException("Data error. Duplicate change request id in database.");
        } else {
            entity = result.get(0);
        }
        return entity;
    }

    private List<ChangeRequestEntity> getAllEntities()
            throws EntityRetrievalException {
        String hql = "SELECT DISTINCT cr "
                + "FROM ChangeRequestEntity cr "
                + "JOIN FETCH cr.changeRequestType "
                + "JOIN FETCH cr.developer "
                + "WHERE cr.deleted = false ";

        List<ChangeRequestEntity> results = entityManager
                .createQuery(hql, ChangeRequestEntity.class)
                .getResultList();

        return results;
    }

    private List<ChangeRequestEntity> getEntitiesByDevelopers(final List<Long> developerIds)
            throws EntityRetrievalException {
        String hql = "SELECT DISTINCT cr "
                + "FROM ChangeRequestEntity cr "
                + "JOIN FETCH cr.changeRequestType "
                + "JOIN FETCH cr.developer "
                + "WHERE cr.deleted = false "
                + "AND cr.developer.id IN (:developerIds)";

        List<ChangeRequestEntity> results = entityManager
                .createQuery(hql, ChangeRequestEntity.class)
                .setParameter("developerIds", developerIds)
                .getResultList();

        return results;
    }

    private ChangeRequestStatus getCurrentStatus(final Long changeRequestId) {
        String hql = "SELECT crStatus "
                + "FROM ChangeRequestStatusEntity crStatus "
                + "WHERE crStatus.deleted = false "
                + "AND crStatus.changeRequest.id = :changeRequestId "
                + "ORDER BY crStatus.statusChangeDate DESC";

        List<ChangeRequestStatus> statuses = entityManager
                .createQuery(hql, ChangeRequestStatusEntity.class)
                .setParameter("changeRequestId", changeRequestId)
                .getResultList().stream()
                .map(ChangeRequestConverter::convert)
                .collect(Collectors.<ChangeRequestStatus>toList());

        if (statuses.size() > 0) {
            return statuses.get(0);
        } else {
            return null;
        }
    }

    private ChangeRequestEntity getNewEntity(final ChangeRequest cr) {
        ChangeRequestEntity entity = new ChangeRequestEntity();
        entity.setChangeRequestType(
                getSession().load(ChangeRequestTypeEntity.class, cr.getChangeRequestType().getId()));
        entity.setDeveloper(getSession().load(DeveloperEntity.class, cr.getDeveloper().getDeveloperId()));
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());
        return entity;
    }

    private List<Long> getUpdatableStatuses() {
        List<Long> statuses = new ArrayList<Long>();
        statuses.add(pendingAcbAction);
        statuses.add(pendingDeveloperAction);
        return statuses;
    }
}
