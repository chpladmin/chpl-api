package gov.healthit.chpl.permissions.domains.surveillance;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityDocumentationEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceRequirementEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("surveillanceDeleteDocumentActionPermissions")
public class DeleteDocumentActionPermissions extends ActionPermissions {

    private SurveillanceDAO survDao;
    private FF4j ff4j;

    @Autowired
    public DeleteDocumentActionPermissions(SurveillanceDAO survDao, FF4j ff4j) {
        this.survDao = survDao;
        this.ff4j = ff4j;
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
                SurveillanceEntity surv = survDao.getSurveillanceByNonconformityId(documentId);
                return isAcbValidForCurrentUser(surv.getCertifiedProduct().getCertificationBodyId())
                        && !isNonconformityForRemovedCriteria(surv, documentId);
            } catch (EntityRetrievalException ex) {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isNonconformityForRemovedCriteria(SurveillanceEntity surv, Long documentId) {
        if (!ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)) {
            return false;
        }

        boolean result = false;
        for (SurveillanceRequirementEntity req : surv.getSurveilledRequirements()) {
            for (SurveillanceNonconformityEntity nc : req.getNonconformities()) {
                boolean isNonconformityForDocument = false;
                for (SurveillanceNonconformityDocumentationEntity doc : nc.getDocuments()) {
                    if (doc.getId() != null && doc.getId().equals(documentId)) {
                        isNonconformityForDocument = true;
                    }
                }
                if (isNonconformityForDocument
                        && nc.getCertificationCriterionEntity() != null
                        && nc.getCertificationCriterionEntity().getRemoved() != null
                        && nc.getCertificationCriterionEntity().getRemoved().booleanValue()) {
                    result = true;
                }
            }
        }
        return result;
    }
}
