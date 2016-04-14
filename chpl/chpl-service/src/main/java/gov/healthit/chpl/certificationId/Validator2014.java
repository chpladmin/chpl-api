package gov.healthit.chpl.certificationId;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Validator2014 {

	private static final List<String> REQUIRED_CRITERIA = new ArrayList<String> (Arrays.asList(
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

	private static final List<String> CPOE_CRITERIA = new ArrayList<String> (Arrays.asList(
		"170.314 (a)(1)",
		"170.314 (a)(18)",
		"170.314 (a)(19)",
		"170.314 (a)(20)"
	));
	
	private static final List<String> INPATIENT_CQMS = new ArrayList<String> (Arrays.asList(
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

	private static final List<String> AMBULATORY_CQMS = new ArrayList<String> (Arrays.asList(
		"CMS2",
		"CMS22",
		"CMS50",
		"CMS52",
		"CMS56",
		"CMS61",
		"CMS62",
		"CMS64",
		"CMS65",
		"CMS66",
		"CMS68",
		"CMS69",
		"CMS74",
		"CMS75",
		"CMS77",
		"CMS82",
		"CMS90",
		"CMS117",
		"CMS122",
		"CMS123",
		"CMS124",
		"CMS125",
		"CMS126",
		"CMS127",
		"CMS128",
		"CMS129",
		"CMS130",
		"CMS131",
		"CMS132",
		"CMS133",
		"CMS134",
		"CMS135",
		"CMS136",
		"CMS137",
		"CMS138",
		"CMS139",
		"CMS140",
		"CMS141",
		"CMS142",
		"CMS143",
		"CMS144",
		"CMS145",
		"CMS146",
		"CMS147",
		"CMS148",
		"CMS149",
		"CMS153",
		"CMS154",
		"CMS155",
		"CMS156",
		"CMS157",
		"CMS158",
		"CMS159",
		"CMS160",
		"CMS161",
		"CMS163",
		"CMS164",
		"CMS165",
		"CMS166",
		"CMS167",
		"CMS169",
		"CMS177",
		"CMS179",
		"CMS182"
	));
	
	private static final List<String> AMBULATORY_CORE_CQMS = new ArrayList<String>(Arrays.asList(
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

	private Map<String, Integer> percents = new HashMap<String, Integer>();
	private Map<String, Integer> counts = new HashMap<String, Integer>();
	private boolean valid = false;
	
	public Map<String, Integer> getCounts() {
		return counts;
	}

	public Map<String, Integer> getPercents() {
		return percents;
	}
	
	public boolean isValid() {
		return this.valid;
	}
	
	private Map<String, Long> criteriaMet = new HashMap<String, Long>();
	private Map<String, Long> cqmsMet = new HashMap<String, Long>();
	private Map<String, Long> domainsMet = new HashMap<String, Long>();

	public Validator2014(Map<String, Long> criteriaMet, Map<String, Long> cqmsMet, Map<String, Long> domainsMet) {
		this.criteriaMet = criteriaMet;
		this.cqmsMet = cqmsMet;
		this.domainsMet = domainsMet;
		this.counts.put("criteriaRequired", REQUIRED_CRITERIA.size());
		this.counts.put("criteriaRequiredMet", 0);
		this.counts.put("criteriaCpoeRequired", 1);
		this.counts.put("criteriaCpoeRequiredMet", 0);
		this.counts.put("criteriaTocRequired", 2);
		this.counts.put("criteriaTocRequiredMet", 0);
		this.counts.put("cqmsInpatientRequired", INPATIENT_CQMS.size());
		this.counts.put("cqmsInpatientRequiredMet", 0);
		this.counts.put("cqmsAmbulatoryRequired", 3);
		this.counts.put("cqmsAmbulatoryRequiredMet", 0);
		this.counts.put("cqmsAmbulatoryCoreRequired", 6);
		this.counts.put("cqmsAmbulatoryCoreRequiredMet", 0);
		this.counts.put("domainsRequired", 3);
		this.counts.put("domainsRequiredMet", 0);
	}
	
	public boolean validate() {
		boolean crit = isBaseCriteriaValid();
		boolean cpoe = isCPOEValid();
		boolean toc = isTOCValid();
		
		this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + this.counts.get("criteriaCpoeRequired") + this.counts.get("criteriaTocRequired"));
		this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + this.counts.get("criteriaCpoeRequiredMet") + this.counts.get("criteriaTocRequiredMet"));
		
		boolean inpatientCqms = isInpatientCqmsValid();
		boolean ambulatoryCqms = isAmbulatoryCqmsValid();
		boolean domains = true;//isDomainsValid();

		this.calculatePercentages();

		this.valid = (crit && cpoe && toc && (inpatientCqms || ambulatoryCqms) && domains);
		
		return this.isValid();
	}
	
	private void calculatePercentages() {
		this.percents.put("criteriaMet", 
			(0 == this.counts.get("criteriaRequired")) ? 0 :
				Math.min((int)Math.floor((this.counts.get("criteriaRequiredMet") * 100.0) / this.counts.get("criteriaRequired")), 100)
		);
		this.percents.put("cqmDomains", 
			(0 == this.counts.get("domainsRequired")) ? 0 :
				Math.min((int)Math.floor((this.counts.get("domainsRequiredMet") * 100.0) / this.counts.get("domainsRequired")), 100)
		);
		this.percents.put("cqmsInpatient", 
			(0 == this.counts.get("cqmsInpatientRequired")) ? 0 :
				Math.min((int)Math.floor((this.counts.get("cqmsInpatientRequiredMet") * 100.0) / this.counts.get("cqmsInpatientRequired")), 100)
		);
		this.percents.put("cqmsAmbulatory", 
			Math.min(
			(int)Math.floor(
				(
					(this.counts.get("cqmsAmbulatoryCoreRequiredMet") + Math.min(this.counts.get("cqmsAmbulatoryRequiredMet"), 
					9 - 6))
					/ (double)(9)
				) * 100.0
			),
			100)
		);
	}

	//**********************************************************************
	// isDomainsValid
	//
	// At least CQM Domains must be met.
	//**********************************************************************
	private boolean isDomainsValid() {
		boolean valid = false;
		
		// Must meet at least 3 CQM Domains
		if (this.domainsMet.size() >= this.counts.get("domainsRequired")) {
			valid = true;
		}

		this.counts.put("domainsRequiredMet", 0);
		
		return valid;
	}

	// Must meet all required base criteria
	private boolean isBaseCriteriaValid() {
		this.counts.put("criteriaRequired", REQUIRED_CRITERIA.size());
		boolean isValid = true;
		for (String crit : REQUIRED_CRITERIA) {
			if (null == criteriaMet.get(crit)) {
				isValid = false;
			} else {
				this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
			}
		}
		return isValid;
	}
	
	//**********************************************************************
	// isCPOEValid
	//
	// At least one of the four Computerized Provider Order Entry-related 
	// criteria must be met.
	//**********************************************************************
	private boolean isCPOEValid() {
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
	private boolean isTOCValid() {

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

	//**********************************************************************
	// isInpatientCqmsValid
	//
	// At least Inpatient CQMs must be met.
	//**********************************************************************
	private boolean isInpatientCqmsValid() {
		for (String cqm : INPATIENT_CQMS) {
			if (this.cqmsMet.containsKey(cqm)) {
				this.counts.put("cqmsInpatientRequiredMet", this.counts.get("cqmsInpatientRequiredMet") + 1);
			}
		}
		
		return (this.counts.get("cqmsInpatientRequiredMet") >= this.counts.get("cqmsInpatientRequired"));
	}

	//**********************************************************************
	// isAmbulatoryCqmsValid
	//
	// At least 9 total Ambulatory CQMs with at least 6 of those being 
	// Ambulatory Core CQMs.
	//**********************************************************************
	private boolean isAmbulatoryCqmsValid() {
		int nonCoreAmbulatory = 0;
		int coreAmbulatory = 0;

		// At least 6
		for (String cqm : AMBULATORY_CORE_CQMS) {
			if (this.cqmsMet.containsKey(cqm)) {
				coreAmbulatory++;
			}
		}
		
		// at least 3 
		for (String cqm : AMBULATORY_CQMS) {
			if (this.cqmsMet.containsKey(cqm)) {
				nonCoreAmbulatory++;
			}
		}
		
		this.counts.put("cqmsAmbulatoryRequiredMet", Math.min(nonCoreAmbulatory, 3));
		this.counts.put("cqmsAmbulatoryCoreRequiredMet", Math.min(coreAmbulatory, 6));
		
		return (this.counts.get("cqmsAmbulatoryRequiredMet") + this.counts.get("cqmsAmbulatoryCoreRequiredMet")) >= 9;
	}
}
