package gov.healthit.chpl.changerequest.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestConverter;
import gov.healthit.chpl.changerequest.domain.service.ChangeRequestDetailsFactory;
import gov.healthit.chpl.changerequest.entity.ChangeRequestEntity;
import gov.healthit.chpl.changerequest.entity.ChangeRequestTypeEntity;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchResult;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("changeRequestDAO")
public class ChangeRequestDAO extends BaseDAOImpl {

    private ChangeRequestDetailsFactory changeRequestDetailsFactory;

    @Value("${changerequest.status.pendingacbaction}")
    private Long pendingAcbAction;

    @Value("${changerequest.status.pendingdeveloperaction}")
    private Long pendingDeveloperAction;

    @Autowired
    public ChangeRequestDAO(@Lazy ChangeRequestDetailsFactory changeRequestDetailsFactory) {
        this.changeRequestDetailsFactory = changeRequestDetailsFactory;
    }

    public ChangeRequest create(ChangeRequest cr) throws EntityRetrievalException {
        ChangeRequestEntity entity = getNewEntity(cr);
        create(entity);
        return ChangeRequestConverter.convert(getEntityById(entity.getId()));
    }

    public ChangeRequest get(Long changeRequestId) throws EntityRetrievalException {
        ChangeRequest cr = ChangeRequestConverter.convert(getEntityById(changeRequestId));
        return populateDependentObjects(cr);
    }

    public List<ChangeRequestSearchResult> getAll() {
        return getSearchResultEntities().stream()
                .map(entity -> ChangeRequestConverter.convertSearchResult(entity))
                .collect(Collectors.<ChangeRequestSearchResult>toList());
    }

    public List<ChangeRequestSearchResult> getAllForAcbs(List<Long> acbIds) {
        return getSearchResultEntitiesByAcbs(acbIds).stream()
                .map(entity -> ChangeRequestConverter.convertSearchResult(entity))
                .collect(Collectors.<ChangeRequestSearchResult>toList());
    }

    public List<ChangeRequestSearchResult> getAllForDevelopers(List<Long> developerIds) {
        return getSearchResultEntitiesByDevelopers(developerIds).stream()
                .map(entity -> ChangeRequestConverter.convertSearchResult(entity))
                .collect(Collectors.<ChangeRequestSearchResult>toList());
    }

    public List<ChangeRequest> getAllWithDetails() throws EntityRetrievalException {
        return getEntities().stream()
                .map(entity -> ChangeRequestConverter.convert(entity))
                .map(cr -> populateDependentObjects(cr))
                .collect(Collectors.<ChangeRequest>toList());
    }

    @Deprecated
    public List<ChangeRequest> getAllWithDetailsForAcbs(List<Long> acbIds) throws EntityRetrievalException {
        return getEntitiesByAcbs(acbIds).stream()
                .map(entity -> ChangeRequestConverter.convert(entity))
                .map(cr -> populateDependentObjects(cr))
                .collect(Collectors.<ChangeRequest>toList());
    }

    @Deprecated
    public List<ChangeRequest> getAllWithDetailsForDevelopers(List<Long> developerIds) throws EntityRetrievalException {
        return getEntitiesByDevelopers(developerIds).stream()
                .map(entity -> ChangeRequestConverter.convert(entity))
                .map(cr -> populateDependentObjects(cr))
                .collect(Collectors.<ChangeRequest>toList());
    }

    public List<ChangeRequest> getAllPending() throws EntityRetrievalException {
        return getAllWithDetails().stream()
                .filter(cr -> getUpdatableStatuses().contains(cr.getCurrentStatus().getChangeRequestStatusType().getId()))
                .collect(Collectors.<ChangeRequest>toList());
    }


    public List<ChangeRequest> getByDeveloper(Long developerId) throws EntityRetrievalException {
        List<Long> developers = new ArrayList<Long>(Arrays.asList(developerId));

        return getEntitiesByDevelopers(developers).stream()
                .map(entity -> ChangeRequestConverter.convert(entity))
                .map(cr -> populateDependentObjects(cr))
                .collect(Collectors.<ChangeRequest>toList());
    }

