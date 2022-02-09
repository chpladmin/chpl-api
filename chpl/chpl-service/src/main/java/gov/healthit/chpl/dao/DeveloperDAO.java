package gov.healthit.chpl.dao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.DecertifiedDeveloperResult;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DecertifiedDeveloperDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.entity.ContactEntity;
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

            DeveloperStatusEventEntity initialStatusEntity = new DeveloperStatusEventEntity();
            initialStatusEntity.setDeveloperId(developerEntity.getId());
            DeveloperStatusEntity defaultStatus = getStatusByName(DEFAULT_STATUS.toString());
            initialStatusEntity.setDeveloperStatusId(defaultStatus.getId());
            initialStatusEntity.setStatusDate(new Date());
            initialStatusEntity.setLastModifiedUser(AuthUtil.getAuditId());
            create(initialStatusEntity);
            return developerEntity.getId();
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public DeveloperDTO create(DeveloperDTO dto) throws EntityCreationException, EntityRetrievalException {

        DeveloperEntity entity = null;
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
            entity = new DeveloperEntity();

            if (dto.getAddress() != null) {
                entity.setAddress(addressDao.saveAddress(dto.getAddress()));
                entity.setAddressId(entity.getAddress().getId());
            }
            if (dto.getContact() != null) {
                if (dto.getContact().getId() != null) {
                    ContactDTO contact = contactDao.getById(dto.getContact().getId());
                    if (contact != null && contact.getId() != null) {
                        entity.setContactId(contact.getId());
                    }
                } else {
                    ContactEntity contact = contactDao.create(dto.getContact());
                    if (contact != null) {
                        entity.setContactId(contact.getId());
                    }
                }
            }

            entity.setName(dto.getName());
            entity.setWebsite(dto.getWebsite());

            if (dto.getSelfDeveloper() != null) {
                entity.setSelfDeveloper(dto.getSelfDeveloper());
            } else {
                entity.setSelfDeveloper(false);
            }

            if (dto.getDeleted() != null) {
                entity.setDeleted(dto.getDeleted());
            } else {
                entity.setDeleted(false);
            }

            if (dto.getLastModifiedUser() != null) {
                entity.setLastModifiedUser(dto.getLastModifiedUser());
            } else {
                entity.setLastModifiedUser(AuthUtil.getAuditId());
            }

            if (dto.getLastModifiedDate() != null) {
                entity.setLastModifiedDate(dto.getLastModifiedDate());
            } else {
                entity.setLastModifiedDate(new Date());
            }

            if (dto.getCreationDate() != null) {
                entity.setCreationDate(dto.getCreationDate());
            } else {
                entity.setCreationDate(new Date());
            }

            create(entity);

            // create a status history entry - will be Active by default
            if (dto.getStatusEvents() == null || dto.getStatusEvents().size() == 0) {
                DeveloperStatusEventEntity initialDeveloperStatus = new DeveloperStatusEventEntity();
                initialDeveloperStatus.setDeveloperId(entity.getId());
                DeveloperStatusEntity defaultStatus = getStatusByName(DEFAULT_STATUS.toString());
                initialDeveloperStatus.setDeveloperStatusId(defaultStatus.getId());
                initialDeveloperStatus.setStatusDate(entity.getCreationDate());
                initialDeveloperStatus.setDeleted(false);
                initialDeveloperStatus.setLastModifiedUser(entity.getLastModifiedUser());
                entityManager.persist(initialDeveloperStatus);
                entityManager.flush();
            } else {
                for (DeveloperStatusEventDTO providedDeveloperStatusEvent : dto.getStatusEvents()) {
                    if (providedDeveloperStatusEvent.getStatus() != null
                            && !StringUtils.isEmpty(providedDeveloperStatusEvent.getStatus().getStatusName())
                            && providedDeveloperStatusEvent.getStatusDate() != null) {
                        DeveloperStatusEventEntity currDevStatus = new DeveloperStatusEventEntity();
                        currDevStatus.setDeveloperId(entity.getId());
                        DeveloperStatusEntity defaultStatus = getStatusByName(
                                providedDeveloperStatusEvent.getStatus().getStatusName());
                        if (defaultStatus != null) {
                            currDevStatus.setDeveloperStatusId(defaultStatus.getId());
                            currDevStatus.setStatusDate(providedDeveloperStatusEvent.getStatusDate());
                            currDevStatus.setDeleted(false);
                            currDevStatus.setLastModifiedUser(entity.getLastModifiedUser());
                            entityManager.persist(currDevStatus);
                            entityManager.flush();
                        } else {
                            String msg = "Could not find status with name "
                                    + providedDeveloperStatusEvent.getStatus().getStatusName()
                                    + "; cannot insert this status history entry for developer " + entity.getName();
                            LOGGER.error(msg);
                            throw new EntityCreationException(msg);
                        }
                    } else {
                        String msg = "Developer Status name and date must be provided but at least one was not found;"
                                + "cannot insert this status history for developer " + entity.getName();
                        LOGGER.error(msg);
                        throw new EntityCreationException(msg);
                    }
                }
            }

            Long id = entity.getId();
            entityManager.clear();
            return getById(id);
        }
    }

    public DeveloperDTO update(DeveloperDTO dto) throws EntityRetrievalException, EntityCreationException {
        DeveloperEntity entity = this.getEntityById(dto.getId());
        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
        }

        if (dto.getAddress() != null) {
            try {
                AddressEntity address = addressDao.saveAddress(dto.getAddress());
                entity.setAddress(address);
                entity.setAddressId(address.getId());
            } catch (EntityCreationException ex) {
                LOGGER.error("Could not create new address in the database.", ex);
                entity.setAddress(null);
                entity.setAddressId(null);
            }
        } else {
            entity.setAddress(null);
            entity.setAddressId(null);
        }

        if (dto.getContact() != null) {
            if (dto.getContact().getId() == null) {
                // if there is not contact id then it must not exist - create it
                ContactEntity contact = contactDao.create(dto.getContact());
                if (contact != null && contact.getId() != null) {
                    entity.setContactId(contact.getId());
                    entity.setContact(contact);
                }
            } else {
                // if there is a contact id then set that on the object
                ContactEntity contact = contactDao.update(dto.getContact());
                if (contact != null) {
                    entity.setContactId(dto.getContact().getId());
                    entity.setContact(contact);
                }
            }
        } else {
            // if there's no contact at all, set the id to null
            entity.setContactId(null);
            entity.setContact(null);
        }

        entity.setWebsite(dto.getWebsite());
        if (dto.getSelfDeveloper() != null) {
            entity.setSelfDeveloper(dto.getSelfDeveloper());
        } else {
            entity.setSelfDeveloper(false);
        }
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }

        if (dto.getDeleted() != null) {
            entity.setDeleted(dto.getDeleted());
        }

        if (dto.getLastModifiedUser() != null) {
            entity.setLastModifiedUser(dto.getLastModifiedUser());
        } else {
            entity.setLastModifiedUser(AuthUtil.getAuditId());
        }

        if (dto.getLastModifiedDate() != null) {
            entity.setLastModifiedDate(dto.getLastModifiedDate());
        } else {
            entity.setLastModifiedDate(new Date());
        }

        update(entity);
        return getById(dto.getId());
    }

    public void createDeveloperStatusEvent(final DeveloperStatusEventDTO statusEventDto)
            throws EntityCreationException {
        if (statusEventDto.getStatus() != null && !StringUtils.isEmpty(statusEventDto.getStatus().getStatusName())
                && statusEventDto.getStatusDate() != null) {
            DeveloperStatusEventEntity statusEvent = new DeveloperStatusEventEntity();
            statusEvent.setDeveloperId(statusEventDto.getDeveloperId());
            DeveloperStatusEntity defaultStatus = getStatusByName(statusEventDto.getStatus().getStatusName());
            if (defaultStatus != null) {
                statusEvent.setDeveloperStatusId(defaultStatus.getId());
                statusEvent.setReason(statusEventDto.getReason());
                statusEvent.setStatusDate(statusEventDto.getStatusDate());
                statusEvent.setLastModifiedUser(AuthUtil.getAuditId());
                statusEvent.setDeleted(Boolean.FALSE);
                entityManager.persist(statusEvent);
                entityManager.flush();
            } else {
                String msg = msgUtil.getMessage("developer.updateStatus.statusNotFound",
                        statusEventDto.getStatus().getStatusName(), statusEventDto.getDeveloperId());
                LOGGER.error(msg);
                throw new EntityCreationException(msg);
            }
        } else {
            String msg = msgUtil.getMessage("developer.updateStatus.missingData", statusEventDto.getDeveloperId());
            LOGGER.error(msg);
            throw new EntityCreationException(msg);
        }
        entityManager.clear();
    }

    public void updateDeveloperStatusEvent(DeveloperStatusEventDTO statusEventDto) throws EntityRetrievalException {
        DeveloperStatusEventEntity entityToUpdate = entityManager.find(DeveloperStatusEventEntity.class,
                statusEventDto.getId());
        if (entityToUpdate == null) {
            String msg = msgUtil.getMessage("developer.updateStatus.idNotFound", statusEventDto.getId());
            LOGGER.error(msg);
            throw new EntityRetrievalException(msg);
        } else {
            if (statusEventDto.getStatus() != null && statusEventDto.getStatus().getStatusName() != null) {
                DeveloperStatusEntity newStatus = getStatusByName(statusEventDto.getStatus().getStatusName());
                if (newStatus != null && newStatus.getId() != null) {
                    entityToUpdate.setDeveloperStatus(newStatus);
                    entityToUpdate.setReason(statusEventDto.getReason());
                    entityToUpdate.setDeveloperStatusId(newStatus.getId());
                }
                entityToUpdate.setStatusDate(statusEventDto.getStatusDate());
            }
            entityManager.merge(entityToUpdate);
            entityManager.flush();
        }
    }

    public void deleteDeveloperStatusEvent(DeveloperStatusEventDTO statusEventDto) throws EntityRetrievalException {
        DeveloperStatusEventEntity statusEventEntity = entityManager.find(DeveloperStatusEventEntity.class,
                statusEventDto.getId());
        if (statusEventEntity == null) {
            String msg = msgUtil.getMessage("developer.updateStatus.idNotFound", statusEventDto.getId());
            LOGGER.error(msg);
            throw new EntityRetrievalException(msg);
        } else {
            statusEventEntity.setDeleted(Boolean.TRUE);
            entityManager.merge(statusEventEntity);
            entityManager.flush();
            entityManager.clear();
        }
    }

    @Transactional
    public void delete(Long id) throws EntityRetrievalException {
        DeveloperEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            update(toDelete);
        }
    }

    public List<DeveloperDTO> findAllIdsAndNames() {

        @SuppressWarnings("unchecked") List<DeveloperEntitySimple> entities = entityManager.createQuery("SELECT dev "
                + "FROM DeveloperEntitySimple dev "
                + "WHERE dev.deleted = false").getResultList();
        List<DeveloperDTO> dtos = new ArrayList<>();

        for (DeveloperEntitySimple entity : entities) {
            DeveloperDTO dto = new DeveloperDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    public List<DeveloperDTO> findAll() {

        List<DeveloperEntity> entities = getAllEntities();
        List<DeveloperDTO> dtos = new ArrayList<>();

        for (DeveloperEntity entity : entities) {
            DeveloperDTO dto = new DeveloperDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    public List<DeveloperDTO> findAllIncludingDeleted() {

        List<DeveloperEntity> entities = getAllEntitiesIncludingDeleted();
        List<DeveloperDTO> dtos = new ArrayList<>();

        for (DeveloperEntity entity : entities) {
            DeveloperDTO dto = new DeveloperDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    public Developer findById(Long id) throws EntityRetrievalException {
        DeveloperEntity entity = getEntityById(id, false);
        return entity.toDomain();
    }

    public DeveloperDTO getById(Long id) throws EntityRetrievalException {
        return getById(id, false);
    }

    public DeveloperDTO getById(final Long id, final boolean includeDeleted) throws EntityRetrievalException {
        DeveloperEntity entity = getEntityById(id, includeDeleted);
        DeveloperDTO dto = null;
        if (entity != null) {
            dto = new DeveloperDTO(entity);
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public DeveloperDTO getSimpleDeveloperById(Long id, boolean includeDeleted) throws EntityRetrievalException {
        DeveloperDTO result = null;
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
            result = new DeveloperDTO(entities.get(0));
        }

        return result;
    }

    public DeveloperDTO getByName(String name) {
        DeveloperEntity entity = getEntityByName(name);
        DeveloperDTO dto = null;
        if (entity != null) {
            dto = new DeveloperDTO(entity);
        }
        return dto;
    }

    public DeveloperDTO getByCode(String code) {
        DeveloperEntity entity = getEntityByCode(code);
        DeveloperDTO dto = null;
        if (entity != null) {
            dto = new DeveloperDTO(entity);
        }
        return dto;
    }

    public DeveloperDTO getByVersion(Long productVersionId) throws EntityRetrievalException {
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
            return new DeveloperDTO(results.get(0));
        }
        return null;
    }

    /**
     * Find any Developers with the given website.
     *
     * @param website
     * @return the developers
     */
    public List<DeveloperDTO> getByWebsite(final String website) {
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
        List<DeveloperDTO> resultDtos = new ArrayList<DeveloperDTO>();
        for (DeveloperEntity entity : results) {
            resultDtos.add(new DeveloperDTO(entity));
        }
        return resultDtos;
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

    public List<DecertifiedDeveloperDTO> getDecertifiedDeveloperCollection() {

        Query query = entityManager.createQuery("FROM ListingsFromBannedDevelopersEntity ",
                ListingsFromBannedDevelopersEntity.class);
        @SuppressWarnings("unchecked") List<ListingsFromBannedDevelopersEntity> listingsFromBannedDevelopers = query
                .getResultList();
        List<DecertifiedDeveloperDTO> decertifiedDevelopers = new ArrayList<DecertifiedDeveloperDTO>();
        for (ListingsFromBannedDevelopersEntity currListing : listingsFromBannedDevelopers) {
            boolean devExists = false;
            if (decertifiedDevelopers.size() > 0) {
                for (DecertifiedDeveloperDTO currDecertDev : decertifiedDevelopers) {
                    // if developer already exists just add the acb
                    if (currDecertDev.getDeveloper() != null && currDecertDev.getDeveloper().getId() != null
                            && currDecertDev.getDeveloper().getId().equals(currListing.getDeveloperId())) {
                        CertificationBodyDTO acb = new CertificationBodyDTO();
                        acb.setId(currListing.getAcbId());
                        acb.setName(currListing.getAcbName());
                        currDecertDev.getAcbs().add(acb);
                        devExists = true;
                        break;
                    }
                }
            }
            if (!devExists) {
                DecertifiedDeveloperDTO decertDev = new DecertifiedDeveloperDTO();
                DeveloperDTO dev = new DeveloperDTO();
                dev.setId(currListing.getDeveloperId());
                dev.setName(currListing.getDeveloperName());
                decertDev.setDeveloper(dev);
                CertificationBodyDTO acb = new CertificationBodyDTO();
                acb.setId(currListing.getAcbId());
                acb.setName(currListing.getAcbName());
                decertDev.getAcbs().add(acb);
                decertDev.setDecertificationDate(currListing.getDeveloperStatusDate());
                decertifiedDevelopers.add(decertDev);
            }
        }

        return decertifiedDevelopers;
    }

    public List<DeveloperDTO> getByCertificationBodyId(final List<Long> certificationBodyIds) {
        return getEntitiesByCertificationBodyId(certificationBodyIds).stream()
                .map(dev -> new DeveloperDTO(dev))
                .collect(Collectors.<DeveloperDTO> toList());
    }

    public List<DeveloperDTO> getDevelopersByUserId(final Long userId) {
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

        List<DeveloperDTO> dtos = new ArrayList<DeveloperDTO>();
        if (result != null) {
            for (UserDeveloperMapEntity entity : result) {
                dtos.add(new DeveloperDTO(entity.getDeveloper()));
            }
        }
        return dtos;
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

    private DeveloperEntity getEntityById(final Long id) throws EntityRetrievalException {
        return getEntityById(id, false);
    }

    private DeveloperEntity getEntityById(final Long id, final boolean includeDeleted) throws EntityRetrievalException {

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

    private DeveloperStatusEntity getStatusByName(final String statusName) {
        List<DeveloperStatusEntity> statuses = statusDao.getEntitiesByName(statusName);
        if (statuses == null || statuses.size() == 0) {
            LOGGER.error("Could not find the " + statusName + " status");
            return null;
        }
        return statuses.get(0);
    }

    private List<DeveloperEntity> getEntitiesByCertificationBodyId(final List<Long> certificationBodyIds) {
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
