package gov.healthit.chpl.permissions.domains.surveillance;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityDocumentationEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceRequirementEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.NullSafeEvaluator;

@Deprecated
@Component("surveillanceDeleteDocumentActionPermissions")
public class DeleteDocumentActionPermissions extends ActionPermissions {

    private SurveillanceDAO survDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public DeleteDocumentActionPermissions(SurveillanceDAO survDao, ErrorMessageUtil msgUtil) {
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
            Long documentId = (Long) obj;
            try {
                SurveillanceEntity surv = survDao.getSurveillanceByDocumentId(documentId);
                if (isAcbValidForCurrentUser(surv.getCertifiedProduct().getCertificationBodyId())) {
                    SurveillanceNonconformityEntity nonconformity = findNonconformityWithDocumentId(surv, documentId);
                    if (isNonconformityForRemovedCriteria(nonconformity)) {
                        //done instead of returning false to get a more customized message than
                        //Access is denied.
                        throw new AccessDeniedException(msgUtil.getMessage(
                                "surveillance.nonconformityDocNotDeletedForRemovedCriteria",
                                nonconformity.getType().getNumber()));
                    } else if (isNonconformityForRemovedRequirement(nonconformity)) {
                        //done instead of returning false to get a more customized message than
                        //Access is denied.
                        throw new AccessDeniedException(msgUtil.getMessage(
                                "surveillance.nonconformityDocNotDeletedForRemovedRequirement",
                                nonconformity.getType().getNumber()));
                    } else if (isListing2014Edition(surv)) {
                        //done instead of returning false to get a more customized message than
                        //Access is denied.
                        throw new AccessDeniedException(msgUtil.getMessage(
                                "surveillance.nonconformityDocNotDeletedFor2014Edition",
                                nonconformity.getType().getNumber()));
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


    private SurveillanceNonconformityEntity findNonconformityWithDocumentId(SurveillanceEntity surv, Long documentId) {
        SurveillanceNonconformityEntity nonconformity = null;
        for (SurveillanceRequirementEntity req : surv.getSurveilledRequirements()) {
            for (SurveillanceNonconformityEntity nc : req.getNonconformities()) {
                for (SurveillanceNonconformityDocumentationEntity doc : nc.getDocuments()) {
                    if (doc.getId() != null && doc.getId().equals(documentId)) {
                        nonconformity = nc;
                    }
                }
            }
        }
        return nonconformity;
    }

    private boolean isNonconformityForRemovedCriteria(SurveillanceNonconformityEntity nonconformity) {
        //return nonconformity != null
        //        && nonconformity.getCertificationCriterionEntity() != null
        //        && nonconformity.getCertificationCriterionEntity().getRemoved() != null
        //        && nonconformity.getCertificationCriterionEntity().getRemoved().booleanValue();
        return NullSafeEvaluator.eval(() -> nonconformity.getType().getRemoved().booleanValue(), false);
    }

    private boolean isNonconformityForRemovedRequirement(SurveillanceNonconformityEntity nonconformity) {
        //return nonconformity != null
        //        && nonconformity.getType().equalsIgnoreCase(NonconformityType.K2.getName());
        return Objects.equals(
                NullSafeEvaluator.eval(() -> nonconformity.getType().getTitle(), null),
                "170.523 (k)(2)");
    }

    private boolean isListing2014Edition(SurveillanceEntity surv) {
        return surv.getCertifiedProduct() != null
                && surv.getCertifiedProduct().getCertificationEditionId().equals(
                CertificationEditionConcept.CERTIFICATION_EDITION_2014.getId());
    }
}
