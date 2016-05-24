package gov.healthit.chpl.certificationId;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import gov.healthit.chpl.certificationId.Validator;

public class Validator2015 extends Validator {

	protected static final List<String> REQUIRED_CRITERIA = new ArrayList<String> (Arrays.asList(
		"170.315 (a)(5)",
		"170.315 (a)(6)",
		"170.315 (a)(7)",
		"170.315 (a)(8)",
		"170.315 (a)(9)",
		"170.315 (a)(11)",
		"170.315 (a)(14)",
		"170.315 (c)(1)",
		"170.315 (b)(1)",
		"170.315 (b)(6)",
		"170.315 (g)(7)",
		"170.315 (g)(8)",
		"170.315 (g)(9)"
	));

	protected static final List<String> CPOE_CRITERIA_OR = new ArrayList<String> (Arrays.asList(
		"170.315 (a)(1)",
		"170.315 (a)(2)",
		"170.315 (a)(3)"
	));

//	protected static final List<String> DIRECTPROJECT_CRITERIA_OR = new ArrayList<String> (Arrays.asList(
//		"170.315 (h)(1)",
//		"170.315 (h)(2)"
//	));	

	protected static final Map<String, Integer> INPATIENT_CQMS;
	static  {
		INPATIENT_CQMS = new HashMap<String, Integer>();
		INPATIENT_CQMS.put("CMS9",5);
		INPATIENT_CQMS.put("CMS26",4);
		INPATIENT_CQMS.put("CMS30",6);
		INPATIENT_CQMS.put("CMS31",5);
		INPATIENT_CQMS.put("CMS32",6);
		INPATIENT_CQMS.put("CMS53",5);
		INPATIENT_CQMS.put("CMS55",5);
		INPATIENT_CQMS.put("CMS60",5);
		INPATIENT_CQMS.put("CMS71",6);
		INPATIENT_CQMS.put("CMS72",5);
		INPATIENT_CQMS.put("CMS73",5);
		INPATIENT_CQMS.put("CMS91",6);
		INPATIENT_CQMS.put("CMS100",5);
		INPATIENT_CQMS.put("CMS102",5);
		INPATIENT_CQMS.put("CMS104",5);
		INPATIENT_CQMS.put("CMS105",5);
		INPATIENT_CQMS.put("CMS107",5);
		INPATIENT_CQMS.put("CMS108",5);
		INPATIENT_CQMS.put("CMS109",5);
		INPATIENT_CQMS.put("CMS110",5);
		INPATIENT_CQMS.put("CMS111",5);
		INPATIENT_CQMS.put("CMS113",5);
		INPATIENT_CQMS.put("CMS114",5);
		INPATIENT_CQMS.put("CMS171",6);
		INPATIENT_CQMS.put("CMS172",6);
		INPATIENT_CQMS.put("CMS178",6);
		INPATIENT_CQMS.put("CMS185",5);
		INPATIENT_CQMS.put("CMS188",6);
		INPATIENT_CQMS.put("CMS190",5);
	}

