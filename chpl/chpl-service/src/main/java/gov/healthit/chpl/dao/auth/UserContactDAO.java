package gov.healthit.chpl.dao.auth;

import java.util.Date;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.auth.UserContactEntity;
import gov.healthit.chpl.util.AuthUtil;

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
        + " last_updated_date = NOW(), "
        + " last_updated_user = "
        + AuthUtil.getAuditId()
        + "WHERE contact_id = :contactid");
        query.setParameter("contactid", contactId);
        query.executeUpdate();
    }

    public void delete(UserContactEntity contact) {
        contact.setLastModifiedDate(new Date());
        contact.setLastModifiedUser(AuthUtil.getAuditId());
        contact.setDeleted(true);
        super.update(contact);
    }
}
