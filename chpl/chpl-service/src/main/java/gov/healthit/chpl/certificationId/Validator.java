package gov.healthit.chpl.certificationId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CQMMetDTO;

public abstract class Validator {
    protected Map<CertificationCriterion, Integer> criteriaMet = new HashMap<CertificationCriterion, Integer>(100);
    protected Map<String, Integer> cqmsMet = new HashMap<String, Integer>(100);
    protected Map<String, Integer> domainsMet = new HashMap<String, Integer>(10);
    protected SortedSet<Integer> editionYears = new TreeSet<Integer>();
    protected String attestationYear = null;

    // missing criteria where all in the set are required
    protected ArrayList<String> missingAnd = new ArrayList<String>();
    // missing 1 criteria from each of the following sets
    protected List<ArrayList<String>> missingOr = new ArrayList<ArrayList<String>>();
    // missing at least one of the following combinations of criteria
    protected List<ArrayList<String>> missingCombo = new ArrayList<ArrayList<String>>();
    // missing X criteria from the OR list of criteria
    protected List<TreeMap<String, ArrayList<String>>> missingXOr = new ArrayList<TreeMap<String, ArrayList<String>>>();

    protected Map<String, Integer> percents = new HashMap<String, Integer>();
    protected Map<String, Integer> counts = new HashMap<String, Integer>();
    protected boolean valid = false;

    public Map<String, Integer> getCounts() {
        return this.counts;
    }

    public Map<String, Integer> getPercents() {
        return this.percents;
    }

    public Map<CertificationCriterion, Integer> getCriteriaMet() {
        return this.criteriaMet;
    }

    public Map<String, Integer> getCqmsMet() {
        return this.cqmsMet;
    }

    public ArrayList<String> getMissingAnd() {
        return missingAnd;
    }

    public List<ArrayList<String>> getMissingOr() {
        return missingOr;
    }

    public List<ArrayList<String>> getMissingCombo() {
        return missingCombo;
    }

    public List<TreeMap<String, ArrayList<String>>> getMissingXOr() {
        return missingXOr;
    }

    public Map<String, Integer> getDomainsMet() {
        return this.domainsMet;
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

    // **********************************************************************
    // validate
    //
    // **********************************************************************
    public boolean validate(List<CertificationCriterion> certDtos, List<CQMMetDTO> cqmDtos, List<Integer> years) {
        this.collectMetData(certDtos, cqmDtos, years);
        this.attestationYear = Validator.calculateAttestationYear(this.editionYears);
        this.valid = this.onValidate();
        this.calculatePercentages();
        return this.isValid();
    }

    // **********************************************************************
    // collectMetData
    //
    // **********************************************************************
    protected void collectMetData(List<CertificationCriterion> criteria, List<CQMMetDTO> cqmDtos, List<Integer> years) {

        // Collect the certification years
        editionYears.addAll(years);

        // Collect criteria met
        if (null != criteria) {
            criteriaMet = new HashMap<CertificationCriterion, Integer>(criteria.size());
            for (CertificationCriterion certDetail : criteria) {
                criteriaMet.put(certDetail, 1);
            }
        }

        // Collect cqms and domains met
        if (null != cqmDtos) {
            cqmsMet = new HashMap<String, Integer>(cqmDtos.size());
            for (CQMMetDTO cqmDetail : cqmDtos) {
                // See what version we've already met...
                Integer verMet = cqmsMet.get(cqmDetail.getCmsId());
                if (null == verMet) {
                    verMet = Integer.valueOf(0);
                }

                // ...store the version that's higher.
                Integer ver = Integer.parseInt(cqmDetail.getVersion().substring(1));
                if (ver > verMet) {
                    cqmsMet.put(cqmDetail.getCmsId(), ver);
                }

                if (null != cqmDetail.getDomain()) {
                    domainsMet.put(cqmDetail.getDomain(), 1);
                }
            }
        }

    }

    // **********************************************************************
    // calculatePercentages
    //
    // **********************************************************************
    protected void calculatePercentages() {
        this.percents.put(
                "criteriaMet",
                (0 == this.counts.get("criteriaRequired")) ? 0 : Math.min(
                        (int) Math.floor((this.counts.get("criteriaRequiredMet") * 100.0)
                                / this.counts.get("criteriaRequired")), 100));
        this.percents.put(
                "cqmDomains",
                (0 == this.counts.get("domainsRequired")) ? 0 : Math.min(
                        (int) Math.floor((this.counts.get("domainsRequiredMet") * 100.0)
                                / this.counts.get("domainsRequired")), 100));
        this.percents.put(
                "cqmsInpatient",
                (0 == this.counts.get("cqmsInpatientRequired")) ? 0 : Math.min(
                        (int) Math.floor((this.counts.get("cqmsInpatientRequiredMet") * 100.0)
                                / this.counts.get("cqmsInpatientRequired")), 100));
        this.percents
                .put("cqmsAmbulatory",
                        (0 == this.counts.get("cqmsAmbulatoryRequired") + this.counts.get("cqmsAmbulatoryCoreRequired")) ? 0
                                : Math.min(
                                        (int) Math.floor(((this.counts.get("cqmsAmbulatoryCoreRequiredMet") + Math.min(
                                                this.counts.get("cqmsAmbulatoryRequiredMet"),
                                                this.counts.get("cqmsAmbulatoryRequired"))) / (double) (this.counts
                                                .get("cqmsAmbulatoryRequired") + this.counts
                                                .get("cqmsAmbulatoryCoreRequired"))) * 100.0), 100));
    }

    // **********************************************************************
    // calculateAttestationYear
    //
    // **********************************************************************
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

    protected Boolean criteriaMetContainsCriterion(String criterion) {
        Boolean found = false;
        for (CertificationCriterion cert : criteriaMet.keySet()) {
            if (cert.getNumber().equalsIgnoreCase(criterion)) {
                found = true;
            }
        }
        return found;
    }
}