	protected static final Map<String, Integer> AMBULATORY_CQMS;
	static {
		AMBULATORY_CQMS = new HashMap<String, Integer>();
// Core	AMBULATORY_CQMS.put("CMS2",6);
		AMBULATORY_CQMS.put("CMS22",5);
// Core	AMBULATORY_CQMS.put("CMS50",5);
		AMBULATORY_CQMS.put("CMS52",5);
		AMBULATORY_CQMS.put("CMS56",5);
		AMBULATORY_CQMS.put("CMS61",6);
		AMBULATORY_CQMS.put("CMS62",5);
		AMBULATORY_CQMS.put("CMS64",6);
		AMBULATORY_CQMS.put("CMS65",6);
		AMBULATORY_CQMS.put("CMS66",5);
// Core	AMBULATORY_CQMS.put("CMS68",6);
// Core	AMBULATORY_CQMS.put("CMS69",5);
		AMBULATORY_CQMS.put("CMS74",6);
// Core	AMBULATORY_CQMS.put("CMS75",5);
		AMBULATORY_CQMS.put("CMS77",5);
		AMBULATORY_CQMS.put("CMS82",4);
// Core	AMBULATORY_CQMS.put("CMS90",6);
// Core	AMBULATORY_CQMS.put("CMS117",5);
		AMBULATORY_CQMS.put("CMS122",5);
		AMBULATORY_CQMS.put("CMS123",5);
		AMBULATORY_CQMS.put("CMS124",5);
		AMBULATORY_CQMS.put("CMS125",5);
// Core	AMBULATORY_CQMS.put("CMS126",5);
		AMBULATORY_CQMS.put("CMS127",5);
		AMBULATORY_CQMS.put("CMS128",5);
		AMBULATORY_CQMS.put("CMS129",6);
		AMBULATORY_CQMS.put("CMS130",5);
		AMBULATORY_CQMS.put("CMS131",5);
		AMBULATORY_CQMS.put("CMS132",5);
		AMBULATORY_CQMS.put("CMS133",5);
		AMBULATORY_CQMS.put("CMS134",5);
		AMBULATORY_CQMS.put("CMS135",5);
// Core	AMBULATORY_CQMS.put("CMS136",6);
		AMBULATORY_CQMS.put("CMS137",5);
// Core	AMBULATORY_CQMS.put("CMS138",5);
		AMBULATORY_CQMS.put("CMS139",5);
		AMBULATORY_CQMS.put("CMS140",5);
		AMBULATORY_CQMS.put("CMS141",6);
		AMBULATORY_CQMS.put("CMS142",5);
		AMBULATORY_CQMS.put("CMS143",5);
		AMBULATORY_CQMS.put("CMS144",5);
		AMBULATORY_CQMS.put("CMS145",5);
// Core	AMBULATORY_CQMS.put("CMS146",5);
		AMBULATORY_CQMS.put("CMS147",6);
		AMBULATORY_CQMS.put("CMS148",5);
		AMBULATORY_CQMS.put("CMS149",5);
// Core	AMBULATORY_CQMS.put("CMS153",5);
// Core	AMBULATORY_CQMS.put("CMS154",5);
// Core	AMBULATORY_CQMS.put("CMS155",5);
// Core	AMBULATORY_CQMS.put("CMS156",5);
		AMBULATORY_CQMS.put("CMS157",5);
		AMBULATORY_CQMS.put("CMS158",5);
		AMBULATORY_CQMS.put("CMS159",5);
		AMBULATORY_CQMS.put("CMS160",5);
		AMBULATORY_CQMS.put("CMS161",5);
		AMBULATORY_CQMS.put("CMS163",5);
		AMBULATORY_CQMS.put("CMS164",5);
// Core	AMBULATORY_CQMS.put("CMS165",5);
// Core	AMBULATORY_CQMS.put("CMS166",6);
		AMBULATORY_CQMS.put("CMS167",5);
		AMBULATORY_CQMS.put("CMS169",5);
		AMBULATORY_CQMS.put("CMS177",5);
		AMBULATORY_CQMS.put("CMS179",5);
		AMBULATORY_CQMS.put("CMS182",6);
	}
	
	protected static final Map<String, Integer> AMBULATORY_CORE_CQMS;
	static {
		AMBULATORY_CORE_CQMS = new HashMap<String, Integer>();
		AMBULATORY_CORE_CQMS.put("CMS2",6);
		AMBULATORY_CORE_CQMS.put("CMS50",5);
		AMBULATORY_CORE_CQMS.put("CMS68",6);
		AMBULATORY_CORE_CQMS.put("CMS69",5);
		AMBULATORY_CORE_CQMS.put("CMS75",5);
		AMBULATORY_CORE_CQMS.put("CMS90",6);
		AMBULATORY_CORE_CQMS.put("CMS117",5);
		AMBULATORY_CORE_CQMS.put("CMS126",5);
		AMBULATORY_CORE_CQMS.put("CMS136",6);
		AMBULATORY_CORE_CQMS.put("CMS138",5);
		AMBULATORY_CORE_CQMS.put("CMS146",5);
		AMBULATORY_CORE_CQMS.put("CMS153",5);
		AMBULATORY_CORE_CQMS.put("CMS154",5);
		AMBULATORY_CORE_CQMS.put("CMS155",5);
		AMBULATORY_CORE_CQMS.put("CMS156",5);
		AMBULATORY_CORE_CQMS.put("CMS165",5);
		AMBULATORY_CORE_CQMS.put("CMS166",6);
	}

