package gov.healthit.chpl.certificationId;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import gov.healthit.chpl.certificationId.Validator;

public class Validator20142015 extends Validator {

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

	public Validator20142015() {
		this.counts.put("criteriaRequired", 0);
		this.counts.put("criteriaRequiredMet", 0);
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
	//**********************************************************************
	protected boolean isCriteriaTOCValid() {
		
		// (i) 45 CFR 170.314(b)(1) and (2).
		if (this.criteriaMet.containsKey("170.314 (b)(1)") && this.criteriaMet.containsKey("170.314 (b)(2)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 2);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 2);
			return true;
		}
		
		// (ii) 45 CFR 170.314(b)(1), (b)(2), and (h)(1).
		if (this.criteriaMet.containsKey("170.314 (b)(1)") && this.criteriaMet.containsKey("170.314 (b)(2)")
			&& this.criteriaMet.containsKey("170.314 (h)(1)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 3);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 3);
			return true;
		}

		// (iii) 45 CFR 170.314(b)(1), (b)(2), and (b)(8).
		if (this.criteriaMet.containsKey("170.314 (b)(1)") && this.criteriaMet.containsKey("170.314 (b)(2)")
			&& this.criteriaMet.containsKey("170.314 (b)(8)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 3);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 3);
			return true;
		}

		// (iv) 45 CFR 170.314(b)(1), (b)(2), (b)(8), and (h)(1).
		if (this.criteriaMet.containsKey("170.314 (b)(1)") && this.criteriaMet.containsKey("170.314 (b)(2)")
			&& this.criteriaMet.containsKey("170.314 (b)(8)") && this.criteriaMet.containsKey("170.314 (h)(1)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 4);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 4);
			return true;
		}

		// (v) 45 CFR 170.314(b)(8) and (h)(1).
		if (this.criteriaMet.containsKey("170.314 (b)(8)") && this.criteriaMet.containsKey("170.314 (h)(1)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 2);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 2);
			return true;
		}

		// (vi) 45 CFR 170.314(b)(1), (b)(2), and 170.315(h)(2).
		if (this.criteriaMet.containsKey("170.314 (b)(1)") && this.criteriaMet.containsKey("170.314 (b)(2)")
			&& this.criteriaMet.containsKey("170.315 (h)(2)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 3);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 3);
			return true;
		}

		// (vii) 45 CFR 170.314(b)(1), (b)(2), (h)(1), and 170.315(h)(2).
		if (this.criteriaMet.containsKey("170.314 (b)(1)") && this.criteriaMet.containsKey("170.314 (b)(2)")
			&& this.criteriaMet.containsKey("170.314 (h)(1)") && this.criteriaMet.containsKey("170.315 (h)(2)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 4);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 4);
			return true;
		}
		
		// (viii) 45 CFR 170.314(b)(1), (b)(2), (b)(8), and 170.315(h)(2).
		if (this.criteriaMet.containsKey("170.314 (b)(1)") && this.criteriaMet.containsKey("170.314 (b)(2)")
			&& this.criteriaMet.containsKey("170.314 (b)(8)") && this.criteriaMet.containsKey("170.315 (h)(2)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 4);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 4);
			return true;
		}

		// (ix) 45 CFR 170.314(b)(1), (b)(2), (b)(8), (h)(1), and 170.315(h)(2).
		if (this.criteriaMet.containsKey("170.314 (b)(1)") && this.criteriaMet.containsKey("170.314 (b)(2)")
			&& this.criteriaMet.containsKey("170.314 (b)(8)") && this.criteriaMet.containsKey("170.314 (h)(1)") 
			&& this.criteriaMet.containsKey("170.315 (h)(2)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 5);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 5);
			return true;
		}

		// (x) 45 CFR 170.314(b)(8), (h)(1), and 170.315(h)(2).
		if (this.criteriaMet.containsKey("170.314 (b)(8)") && this.criteriaMet.containsKey("170.314 (h)(1)")
			&& this.criteriaMet.containsKey("170.315 (h)(2)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 3);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 3);
			return true;
		}
		
		// (xi) 45 CFR 170.314(b)(1), (b)(2), and 170.315(b)(1).
		if (this.criteriaMet.containsKey("170.314 (b)(1)") && this.criteriaMet.containsKey("170.314 (b)(2)")
			&& this.criteriaMet.containsKey("170.315 (b)(1)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 3);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 3);
			return true;
		}
		
		// (xii) 45 CFR 170.314(b)(1), (b)(2), (h)(1), and 170.315(b)(1). 
		if (this.criteriaMet.containsKey("170.314 (b)(1)") && this.criteriaMet.containsKey("170.314 (b)(2)")
			&& this.criteriaMet.containsKey("170.314 (h)(1)") && this.criteriaMet.containsKey("170.315 (b)(1)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 4);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 4);
			return true;
		}
		
		// (xiii) 45 CFR 170.314(b)(1), (b)(2), (b)(8), and 170.315(b)(1).
		if (this.criteriaMet.containsKey("170.314 (b)(1)") && this.criteriaMet.containsKey("170.314 (b)(2)")
			&& this.criteriaMet.containsKey("170.314 (b)(8)") && this.criteriaMet.containsKey("170.315 (b)(1)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 4);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 4);
			return true;
		}
		
		// (xiv) 45 CFR 170.314(b)(1), (b)(2),(b)(8), (h)(1), and 170.315(b)(1).
		if (this.criteriaMet.containsKey("170.314 (b)(1)") && this.criteriaMet.containsKey("170.314 (b)(2)")
			&& this.criteriaMet.containsKey("170.314 (b)(8)") && this.criteriaMet.containsKey("170.314 (h)(1)") 
			&& this.criteriaMet.containsKey("170.315 (b)(1)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 5);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 5);
			return true;
		}
		
		// (xv) 45 CFR 170.314(b)(8), (h)(1), and 170.315(b)(1).
		if (this.criteriaMet.containsKey("170.314 (b)(8)") && this.criteriaMet.containsKey("170.314 (h)(1)")
			&& this.criteriaMet.containsKey("170.315 (b)(1)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 3);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 3);
			return true;
		}
		
		// (xvi) 45 CFR 170.314(b)(1), (b)(2), (b)(8), (h)(1), 170.315(b)(1), and 170.315(h)(1).
		if (this.criteriaMet.containsKey("170.314 (b)(1)") && this.criteriaMet.containsKey("170.314 (b)(2)")
			&& this.criteriaMet.containsKey("170.314 (b)(8)") && this.criteriaMet.containsKey("170.314 (h)(1)") 
			&& this.criteriaMet.containsKey("170.315 (b)(1)") && this.criteriaMet.containsKey("170.315 (h)(1)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 6);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 6);
			return true;
		}
		
		// (xvii) 45 CFR 170.314(b)(1), (b)(2), (b)(8), (h)(1), 170.315(b)(1), and 170.315(h)(2).
		if (this.criteriaMet.containsKey("170.314 (b)(1)") && this.criteriaMet.containsKey("170.314 (b)(2)")
			&& this.criteriaMet.containsKey("170.314 (b)(8)") && this.criteriaMet.containsKey("170.314 (h)(1)") 
			&& this.criteriaMet.containsKey("170.315 (b)(1)") && this.criteriaMet.containsKey("170.315 (h)(2)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 6);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 6);
			return true;
		}
		
		// (xviii) 45 CFR 170.314(h)(1) and 170.315(b)(1).
		if (this.criteriaMet.containsKey("170.314 (h)(1)") && this.criteriaMet.containsKey("170.315 (b)(1)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 2);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 2);
			return true;
		}
		
		// (xix) 45 CFR 170.315(b)(1) and (h)(1).
		if (this.criteriaMet.containsKey("170.315 (b)(1)") && this.criteriaMet.containsKey("170.315 (h)(1)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 2);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 2);
			return true;
		}
		
		// (xx) 45 CFR 170.315(b)(1) and (h)(2).
		if (this.criteriaMet.containsKey("170.315 (b)(1)") && this.criteriaMet.containsKey("170.315 (h)(2)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 2);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 2);
			return true;
		}
		
		// (xxi) 45 CFR 170.315(b)(1), (h)(1), and (h)(2)
		if (this.criteriaMet.containsKey("170.315 (b)(1)") && this.criteriaMet.containsKey("170.315 (h)(1)")
			&& this.criteriaMet.containsKey("170.315 (h)(2)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 3);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 3);
			return true;
		}
		
		return false;
	}
	
	//**********************************************************************
	// isCriteriaCqmValid
	//
	// Must match at least one of the 3 sets.
	//**********************************************************************
	protected boolean isCriteriaCqmValid() {
		// (1) 45 CFR 170.314(c)(1) or 170.315(c)(1);
		if (this.criteriaMet.containsKey("170.314 (c)(1)") || this.criteriaMet.containsKey("170.315 (c)(1)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 1);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
			return true;
		}

		// (2) 45 CFR 170.314(c)(2) or 170.315(c)(2)
		if (this.criteriaMet.containsKey("170.314 (c)(2)") || this.criteriaMet.containsKey("170.315 (c)(2)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 1);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
			return true;
		}
		
		// (3) 45 CFR 170.314(c)(3) or 170.315(c)(3)
		if (this.criteriaMet.containsKey("170.314 (c)(3)") || this.criteriaMet.containsKey("170.315 (c)(3)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 1);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
			return true;
		}
		
		return false;
	}
	
	//**********************************************************************
	// isCriteriaPSValid
	//
	// Must match at least one of the 8 pairs.
	//**********************************************************************
	protected boolean isCriteriaPSValid() {
		// (1) 45 CFR 170.314(d)(1) or 170.315(d)(1);
		if (this.criteriaMet.containsKey("170.314 (d)(1)") || this.criteriaMet.containsKey("170.315 (d)(1)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 1);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
			return true;
		}
		
		// (2) 45 CFR 170.314(d)(2) or 170.315(d)(2);
		if (this.criteriaMet.containsKey("170.314 (d)(2)") || this.criteriaMet.containsKey("170.315 (d)(2)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 1);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
			return true;
		}

		// (3) 45 CFR 170.314(d)(3) or 170.315(d)(3);
		if (this.criteriaMet.containsKey("170.314 (d)(3)") || this.criteriaMet.containsKey("170.315 (d)(3)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 1);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
			return true;
		}

		// (4) 45 CFR 170.314(d)(4) or 170.315(d)(4);
		if (this.criteriaMet.containsKey("170.314 (d)(4)") || this.criteriaMet.containsKey("170.315 (d)(4)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 1);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
			return true;
		}

		// (5) 45 CFR 170.314(d)(5) or 170.315(d)(5);
		if (this.criteriaMet.containsKey("170.314 (d)(5)") || this.criteriaMet.containsKey("170.315 (d)(5)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 1);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
			return true;
		}

		// (6) 45 CFR 170.314(d)(6) or 170.315(d)(6);
		if (this.criteriaMet.containsKey("170.314 (d)(6)") || this.criteriaMet.containsKey("170.315 (d)(6)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 1);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
			return true;
		}

		// (7) 45 CFR 170.314(d)(7) or 170.315(d)(7);
		if (this.criteriaMet.containsKey("170.314 (d)(7)") || this.criteriaMet.containsKey("170.315 (d)(7)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 1);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
			return true;
		}

		// (8) 45 CFR 170.314(d)(8) or 170.315(d)(8);
		if (this.criteriaMet.containsKey("170.314 (d)(8)") || this.criteriaMet.containsKey("170.315 (d)(8)")) {
			this.counts.put("criteriaRequired", this.counts.get("criteriaRequired") + 1);
			this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
			return true;
		}
		
		return false;
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
}
