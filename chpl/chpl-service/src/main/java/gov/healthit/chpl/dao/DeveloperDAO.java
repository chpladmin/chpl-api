package gov.healthit.chpl.dao;

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
import gov.healthit.chpl.domain.DeveloperTransparency;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DecertifiedDeveloperDTO;
import gov.healthit.chpl.dto.DecertifiedDeveloperDTODeprecated;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.entity.AttestationType;
import gov.healthit.chpl.entity.ContactEntity;
import gov.healthit.chpl.entity.developer.DeveloperACBMapEntity;
import gov.healthit.chpl.entity.developer.DeveloperACBTransparencyMapEntity;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import gov.healthit.chpl.entity.developer.DeveloperEntitySimple;
import gov.healthit.chpl.entity.developer.DeveloperStatusEntity;
import gov.healthit.chpl.entity.developer.DeveloperStatusEventEntity;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.entity.developer.DeveloperTransparencyEntity;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import gov.healthit.chpl.entity.listing.ListingsFromBannedDevelopersEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Repository("developerDAO")
public class DeveloperDAO extends BaseDAOImpl {

    private static final Logger LOGGER = LogManager.getLogger(DeveloperDAO.class);
    private static final DeveloperStatusType DEFAULT_STATUS = DeveloperStatusType.Active;
    @Autowired
    private AddressDAO addressDao;
    @Autowired
    private ContactDAO contactDao;
    @Autowired
    private DeveloperStatusDAO statusDao;
    @Autowired
    private ErrorMessageUtil msgUtil;

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

    public DeveloperACBMapDTO createTransparencyMapping(DeveloperACBMapDTO dto) {
        DeveloperACBMapEntity mapping = new DeveloperACBMapEntity();
        mapping.getDeveloperId(dto.getDeveloperId());
        mapping.setCertificationBodyId(dto.getAcbId());
        if (dto.getTransparencyAttestation() != null && dto.getTransparencyAttestation().getTransparencyAttestation() != null) {
            mapping.setTransparencyAttestation(
                    AttestationType.getValue(dto.getTransparencyAttestation().getTransparencyAttestation()));
        }
        mapping.setCreationDate(new Date());
        mapping.setDeleted(false);
        mapping.setLastModifiedDate(new Date());
        mapping.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.persist(mapping);
        entityManager.flush();
        return new DeveloperACBMapDTO(mapping);
    }

    public DeveloperDTO update(DeveloperDTO dto) throws EntityRetrievalException, EntityCreationException {
        DeveloperEntity entity = this.getEntityById(dto.getId());
        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
        }

