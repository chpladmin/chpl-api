package gov.healthit.chpl.dao.auth;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.auth.UserContactEntity;
import gov.healthit.chpl.util.AuthUtil;
import jakarta.persistence.Query;

@Repository(value = "userContactDAO")
public class UserContactDAO extends BaseDAOImpl {

    @Transactional
    public void create(UserContactEntity contact) {
        super.create(contact);
    }

    @Transactional
    public void update(UserContactEntity contact) {
        super.update(contact);
    }

    public void delete(Long contactId) {
        Query query = entityManager.createQuery("UPDATE UserContact "
        + "SET deleted = true, "
        + AuthUtil.getAuditId()
        + "WHERE id = :contactid");
        query.setParameter("contactid", contactId);
        query.executeUpdate();
    }

    public void delete(UserContactEntity contact) {
        contact.setDeleted(true);
        super.update(contact);
    }
}
