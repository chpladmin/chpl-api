package gov.healthit.chpl.permissions.domains.surveillance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceRequirementEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("surveillanceAddDocumentActionPermissions")
public class AddDocumentActionPermissions extends ActionPermissions {

    private SurveillanceDAO survDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public AddDocumentActionPermissions(SurveillanceDAO survDao, ErrorMessageUtil msgUtil) {
        this.survDao = survDao;
        this.msgUtil = msgUtil;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Long)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Long nonconformityId = (Long) obj;
            try {
                SurveillanceEntity surv = survDao.getSurveillanceByNonconformityId(nonconformityId);
                if (isAcbValidForCurrentUser(surv.getCertifiedProduct().getCertificationBodyId())) {
                    SurveillanceNonconformityEntity nonconformity = findNonconformityWithId(surv, nonconformityId);
                    if (isNonconformityForRemovedCriteria(nonconformity)) {
                        //done instead of returning false to get a more customized message than
                        //Access is denied.
                        throw new AccessDeniedException(msgUtil.getMessage(
                                "surveillance.nonconformityDocNotAddedForRemovedCriteria", nonconformity.getType()));
                    } else if (isNonconformityForRemovedRequirement(nonconformity)) {
                        //done instead of returning false to get a more customized message than
                        //Access is denied.
                        throw new AccessDeniedException(msgUtil.getMessage(
                                "surveillance.nonconformityDocNotAddedForRemovedRequirement", nonconformity.getType()));
                    } else if (isListing2014Edition(surv)) {
                        //done instead of returning false to get a more customized message than
                        //Access is denied.
                        throw new AccessDeniedException(msgUtil.getMessage(
                                "surveillance.nonconformityDocNotAddedFor2014Edition", nonconformity.getType()));
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }
            } catch (EntityRetrievalException ex) {
                return false;
            }
        } else {
            return false;
        }
    }

    private SurveillanceNonconformityEntity findNonconformityWithId(SurveillanceEntity surv, Long nonconformityId) {
        SurveillanceNonconformityEntity nonconformity = null;
        for (SurveillanceRequirementEntity req : surv.getSurveilledRequirements()) {
            for (SurveillanceNonconformityEntity nc : req.getNonconformities()) {
                if (nc.getId() != null && nc.getId().equals(nonconformityId)) {
                    nonconformity = nc;
                }
            }
        }
        return nonconformity;
    }

    private boolean isNonconformityForRemovedCriteria(SurveillanceNonconformityEntity nonconformity) {
        return nonconformity != null
                && nonconformity.getCertificationCriterionEntity() != null
                && nonconformity.getCertificationCriterionEntity().getRemoved() != null
                && nonconformity.getCertificationCriterionEntity().getRemoved().booleanValue();
    }

    private boolean isNonconformityForRemovedRequirement(SurveillanceNonconformityEntity nonconformity) {
        return nonconformity != null
                && nonconformity.getType().equalsIgnoreCase(NonconformityType.K2.getName());
    }

    private boolean isListing2014Edition(SurveillanceEntity surv) {
        return surv.getCertifiedProduct() != null
                && surv.getCertifiedProduct().getCertificationEditionId().equals(
                CertificationEditionConcept.CERTIFICATION_EDITION_2014.getId());
    }
}
