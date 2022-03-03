package gov.healthit.chpl.dao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.DecertifiedDeveloper;
import gov.healthit.chpl.domain.DecertifiedDeveloperResult;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.domain.KeyValueModelStatuses;
import gov.healthit.chpl.domain.Statuses;
import gov.healthit.chpl.entity.UserDeveloperMapEntity;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import gov.healthit.chpl.entity.developer.DeveloperEntitySimple;
import gov.healthit.chpl.entity.developer.DeveloperStatusEntity;
import gov.healthit.chpl.entity.developer.DeveloperStatusEventEntity;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import gov.healthit.chpl.entity.listing.ListingsFromBannedDevelopersEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Repository("developerDAO")
public class DeveloperDAO extends BaseDAOImpl {

    private static final Logger LOGGER = LogManager.getLogger(DeveloperDAO.class);
    private static final DeveloperStatusType DEFAULT_STATUS = DeveloperStatusType.Active;
    private AddressDAO addressDao;
    private ContactDAO contactDao;
    private DeveloperStatusDAO statusDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public DeveloperDAO(AddressDAO addressDao, ContactDAO contactDao, DeveloperStatusDAO statusDao,
            ErrorMessageUtil msgUtil) {
       this.addressDao = addressDao;
       this.contactDao = contactDao;
       this.statusDao = statusDao;
       this.msgUtil = msgUtil;
    }

    public Long create(Developer developer) throws EntityCreationException {
        try {
            DeveloperEntity developerEntity = new DeveloperEntity();
            Long addressId = addressDao.create(developer.getAddress());
            developerEntity.setAddressId(addressId);
            Long contactId = contactDao.create(developer.getContact());
            developerEntity.setContactId(contactId);
            developerEntity.setName(developer.getName());
            developerEntity.setWebsite(developer.getWebsite());
            developerEntity.setSelfDeveloper(developer.getSelfDeveloper());
            developerEntity.setLastModifiedUser(AuthUtil.getAuditId());
            create(developerEntity);

            if (CollectionUtils.isEmpty(developer.getStatusEvents())) {
                DeveloperStatusEventEntity initialStatusEntity = new DeveloperStatusEventEntity();
                initialStatusEntity.setDeveloperId(developerEntity.getId());
                DeveloperStatusEntity defaultStatus = getStatusByName(DEFAULT_STATUS.toString());
                initialStatusEntity.setDeveloperStatusId(defaultStatus.getId());
                initialStatusEntity.setStatusDate(new Date());
                initialStatusEntity.setLastModifiedUser(AuthUtil.getAuditId());
                create(initialStatusEntity);
            } else {
                for (DeveloperStatusEvent providedDeveloperStatusEvent : developer.getStatusEvents()) {
                    if (providedDeveloperStatusEvent.getStatus() != null
                            && !ObjectUtils.isEmpty(providedDeveloperStatusEvent.getStatus().getStatus())
                            && providedDeveloperStatusEvent.getStatusDate() != null) {
                        DeveloperStatusEventEntity currDevStatus = new DeveloperStatusEventEntity();
                        currDevStatus.setDeveloperId(developerEntity.getId());
                        DeveloperStatusEntity defaultStatus = getStatusByName(
                                providedDeveloperStatusEvent.getStatus().getStatus());
                        if (defaultStatus != null) {
                            currDevStatus.setDeveloperStatusId(defaultStatus.getId());
                            currDevStatus.setStatusDate(providedDeveloperStatusEvent.getStatusDate());
                            currDevStatus.setLastModifiedUser(AuthUtil.getAuditId());
                            create(currDevStatus);
                        } else {
                            String msg = "Could not find status with name "
                                    + providedDeveloperStatusEvent.getStatus().getStatus()
                                    + "; cannot insert this status history entry for developer " + developer.getName();
                            LOGGER.error(msg);
                            throw new EntityCreationException(msg);
                        }
                    } else {
                        String msg = "Developer Status name and date must be provided but at least one was not found;"
                                + "cannot insert this status history for developer " + developer.getName();
                        LOGGER.error(msg);
                        throw new EntityCreationException(msg);
                    }
                }
            }
            return developerEntity.getId();
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public void update(Developer developer) throws EntityRetrievalException, EntityCreationException {
        DeveloperEntity entity = this.getEntityById(developer.getDeveloperId());
        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + developer.getDeveloperId() + " does not exist");
        }

        if (developer.getAddress() != null) {
            try {
                Long addressId = addressDao.saveAddress(developer.getAddress());
                entity.setAddressId(addressId);
            } catch (EntityCreationException ex) {
                LOGGER.error("Could not create new address in the database.", ex);
                entity.setAddress(null);
                entity.setAddressId(null);
            }
        } else {
            entity.setAddress(null);
            entity.setAddressId(null);
        }

        if (developer.getContact() != null) {
            if (developer.getContact().getContactId() == null) {
                // if there is not contact id then it must not exist - create it
                Long contactId = contactDao.create(developer.getContact());
                entity.setContactId(contactId);
            } else {
                // if there is a contact id then set that on the object
                contactDao.update(developer.getContact());
            }
        } else {
            // if there's no contact at all, set the id to null
            entity.setContactId(null);
            entity.setContact(null);
        }

        entity.setWebsite(developer.getWebsite());
        entity.setSelfDeveloper(developer.getSelfDeveloper() == null ? false : developer.getSelfDeveloper());
        entity.setName(developer.getName());
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        update(entity);
    }

