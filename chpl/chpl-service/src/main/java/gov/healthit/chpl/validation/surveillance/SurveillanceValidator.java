package gov.healthit.chpl.validation.surveillance;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.auth.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.domain.Authority;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.RequirementTypeEnum;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.SurveillanceEntity;

@Component("surveillanceValidator")
public class SurveillanceValidator implements MessageSourceAware {
	private static final Logger logger = LogManager.getLogger(SurveillanceValidator.class);

	private static final String CRITERION_REQUIREMENT_TYPE = "Certified Capability";
	private static final String TRANSPARENCY_REQUIREMENT_TYPE = "Transparency or Disclosure Requirement";
	private static final String HAS_NON_CONFORMITY = "Non-Conformity";

	@Autowired SurveillanceDAO survDao;
	@Autowired CertifiedProductDAO cpDao;
	@Autowired CertificationResultDetailsDAO certResultDetailsDao;;
	@Autowired CertificationCriterionDAO criterionDao;
	@Autowired UserPermissionDAO userPermissionDao;
	@Autowired MessageSource messageSource;

	public void validate(Surveillance surv) {
		CertifiedProductDetailsDTO cpDetails = null;
		
		//make sure chpl id is valid
		if(surv.getCertifiedProduct() == null) {
			surv.getErrorMessages().add(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.nullCertifiedProduct"), LocaleContextHolder.getLocale()));
		} else if(surv.getCertifiedProduct().getId() == null && surv.getCertifiedProduct().getChplProductNumber() == null) {
			surv.getErrorMessages().add(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.nullCertifiedProductAndChplNumber"), LocaleContextHolder.getLocale()));
		} else if(surv.getCertifiedProduct().getId() == null || surv.getCertifiedProduct().getId().longValue() <= 0) {
			//the id is null, try to lookup by unique chpl number
			String chplId = surv.getCertifiedProduct().getChplProductNumber();
			if(chplId.startsWith("CHP-")) {
				try {
					CertifiedProductDTO chplProduct = cpDao.getByChplNumber(chplId);
					if(chplProduct != null) {
						cpDetails = cpDao.getDetailsById(chplProduct.getId());
						if(cpDetails != null) {
							surv.setCertifiedProduct(new CertifiedProduct(cpDetails));
						} else {
							surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.certifiedProductIdNotFound"), LocaleContextHolder.getLocale()), chplId, chplProduct.getId()));
						}
					} else {
						surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.productIdNotFound"), LocaleContextHolder.getLocale()), chplId));
					}
				} catch(EntityRetrievalException ex) {
					surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.productDetailsRetrievalException"), LocaleContextHolder.getLocale()), chplId));
				}
			} else {
				try {
					cpDetails = cpDao.getByChplUniqueId(chplId);
					if(cpDetails != null) {
						surv.setCertifiedProduct(new CertifiedProduct(cpDetails));
					} else {
						surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.productUniqueIdNotFound"), LocaleContextHolder.getLocale()), chplId));
					}
				} catch(EntityRetrievalException ex){
					surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.productDetailsRetrievalException"), LocaleContextHolder.getLocale()), chplId));
				}
			}
		} else if(surv.getCertifiedProduct().getId() != null) {
			try {
				cpDetails = cpDao.getDetailsById(surv.getCertifiedProduct().getId());
			} catch(EntityRetrievalException ex) {
				surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.detailsNotFoundForCertifiedProduct"), LocaleContextHolder.getLocale()), surv.getCertifiedProduct().getId()));
			}
			surv.setCertifiedProduct(new CertifiedProduct(cpDetails));
		}

		if(!StringUtils.isEmpty(surv.getSurveillanceIdToReplace()) && surv.getCertifiedProduct() != null) {
			SurveillanceEntity existing = survDao.getSurveillanceByCertifiedProductAndFriendlyId(
					surv.getCertifiedProduct().getId(),
					surv.getSurveillanceIdToReplace());
			if(existing == null) {
				surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.surveillanceIdNotFound"), LocaleContextHolder.getLocale()), surv.getSurveillanceIdToReplace()));
			}
		}

		if(surv.getStartDate() == null) {
			surv.getErrorMessages().add(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.startDateRequired"), LocaleContextHolder.getLocale()));
		}

		if(surv.getType() == null) {
			surv.getErrorMessages().add(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.typeRequired"), LocaleContextHolder.getLocale()));
		} else if(surv.getType().getId() == null || surv.getType().getId().longValue() <= 0) {
			SurveillanceType survType = survDao.findSurveillanceType(surv.getType().getName());
			if(survType == null) {
				surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.typeMismatch"), LocaleContextHolder.getLocale()), surv.getType().getName()));
			} else {
				surv.setType(survType);
			}
		} else {
			SurveillanceType survType = survDao.findSurveillanceType(surv.getType().getId());
			if(survType == null) {
				surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.typeNotFound"), LocaleContextHolder.getLocale()), surv.getType().getId()));
			} else {
				surv.setType(survType);
			}
		}

		//randomized surveillance requires number of sites used but
		//any other type of surveillance should not have that value
		if(surv.getType() != null && surv.getType().getName() != null &&
			surv.getType().getName().equalsIgnoreCase("Randomized")) {
			if(surv.getRandomizedSitesUsed() == null || surv.getRandomizedSitesUsed().intValue() < 0) {
				surv.getErrorMessages().add(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.randomizedNonzeroValue"), LocaleContextHolder.getLocale()));
			}
		} else if(surv.getType() != null && surv.getType().getName() != null &&
				!surv.getType().getName().equalsIgnoreCase("Randomized")) {
			if(surv.getRandomizedSitesUsed() != null && surv.getRandomizedSitesUsed().intValue() >= 0) {
				surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.randomizedSitesNotApplicable"), LocaleContextHolder.getLocale()), surv.getType().getName()));
			}
		}
				
		List<CertificationResultDetailsDTO> certResults = null;
		if(surv.getCertifiedProduct() != null && surv.getCertifiedProduct().getId() != null) {
			try {
				certResults = certResultDetailsDao.getCertificationResultDetailsByCertifiedProductId(surv.getCertifiedProduct().getId());
			} catch(EntityRetrievalException ex) {
				logger.error("Could not find cert results for certified product " + surv.getCertifiedProduct().getId(), ex);
			}
		}
		
		validateSurveillanceAuthority(surv);
		validateSurveillanceRequirements(surv, certResults);
		validateSurveillanceNonconformities(surv, certResults);
	}

	public void validateSurveillanceRequirements(Surveillance surv, List<CertificationResultDetailsDTO> certResults) {
		if(surv.getRequirements() == null || surv.getRequirements().size() == 0) {
			surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.requirementIsRequiredForProduct"), LocaleContextHolder.getLocale()), surv.getCertifiedProduct().getChplProductNumber()));
		} else {
			for(SurveillanceRequirement req : surv.getRequirements()) {
				if(StringUtils.isEmpty(req.getRequirement())) {
					surv.getErrorMessages().add(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.requirementIsRequired"), LocaleContextHolder.getLocale()));
				}

				if(req.getType() == null) {
					surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.typeMissingForRequirement"), LocaleContextHolder.getLocale()), req.getRequirement()));
				} else if(req.getType().getId() == null || req.getType().getId().longValue() <= 0) {
					SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType(req.getType().getName());
					if(reqType == null) {
						surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.typeNameMissingForRequirement"), LocaleContextHolder.getLocale()), req.getType().getName(), req.getRequirement()));
					} else {
						req.setType(reqType);
					}
				} else {
					SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType(req.getType().getId());
					if(reqType == null) {
						surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.typeIdMissingForRequirement"), LocaleContextHolder.getLocale()), req.getType().getId(), req.getRequirement()));
					} else {
						req.setType(reqType);
					}
				}

				//the surveillance requirement validation is different depending on the requirement type
				if(req.getType() != null && !StringUtils.isEmpty(req.getType().getName())) {
					if(req.getType().getName().equalsIgnoreCase(CRITERION_REQUIREMENT_TYPE) &&
							surv.getCertifiedProduct() != null && surv.getCertifiedProduct().getId() != null) {

						req.setRequirement(gov.healthit.chpl.Util.coerceToCriterionNumberFormat(req.getRequirement()));
						CertificationCriterionDTO criterion = null;
						//see if the nonconformity type is a criterion that the product has attested to
						//List<CertificationResultDetailsDTO> certResults = certResultDetailsDao.getCertificationResultDetailsByCertifiedProductId(surv.getCertifiedProduct().getId());
						if(certResults != null && certResults.size() > 0) {
							for(CertificationResultDetailsDTO certResult : certResults) {
								if(!StringUtils.isEmpty(certResult.getNumber()) &&
										certResult.getSuccess() != null && certResult.getSuccess() == Boolean.TRUE &&
										certResult.getNumber().equals(req.getRequirement())) {
									criterion = criterionDao.getByName(req.getRequirement());
								}
							}
						}
						if(criterion == null) {
							surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.requirementInvalidForRequirementType"), LocaleContextHolder.getLocale()), req.getRequirement(), req.getType().getName()));
						}
					} else if(req.getType().getName().equals(TRANSPARENCY_REQUIREMENT_TYPE)) {
						//requirement has to be one of 170.523 (k)(1) or (k)(2)
						req.setRequirement(gov.healthit.chpl.Util.coerceToCriterionNumberFormat(req.getRequirement()));
						if(!RequirementTypeEnum.K1.getName().equals(req.getRequirement()) &&
							!RequirementTypeEnum.K2.getName().equals(req.getRequirement())) {
							surv.getErrorMessages().add("The requirement '" + req.getRequirement() + "' is not valid for requirement type '" + req.getType().getName() + "'. "
									+ "Valid values are " + RequirementTypeEnum.K1.getName() +
									" or " + RequirementTypeEnum.K2.getName());
						}
					}
				} else {
					surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.requirementMustHaveValue"), LocaleContextHolder.getLocale()), req.getRequirement()));
				}

				if(surv.getEndDate() != null) {
					if(req.getResult() == null) {
						surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.resultNotFound"), LocaleContextHolder.getLocale()), req.getRequirement()));
					}
				}

				if(req.getResult() != null &&
						(req.getResult().getId() == null || req.getResult().getId().longValue() <= 0)) {
					SurveillanceResultType resType = survDao.findSurveillanceResultType(req.getResult().getName());
					if(resType == null) {
						surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.resultWithNameNotFound"), LocaleContextHolder.getLocale()), req.getResult().getName(), req.getRequirement()));
					} else {
						req.setResult(resType);
					}
				} else if(req.getResult() != null){
					SurveillanceResultType resType = survDao.findSurveillanceResultType(req.getResult().getId());
					if(resType == null) {
						surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.resultWithIdNotFound"), LocaleContextHolder.getLocale()), req.getResult().getId(), req.getRequirement()));
					} else {
						req.setResult(resType);
					}
				}
			}
		}
	}

	public void validateSurveillanceNonconformities(Surveillance surv, List<CertificationResultDetailsDTO> certResults) {
		if(surv.getRequirements() == null) {
			return;
		}
        //assume surveillance requires a close date until proven otherwise
        boolean requiresCloseDate = true;
		for(SurveillanceRequirement req : surv.getRequirements()) {
			if(req.getResult() != null && !StringUtils.isEmpty(req.getResult().getName()) &&
					req.getResult().getName().equalsIgnoreCase(HAS_NON_CONFORMITY)) {
				//there should be nonconformities
				if(req.getNonconformities() == null || req.getNonconformities().size() == 0) {
					surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.nonConformityNotFound"), LocaleContextHolder.getLocale()), req.getRequirement()));
				} else {
					for(SurveillanceNonconformity nc : req.getNonconformities()) {
						if(StringUtils.isEmpty(nc.getNonconformityType())) {
							surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.nonConformityTypeRequired"), LocaleContextHolder.getLocale()), req.getRequirement()));
						} else {
							//non-conformity type is not empty. is a certification criteria or just a string?
							CertificationCriterionDTO criterion = null;
							if(surv.getCertifiedProduct() != null && surv.getCertifiedProduct().getId() != null) {
								nc.setNonconformityType(gov.healthit.chpl.Util.coerceToCriterionNumberFormat(nc.getNonconformityType()));
								//see if the nonconformity type is a criterion that the product has attested to
								if(certResults != null && certResults.size() > 0) {
									for(CertificationResultDetailsDTO certResult : certResults) {
										if(!StringUtils.isEmpty(certResult.getNumber()) &&
												certResult.getSuccess() != null && certResult.getSuccess() == Boolean.TRUE &&
												certResult.getNumber().equals(nc.getNonconformityType())) {
											criterion = criterionDao.getByName(nc.getNonconformityType());
										}
									}
								}
							}
							//if it could have matched a criterion but didn't, it has to be one of a few other values
							if(surv.getCertifiedProduct() != null && surv.getCertifiedProduct().getId() != null
									&& criterion == null) {
								nc.setNonconformityType(gov.healthit.chpl.Util.coerceToCriterionNumberFormat(nc.getNonconformityType()));
								if(!NonconformityType.K1.getName().equals(nc.getNonconformityType())
										&& !NonconformityType.K2.getName().equals(nc.getNonconformityType())
										&& !NonconformityType.L.getName().equals(nc.getNonconformityType())
										&& !NonconformityType.OTHER.getName().equals(nc.getNonconformityType()) ) {
									surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.nonConformityTypeMatchException"), LocaleContextHolder.getLocale()),
											nc.getNonconformityType(), NonconformityType.K1.getName(), NonconformityType.K2.getName(), NonconformityType.L.getName(), NonconformityType.OTHER.getName()));
								}
							}
						}

						if(nc.getStatus() == null) {
							surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.nonConformityStatusNotFound"), LocaleContextHolder.getLocale()), req.getRequirement(), nc.getNonconformityType()));
						} else if(nc.getStatus().getId() == null || nc.getStatus().getId().longValue() <= 0) {
							SurveillanceNonconformityStatus ncStatus = survDao.findSurveillanceNonconformityStatusType(nc.getStatus().getName());
							if(ncStatus == null) {
								surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.nonConformityStatusWithNameNotFound"), LocaleContextHolder.getLocale()), nc.getStatus().getName(), req.getRequirement(), nc.getNonconformityType()));
							} else {
								nc.setStatus(ncStatus);
							}
						} else {
							SurveillanceNonconformityStatus ncStatus = survDao.findSurveillanceNonconformityStatusType(nc.getStatus().getId());
							if(ncStatus == null) {
								surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.nonConformityStatusWithIdNotFound"), LocaleContextHolder.getLocale()), nc.getStatus().getId(), req.getRequirement(), nc.getNonconformityType()));
							} else {
								nc.setStatus(ncStatus);
							}
						}
						
						if(!StringUtils.isEmpty(nc.getCapApprovalDate()) && StringUtils.isEmpty(nc.getCapMustCompleteDate())){
							surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.dateCAPMustCompleteIsRequired"), LocaleContextHolder.getLocale()), req.getRequirement(), nc.getNonconformityType()));
						}

						if(!StringUtils.isEmpty(nc.getCapEndDate()) && StringUtils.isEmpty(nc.getCapStartDate())){
							surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.dateCAPStartIsRequired"), LocaleContextHolder.getLocale()), req.getRequirement(), nc.getNonconformityType()));
						}
						
						if(!StringUtils.isEmpty(nc.getCapEndDate()) && StringUtils.isEmpty(nc.getCapApprovalDate())){
							surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.dateCAPApprovalIsRequired"), LocaleContextHolder.getLocale()), req.getRequirement(), nc.getNonconformityType()));
						}
						
						if(!StringUtils.isEmpty(nc.getCapEndDate()) && !StringUtils.isEmpty(nc.getCapStartDate()) && nc.getCapEndDate().compareTo(nc.getCapStartDate()) < 0){
							surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.dateCAPEndNotGreaterThanDateCAPStart"), LocaleContextHolder.getLocale()), req.getRequirement(), nc.getNonconformityType()));
						}

						if(nc.getDateOfDetermination() == null) {
							surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.dateOfDeterminationIsRequired"), LocaleContextHolder.getLocale()), req.getRequirement(), nc.getNonconformityType()));
						}

						if(StringUtils.isEmpty(nc.getSummary())) {
							surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.summaryIsRequired"), LocaleContextHolder.getLocale()), req.getRequirement(), nc.getNonconformityType()));
						}

						if(StringUtils.isEmpty(nc.getFindings())) {
							surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.findingsAreRequired"), LocaleContextHolder.getLocale()), req.getRequirement(), nc.getNonconformityType()));
						}

						//site counts are required for completed randomized surveillance
						//but not allowed for other types of surveillance
						if(surv.getType() != null && surv.getType().getName() != null &&
								surv.getType().getName().equalsIgnoreCase("Randomized")) {
							if(nc.getSitesPassed() == null || nc.getSitesPassed().intValue() < 0) {
								surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.numberOfSitesPassedIsRequired"), LocaleContextHolder.getLocale()), req.getRequirement(), nc.getNonconformityType()));
							}

							if(nc.getTotalSites() == null || nc.getTotalSites().intValue() < 0) {
								surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.totalNumberOfSitesIsRequired"), LocaleContextHolder.getLocale()), req.getRequirement(), nc.getNonconformityType()));
							}
						} else if(surv.getType() != null && surv.getType().getName() != null &&
								!surv.getType().getName().equalsIgnoreCase("Randomized")) {
							if(nc.getSitesPassed() != null && nc.getSitesPassed().intValue() >= 0) {
								surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.numberOfSitesPassedNotApplicable"), LocaleContextHolder.getLocale()), req.getRequirement(), nc.getNonconformityType()));
							}

							if(nc.getTotalSites() != null && nc.getTotalSites().intValue() >= 0) {
								surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.totalNumberOfSitesNotApplicable"), LocaleContextHolder.getLocale()), req.getRequirement(), nc.getNonconformityType()));
							}
						}

						if(nc.getStatus() != null && nc.getStatus().getName() != null &&
								nc.getStatus().getName().equalsIgnoreCase("Closed")) {
							if(StringUtils.isEmpty(nc.getResolution())) {
								surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.resolutionDescriptionIsRequired"), LocaleContextHolder.getLocale()), req.getRequirement(), nc.getNonconformityType()));
							}
						} else if(nc.getStatus() != null && nc.getStatus().getName() != null &&
								nc.getStatus().getName().equalsIgnoreCase("Open")) {
							if(!StringUtils.isEmpty(nc.getResolution())) {
								surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.resolutionDescriptionNotApplicable"), LocaleContextHolder.getLocale()), req.getRequirement(), nc.getNonconformityType()));
							}
                            requiresCloseDate = false;
                        }
					}
				}
			} else {
				if(req.getNonconformities() != null && req.getNonconformities().size() > 0) {
					surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.requirementNonConformityMismatch"), LocaleContextHolder.getLocale()), req.getRequirement()));
				}
			}
		}
        if(requiresCloseDate && surv.getEndDate() == null) {
        	surv.getErrorMessages().add(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.endDateRequiredNoOpenNonConformities"), LocaleContextHolder.getLocale()));
		}
	}
	
	public void validateSurveillanceAuthority(Surveillance surv) {
		// non-null surveillance must be ROLE_ADMIN, ROLE_ACB_ADMIN, or ROLE_ACB_STAFF
		if(!StringUtils.isEmpty(surv.getAuthority())){
			if(!surv.getAuthority().equalsIgnoreCase(Authority.ROLE_ADMIN) 
					&& !surv.getAuthority().equalsIgnoreCase(Authority.ROLE_ACB_ADMIN) 
					&& !surv.getAuthority().equalsIgnoreCase(Authority.ROLE_ACB_STAFF)){
				surv.getErrorMessages().add(String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("surveillance.authorityRequired"), LocaleContextHolder.getLocale()), Authority.ROLE_ADMIN, Authority.ROLE_ACB_ADMIN, Authority.ROLE_ACB_STAFF));
			}
		}
	}

	@Override
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
