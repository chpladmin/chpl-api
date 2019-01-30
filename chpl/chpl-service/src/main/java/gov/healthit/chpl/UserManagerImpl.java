package gov.healthit.chpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.model.Permission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Component
public class UserManagerImpl {
    private CertificationBodyDAO certifcationBodyDAO;

    @Autowired
    public UserManagerImpl(final CertificationBodyDAO certifcationBodyDAO) {
        this.certifcationBodyDAO = certifcationBodyDAO;
    }

    public ChplUser getCurrentUser() throws EntityRetrievalException {
        ChplUser chplUser = new ChplUser(Util.getCurrentUser());
        chplUser.getCertificationBodies().add(certifcationBodyDAO.getById(4l));
        return chplUser;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY, "
            + "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).ADD_PERMISSION, #acb)")
    public void addPermission(final CertificationBodyDTO acb, final Long userId, final Permission permission)
            throws UserRetrievalException {
        // MutableAcl acl;
        // ObjectIdentity oid = new
        // ObjectIdentityImpl(CertificationBodyDTO.class, acb.getId());
        //
        // try {
        // acl = (MutableAcl) mutableAclService.readAclById(oid);
        // } catch (final NotFoundException nfe) {
        // acl = mutableAclService.createAcl(oid);
        // }
        //
        // UserDTO user = userDAO.getById(userId);
        // if (user == null || user.getSubjectName() == null) {
        // throw new UserRetrievalException("Could not find user with id " +
        // userId);
        // }
        //
        // Sid recipient = new PrincipalSid(user.getSubjectName());
        // if (permissionExists(acl, recipient, permission)) {
        // LOGGER.debug("User " + recipient + " already has permission on the
        // ACB " + acb.getName());
        // } else {
        // acl.insertAce(acl.getEntries().size(), permission, recipient, true);
        // mutableAclService.updateAcl(acl);
        // LOGGER.debug("Added permission " + permission + " for Sid " +
        // recipient + " acb " + acb);
        // }
    }

}
