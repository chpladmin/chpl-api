package gov.healthit.chpl.certificationId;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import gov.healthit.chpl.certificationId.Validator;

public class Validator2014 extends Validator {

	protected static final List<String> REQUIRED_CRITERIA = new ArrayList<String> (Arrays.asList(
		"170.314 (a)(3)",
		"170.314 (a)(5)",
		"170.314 (a)(6)",
		"170.314 (a)(7)",
		"170.314 (a)(8)",
		"170.314 (b)(7)",
		"170.314 (c)(1)",
		"170.314 (c)(2)",
		"170.314 (c)(3)",
		"170.314 (d)(1)",
		"170.314 (d)(2)",
		"170.314 (d)(3)",
		"170.314 (d)(4)",
		"170.314 (d)(5)",
		"170.314 (d)(6)",
		"170.314 (d)(7)",
		"170.314 (d)(8)",
		"170.314 (g)(4)"
	));

	protected static final List<String> CPOE_CRITERIA = new ArrayList<String> (Arrays.asList(
		"170.314 (a)(1)",
		"170.314 (a)(18)",
		"170.314 (a)(19)",
		"170.314 (a)(20)"
	));
	
	protected static final List<String> INPATIENT_CQMS = new ArrayList<String> (Arrays.asList(
		"CMS9",
		"CMS26",
		"CMS30",
		"CMS31",
		"CMS32",
		"CMS53",
		"CMS55",
		"CMS60",
		"CMS71",
		"CMS72",
		"CMS73",
		"CMS91",
		"CMS100",
		"CMS102",
		"CMS104",
		"CMS105",
		"CMS107",
		"CMS108",
		"CMS109",
		"CMS110",
		"CMS111",
		"CMS113",
		"CMS114",
		"CMS171",
		"CMS172",
		"CMS178",
		"CMS185",
		"CMS188",
		"CMS190"
	));

	protected static final List<String> AMBULATORY_CQMS = new ArrayList<String> (Arrays.asList(
// Core		"CMS2",
		"CMS22",
// Core		"CMS50",
		"CMS52",
		"CMS56",
		"CMS61",
		"CMS62",
		"CMS64",
		"CMS65",
		"CMS66",
// Core		"CMS68",
// Core		"CMS69",
		"CMS74",
// Core		"CMS75",
		"CMS77",
		"CMS82",
// Core		"CMS90",
// Core		"CMS117",
		"CMS122",
		"CMS123",
		"CMS124",
		"CMS125",
// Core		"CMS126",
		"CMS127",
		"CMS128",
		"CMS129",
		"CMS130",
		"CMS131",
		"CMS132",
		"CMS133",
		"CMS134",
		"CMS135",
// Core		"CMS136",
		"CMS137",
// Core		"CMS138",
		"CMS139",
		"CMS140",
		"CMS141",
		"CMS142",
		"CMS143",
		"CMS144",
		"CMS145",
// Core		"CMS146",
		"CMS147",
		"CMS148",
		"CMS149",
// Core		"CMS153",
// Core		"CMS154",
// Core		"CMS155",
// Core		"CMS156",
		"CMS157",
		"CMS158",
		"CMS159",
		"CMS160",
		"CMS161",
		"CMS163",
		"CMS164",
// Core		"CMS165",
// Core		"CMS166",
		"CMS167",
		"CMS169",
		"CMS177",
		"CMS179",
		"CMS182"
	));
	
	protected static final List<String> AMBULATORY_CORE_CQMS = new ArrayList<String>(Arrays.asList(
		"CMS2",
		"CMS50",
		"CMS68",
		"CMS69",
		"CMS75",
		"CMS90",
		"CMS117",
		"CMS126",
		"CMS136",
		"CMS138",
		"CMS146",
		"CMS153",
		"CMS154",
		"CMS155",
		"CMS156",
		"CMS165",
		"CMS166"	
	));

