package gov.healthit.chpl.validation.surveillance.reviewer;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.concept.RequirementTypeEnum;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class SurveillanceRequirementReviewer extends Reviewer {
    private static final Logger LOGGER = LogManager.getLogger(SurveillanceRequirementReviewer.class);

    private CertificationResultDetailsDAO certResultDetailsDao;
    private CertificationCriterionDAO criterionDao;
    private SurveillanceDAO survDao;

    @Autowired
    public SurveillanceRequirementReviewer(SurveillanceDAO survDao, CertificationResultDetailsDAO certResultDetailsDao,
            CertificationCriterionDAO criterionDao, ErrorMessageUtil msgUtil) {
        super(msgUtil);
        this.survDao = survDao;
        this.certResultDetailsDao = certResultDetailsDao;
        this.criterionDao = criterionDao;
    }

    public void review(Surveillance surv) {
        List<CertificationResultDetailsDTO> certResults = null;
        if (surv.getCertifiedProduct() != null && surv.getCertifiedProduct().getId() != null) {
            try {
                certResults = certResultDetailsDao
                        .getCertificationResultDetailsByCertifiedProductId(surv.getCertifiedProduct().getId());
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Could not find cert results for certified product " + surv.getCertifiedProduct().getId(),
                        ex);
            }
        }

        for (SurveillanceRequirement req : surv.getRequirements()) {
            if (StringUtils.isEmpty(req.getRequirement())) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.requirementIsRequired"));
            }

            if (req.getType() == null) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.typeMissingForRequirement", req.getRequirement()));
            } else if (req.getType().getId() == null || req.getType().getId().longValue() <= 0) {
                SurveillanceRequirementType reqType = survDao
                        .findSurveillanceRequirementType(req.getType().getName());
                if (reqType == null) {
                    surv.getErrorMessages().add(msgUtil.getMessage("surveillance.typeNameMissingForRequirement",
                            req.getType().getName(), req.getRequirement()));
                } else {
                    req.setType(reqType);
                }
            } else {
                SurveillanceRequirementType reqType = survDao
                        .findSurveillanceRequirementType(req.getType().getId());
                if (reqType == null) {
                    surv.getErrorMessages().add(msgUtil.getMessage("surveillance.typeIdMissingForRequirement",
                            req.getType().getId(), req.getRequirement()));
                } else {
                    req.setType(reqType);
                }
            }

            // the surveillance requirement validation is different
            // depending on the requirement type
            if (req.getType() != null && !StringUtils.isEmpty(req.getType().getName())) {
                if (req.getType().getName().equalsIgnoreCase(SurveillanceReviewerUtils.CRITERION_REQUIREMENT_TYPE)
                        && surv.getCertifiedProduct() != null && surv.getCertifiedProduct().getId() != null) {

                    req.setRequirement(
                            gov.healthit.chpl.util.Util.coerceToCriterionNumberFormat(req.getRequirement()));
                    CertificationCriterionDTO criterion = null;
                    // see if the nonconformity type is a criterion that the
                    // product has attested to
                    if (certResults != null && certResults.size() > 0) {
                        for (CertificationResultDetailsDTO certResult : certResults) {
                            if (!StringUtils.isEmpty(certResult.getNumber()) && certResult.getSuccess() != null
                                    && certResult.getSuccess()
                                    && certResult.getNumber().equals(req.getRequirement())) {
                                criterion = criterionDao.getByName(req.getRequirement());
                            }
                        }
                    }
                    if (criterion == null) {
                        surv.getErrorMessages().add(
                                msgUtil.getMessage("surveillance.requirementInvalidForRequirementType",
                                        req.getRequirement(), req.getType().getName()));
                    }
                } else if (req.getType().getName().equals(SurveillanceReviewerUtils.TRANSPARENCY_REQUIREMENT_TYPE)) {
                    // requirement has to be one of 170.523 (k)(1) or (k)(2)
                    req.setRequirement(
                            gov.healthit.chpl.util.Util.coerceToCriterionNumberFormat(req.getRequirement()));
                    if (!RequirementTypeEnum.K1.getName().equals(req.getRequirement())
                            && !RequirementTypeEnum.K2.getName().equals(req.getRequirement())) {
                        surv.getErrorMessages()
                                .add(msgUtil.getMessage("surveillance.requirementInvalidForTransparencyType",
                                        req.getRequirement(), req.getType().getName(),
                                        RequirementTypeEnum.K1.getName(), RequirementTypeEnum.K2.getName()));
                    }
                }
            } else {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.requirementMustHaveValue", req.getRequirement()));
            }

            if (surv.getEndDate() != null) {
                if (req.getResult() == null) {
                    surv.getErrorMessages()
                            .add(msgUtil.getMessage("surveillance.resultNotFound", req.getRequirement()));
                }
            }

            if (req.getResult() != null
                    && (req.getResult().getId() == null || req.getResult().getId().longValue() <= 0)) {
                SurveillanceResultType resType = survDao.findSurveillanceResultType(req.getResult().getName());
                if (resType == null) {
                    surv.getErrorMessages().add(msgUtil.getMessage("surveillance.resultWithNameNotFound",
                            req.getResult().getName(), req.getRequirement()));
                } else {
                    req.setResult(resType);
                }
            } else if (req.getResult() != null) {
                SurveillanceResultType resType = survDao.findSurveillanceResultType(req.getResult().getId());
                if (resType == null) {
                    surv.getErrorMessages().add(msgUtil.getMessage("surveillance.resultWithIdNotFound",
                            req.getResult().getId(), req.getRequirement()));
                } else {
                    req.setResult(resType);
                }
            }

            addSurveillanceWarningIfNotValid(surv, req.getRequirement(),
                    "Requirement '" + req.getRequirement() + "'");
        }
    }
}
