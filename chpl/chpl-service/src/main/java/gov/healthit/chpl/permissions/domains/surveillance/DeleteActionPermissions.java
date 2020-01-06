package gov.healthit.chpl.permissions.domains.surveillance;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("surveillanceDeleteActionPermissions")
public class DeleteActionPermissions extends ActionPermissions {
    private SurveillanceDAO survDao;
    private ErrorMessageUtil msgUtil;
    private FF4j ff4j;

    @Autowired
    public DeleteActionPermissions(SurveillanceDAO survDao, ErrorMessageUtil msgUtil, FF4j ff4j) {
        this.survDao = survDao;
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Surveillance)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Surveillance surv = (Surveillance) obj;
            SurveillanceEntity survEntity = null;
            try {
                survEntity = survDao.getSurveillanceById(surv.getId());
            } catch (EntityRetrievalException ex) {
                return false;
            }

            if (isListing2014Edition(survEntity)) {
              //done instead of returning false to get a more customized message than Access is denied.
                throw new AccessDeniedException(msgUtil.getMessage(
                        "surveillance.noDelete2014", surv.getFriendlyId()));
            }
            return isAcbValidForCurrentUser(survEntity.getCertifiedProduct().getCertificationBodyId());
        } else {
            return false;
        }
    }

    private boolean isListing2014Edition(SurveillanceEntity surv) {
        if (!ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)) {
            return false;
        }

        return surv.getCertifiedProduct() != null
                && surv.getCertifiedProduct().getCertificationEditionId().equals(
                CertificationEditionConcept.CERTIFICATION_EDITION_2014.getId());
    }
}
