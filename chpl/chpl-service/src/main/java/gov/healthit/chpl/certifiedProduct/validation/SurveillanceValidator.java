package gov.healthit.chpl.certifiedProduct.validation;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceRequirement;

public class SurveillanceValidator {
	public void validate(Surveillance surv) {
		//make sure chpl id is valid
		if(surv.getCertifiedProduct() == null) {
			surv.getErrors().add("Certified product associated with the surveillance was null. Please check the CHPL Product ID.");
		} else if(surv.getCertifiedProduct().getId() == null || surv.getCertifiedProduct().getId().longValue() <= 0) {
			surv.getErrors().add("Certified product was not found for CHPL product number '" + surv.getCertifiedProduct().getChplProductNumber() + "'.");
		}
		
		if(surv.getStartDate() == null) {
			surv.getErrors().add("Start date for surveillance is required.");
		}
		
		if(surv.getType() == null) {
			surv.getErrors().add("A surveillance type is required but was null.");
		} else if(surv.getType().getId() == null || surv.getType().getId().longValue() <= 0) {
			surv.getErrors().add("A surveillance type was not found matching '" + surv.getType().getName() + "'.");
		}
		
		//randomized surveillance requires some things
		if(surv.getType() != null && surv.getType().getName() != null && 
			surv.getType().getName().equalsIgnoreCase("Randomized")) {
			if(surv.getRandomizedSitesUsed() == null || surv.getRandomizedSitesUsed().intValue() <= 0) {
				surv.getErrors().add("Randomized surveillance must provide a nonzero value for number of randomized sites used.");
			}
		}
			
		validateSurveillanceRequirements(surv);
		validateSurveillanceNonconformities(surv);
	}
	
	public void validateSurveillanceRequirements(Surveillance surv) {
		if(surv.getRequirements() == null || surv.getRequirements().size() > 0) {
			surv.getErrors().add("At least one surveillance requirement is required for CHPL product " + surv.getCertifiedProduct().getChplProductNumber() + ".");
		} else {
			for(SurveillanceRequirement req : surv.getRequirements()) {
				if(StringUtils.isEmpty(req.getRequirement())) {
					surv.getErrors().add("A surveillance requirement (reg text number or other value) is required.");
				}
				
				if(req.getType() == null) {
					surv.getErrors().add("Type was not found for surveillance requirement " + req.getRequirement() + ".");
				} else if(req.getType().getId() == null || req.getType().getId().longValue() <= 0) {
					surv.getErrors().add("No type was found for surveillance requirement " + req.getRequirement() + " matching '" + req.getType().getName() + "'.");
				}
				
				if(surv.getEndDate() != null) {
					if(req.getResult() == null) {
						surv.getErrors().add("Result was not found for surveillance requirement " + req.getRequirement() + ".");
					} else if(req.getResult().getId() == null || req.getResult().getId().longValue() <= 0) {
						surv.getErrors().add("No result type was found for surveillance requirement " + req.getRequirement() + " matching '" + req.getResult().getName() + "'.");
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
					surv.getErrors().add("Surveillance Requirement " + req.getRequirement() + " has a result of 'Non-Conformity' but no nonconformities were found.");
				} else {
					for(SurveillanceNonconformity nc : req.getNonconformities()) {
						if(StringUtils.isEmpty(nc.getNonconformityType())) {
							surv.getErrors().add("Nonconformity type (reg text number or other value) is required for surveillance requirement " + req.getRequirement());
						}
						if(nc.getStatus() == null) {
							surv.getErrors().add("Nonconformity status is required for requirement " + req.getRequirement() + ", nonconformity " + nc.getNonconformityType());
						} else if(nc.getStatus().getId() == null || nc.getStatus().getId().longValue() <= 0) {
							surv.getErrors().add("No nonconformity status was found for requirement " + req.getRequirement() + ", nonconformity " + nc.getNonconformityType() + " matching '" + nc.getStatus().getName() + "'.");
						}
						
						if(nc.getDateOfDetermination() == null) {
							surv.getErrors().add("Date of determination is required for requirement " + req.getRequirement() + ", nonconformity " + nc.getNonconformityType());
						}
						
						if(StringUtils.isEmpty(nc.getSummary())) {
							surv.getErrors().add("Summary is required for requirement " + req.getRequirement() + ", nonconformity " + nc.getNonconformityType());
						}
						
						if(StringUtils.isEmpty(nc.getFindings())) {
							surv.getErrors().add("Findings are required for requirement " + req.getRequirement() + ", nonconformity " + nc.getNonconformityType());
						}
						
						if(surv.getType() != null && surv.getType().getName() != null && 
								surv.getType().getName().equalsIgnoreCase("Randomized")) {
							if(nc.getSitesPassed() == null || nc.getSitesPassed().intValue() < 0) {
								surv.getErrors().add("Number of sites passed is required for requirement " + req.getRequirement() + ", nonconformity " + nc.getNonconformityType());
							}
							
							if(nc.getTotalSites() == null || nc.getTotalSites().intValue() < 0) {
								surv.getErrors().add("Total number of sites is required for requirement " + req.getRequirement() + ", nonconformity " + nc.getNonconformityType());
							}
						}
						
						if(nc.getStatus() != null && nc.getStatus().getName() != null && 
								nc.getStatus().getName().equalsIgnoreCase("Closed")) {
							if(StringUtils.isEmpty(nc.getResolution())) {
								surv.getErrors().add("Resolution description is required for requirement " + req.getRequirement() + ", nonconformity " + nc.getNonconformityType());
							}
						}
					}
				}
			} else {
				if(req.getNonconformities() != null && req.getNonconformities().size() > 0) {
					surv.getErrors().add("Surveillance Requirement " + req.getRequirement() + " lists nonconformities but its result is not 'Non-Conformity'.");
				}
			}
		}
	}
}