	public Validator2015() {
		this.counts.put("criteriaRequired", REQUIRED_CRITERIA.size());
		this.counts.put("criteriaRequiredMet", 0);
		this.counts.put("criteriaCpoeRequired", 1);
		this.counts.put("criteriaCpoeRequiredMet", 0);
		this.counts.put("criteriaDpRequired", 1);
		this.counts.put("criteriaDpRequiredMet", 0);
		this.counts.put("cqmsInpatientRequired", 16);
		this.counts.put("cqmsInpatientRequiredMet", 0);
		this.counts.put("cqmsAmbulatoryRequired", 3);
		this.counts.put("cqmsAmbulatoryRequiredMet", 0);
		this.counts.put("cqmsAmbulatoryCoreRequired", 6);
		this.counts.put("cqmsAmbulatoryCoreRequiredMet", 0);
		this.counts.put("domainsRequired", 3);
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
		this.counts.put("criteriaRequired", REQUIRED_CRITERIA.size());
		boolean criteriaValid = true;
		for (String crit : REQUIRED_CRITERIA) {
			if (null == criteriaMet.get(crit)) {
				criteriaValid = false;
			} else {
				this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
			}
		}
		
		boolean cpoeValid = isCPOEValid();
		boolean dpValid = isDPValid();
		
		this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + this.counts.get("criteriaCpoeRequired") + this.counts.get("criteriaDpRequired"));
		this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + this.counts.get("criteriaCpoeRequiredMet") + this.counts.get("criteriaDpRequiredMet"));
		
		return (criteriaValid && cpoeValid && dpValid);
	}
	
	//**********************************************************************
	// isCqmsValid
	//
	// Either Inpatient or Ambulatory CQMs required.
	//**********************************************************************
	protected boolean isCqmsValid() {
		boolean valid = false;
		if (isAmbulatoryCqmsValid()) {
			valid = true;
		}
		if (isInpatientCqmsValid()) {
			valid = true;
		}
		return valid;
	}

	//**********************************************************************
	// isDomainsValid
	//
	// At least 3 CQM Domains must be met.
	//**********************************************************************
	protected boolean isDomainsValid() {
		this.counts.put("domainsRequiredMet", 
			this.domainsMet.size() >= this.counts.get("domainsRequired") ?
				this.counts.get("domainsRequired") : this.domainsMet.size());
		return (this.counts.get("domainsRequiredMet") >= this.counts.get("domainsRequired"));
	}
	
	//**********************************************************************
	// isInpatientCqmsValid
	//
	// At least 16 Inpatient CQMs must be met.
	//**********************************************************************
	protected boolean isInpatientCqmsValid() {
		int cqmCount = 0;
		for (String cqm : this.INPATIENT_CQMS.keySet()) {
			Integer reqVersion = INPATIENT_CQMS.get(cqm);
			Integer metVersion = this.cqmsMet.get(cqm);
			if ((null != metVersion) && (metVersion >= reqVersion)) {
				++cqmCount;
			}
		}
		
		this.counts.put("cqmsInpatientRequiredMet", cqmCount);
		return (this.counts.get("cqmsInpatientRequiredMet") >= this.counts.get("cqmsInpatientRequired"));
	}

	//**********************************************************************
	// isAmbulatoryCqmsValid
	//
	// At least 9 total Ambulatory CQMs with at least 6 of those being 
	// Ambulatory Core CQMs.
	//**********************************************************************
	protected boolean isAmbulatoryCqmsValid() {
		int nonCoreAmbulatory = 0;
		int coreAmbulatory = 0;

		for (String cqm : cqmsMet.keySet()) {
			Integer metVersion = this.cqmsMet.get(cqm);
			
			// Check Core
			Integer reqVersion = this.AMBULATORY_CORE_CQMS.get(cqm);
			if (null != reqVersion) {
				if (metVersion >= reqVersion) {
					++coreAmbulatory;
				}
			}
			
			// Check Non-Core
			reqVersion = this.AMBULATORY_CQMS.get(cqm);
			if (null != reqVersion) {
				if (metVersion >= reqVersion) {
					++nonCoreAmbulatory;
				}
			}
			
		}
		
		this.counts.put("cqmsAmbulatoryRequiredMet", nonCoreAmbulatory);
		this.counts.put("cqmsAmbulatoryCoreRequiredMet", coreAmbulatory);

		return ((this.counts.get("cqmsAmbulatoryRequiredMet") + this.counts.get("cqmsAmbulatoryCoreRequiredMet")) >= 
			(this.counts.get("cqmsAmbulatoryRequired") + this.counts.get("cqmsAmbulatoryCoreRequired")));
	}

	//**********************************************************************
	// isCPOEValid
	//
	// At least one of the four Computerized Provider Order Entry-related 
	// criteria must be met.
	//**********************************************************************
	protected boolean isCPOEValid() {
		for (String crit : CPOE_CRITERIA_OR) {
			if (null != criteriaMet.get(crit)) {
				this.counts.put("criteriaCpoeRequiredMet", 1);
				return true;
			}
		}
		return false;
	}
	
	//**********************************************************************
	// isDPValid
	//
	// Either Direct Project or Direct Project, Edge Protocol, and 
	// XDR/XDM must be met.
	//**********************************************************************
	protected boolean isDPValid() {
		this.counts.put("criteriaDpRequired", 1);
		
		// 170.315 (h)(1)
		if (this.criteriaMet.containsKey("170.315 (h)(1)")) {
			this.counts.put("criteriaDpRequiredMet", 1);
		}

		// 170.315 (h)(2)
		if (this.criteriaMet.containsKey("170.315 (h)(2)")) {
			this.counts.put("criteriaDpRequiredMet", 1);
		}
	
		return (this.counts.get("criteriaDpRequiredMet") >= this.counts.get("criteriaDpRequired"));
	}	
}