	public Validator2014() {
		this.counts.put("criteriaRequired", REQUIRED_CRITERIA.size());
		this.counts.put("criteriaRequiredMet", 0);
		this.counts.put("criteriaCpoeRequired", 1);
		this.counts.put("criteriaCpoeRequiredMet", 0);
		this.counts.put("criteriaTocRequired", 2);
		this.counts.put("criteriaTocRequiredMet", 0);
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
		boolean tocValid = isTOCValid();
		
		this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + this.counts.get("criteriaCpoeRequired") + this.counts.get("criteriaTocRequired"));
		this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + this.counts.get("criteriaCpoeRequiredMet") + this.counts.get("criteriaTocRequiredMet"));
		
		return (criteriaValid && cpoeValid && tocValid);
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
		for (String cqm : INPATIENT_CQMS) {
			if (this.cqmsMet.containsKey(cqm)) {
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
			if (this.AMBULATORY_CORE_CQMS.contains(cqm)) {
				++coreAmbulatory;
			}
			if (this.AMBULATORY_CQMS.contains(cqm)) {
				++nonCoreAmbulatory;
			}
		}
		
		this.counts.put("cqmsAmbulatoryRequiredMet", nonCoreAmbulatory);
		this.counts.put("cqmsAmbulatoryCoreRequiredMet", coreAmbulatory);
		
		return ((this.counts.get("cqmsAmbulatoryRequiredMet") + this.counts.get("cqmsAmbulatoryCoreRequiredMet")) >= 
			(this.counts.get("cqmsAmbulatoryRequiredMet") + this.counts.get("cqmsAmbulatoryCoreRequiredMet")));
	}

	//**********************************************************************
	// isCPOEValid
	//
	// At least one of the four Computerized Provider Order Entry-related 
	// criteria must be met.
	//**********************************************************************
	protected boolean isCPOEValid() {
		for (String crit : CPOE_CRITERIA) {
			if (null != criteriaMet.get(crit)) {
				this.counts.put("criteriaCpoeRequiredMet", 1);
				return true;
			}
		}
		return false;
	}
	
	//**********************************************************************
	// isTOCValid
	//
	// A combination of the Transitions of Care criteria must be met.
	//**********************************************************************
	protected boolean isTOCValid() {

		// 170.314(b)(1) and 170.314(b)(2) and 170.314(b)(8) and 170.314(h)(1)
		if (this.criteriaMet.containsKey("170.314 (b)(1)") && this.criteriaMet.containsKey("170.314 (b)(2)") &&
			this.criteriaMet.containsKey("170.314 (8)(8)") && this.criteriaMet.containsKey("170.314 (h)(1)")) {
			this.counts.put("criteriaTocRequiredMet", 4);
			this.counts.put("criteriaTocRequired", 4);
			return true;
		}

		// 170.314(b)(1) and 170.314(b)(2) and 170.314(h)(1)
		if (this.criteriaMet.containsKey("170.314 (b)(1)") && this.criteriaMet.containsKey("170.314 (b)(2)") &&
			this.criteriaMet.containsKey("170.314 (h)(1)")) {
			this.counts.put("criteriaTocRequiredMet", 3);
			this.counts.put("criteriaTocRequired", 3);
			return true;
		}
		
		// 170.314(b)(1) and 170.314(b)(2) and 170.314(b)(8)
		if (this.criteriaMet.containsKey("170.314 (b)(1)") && this.criteriaMet.containsKey("170.314 (b)(2)") &&
			this.criteriaMet.containsKey("170.314 (b)(8)")) {
			this.counts.put("criteriaTocRequiredMet", 3);
			this.counts.put("criteriaTocRequired", 3);
			return true;
		}
		
		// 170.314(b)(8) and 170.314(h)(1)
		if (this.criteriaMet.containsKey("170.314 (b)(8)") && this.criteriaMet.containsKey("170.314 (h)(1)")) {
			this.counts.put("criteriaTocRequiredMet", 2);
			this.counts.put("criteriaTocRequired", 2);
			return true;
		}

		// 170.314(b)(1) and 170.314(b)(2)
		if (this.criteriaMet.containsKey("170.314 (b)(1)") && this.criteriaMet.containsKey("170.314 (b)(2)")) {
			this.counts.put("criteriaTocRequiredMet", 2);
			this.counts.put("criteriaTocRequired", 2);
			return true;
		}
	
		return false;
	}	
}