    public void createDeveloperStatusEvent(DeveloperStatusEvent statusEvent)
            throws EntityCreationException {
        if (statusEvent.getStatus() != null && !ObjectUtils.isEmpty(statusEvent.getStatus().getStatus())
                && statusEvent.getStatusDate() != null) {
            DeveloperStatusEventEntity statusEventEntity = new DeveloperStatusEventEntity();
            statusEventEntity.setDeveloperId(statusEvent.getDeveloperId());
            DeveloperStatusEntity defaultStatus = getStatusByName(statusEvent.getStatus().getStatus());
            if (defaultStatus != null) {
                statusEventEntity.setDeveloperStatusId(defaultStatus.getId());
                statusEventEntity.setReason(statusEvent.getReason());
                statusEventEntity.setStatusDate(statusEvent.getStatusDate());
                statusEventEntity.setLastModifiedUser(AuthUtil.getAuditId());
                statusEventEntity.setDeleted(Boolean.FALSE);
                create(statusEventEntity);
            } else {
                String msg = msgUtil.getMessage("developer.updateStatus.statusNotFound",
                        statusEvent.getStatus().getStatus(), statusEvent.getDeveloperId());
                LOGGER.error(msg);
                throw new EntityCreationException(msg);
            }
        } else {
            String msg = msgUtil.getMessage("developer.updateStatus.missingData", statusEvent.getDeveloperId());
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        }
    }

    public void updateDeveloperStatusEvent(DeveloperStatusEvent statusEvent) throws EntityRetrievalException {
        DeveloperStatusEventEntity statusEventEntity = entityManager.find(DeveloperStatusEventEntity.class,
                statusEvent.getId());
        if (statusEventEntity == null) {
            String msg = msgUtil.getMessage("developer.updateStatus.idNotFound", statusEvent.getId());
            LOGGER.error(msg);
            throw new EntityRetrievalException(msg);
        } else {
            if (statusEvent.getStatus() != null && statusEvent.getStatus().getStatus() != null) {
                DeveloperStatusEntity newStatusEventEntity = getStatusByName(statusEvent.getStatus().getStatus());
                if (newStatusEventEntity != null && newStatusEventEntity.getId() != null) {
                    statusEventEntity.setDeveloperStatus(newStatusEventEntity);
                    statusEventEntity.setReason(statusEvent.getReason());
                    statusEventEntity.setDeveloperStatusId(newStatusEventEntity.getId());
                }
                statusEventEntity.setStatusDate(statusEvent.getStatusDate());
            }
            update(statusEventEntity);
        }
    }

    public void deleteDeveloperStatusEvent(DeveloperStatusEvent statusEvent) throws EntityRetrievalException {
        DeveloperStatusEventEntity statusEventEntity = entityManager.find(DeveloperStatusEventEntity.class,
                statusEvent.getId());
        if (statusEventEntity == null) {
            String msg = msgUtil.getMessage("developer.updateStatus.idNotFound", statusEvent.getId());
            LOGGER.error(msg);
            throw new EntityRetrievalException(msg);
        } else {
            statusEventEntity.setDeleted(Boolean.TRUE);
            statusEventEntity.setLastModifiedUser(AuthUtil.getAuditId());
            update(statusEventEntity);
        }
    }

