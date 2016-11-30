package gov.healthit.chpl.certificationId;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import gov.healthit.chpl.certificationId.Validator;

public class Validator20142015 extends Validator {

	public Validator20142015() {
		this.counts.put("criteriaRequired", 0);	// This number is calculated during the checks
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
		boolean crit = isCriteriaValid();
		boolean cqms = isCqmsValid();
		boolean domains = isDomainsValid();
		return (crit && cqms && domains);
	}

	//**********************************************************************
	// isCriteriaValid
	//
	// Must meet all required criteria.
	//**********************************************************************
	protected boolean isCriteriaValid() {
		this.counts.put("criteriaRequired", 6);

		boolean cpoe = false;
		boolean recordDemo = false;
		boolean problemList = false;
		boolean medList = false;
		boolean medAllergyList = false;
		boolean clinicalDecision = false;

		// (1)(i) 45 CFR 170.314(a)(1), (18), (19) or (20); or (ii) 45 CFR 170.315(a)(1), (2) or (3).
		if ((this.criteriaMet.containsKey("170.314 (a)(1)") ||
		this.criteriaMet.containsKey("170.314 (a)(18)") ||
		this.criteriaMet.containsKey("170.314 (a)(19)") ||
		this.criteriaMet.containsKey("170.314 (a)(20)"))
		||
		(this.criteriaMet.containsKey("170.315 (a)(1)") ||
		this.criteriaMet.containsKey("170.315 (a)(2)") ||
		this.criteriaMet.containsKey("170.315 (a)(3)"))) {
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
			cpoe = true;
		}
		
		// (2)(i) Record demographics at 45 CFR 170.314(a)(3); or (ii) 45 CFR 170.315(a)(5).
		if (this.criteriaMet.containsKey("170.314 (a)(3)") || this.criteriaMet.containsKey("170.315 (a)(5)")) {
			recordDemo = true;
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
		}

		// (3)(i) Problem list at 45 CFR 170.314(a)(5); or (ii) 45 CFR 170.315(a)(6).
		if (this.criteriaMet.containsKey("170.314 (a)(5)") || this.criteriaMet.containsKey("170.315 (a)(6)")) {
			problemList = true;
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
		}

		// (4)(i) Medication list at 45 CFR 170.314(a)(6); or (ii) 45 CFR 170.315(a)(7).
		if (this.criteriaMet.containsKey("170.314 (a)(6)") || this.criteriaMet.containsKey("170.315 (a)(7)")) {
			medList = true;
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
		}

		// (5)(i) Medication allergy list 45 CFR 170.314(a)(7); or (ii) 45 CFR 170.315(a)(8).
		if (this.criteriaMet.containsKey("170.314 (a)(7)") || this.criteriaMet.containsKey("170.315 (a)(8)")) {
			medAllergyList = true;
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
		}

		// (6)(i) Clinical decision support at 45 CFR 170.314(a)(8); or (ii) 45 CFR 170.315(a)(9).
		if (this.criteriaMet.containsKey("170.314 (a)(8)") || this.criteriaMet.containsKey("170.315 (a)(9)")) {
			clinicalDecision = true;
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
		}
		
		boolean critToc = this.isCriteriaTOCValid();
		boolean critCqm = this.isCriteriaCqmValid();
		boolean critPs = this.isCriteriaPSValid();
	
		return (cpoe && recordDemo && problemList && medList
				&& medAllergyList && clinicalDecision 
				&& critToc && critCqm && critPs);
	}
	
