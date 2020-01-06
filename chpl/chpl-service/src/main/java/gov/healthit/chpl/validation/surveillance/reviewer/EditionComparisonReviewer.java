package gov.healthit.chpl.validation.surveillance.reviewer;

import java.util.Optional;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("survEditionComparisonReviewer")
public class EditionComparisonReviewer implements ComparisonReviewer {
    private CertifiedProductDAO listingDao;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;
    private FF4j ff4j;

    @Autowired
    public EditionComparisonReviewer(CertifiedProductDAO listingDao, ErrorMessageUtil msgUtil,
            ResourcePermissions resourcePermissions, FF4j ff4j) {
        this.listingDao = listingDao;
        this.msgUtil = msgUtil;
        this.resourcePermissions = resourcePermissions;
        this.ff4j = ff4j;
    }

    /**
     * Surveillance and nonconformities for 2014 edition listings cannot
     * be added/edited/removed by ACBs.
     */
    @Override
    public void review(Surveillance existingSurveillance, Surveillance updatedSurveillance) {
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        } else if (!ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)) {
            return;
        }

        String edition = updatedSurveillance.getCertifiedProduct().getEdition();
        if (StringUtils.isEmpty(edition)) {
            edition = determineEdition(updatedSurveillance);
            if (StringUtils.isEmpty(edition)) {
                updatedSurveillance.getErrorMessages().add(msgUtil.getMessage("surveillance.noEditNoEdition"));
            }
        } else if (edition.equals(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear())) {
            //look for any changes to the surveillance data
            if (!existingSurveillance.propertiesMatch(updatedSurveillance)) {
                updatedSurveillance.getErrorMessages().add(msgUtil.getMessage("surveillance.noEdit2014"));
            }

            //look for any changes to the surveilled requirements or the nonconformities
            for (SurveillanceRequirement updatedReq : updatedSurveillance.getRequirements()) {
                Optional<SurveillanceRequirement> existingReq
                    = existingSurveillance.getRequirements().stream()
                        .filter(existingSurvReq ->
                            doRequirementsMatch(updatedReq, existingSurvReq))
                        .findFirst();

                if (!existingReq.isPresent()) {
                    //this requirement was added to the surveillance - not allowed
                    updatedSurveillance.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.requirementNotAddedFor2014Edition",
                                    updatedReq.getRequirement()));
                } else if (existingReq.isPresent() && !updatedReq.propertiesMatch(existingReq.get())) {
                        //an existing requirement that was edited = not allowed
                        updatedSurveillance.getErrorMessages().add(
                                msgUtil.getMessage("surveillance.requirementNotEditedFor2014Edition",
                                        updatedReq.getRequirement()));
                } else if (existingReq.isPresent() && updatedReq.propertiesMatch(existingReq.get())) {
                    //look at the nonconformities for this existing requirement - were any added or edited?
                    for (SurveillanceNonconformity updatedNc : updatedReq.getNonconformities()) {
                        Optional<SurveillanceNonconformity> existingNc
                        = existingReq.get().getNonconformities().stream()
                            .filter(existingSurvNc ->
                                doNonconformitiesMatch(updatedNc, existingSurvNc))
                            .findFirst();
                        if (!existingNc.isPresent()) {
                            //added this nonconformity - not allowed
                            updatedSurveillance.getErrorMessages().add(
                                    msgUtil.getMessage("surveillance.nonconformityNotAddedFor2014Edition",
                                            updatedNc.getNonconformityType(), updatedReq.getRequirement()));
                        } else if (existingNc.isPresent() && !updatedNc.propertiesMatch(existingNc.get())) {
                            updatedSurveillance.getErrorMessages().add(
                                    //edited a nonconformity - not allowed
                                    msgUtil.getMessage("surveillance.nonconformityNotEditedFor2014Edition",
                                            updatedNc.getNonconformityType(), updatedReq.getRequirement()));
                        }
                    }

                    //look for existing nonconformities that are no longer present in the updated requirement
                    for (SurveillanceNonconformity existingNc : existingReq.get().getNonconformities()) {
                        Optional<SurveillanceNonconformity> updatedNc
                        = updatedReq.getNonconformities().stream()
                            .filter(updatedSurvNc ->
                                doNonconformitiesMatch(existingNc, updatedSurvNc))
                            .findFirst();
                        if (!updatedNc.isPresent()) {
                            updatedSurveillance.getErrorMessages().add(
                                //edited a nonconformity - not allowed
                                msgUtil.getMessage("surveillance.nonconformityNotEditedFor2014Edition",
                                        existingNc.getNonconformityType(), updatedReq.getRequirement()));
                        }
                    }
                }
            }

            //look for any existing reqs that could have been removed in the updated surv
            for (SurveillanceRequirement existingReq : existingSurveillance.getRequirements()) {
                Optional<SurveillanceRequirement> updatedReq
                    = updatedSurveillance.getRequirements().stream()
                        .filter(updatedSurvReq ->
                            doRequirementsMatch(existingReq, updatedSurvReq))
                        .findFirst();
                if (!updatedReq.isPresent()) {
                    updatedSurveillance.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.requirementNotEditedFor2014Edition",
                                    existingReq.getRequirement()));
                }
            }
        }
    }

    private String determineEdition(Surveillance surv) {
        String edition = null;
        if (surv.getCertifiedProduct() != null && surv.getCertifiedProduct().getId() != null) {
            try {
                CertifiedProductDetailsDTO listing = listingDao.getDetailsById(surv.getCertifiedProduct().getId());
                if (listing != null) {
                    edition = listing.getYear();
                }
            } catch (EntityRetrievalException ignore) { }
        }
        return edition;
    }

    /**
     * Determine if two surveillance requirements have the same ID.
     */
    private boolean doRequirementsMatch(SurveillanceRequirement updatedReq, SurveillanceRequirement existingReq) {
        return updatedReq.getId() != null && existingReq.getId() != null
                && updatedReq.getId().equals(existingReq.getId());
    }

    /**
     * Determine if two nonconformities have the same ID.
     */
    private boolean doNonconformitiesMatch(SurveillanceNonconformity updatedNonconformity,
            SurveillanceNonconformity existingNonconformity) {
        return updatedNonconformity.getId() != null && existingNonconformity.getId() != null
                && updatedNonconformity.getId().equals(existingNonconformity.getId());
    }
}
