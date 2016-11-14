package gov.healthit.chpl.validation.surveillance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceType;

@Component("surveillanceValidator")
public class SurveillanceValidator {
	@Autowired SurveillanceDAO survDao;
	
	public void validate(Surveillance surv) {
		//make sure chpl id is valid
		if(surv.getCertifiedProduct() == null) {
			surv.getErrorMessages().add("Certified product associated with the surveillance was null. Please check the CHPL Product ID.");
		} else if(surv.getCertifiedProduct().getId() == null || surv.getCertifiedProduct().getId().longValue() <= 0) {
			surv.getErrorMessages().add("Certified product was not found for CHPL product number '" + surv.getCertifiedProduct().getChplProductNumber() + "'.");
		}
		
		if(surv.getStartDate() == null) {
			surv.getErrorMessages().add("Start date for surveillance is required.");
		}
		
		if(surv.getType() == null) {
			surv.getErrorMessages().add("A surveillance type is required but was null.");
		} else if(surv.getType().getId() == null || surv.getType().getId().longValue() <= 0) {
			SurveillanceType survType = survDao.findSurveillanceType(surv.getType().getName());
			if(survType == null) {
				surv.getErrorMessages().add("A surveillance type was not found matching '" + surv.getType().getName() + "'.");
			}
		} else {
			SurveillanceType survType = survDao.findSurveillanceType(surv.getType().getId());
			if(survType == null) {
				surv.getErrorMessages().add("A surveillance type was not found with id '" + surv.getType().getId() + "'.");
			} 
		}
		
		//randomized surveillance requires some things
		if(surv.getType() != null && surv.getType().getName() != null && 
			surv.getType().getName().equalsIgnoreCase("Randomized")) {
			if(surv.getRandomizedSitesUsed() == null || surv.getRandomizedSitesUsed().intValue() <= 0) {
				surv.getErrorMessages().add("Randomized surveillance must provide a nonzero value for number of randomized sites used.");
			}
		}
			
		validateSurveillanceRequirements(surv);
		validateSurveillanceNonconformities(surv);
	}
	
	public void validateSurveillanceRequirements(Surveillance surv) {
		if(surv.getRequirements() == null || surv.getRequirements().size() > 0) {
			surv.getErrorMessages().add("At least one surveillance requirement is required for CHPL product " + surv.getCertifiedProduct().getChplProductNumber() + ".");
		} else {
			for(SurveillanceRequirement req : surv.getRequirements()) {
				if(StringUtils.isEmpty(req.getRequirement())) {
					surv.getErrorMessages().add("A surveillance requirement (reg text number or other value) is required.");
				}
				
				if(req.getType() == null) {
					surv.getErrorMessages().add("Type was not found for surveillance requirement " + req.getRequirement() + ".");
				} else if(req.getType().getId() == null || req.getType().getId().longValue() <= 0) {
					SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType(req.getType().getName());
					if(reqType == null) {
						surv.getErrorMessages().add("No type with name '" + req.getType().getName() + "' was found for surveillance requirement " + req.getRequirement() + ".");
					}
				} else {
					SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType(req.getType().getId());
					if(reqType == null) {
						surv.getErrorMessages().add("No type with id '" + req.getType().getId() + "' was found for surveillance requirement " + req.getRequirement() + ".");
					}
				}
				
				if(surv.getEndDate() != null) {
					if(req.getResult() == null) {
						surv.getErrorMessages().add("Result was not found for surveillance requirement " + req.getRequirement() + ".");
					} else if(req.getResult().getId() == null || req.getResult().getId().longValue() <= 0) {
						surv.getErrorMessages().add("No result type was found for surveillance requirement " + req.getRequirement() + " matching '" + req.getResult().getName() + "'.");
					}
				}
			}
		}
	}
	