	//**********************************************************************
	// isCriteriaTOCValid
	//
	// Must match at least one of the 21 sets.
	//
	// Checks should be performed in the order of sets with the most 
	// required criteria to the sets with the least required criteria.
	//**********************************************************************
	protected boolean isCriteriaTOCValid() {
		int tocCriteriaMet = 0;
		int tocCriteriaRequired = 2; //minimum 2 criteria required
		
		if(this.criteriaMet.containsKey("170.314 (b)(1)")) {
			tocCriteriaMet++;
			if(this.criteriaMet.containsKey("170.314 (b)(2)")) {
				tocCriteriaMet++;
				
				if(this.criteriaMet.containsKey("170.314 (h)(1)")) {
					tocCriteriaRequired++;
					tocCriteriaMet++;
				}
				if(this.criteriaMet.containsKey("170.314 (b)(8)")) {
					tocCriteriaRequired++;
					tocCriteriaMet++;
				}
				if(this.criteriaMet.containsKey("170.315 (h)(2)")) {
					tocCriteriaRequired++;
					tocCriteriaMet++;
				} 
				if(this.criteriaMet.containsKey("170.315 (b)(1)")) {
					tocCriteriaRequired++;
					tocCriteriaMet++;
					if(this.criteriaMet.containsKey("170.315 (h)(1)")) {
						//(xvi) 45 CFR 170.314(b)(1), (b)(2), (b)(8), (h)(1), 170.315(b)(1), and 170.315(h)(1).
						tocCriteriaRequired++;
						tocCriteriaMet++;
					}
				}
			}
		} else if(this.criteriaMet.containsKey("170.314 (b)(8)")) {
			tocCriteriaMet++;
			if(this.criteriaMet.containsKey("170.314 (h)(1)")) {
				tocCriteriaMet++;
				
				if(this.criteriaMet.containsKey("170.315 (h)(2)")) {
					tocCriteriaRequired++;
					tocCriteriaMet++;
				} else if(this.criteriaMet.containsKey("170.315 (b)(1)")) {
					tocCriteriaRequired++;
					tocCriteriaMet++;
				}
			} else if(this.criteriaMet.containsKey("170.315 (b)(1)")) {
				tocCriteriaMet++;
			}
		} else if(this.criteriaMet.containsKey("170.315 (b)(1)")) {
			tocCriteriaMet++;
			if(this.criteriaMet.containsKey("170.314 (h)(1)")) {
				tocCriteriaMet++;
			} else if(this.criteriaMet.containsKey("170.315 (h)(1)")) {
				tocCriteriaMet++;
				if(this.criteriaMet.containsKey("170.315 (h)(2)")) {
					tocCriteriaRequired++;
					tocCriteriaMet++;
				}
			} else if(this.criteriaMet.containsKey("170.315 (h)(2)")) {
				tocCriteriaMet++;
			}
		}
		
		this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + tocCriteriaRequired);
		this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + tocCriteriaMet);
		
		return tocCriteriaMet >= tocCriteriaRequired;
	}
	
	//**********************************************************************
	// isCriteriaCqmValid
	//
	// Must match all 3 (c) criteria.
	//**********************************************************************
	protected boolean isCriteriaCqmValid() {
		int cqmCritCount = 0;
		int cqmCritRequired = 3;
		
		// (1) 45 CFR 170.314(c)(1) or 170.315(c)(1);
		if (this.criteriaMet.containsKey("170.314 (c)(1)") || this.criteriaMet.containsKey("170.315 (c)(1)")) {
			++cqmCritCount;
		}

		// (2) 45 CFR 170.314(c)(2) or 170.315(c)(2)
		if (this.criteriaMet.containsKey("170.314 (c)(2)") || this.criteriaMet.containsKey("170.315 (c)(2)")) {
			++cqmCritCount;
		}
		
		// (3) 45 CFR 170.314(c)(3) or 170.315(c)(3)
		if (this.criteriaMet.containsKey("170.314 (c)(3)") || this.criteriaMet.containsKey("170.315 (c)(3)")) {
			++cqmCritCount;
		}

		this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + cqmCritRequired);
		this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + cqmCritCount);
		
		return (cqmCritCount == cqmCritRequired);
	}
	
	//**********************************************************************
	// isCriteriaPSValid
	//
	// Must match all 8 (d) criteria.
	//**********************************************************************
	protected boolean isCriteriaPSValid() {
		
		int psCritMetCount = 0;
		int psCritMetRequired = 8;
		
		// (1) 45 CFR 170.314(d)(1) or 170.315(d)(1);
		if (this.criteriaMet.containsKey("170.314 (d)(1)") || this.criteriaMet.containsKey("170.315 (d)(1)")) {
			++psCritMetCount;
		}
		
		// (2) 45 CFR 170.314(d)(2) or 170.315(d)(2);
		if (this.criteriaMet.containsKey("170.314 (d)(2)") || this.criteriaMet.containsKey("170.315 (d)(2)")) {
			++psCritMetCount;
		}

		// (3) 45 CFR 170.314(d)(3) or 170.315(d)(3);
		if (this.criteriaMet.containsKey("170.314 (d)(3)") || this.criteriaMet.containsKey("170.315 (d)(3)")) {
			++psCritMetCount;
		}

		// (4) 45 CFR 170.314(d)(4) or 170.315(d)(4);
		if (this.criteriaMet.containsKey("170.314 (d)(4)") || this.criteriaMet.containsKey("170.315 (d)(4)")) {
			++psCritMetCount;
		}

		// (5) 45 CFR 170.314(d)(5) or 170.315(d)(5);
		if (this.criteriaMet.containsKey("170.314 (d)(5)") || this.criteriaMet.containsKey("170.315 (d)(5)")) {
			++psCritMetCount;
		}

		// (6) 45 CFR 170.314(d)(6) or 170.315(d)(6);
		if (this.criteriaMet.containsKey("170.314 (d)(6)") || this.criteriaMet.containsKey("170.315 (d)(6)")) {
			++psCritMetCount;
		}

		// (7) 45 CFR 170.314(d)(7) or 170.315(d)(7);
		if (this.criteriaMet.containsKey("170.314 (d)(7)") || this.criteriaMet.containsKey("170.315 (d)(7)")) {
			++psCritMetCount;
		}

		// (8) 45 CFR 170.314(d)(8) or 170.315(d)(8);
		if (this.criteriaMet.containsKey("170.314 (d)(8)") || this.criteriaMet.containsKey("170.315 (d)(8)")) {
			++psCritMetCount;
		}

		this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + psCritMetCount);
		this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + psCritMetRequired);
		
		return (psCritMetCount == psCritMetRequired);
	}
	
	//**********************************************************************
	// isCqmsValid
	//
	// There is no CQM check for 2014/2015 hybrid attestation.
	//**********************************************************************
	protected boolean isCqmsValid() {
		return true;
	}

	//**********************************************************************
	// isDomainsValid
	//
	// There is no CQM check for 2014/2015 hybrid attestation.
	//**********************************************************************
	protected boolean isDomainsValid() {
		return true;
	}
}
