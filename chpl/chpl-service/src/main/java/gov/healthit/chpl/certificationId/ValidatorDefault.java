package gov.healthit.chpl.certificationId;

public class ValidatorDefault extends Validator {

	public ValidatorDefault() {
		this.counts.put("criteriaRequired", 0);
		this.counts.put("criteriaRequiredMet", 0);
		this.counts.put("cqmsInpatientRequired", 0);
		this.counts.put("cqmsInpatientRequiredMet", 0);
		this.counts.put("cqmsAmbulatoryRequired", 0);
		this.counts.put("cqmsAmbulatoryRequiredMet", 0);
		this.counts.put("cqmsAmbulatoryCoreRequired", 0);
		this.counts.put("cqmsAmbulatoryCoreRequiredMet", 0);
		this.counts.put("domainsRequired", 0);
		this.counts.put("domainsRequiredMet", 0);
	}
	
	//**********************************************************************
	// onValidate
	//
	//**********************************************************************
	public boolean onValidate() {
		return false;
	}

	//**********************************************************************
	// isCriteriaValid
	//
	// Must meet all required criteria.
	//**********************************************************************
	protected boolean isCriteriaValid() {
		return false;
	}
	
	//**********************************************************************
	// isCqmsValid
	//
	// Either Inpatient or Ambulatory CQMs required.
	//**********************************************************************
	protected boolean isCqmsValid() {
		return false;
	}

	//**********************************************************************
	// isDomainsValid
	//
	// At least 3 CQM Domains must be met.
	//**********************************************************************
	protected boolean isDomainsValid() {
		return false;
	}

}
