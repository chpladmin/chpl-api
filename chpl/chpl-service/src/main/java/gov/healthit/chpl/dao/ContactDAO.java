package gov.healthit.chpl.dao;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.entity.ContactEntity;
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

    public void update(PointOfContact contact) throws EntityRetrievalException {
        ContactEntity contactEntity = this.getEntityById(contact.getContactId());
        contactEntity.setEmail(contact.getEmail());
        contactEntity.setFullName(contact.getFullName());
        contactEntity.setPhoneNumber(contact.getPhoneNumber());
        contactEntity.setTitle(contact.getTitle());
        contactEntity.setSignatureDate(null);
        contactEntity.setDeleted(false);
        contactEntity.setLastModifiedUser(AuthUtil.getAuditId());
        update(contactEntity);
    }

    public void delete(Long id) throws EntityRetrievalException {
        ContactEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            update(toDelete);
        }
    }

    public List<PointOfContact> findAll() {
        List<ContactEntity> result = this.findAllEntities();
        if (result == null) {
            return null;
        }

        return result.stream()
                .map(contactEntity -> contactEntity.toDomain())
                .toList();
    }

    public PointOfContact getById(Long id) throws EntityRetrievalException {
        ContactEntity result = this.getEntityById(id);
        if (result != null) {
            return result.toDomain();
        }
        return null;
    }

    public PointOfContact getByValues(PointOfContact contact) {
        ContactEntity result = this.searchEntities(contact);
        if (result == null) {
            return null;
        }
        return result.toDomain();
    }

    private List<ContactEntity> findAllEntities() {
        Query query = entityManager.createQuery("SELECT c "
                + "FROM ContactEntity c "
                + "WHERE (NOT c.deleted = true)");
        return query.getResultList();
    }

    public ContactEntity getEntityById(Long id) throws EntityRetrievalException {
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

    private ContactEntity searchEntities(PointOfContact toSearch) {
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
