package gov.healthit.chpl.dao.auth.impl;

import java.util.Date;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.entity.UserContactEntity;
import gov.healthit.chpl.dao.auth.UserContactDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.util.AuthUtil;

@Repository(value = "userContactDAO")
public class UserContactDAOImpl extends BaseDAOImpl implements UserContactDAO {

    @Override
    @Transactional
    public void create(final UserContactEntity contact) {
        super.create(contact);
    }

    @Override
    @Transactional
    public void update(final UserContactEntity contact) {
        super.update(contact);
    }

    @Override
    public void delete(final Long contactId) {
        Query query = entityManager
                .createQuery("UPDATE UserContact " + "SET deleted = true, " + " last_updated_date = NOW(), "
                        + " last_updated_user = " + AuthUtil.getAuditId() + "WHERE contact_id = :contactid");
        query.setParameter("contactid", contactId);
        query.executeUpdate();
    }

    @Override
    public void delete(final UserContactEntity contact) {
        contact.setLastModifiedDate(new Date());
        contact.setLastModifiedUser(AuthUtil.getAuditId());
        contact.setDeleted(true);
        super.update(contact);
    }
}
