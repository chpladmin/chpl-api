package gov.healthit.chpl.permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;
import gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions;
import gov.healthit.chpl.permissions.domains.CertificationResultsDomainPermissions;
import gov.healthit.chpl.permissions.domains.CertifiedProductDomainPermissions;
import gov.healthit.chpl.permissions.domains.CorrectiveActionPlanDomainPermissions;
import gov.healthit.chpl.permissions.domains.DomainPermissions;
import gov.healthit.chpl.permissions.domains.InvitationDomainPermissions;
import gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions;
import gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions;
import gov.healthit.chpl.permissions.domains.SurveillanceDomainPermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class Permissions {
    public static final String PENDING_SURVEILLANCE = "PENDING_SURVEILLANCE";
    public static final String CERTIFICATION_RESULTS = "CERTIFICATION_RESULTS";
    public static final String CERTIFIED_PRODUCT = "CERTIFIED_PRODUCT";
    public static final String CORRECTIVE_ACTION_PLAN = "CORRECTIVE_ACTION_PLAN";
    public static final String INVITATION = "INVITATION";
    public static final String PENDING_CERTIFIED_PRODUCT = "PENDING_CERTIFIED_PRODUCT";
    public static final String SURVEILLANCE = "SURVEILLANCE";
    public static final String CERTIFICATION_BODY = "CERTIFICATION_BODY";
    public static final String SCHEDULER = "SCHEDULER";

    private Map<String, DomainPermissions> domainPermissions = new HashMap<String, DomainPermissions>();
    private UserCertificationBodyMapDAO userCertificationBodyMapDAO;
    private ErrorMessageUtil errorMessageUtil;
    private CertificationBodyDAO acbDAO;

    @Autowired
    public Permissions(final UserCertificationBodyMapDAO userCertificationBodyMapDAO, final CertificationBodyDAO acbDAO,
            final ErrorMessageUtil errorMessageUtil,
            final PendingSurveillanceDomainPermissions pendingSurveillanceDomainPermissions,
            final CertificationResultsDomainPermissions certificationResultsDomainPermissions,
            final CertifiedProductDomainPermissions certifiedProductDomainPermissions,
            final CorrectiveActionPlanDomainPermissions correctiveActionPlanDomainPermissions,
            final InvitationDomainPermissions invitationDomainPermissions,
            final PendingCertifiedProductDomainPermissions pendingCertifiedProductDomainPermissions,
            final SurveillanceDomainPermissions surveillanceDomainPermissions,
            final CertificationBodyDomainPermissions certificationBodyDomainPermissions) {

        this.userCertificationBodyMapDAO = userCertificationBodyMapDAO;
        this.acbDAO = acbDAO;
        this.errorMessageUtil = errorMessageUtil;

        domainPermissions.put(PENDING_SURVEILLANCE, pendingSurveillanceDomainPermissions);
        domainPermissions.put(CERTIFICATION_RESULTS, certificationResultsDomainPermissions);
        domainPermissions.put(CERTIFIED_PRODUCT, certifiedProductDomainPermissions);
        domainPermissions.put(CORRECTIVE_ACTION_PLAN, correctiveActionPlanDomainPermissions);
        domainPermissions.put(INVITATION, invitationDomainPermissions);
        domainPermissions.put(PENDING_CERTIFIED_PRODUCT, pendingCertifiedProductDomainPermissions);
        domainPermissions.put(SURVEILLANCE, surveillanceDomainPermissions);
        domainPermissions.put(CERTIFICATION_BODY, certificationBodyDomainPermissions);
    }

    public boolean hasAccess(final String domain, final String action) {
        if (domainPermissions.containsKey(domain)) {
            return domainPermissions.get(domain).hasAccess(action);
        } else {
            return false;
        }
    }

    public boolean hasAccess(final String domain, final String action, final Object obj) {
        if (domainPermissions.containsKey(domain)) {
            return domainPermissions.get(domain).hasAccess(action, obj);
        } else {
            return false;
        }
    }

    @Transactional
    public List<UserDTO> getAllUsersOnAcb(final CertificationBodyDTO acb) {
        List<UserDTO> userDtos = new ArrayList<UserDTO>();
        List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByAcbId(acb.getId());

        for (UserCertificationBodyMapDTO dto : dtos) {
            userDtos.add(dto.getUser());
        }

        return userDtos;
    }

    @Transactional(readOnly = true)
    public List<CertificationBodyDTO> getAllAcbsForCurrentUser() {
        User user = Util.getCurrentUser();
        List<CertificationBodyDTO> acbs = new ArrayList<CertificationBodyDTO>();

        if (Util.isUserRoleAdmin() || Util.isUserRoleOnc()) {
            acbs = acbDAO.findAllActive();
        } else {
            List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByUserId(user.getId());
            for (UserCertificationBodyMapDTO dto : dtos) {
                acbs.add(dto.getCertificationBody());
            }
        }
        return acbs;
    }

    @Transactional(readOnly = true)
    public CertificationBodyDTO getIfPermissionById(final Long id) {
        List<CertificationBodyDTO> dtos = getAllAcbsForCurrentUser();
        dtos.stream().filter(dto -> dto.getId().equals(id));
        if (dtos.size() == 0) {
            throw new AccessDeniedException(errorMessageUtil.getMessage("access.denied"));
        }
        return dtos.get(0);
    }

}
