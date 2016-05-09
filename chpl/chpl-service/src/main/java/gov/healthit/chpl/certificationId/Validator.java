package gov.healthit.chpl.certificationId;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.List;
import java.util.Map;

public abstract class Validator {

	protected Map<String, Long> criteriaMet = new HashMap<String, Long>();
	protected Map<String, Long> cqmsMet = new HashMap<String, Long>();
	protected Map<String, Long> domainsMet = new HashMap<String, Long>();
	protected SortedSet<Integer> editionYears = new TreeSet<Integer>();
	protected String attestationYear = null;

	protected Map<String, Integer> percents = new HashMap<String, Integer>();
	protected Map<String, Integer> counts = new HashMap<String, Integer>();
	protected boolean valid = false;
	
	public Map<String, Integer> getCounts() {
		return this.counts;
	}

	public Map<String, Integer> getPercents() {
		return this.percents;
	}
	
	public String getAttestationYear() {
		return this.attestationYear;
	}
	
	public boolean isValid() {
		return this.valid;
	}
	
	protected abstract boolean onValidate();
	protected abstract boolean isCriteriaValid();
	protected abstract boolean isCqmsValid();
	protected abstract boolean isDomainsValid();

	//**********************************************************************
	// validate
	//
	//**********************************************************************
	public boolean validate(List<CertifiedProductDetailsDTO> productDtos) {
		this.collectMetData(productDtos);
		this.attestationYear = Validator.calculateAttestationYear(this.editionYears);
		this.valid = this.onValidate();
		this.calculatePercentages();
		return this.isValid();
	}
	
	//**********************************************************************
	// collectMetData
	//
	//**********************************************************************
	protected void collectMetData(List<CertifiedProductDetailsDTO> productDtos) {
		for (CertifiedProductDetailsDTO dto : productDtos) {

			// Collect the certification years
			editionYears.add(new Integer(dto.getYear()));
			
			// Collect criteria met
			for (CertificationResultDetailsDTO certDetail : dto.getCertResults()) {
				if (certDetail.getSuccess()) {
					criteriaMet.put(certDetail.getNumber(), 1L);
				}
			}

			// Collect cqms and domains met
			for (CQMResultDetailsDTO cqmDetail : dto.getCqmResults()) {
				if (cqmDetail.getSuccess()) {
					cqmsMet.put(cqmDetail.getCmsId(), 1L);
					if (null != cqmDetail.getDomain()) {
						domainsMet.put(cqmDetail.getDomain(), 1L);
					}
				}
			}
			
		}
	}
	
	//**********************************************************************
	// calculatePercentages
	//
	//**********************************************************************
	protected void calculatePercentages() {
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
			(0 == this.counts.get("cqmsAmbulatoryRequired") + this.counts.get("cqmsAmbulatoryCoreRequired")) ? 0 :
				Math.min(
				(int)Math.floor(
					(
						(
							this.counts.get("cqmsAmbulatoryCoreRequiredMet") + 
							Math.min(this.counts.get("cqmsAmbulatoryRequiredMet"), this.counts.get("cqmsAmbulatoryRequired"))
						)
						/ (double)(this.counts.get("cqmsAmbulatoryRequired") + this.counts.get("cqmsAmbulatoryCoreRequired"))
					) * 100.0
				),
				100)
		);
	}

	//**********************************************************************
	// calculateAttestationYear
	//
	//**********************************************************************
	public static String calculateAttestationYear(SortedSet<Integer> editionYears) {
		String attYearString = null;
		
		if ((null != editionYears) && (editionYears.size() > 0)) {
			
			// Get the lowest year...
			attYearString = editionYears.first().toString();

			// ...if there are two years then we have a hybrid
			// so add the second year.
			if (editionYears.size() > 1) {
				attYearString += "/" + editionYears.last().toString();
			}
		}
		
		return attYearString;
	}
	
}
