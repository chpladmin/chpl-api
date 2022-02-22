package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.entity.ContactEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("contactDao")
public class ContactDAO extends BaseDAOImpl {

    public Long create(PointOfContact contact) {
        ContactEntity toInsert = new ContactEntity();
        toInsert.setEmail(contact.getEmail());
        toInsert.setFullName(contact.getFullName());
        toInsert.setPhoneNumber(contact.getPhoneNumber());
        toInsert.setTitle(contact.getTitle());
        toInsert.setSignatureDate(null);
        toInsert.setDeleted(false);
        toInsert.setLastModifiedUser(AuthUtil.getAuditId());
        create(toInsert);
        return toInsert.getId();
    }

    @Deprecated
    public ContactEntity create(ContactDTO dto) throws EntityCreationException, EntityRetrievalException {
        ContactEntity toInsert = new ContactEntity();
        toInsert.setEmail(dto.getEmail());
        toInsert.setFullName(dto.getFullName());
        toInsert.setPhoneNumber(dto.getPhoneNumber());
        toInsert.setTitle(dto.getTitle());
        toInsert.setSignatureDate(null);

        toInsert.setDeleted(false);
        toInsert.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.persist(toInsert);
        entityManager.flush();
        return toInsert;
    }

    @Transactional
    public ContactEntity update(ContactDTO dto) throws EntityRetrievalException {
        ContactEntity contact = this.getEntityById(dto.getId());

        contact.setEmail(dto.getEmail());
        contact.setFullName(dto.getFullName());
        contact.setPhoneNumber(dto.getPhoneNumber());
        contact.setTitle(dto.getTitle());
        contact.setSignatureDate(dto.getSignatureDate());

        contact.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.merge(contact);
        return contact;
    }

    @Transactional
    public void delete(Long id) throws EntityRetrievalException {
        ContactEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.merge(toDelete);
        }
    }

    public List<ContactDTO> findAll() {
        List<ContactEntity> result = this.findAllEntities();
        if (result == null) {
            return null;
        }

        List<ContactDTO> dtos = new ArrayList<ContactDTO>(result.size());
        for (ContactEntity entity : result) {
            dtos.add(new ContactDTO(entity));
        }
        return dtos;
    }

    public ContactDTO getById(Long id) throws EntityRetrievalException {
        ContactDTO dto = null;
        ContactEntity entity = this.getEntityById(id);

        if (entity != null) {
            dto = new ContactDTO(entity);
        }
        return dto;
    }

    public ContactDTO getByValues(ContactDTO contact) {
        ContactEntity entity = this.searchEntities(contact);
        if (entity == null) {
            return null;
        }
        return new ContactDTO(entity);
    }

    private List<ContactEntity> findAllEntities() {
        Query query = entityManager.createQuery("SELECT c "
                + "FROM ContactEntity c "
                + "WHERE (NOT c.deleted = true)");
        return query.getResultList();
    }

    public ContactEntity getEntityById(final Long id) throws EntityRetrievalException {
        ContactEntity entity = null;

        Query query = entityManager.createQuery(
                "SELECT c "
                + "FROM ContactEntity c "
                + "WHERE (NOT deleted = true) AND (id = :entityid) ", ContactEntity.class);
        query.setParameter("entityid", id);
        List<ContactEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate contact id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }

        return entity;
    }

    private ContactEntity searchEntities(ContactDTO toSearch) {
        ContactEntity entity = null;

        String contactQuery = "SELECT c "
                + "FROM ContactEntity c "
                + "WHERE (NOT deleted = true) ";
        if (toSearch.getFullName() != null) {
            contactQuery += " AND (fullName = :fullName) ";
        }

        Query query = entityManager.createQuery(contactQuery, ContactEntity.class);
        if (toSearch.getFullName() != null) {
            query.setParameter("fullName", toSearch.getFullName());
        }

        List<ContactEntity> result = query.getResultList();
        if (result.size() >= 1) {
            entity = result.get(0);
        }

        return entity;
    }
}