    private ChangeRequestEntity getEntityById(Long id) throws EntityRetrievalException {
        String hql = "SELECT DISTINCT cr "
                + "FROM ChangeRequestEntity cr "
                + "JOIN FETCH cr.changeRequestType "
                + "JOIN FETCH cr.developer dev "
                + "LEFT JOIN FETCH dev.address "
                + "LEFT JOIN FETCH dev.contact "
                + "LEFT JOIN FETCH dev.statusEvents statusEvents "
                + "LEFT JOIN FETCH statusEvents.developerStatus "
                + "LEFT JOIN FETCH dev.certificationBodyMaps devAcbMaps "
                + "LEFT JOIN FETCH devAcbMaps.certificationBody devAcb "
                + "LEFT JOIN FETCH devAcb.address "
                //Some of the below fields related to crStatus should not be LEFT JOINed...
                //a change request always has a status. However, during creation of a change request
                //the change request object tries to be populated before the status is created
                //so it can't be found without the LEFT JOIN here. To change that behavior is a bigger
                //update that would affect all types of change requests and require all of them to be
                //regression tested.
                + "LEFT JOIN FETCH cr.statuses crStatus "
                + "LEFT JOIN FETCH crStatus.changeRequestStatusType "
                + "LEFT JOIN FETCH crStatus.certificationBody acb "
                + "LEFT JOIN FETCH acb.address "
                + "LEFT JOIN FETCH crStatus.userPermission "
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

    private List<ChangeRequestEntity> getEntitiesByAcbs(List<Long> acbIds)
            throws EntityRetrievalException {

        String hql = "SELECT DISTINCT cr "
                + "FROM ChangeRequestEntity cr  "
                + "JOIN FETCH cr.changeRequestType crt "
                + "JOIN FETCH cr.developer dev "
                + "LEFT JOIN FETCH dev.address "
                + "LEFT JOIN FETCH dev.contact "
                + "LEFT JOIN FETCH dev.statusEvents statusEvents "
                + "LEFT JOIN FETCH statusEvents.developerStatus "
                + "LEFT JOIN FETCH dev.certificationBodyMaps devAcbMaps "
                + "LEFT JOIN FETCH devAcbMaps.certificationBody devAcb "
                + "LEFT JOIN FETCH devAcb.address "
                + "JOIN FETCH cr.statuses crStatus "
                + "JOIN FETCH crStatus.changeRequestStatusType "
                + "LEFT JOIN FETCH crStatus.certificationBody acb "
                + "LEFT JOIN FETCH acb.address "
                + "JOIN FETCH crStatus.userPermission "
                + "INNER JOIN DeveloperCertificationBodyMapEntity devAcbMap ON devAcbMap.developer.id = dev.id "
                + "WHERE devAcbMap.certificationBody.id IN (:acbIds) "
                + "AND cr.deleted = false ";

        List<ChangeRequestEntity> results = entityManager
                .createQuery(hql, ChangeRequestEntity.class)
                .setParameter("acbIds", acbIds)
                .getResultList();

        return results;
    }

    private List<ChangeRequestEntity> getEntitiesByDevelopers(List<Long> developerIds)
            throws EntityRetrievalException {
        String hql = "SELECT DISTINCT cr "
                + "FROM ChangeRequestEntity cr "
                + "JOIN FETCH cr.changeRequestType "
                + "JOIN FETCH cr.developer dev "
                + "LEFT JOIN FETCH dev.address "
                + "LEFT JOIN FETCH dev.contact "
                + "LEFT JOIN FETCH dev.statusEvents statusEvents "
                + "LEFT JOIN FETCH statusEvents.developerStatus "
                + "LEFT JOIN FETCH dev.certificationBodyMaps devAcbMaps "
                + "LEFT JOIN FETCH devAcbMaps.certificationBody devAcb "
                + "LEFT JOIN FETCH devAcb.address "
                + "JOIN FETCH cr.statuses crStatus "
                + "JOIN FETCH crStatus.changeRequestStatusType "
                + "LEFT JOIN FETCH crStatus.certificationBody acb "
                + "LEFT JOIN FETCH acb.address "
                + "JOIN FETCH crStatus.userPermission "
                + "WHERE cr.deleted = false "
                + "AND cr.developer.id IN (:developerIds)";

        List<ChangeRequestEntity> results = entityManager
                .createQuery(hql, ChangeRequestEntity.class)
                .setParameter("developerIds", developerIds)
                .getResultList();

        return results;
    }

    private List<ChangeRequestEntity> getEntities()
            throws EntityRetrievalException {

        String hql = "SELECT DISTINCT cr "
                + "FROM ChangeRequestEntity cr  "
                + "JOIN FETCH cr.changeRequestType crt "
                + "JOIN FETCH cr.developer dev "
                + "LEFT JOIN FETCH dev.address "
                + "LEFT JOIN FETCH dev.contact "
                + "LEFT JOIN FETCH dev.statusEvents statusEvents "
                + "LEFT JOIN FETCH statusEvents.developerStatus "
                + "LEFT JOIN FETCH dev.certificationBodyMaps devAcbMaps "
                + "LEFT JOIN FETCH devAcbMaps.certificationBody devAcb "
                + "LEFT JOIN FETCH devAcb.address "
                + "JOIN FETCH cr.statuses crStatus "
                + "JOIN FETCH crStatus.changeRequestStatusType "
                + "LEFT JOIN FETCH crStatus.certificationBody acb "
                + "LEFT JOIN FETCH acb.address "
                + "JOIN FETCH crStatus.userPermission "
                + "WHERE cr.deleted = false ";

        List<ChangeRequestEntity> results = entityManager
                .createQuery(hql, ChangeRequestEntity.class)
                .getResultList();

        return results;
    }

    private List<ChangeRequestEntity> getSearchResultEntities() {

        String hql = "SELECT DISTINCT cr "
                + "FROM ChangeRequestEntity cr  "
                + "JOIN FETCH cr.changeRequestType crt "
                + "JOIN FETCH cr.developer dev "
                + "LEFT JOIN FETCH dev.certificationBodyMaps devAcbMaps "
                + "LEFT JOIN FETCH devAcbMaps.certificationBody devAcb "
                + "JOIN FETCH cr.statuses crStatus "
                + "JOIN FETCH crStatus.changeRequestStatusType "
                + "LEFT JOIN FETCH crStatus.certificationBody acb "
                + "JOIN FETCH crStatus.userPermission "
                + "WHERE cr.deleted = false ";

        List<ChangeRequestEntity> results = entityManager
                .createQuery(hql, ChangeRequestEntity.class)
                .getResultList();

        return results;
    }

    private List<ChangeRequestEntity> getSearchResultEntitiesByAcbs(List<Long> acbIds) {

        String hql = "SELECT DISTINCT cr "
                + "FROM ChangeRequestEntity cr  "
                + "JOIN FETCH cr.changeRequestType crt "
                + "JOIN FETCH cr.developer dev "
                + "LEFT JOIN FETCH dev.certificationBodyMaps devAcbMaps "
                + "LEFT JOIN FETCH devAcbMaps.certificationBody devAcb "
                + "JOIN FETCH cr.statuses crStatus "
                + "JOIN FETCH crStatus.changeRequestStatusType "
                + "LEFT JOIN FETCH crStatus.certificationBody acb "
                + "JOIN FETCH crStatus.userPermission "
                + "INNER JOIN DeveloperCertificationBodyMapEntity devAcbMap ON devAcbMap.developer.id = dev.id "
                + "WHERE devAcbMap.certificationBody.id IN (:acbIds) "
                + "AND cr.deleted = false ";

        List<ChangeRequestEntity> results = entityManager
                .createQuery(hql, ChangeRequestEntity.class)
                .setParameter("acbIds", acbIds)
                .getResultList();

        return results;
    }

    private List<ChangeRequestEntity> getSearchResultEntitiesByDevelopers(List<Long> developerIds) {
        String hql = "SELECT DISTINCT cr "
                + "FROM ChangeRequestEntity cr "
                + "JOIN FETCH cr.changeRequestType "
                + "JOIN FETCH cr.developer dev "
                + "LEFT JOIN FETCH dev.certificationBodyMaps devAcbMaps "
                + "LEFT JOIN FETCH devAcbMaps.certificationBody devAcb "
                + "JOIN FETCH cr.statuses crStatus "
                + "JOIN FETCH crStatus.changeRequestStatusType "
                + "LEFT JOIN FETCH crStatus.certificationBody acb "
                + "JOIN FETCH crStatus.userPermission "
                + "WHERE cr.deleted = false "
                + "AND cr.developer.id IN (:developerIds)";

        List<ChangeRequestEntity> results = entityManager
                .createQuery(hql, ChangeRequestEntity.class)
                .setParameter("developerIds", developerIds)
                .getResultList();

        return results;
    }

    private ChangeRequestEntity getNewEntity(ChangeRequest cr) {
        ChangeRequestEntity entity = new ChangeRequestEntity();
        entity.setChangeRequestType(
                getSession().load(ChangeRequestTypeEntity.class, cr.getChangeRequestType().getId()));
        entity.setDeveloper(getSession().load(DeveloperEntity.class, cr.getDeveloper().getId()));
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

    private ChangeRequest populateDependentObjects(ChangeRequest cr) {
        try {
            cr.setDetails(
                    changeRequestDetailsFactory.get(cr.getChangeRequestType().getId())
                            .getByChangeRequestId(cr.getId()));
            return cr;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