	public void validateSurveillanceNonconformities(Surveillance surv) {
		if(surv.getRequirements() == null) {
			return;
		}
		for(SurveillanceRequirement req : surv.getRequirements()) {
			if(req.getResult() != null && !StringUtils.isEmpty(req.getResult().getName()) && 
					req.getResult().getName().equalsIgnoreCase("Non-Conformity")) {
				//there should be nonconformities
				if(req.getNonconformities() == null || req.getNonconformities().size() == 0) {
					surv.getErrorMessages().add("Surveillance Requirement " + req.getRequirement() + " has a result of 'Non-Conformity' but no nonconformities were found.");
				} else {
					for(SurveillanceNonconformity nc : req.getNonconformities()) {
						if(StringUtils.isEmpty(nc.getNonconformityType())) {
							surv.getErrorMessages().add("Nonconformity type (reg text number or other value) is required for surveillance requirement " + req.getRequirement());
						}
						if(nc.getStatus() == null) {
							surv.getErrorMessages().add("Nonconformity status is required for requirement " + req.getRequirement() + ", nonconformity " + nc.getNonconformityType());
						} else if(nc.getStatus().getId() == null || nc.getStatus().getId().longValue() <= 0) {
							SurveillanceNonconformityStatus ncStatus = survDao.findSurveillanceNonconformityStatusType(nc.getStatus().getName());
							if(ncStatus == null) {
								surv.getErrorMessages().add("No non-conformity status with name '" + nc.getStatus().getName() + "' was found for requirement " + req.getRequirement() + ", nonconformity '" + nc.getNonconformityType() + "'.");
							}
						} else {
							SurveillanceNonconformityStatus ncStatus = survDao.findSurveillanceNonconformityStatusType(nc.getStatus().getId());
							if(ncStatus == null) {
								surv.getErrorMessages().add("No non-conformity status with id '" + nc.getStatus().getId() + "' was found for requirement " + req.getRequirement() + ", nonconformity '" + nc.getNonconformityType() + "'.");
							}
						}
						
						if(nc.getDateOfDetermination() == null) {
							surv.getErrorMessages().add("Date of determination is required for requirement " + req.getRequirement() + ", nonconformity " + nc.getNonconformityType());
						}
						
						if(StringUtils.isEmpty(nc.getSummary())) {
							surv.getErrorMessages().add("Summary is required for requirement " + req.getRequirement() + ", nonconformity " + nc.getNonconformityType());
						}
						
						if(StringUtils.isEmpty(nc.getFindings())) {
							surv.getErrorMessages().add("Findings are required for requirement " + req.getRequirement() + ", nonconformity " + nc.getNonconformityType());
						}
						
						if(surv.getType() != null && surv.getType().getName() != null && 
								surv.getType().getName().equalsIgnoreCase("Randomized")) {
							if(nc.getSitesPassed() == null || nc.getSitesPassed().intValue() < 0) {
								surv.getErrorMessages().add("Number of sites passed is required for requirement " + req.getRequirement() + ", nonconformity " + nc.getNonconformityType());
							}
							
							if(nc.getTotalSites() == null || nc.getTotalSites().intValue() < 0) {
								surv.getErrorMessages().add("Total number of sites is required for requirement " + req.getRequirement() + ", nonconformity " + nc.getNonconformityType());
							}
						}
						
						if(nc.getStatus() != null && nc.getStatus().getName() != null && 
								nc.getStatus().getName().equalsIgnoreCase("Closed")) {
							if(StringUtils.isEmpty(nc.getResolution())) {
								surv.getErrorMessages().add("Resolution description is required for requirement " + req.getRequirement() + ", nonconformity " + nc.getNonconformityType());
							}
						}
					}
				}
			} else {
				if(req.getNonconformities() != null && req.getNonconformities().size() > 0) {
					surv.getErrorMessages().add("Surveillance Requirement " + req.getRequirement() + " lists nonconformities but its result is not 'Non-Conformity'.");
				}
			}
		}
	}
}