    @Transactional
    public void delete(Long id) throws EntityRetrievalException {
        DeveloperEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            update(toDelete);
        }
    }

    public List<Developer> findAllIdsAndNames() {
        @SuppressWarnings("unchecked") List<DeveloperEntitySimple> entities = entityManager.createQuery("SELECT dev "
                + "FROM DeveloperEntitySimple dev "
                + "WHERE dev.deleted = false").getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public List<Developer> findAll() {
        List<DeveloperEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public Set<KeyValueModelStatuses> findAllWithStatuses() {
        List<DeveloperEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> new KeyValueModelStatuses(entity.getId(), entity.getName(), createStatuses(entity)))
                .collect(Collectors.toSet());
    }

    private Statuses createStatuses(DeveloperEntity entity) {
        return new Statuses(entity.getDeveloperCertificationStatuses().getActive(),
                entity.getDeveloperCertificationStatuses().getRetired(),
                entity.getDeveloperCertificationStatuses().getWithdrawnByDeveloper(),
                entity.getDeveloperCertificationStatuses().getWithdrawnByAcb(),
                entity.getDeveloperCertificationStatuses().getSuspendedByAcb(),
                entity.getDeveloperCertificationStatuses().getSuspendedByOnc(),
                entity.getDeveloperCertificationStatuses().getTerminatedByOnc());
    }

    public List<Developer> findAllIncludingDeleted() {
        List<DeveloperEntity> entities = getAllEntitiesIncludingDeleted();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public Developer findById(Long id) throws EntityRetrievalException {
        DeveloperEntity entity = getEntityById(id, false);
        return entity.toDomain();
    }

    public Developer getById(Long id) throws EntityRetrievalException {
        return getById(id, false);
    }

    public Developer getById(Long id, final boolean includeDeleted) throws EntityRetrievalException {
        DeveloperEntity entity = getEntityById(id, includeDeleted);
        return entity.toDomain();
    }

    @Transactional(readOnly = true)
    public Developer getSimpleDeveloperById(Long id, boolean includeDeleted) throws EntityRetrievalException {
        Developer result = null;
        String queryStr = "SELECT DISTINCT de "
                + "FROM DeveloperEntitySimple de "
                + "WHERE de.id = :entityid ";
        if (!includeDeleted) {
            queryStr += " AND de.deleted = false";
        }

        Query query = entityManager.createQuery(queryStr, DeveloperEntitySimple.class);
        query.setParameter("entityid", id);
        List<DeveloperEntitySimple> entities = query.getResultList();

        if (entities == null || entities.size() == 0) {
            String msg = msgUtil.getMessage("developer.notFound");
            throw new EntityRetrievalException(msg);
        } else if (entities.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate developer id in database.");
        } else if (entities.size() == 1) {
            result = entities.get(0).toDomain();
        }
        return result;
    }

    public Developer getByName(String name) {
        DeveloperEntity entity = getEntityByName(name);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public Developer getByCode(String code) {
        DeveloperEntity entity = getEntityByCode(code);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public Developer getByVersion(Long productVersionId) throws EntityRetrievalException {
        if (productVersionId == null) {
            throw new EntityRetrievalException("Version ID cannot be null!");
        }
        Query getDeveloperByVersionIdQuery = entityManager.createQuery("SELECT ve FROM ProductVersionEntity pve,"
                + "ProductEntity pe, DeveloperEntity ve " + "WHERE (NOT pve.deleted = true) "
                + "AND pve.id = :versionId " + "AND pve.productId = pe.id " + "AND ve.id = pe.developerId ",
                DeveloperEntity.class);
        getDeveloperByVersionIdQuery.setParameter("versionId", productVersionId);
        @SuppressWarnings("unchecked") List<DeveloperEntity> results = getDeveloperByVersionIdQuery.getResultList();
        if (results != null && results.size() > 0) {
            return results.get(0).toDomain();
        }
        return null;
    }

    public List<Developer> getByWebsite(String website) {
        Query query = entityManager.createQuery("SELECT DISTINCT dev "
                + "FROM DeveloperEntity dev "
                + "LEFT OUTER JOIN FETCH dev.address "
                + "LEFT OUTER JOIN FETCH dev.contact "
                + "LEFT OUTER JOIN FETCH dev.statusEvents statusEvents "
                + "LEFT OUTER JOIN FETCH statusEvents.developerStatus "
                + "WHERE dev.deleted = false "
                + "AND dev.website = :website ", DeveloperEntity.class);
        query.setParameter("website", website);
        @SuppressWarnings("unchecked") List<DeveloperEntity> results = query.getResultList();
        return results.stream()
                .map(result -> result.toDomain())
                .toList();
    }

    public List<DecertifiedDeveloperResult> getDecertifiedDevelopers() {
        Query bannedListingsQuery = entityManager.createQuery("SELECT entity "
                + "FROM CertifiedProductDetailsEntity entity "
                + "WHERE developerStatusName IN (:banned) "
                + "AND deleted = false AND acbIsRetired = false",
                CertifiedProductDetailsEntity.class);
        bannedListingsQuery.setParameter("banned", String.valueOf(DeveloperStatusType.UnderCertificationBanByOnc));
        List<CertifiedProductDetailsEntity> bannedListings = bannedListingsQuery.getResultList();
        List<DecertifiedDeveloperResult> decertifiedDevelopers = new ArrayList<DecertifiedDeveloperResult>();
        // populate dtoList from result
        for (CertifiedProductDetailsEntity currListing : bannedListings) {
            Boolean devExists = false;
            if (decertifiedDevelopers.size() > 0) {
                for (DecertifiedDeveloperResult currDev : decertifiedDevelopers) {
                    if (currDev.getDeveloper().getDeveloperId().equals(currListing.getDeveloperId())) {
                        currDev.setDecertificationDate(currListing.getDeveloperStatusDate());
                        if (!currDev.refersToAcbId(currListing.getCertificationBodyId())) {
                            currDev.getCertifyingBody().add(CertificationBody.builder()
                                    .id(currListing.getCertificationBodyId())
                                    .name(currListing.getCertificationBodyName())
                                    .build());
                        }
                        // aggregate promoting interoperability user count for existing developer
                        if (currListing.getPromotingInteroperabilityUserCount() != null) {
                            currDev.incrementPromotingInteroperabilityUserCount(currListing.getPromotingInteroperabilityUserCount());
                        }
                        // check earliest vs latest promoting interoperability use dates for existing developer
                        if (currListing.getPromotingInteroperabilityUserCountDate() != null) {
                            LocalDate promotingInteroperabilityUserDate = currListing.getPromotingInteroperabilityUserCountDate();
                            if (currDev.getEarliestPromotingInteroperabilityUserCountDate() == null
                                    || promotingInteroperabilityUserDate.isBefore(currDev.getEarliestPromotingInteroperabilityUserCountDate())) {
                                currDev.setEarliestPromotingInteroperabilityUserCountDate(promotingInteroperabilityUserDate);
                                currDev.setEarliestMeaningfulUseDate(promotingInteroperabilityUserDate != null ? DateUtil.toEpochMillis(promotingInteroperabilityUserDate) : null);
                            }
                            if (currDev.getLatestPromotingInteroperabilityUserCountDate() == null
                                    || promotingInteroperabilityUserDate.isAfter(promotingInteroperabilityUserDate)) {
                                currDev.setLatestPromotingInteroperabilityUserCountDate(promotingInteroperabilityUserDate);
                                currDev.setLatestMeaningfulUseDate(promotingInteroperabilityUserDate != null ? DateUtil.toEpochMillis(promotingInteroperabilityUserDate) : null);
                            }
                        }
                        devExists = true;
                        break;
                    }
                }
            }
            if (!devExists) {
                List<CertificationBody> acbList = new ArrayList<CertificationBody>();
                acbList.add(CertificationBody.builder()
                        .id(currListing.getCertificationBodyId())
                        .name(currListing.getCertificationBodyName())
                        .build());
                Developer developer = Developer.builder()
                        .developerId(currListing.getDeveloperId())
                        .name(currListing.getDeveloperName())
                        .developerCode(currListing.getDeveloperCode())
                        .selfDeveloper(currListing.getSelfDeveloper())
                        .website(currListing.getDeveloperWebsite())
                        .build();
                DecertifiedDeveloperResult decertifiedDeveloper = new DecertifiedDeveloperResult(
                        developer, acbList, currListing.getDeveloperStatusDate(),
                        currListing.getPromotingInteroperabilityUserCount(),
                        currListing.getPromotingInteroperabilityUserCountDate() != null ? new Date(DateUtil.toEpochMillis(currListing.getPromotingInteroperabilityUserCountDate())) : null,
                        currListing.getPromotingInteroperabilityUserCountDate() != null ? new Date(DateUtil.toEpochMillis(currListing.getPromotingInteroperabilityUserCountDate())) : null);
                decertifiedDevelopers.add(decertifiedDeveloper);
            }
        }
        return decertifiedDevelopers;
    }

    public List<DecertifiedDeveloper> getDecertifiedDeveloperCollection() {
        Query query = entityManager.createQuery("FROM ListingsFromBannedDevelopersEntity ",
                ListingsFromBannedDevelopersEntity.class);
        @SuppressWarnings("unchecked") List<ListingsFromBannedDevelopersEntity> listingsFromBannedDevelopers = query
                .getResultList();
        List<DecertifiedDeveloper> decertifiedDevelopers = new ArrayList<DecertifiedDeveloper>();
        for (ListingsFromBannedDevelopersEntity currListing : listingsFromBannedDevelopers) {
            boolean devExists = false;
            if (decertifiedDevelopers.size() > 0) {
                for (DecertifiedDeveloper currDecertDev : decertifiedDevelopers) {
                    // if developer already exists just add the acb
                    if (currDecertDev.getDeveloperId() != null
                            && currDecertDev.getDeveloperId().equals(currListing.getDeveloperId())) {
                        currDecertDev.getAcbNames().add(currListing.getAcbName());
                        devExists = true;
                        break;
                    }
                }
            }
            if (!devExists) {
                DecertifiedDeveloper decertDev = new DecertifiedDeveloper();
                decertDev.setDeveloperId(currListing.getDeveloperId());
                decertDev.setDeveloperName(currListing.getDeveloperName());
                decertDev.getAcbNames().add(currListing.getAcbName());
                decertDev.setDecertificationDate(currListing.getDeveloperStatusDate());
                decertifiedDevelopers.add(decertDev);
            }
        }

        return decertifiedDevelopers;
    }

    public List<Developer> getByCertificationBodyId(List<Long> certificationBodyIds) {
        return getEntitiesByCertificationBodyId(certificationBodyIds).stream()
                .map(dev -> dev.toDomain())
                .toList();
    }

    public List<Developer> getDevelopersByUserId(Long userId) {
        Query query = entityManager.createQuery(
                "FROM UserDeveloperMapEntity udm "
                + "join fetch udm.developer developer "
                + "join fetch udm.user u "
                + "join fetch u.permission perm "
                + "join fetch u.contact contact "
                + "where (udm.deleted != true) AND (u.id = :userId) ",
                UserDeveloperMapEntity.class);
        query.setParameter("userId", userId);
        List<UserDeveloperMapEntity> result = query.getResultList();
        return result.stream()
                .map(entity -> entity.getDeveloper().toDomain())
                .toList();
    }

    private List<DeveloperEntity> getAllEntities() {
        List<DeveloperEntity> result = entityManager.createQuery(
                "SELECT DISTINCT v from "
                        + "DeveloperEntity v "
                        + "LEFT OUTER JOIN FETCH v.address "
                        + "LEFT OUTER JOIN FETCH v.contact "
                        + "LEFT OUTER JOIN FETCH v.statusEvents statusEvents "
                        + "LEFT OUTER JOIN FETCH statusEvents.developerStatus "
                        + "LEFT OUTER JOIN FETCH v.developerCertificationStatuses "
                        + "where (NOT v.deleted = true)",
                DeveloperEntity.class).getResultList();
        return result;
    }

    private List<DeveloperEntity> getAllEntitiesIncludingDeleted() {
        List<DeveloperEntity> result = entityManager
                .createQuery("SELECT DISTINCT v from "
                        + "DeveloperEntity v "
                        + "LEFT OUTER JOIN FETCH v.address "
                        + "LEFT OUTER JOIN FETCH v.contact "
                        + "LEFT OUTER JOIN FETCH v.statusEvents statusEvents "
                        + "LEFT OUTER JOIN FETCH statusEvents.developerStatus "
                        + "LEFT OUTER JOIN FETCH v.developerCertificationStatuses ", DeveloperEntity.class)
                .getResultList();
        return result;
    }

    private DeveloperEntity getEntityById(Long id) throws EntityRetrievalException {
        return getEntityById(id, false);
    }

    private DeveloperEntity getEntityById(Long id, boolean includeDeleted) throws EntityRetrievalException {
        DeveloperEntity entity = null;
        String queryStr = "SELECT DISTINCT v FROM "
                + "DeveloperEntity v "
                + "LEFT OUTER JOIN FETCH v.address "
                + "LEFT OUTER JOIN FETCH v.contact "
                + "LEFT OUTER JOIN FETCH v.statusEvents statusEvents "
                + "LEFT OUTER JOIN FETCH statusEvents.developerStatus "
                + "LEFT OUTER JOIN FETCH v.developerCertificationStatuses "
                + "WHERE v.id = :entityid ";
        if (!includeDeleted) {
            queryStr += " AND v.deleted = false";
        }
        Query query = entityManager.createQuery(queryStr, DeveloperEntity.class);
        query.setParameter("entityid", id);
        @SuppressWarnings("unchecked") List<DeveloperEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = msgUtil.getMessage("developer.notFound");
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private DeveloperEntity getEntityByName(final String name) {
        DeveloperEntity entity = null;
        Query query = entityManager
                .createQuery("SELECT DISTINCT v from "
                        + "DeveloperEntity v "
                        + "LEFT OUTER JOIN FETCH v.address "
                        + "LEFT OUTER JOIN FETCH v.contact "
                        + "LEFT OUTER JOIN FETCH v.statusEvents statusEvents "
                        + "LEFT OUTER JOIN FETCH statusEvents.developerStatus "
                        + "LEFT OUTER JOIN FETCH v.developerCertificationStatuses "
                        + "where (NOT v.deleted = true) AND (v.name = :name) ", DeveloperEntity.class);
        query.setParameter("name", name);
        @SuppressWarnings("unchecked") List<DeveloperEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private DeveloperEntity getEntityByCode(final String code) {

        DeveloperEntity entity = null;
        Query query = entityManager
                .createQuery("SELECT DISTINCT v from "
                        + "DeveloperEntity v "
                        + "LEFT OUTER JOIN FETCH v.address "
                        + "LEFT OUTER JOIN FETCH v.contact "
                        + "LEFT OUTER JOIN FETCH v.statusEvents statusEvents "
                        + "LEFT OUTER JOIN FETCH statusEvents.developerStatus "
                        + "LEFT OUTER JOIN FETCH v.developerCertificationStatuses "
                        + "where (NOT v.deleted = true) AND (v.developerCode = :code) ", DeveloperEntity.class);
        query.setParameter("code", code);
        @SuppressWarnings("unchecked") List<DeveloperEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private DeveloperStatusEntity getStatusByName(String statusName) {
        List<DeveloperStatusEntity> statuses = statusDao.getEntitiesByName(statusName);
        if (statuses == null || statuses.size() == 0) {
            LOGGER.error("Could not find the " + statusName + " status");
            return null;
        }
        return statuses.get(0);
    }

    private List<DeveloperEntity> getEntitiesByCertificationBodyId(List<Long> certificationBodyIds) {
        String hql = "SELECT DISTINCT dev "
                + "FROM CertifiedProductEntity cp "
                + "JOIN FETCH cp.productVersion pv "
                + "JOIN FETCH pv.product prod "
                + "JOIN FETCH prod.developer dev "
                + "WHERE cp.certificationBody.id IN (:certificationBodyIds)";

        return entityManager.createQuery(hql, DeveloperEntity.class)
                .setParameter("certificationBodyIds", certificationBodyIds)
                .getResultList();
    }
}