        if (dto.getAddress() != null) {
            try {
                entity.setAddress(addressDao.saveAddress(dto.getAddress()));
            } catch (final EntityCreationException ex) {
                LOGGER.error("Could not create new address in the database.", ex);
                entity.setAddress(null);
            }
        } else {
            entity.setAddress(null);
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

        entityManager.clear();
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

    public DeveloperACBMapDTO updateTransparencyMapping(DeveloperACBMapDTO dto) {
        DeveloperACBMapEntity mapping = getTransparencyMappingEntity(dto.getDeveloperId(), dto.getAcbId());
        if (mapping == null) {
            return null;
        }

        mapping.setTransparencyAttestation(
                AttestationType.getValue(dto.getTransparencyAttestation().getTransparencyAttestation()));
        mapping.setLastModifiedDate(new Date());
        mapping.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.persist(mapping);
        entityManager.flush();
        return new DeveloperACBMapDTO(mapping);
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

    public void deleteTransparencyMapping(Long developerId, Long acbId) {
        DeveloperACBMapEntity toDelete = getTransparencyMappingEntity(developerId, acbId);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.persist(toDelete);
            entityManager.flush();
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

    public List<DeveloperTransparency> getAllDevelopersWithTransparencies() {
        Query query = entityManager.createQuery("SELECT dt " + "FROM DeveloperTransparencyEntity dt ",
                DeveloperTransparencyEntity.class);

        @SuppressWarnings("unchecked") List<DeveloperTransparencyEntity> entityResults = query.getResultList();
        List<DeveloperTransparency> domainResults = new ArrayList<DeveloperTransparency>();
        for (DeveloperTransparencyEntity entity : entityResults) {
            DeveloperTransparency domain = new DeveloperTransparency();
            domain.setId(entity.getId());
            domain.setName(entity.getName());
            domain.setStatus(entity.getStatus());
            domain.getListingCounts().setActive(entity.getCountActiveListings());
            domain.getListingCounts().setRetired(entity.getCountRetiredListings());
            domain.getListingCounts().setPending(entity.getCountPendingListings());
            domain.getListingCounts().setSuspendedByOncAcb(entity.getCountSuspendedByOncAcbListings());
            domain.getListingCounts().setSuspendedByOnc(entity.getCountSuspendedByOncListings());
            domain.getListingCounts().setTerminatedByOnc(entity.getCountTerminatedByOncListings());
            domain.getListingCounts().setWithdrawnByDeveloper(entity.getCountWithdrawnByDeveloperListings());
            domain.getListingCounts().setWithdrawnByDeveloperUnderSurveillance(
                    entity.getCountWithdrawnByDeveloperUnderSurveillanceListings());
            domain.getListingCounts().setWithdrawnByOncAcb(entity.getCountWithdrawnByOncAcbListings());
            domain.setAcbAttestations(entity.getAcbAttestations());
            domain.setTransparencyAttestationUrls(entity.getTransparencyAttestationUrls());
            domainResults.add(domain);
        }
        return domainResults;
    }

    public DeveloperACBMapDTO getTransparencyMapping(Long developerId, Long acbId) {
        DeveloperACBMapEntity mapping = getTransparencyMappingEntity(developerId, acbId);
        if (mapping == null) {
            return null;
        }
        return new DeveloperACBMapDTO(mapping);
    }

    public List<DeveloperACBMapDTO> getAllTransparencyMappings() {
        List<DeveloperACBTransparencyMapEntity> entities = getTransparencyMappingEntities();
        List<DeveloperACBMapDTO> dtos = new ArrayList<>();

        for (DeveloperACBTransparencyMapEntity entity : entities) {
            DeveloperACBMapDTO dto = new DeveloperACBMapDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    public DeveloperDTO getById(final Long id) throws EntityRetrievalException {
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

    public List<DecertifiedDeveloperDTODeprecated> getDecertifiedDevelopers() {

        Query bannedListingsQuery = entityManager.createQuery(
                "FROM CertifiedProductDetailsEntity "
                        + "WHERE developerStatusName IN (:banned) AND deleted = false AND acbIsRetired = false",
                CertifiedProductDetailsEntity.class);
        bannedListingsQuery.setParameter("banned", String.valueOf(DeveloperStatusType.UnderCertificationBanByOnc));
        @SuppressWarnings("unchecked") List<CertifiedProductDetailsEntity> bannedListings = bannedListingsQuery.getResultList();
        List<DecertifiedDeveloperDTODeprecated> decertifiedDevelopers = new ArrayList<DecertifiedDeveloperDTODeprecated>();
        // populate dtoList from result
        for (CertifiedProductDetailsEntity currListing : bannedListings) {
            LOGGER.debug("CertifiedProductDetailsEntity: " + currListing.getDeveloperId() + " "
                    + currListing.getCertificationBodyId() + " " + currListing.getMeaningfulUseUsers());
            Boolean devExists = false;
            if (decertifiedDevelopers.size() > 0) {
                for (DecertifiedDeveloperDTODeprecated currDev : decertifiedDevelopers) {
                    LOGGER.debug("DeveloperDecertifiedDTO: " + currDev.getDeveloperId() + " " + currDev.getAcbIdList()
                            + " " + currDev.getNumMeaningfulUse());
                    // if developer already exists, update it to include ACB and
                    // aggregate numMeaningfulUse
                    if (currDev.getDeveloperId().equals(currListing.getDeveloperId())) {
                        LOGGER.debug(currDev.getDeveloperId() + " == " + currListing.getDeveloperId());
                        currDev.setDeveloperStatus(currListing.getDeveloperStatusName());
                        LOGGER.debug("set dto dev status to " + currListing.getDeveloperStatusName());
                        currDev.setDecertificationDate(currListing.getDeveloperStatusDate());
                        LOGGER.debug("set dev decert date to " + currListing.getDeveloperStatusDate());
                        // If this developer is not associated with the ACB, add
                        // the ACB
                        if (!currDev.getAcbIdList().contains(currListing.getCertificationBodyId())) {
                            LOGGER.debug("dto does not contain " + currListing.getCertificationBodyName());
                            currDev.addAcb(currListing.getCertificationBodyId());
                        }
                        LOGGER.debug("added acb " + currListing.getCertificationBodyId() + " to dto with dev id == "
                                + currDev.getDeveloperId());
                        // aggregate meaningful use count for existing developer
                        if (currListing.getMeaningfulUseUsers() != null) {
                            currDev.incrementNumMeaningfulUse(currListing.getMeaningfulUseUsers());
                            LOGGER.debug(
                                    "added numMeaningfulUse to dto with value " + currListing.getMeaningfulUseUsers());
                        }
                        // check earliest vs latest meaningful use dates for
                        // existing developer
                        if (currListing.getMeaningfulUseUsersDate() != null) {
                            if (currDev.getEarliestNumMeaningfulUseDate() == null) {
                                currDev.setEarliestNumMeaningfulUseDate(currListing.getMeaningfulUseUsersDate());
                            } else if (currListing.getMeaningfulUseUsersDate().getTime() < currDev
                                    .getEarliestNumMeaningfulUseDate().getTime()) {
                                currDev.setEarliestNumMeaningfulUseDate(currListing.getMeaningfulUseUsersDate());
                            }
                            if (currDev.getLatestNumMeaningfulUseDate() == null) {
                                currDev.setLatestNumMeaningfulUseDate(currListing.getMeaningfulUseUsersDate());
                            } else if (currListing.getMeaningfulUseUsersDate().getTime() > currDev
                                    .getLatestNumMeaningfulUseDate().getTime()) {
                                currDev.setLatestNumMeaningfulUseDate(currListing.getMeaningfulUseUsersDate());
                            }
                        }
                        devExists = true;
                        break;
                    }
                }
            }
            if (!devExists) {
                List<Long> acbList = new ArrayList<Long>();
                acbList.add(currListing.getCertificationBodyId());
                DecertifiedDeveloperDTODeprecated decertDev = new DecertifiedDeveloperDTODeprecated(
                        currListing.getDeveloperId(), acbList, currListing.getDeveloperStatusName(),
                        currListing.getDeveloperStatusDate(), currListing.getMeaningfulUseUsers());
                decertDev.setEarliestNumMeaningfulUseDate(currListing.getMeaningfulUseUsersDate());
                decertDev.setLatestNumMeaningfulUseDate(currListing.getMeaningfulUseUsersDate());
                decertifiedDevelopers.add(decertDev);
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
                .collect(Collectors.<DeveloperDTO>toList());
    }

    private void create(final DeveloperEntity entity) {
        entityManager.persist(entity);
        entityManager.flush();
    }

    private void update(final DeveloperEntity entity) {
        entityManager.merge(entity);
        entityManager.flush();
    }

    private List<DeveloperEntity> getAllEntities() {
        List<DeveloperEntity> result = entityManager.createQuery(
                "SELECT DISTINCT v from " + "DeveloperEntity v " + "LEFT OUTER JOIN FETCH v.address "
                        + "LEFT OUTER JOIN FETCH v.contact " + "LEFT OUTER JOIN FETCH v.statusEvents statusEvents "
                        + "LEFT OUTER JOIN FETCH statusEvents.developerStatus "
                        + "LEFT OUTER JOIN FETCH v.developerCertificationStatuses " + "where (NOT v.deleted = true)",
                DeveloperEntity.class).getResultList();
        return result;
    }

    private List<DeveloperEntity> getAllEntitiesIncludingDeleted() {
        List<DeveloperEntity> result = entityManager
                .createQuery("SELECT DISTINCT v from " + "DeveloperEntity v " + "LEFT OUTER JOIN FETCH v.address "
                        + "LEFT OUTER JOIN FETCH v.contact " + "LEFT OUTER JOIN FETCH v.statusEvents statusEvents "
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
        String queryStr = "SELECT DISTINCT v FROM " + "DeveloperEntity v " + "LEFT OUTER JOIN FETCH v.address "
                + "LEFT OUTER JOIN FETCH v.contact " + "LEFT OUTER JOIN FETCH v.statusEvents statusEvents "
                + "LEFT OUTER JOIN FETCH statusEvents.developerStatus " + "WHERE v.id = :entityid ";
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
                .createQuery("SELECT DISTINCT v from " + "DeveloperEntity v " + "LEFT OUTER JOIN FETCH v.address "
                        + "LEFT OUTER JOIN FETCH v.contact " + "LEFT OUTER JOIN FETCH v.statusEvents statusEvents "
                        + "LEFT OUTER JOIN FETCH statusEvents.developerStatus "
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
                .createQuery("SELECT DISTINCT v from " + "DeveloperEntity v " + "LEFT OUTER JOIN FETCH v.address "
                        + "LEFT OUTER JOIN FETCH v.contact " + "LEFT OUTER JOIN FETCH v.statusEvents statusEvents "
                        + "LEFT OUTER JOIN FETCH statusEvents.developerStatus "
                        + "where (NOT v.deleted = true) AND (v.developerCode = :code) ", DeveloperEntity.class);
        query.setParameter("code", code);
        @SuppressWarnings("unchecked") List<DeveloperEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private DeveloperACBMapEntity getTransparencyMappingEntity(final Long developerId, final Long acbId) {
        Query query = entityManager.createQuery("FROM DeveloperACBMapEntity map "
                + "LEFT OUTER JOIN FETCH map.certificationBody where " + "(NOT map.deleted = true) "
                + "AND map.developerId = :developerId " + "AND map.certificationBodyId = :acbId",
                DeveloperACBMapEntity.class);
        query.setParameter("developerId", developerId);
        query.setParameter("acbId", acbId);

        @SuppressWarnings("unchecked") List<DeveloperACBMapEntity> results = query.getResultList();
        if (results != null && results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

    private List<DeveloperACBTransparencyMapEntity> getTransparencyMappingEntities() {
        List<DeveloperACBTransparencyMapEntity> result = entityManager
                .createQuery("FROM DeveloperACBTransparencyMapEntity", DeveloperACBTransparencyMapEntity.class)
                .getResultList();
        return result;
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
