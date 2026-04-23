package gov.healthit.chpl.certificationId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;

public abstract class Validator {
    private Map<CertificationCriterion, Integer> criteriaMet = new HashMap<CertificationCriterion, Integer>();
    private Map<String, Integer> cqmsMet = new HashMap<String, Integer>();
    private Map<String, Integer> domainsMet = new HashMap<String, Integer>();

    // missing criteria where all in the set are required
    private ArrayList<String> missingAnd = new ArrayList<String>();
    // missing 1 criteria from each of the following sets
    private List<ArrayList<String>> missingOr = new ArrayList<ArrayList<String>>();
    // missing at least one of the following combinations of criteria
    private List<ArrayList<String>> missingCombo = new ArrayList<ArrayList<String>>();
    // missing X criteria from the OR list of criteria
    private List<TreeMap<String, ArrayList<String>>> missingXOr = new ArrayList<TreeMap<String, ArrayList<String>>>();

    private CertificationIdMetPercentages percents = new CertificationIdMetPercentages();
    private CertificationIdRequirements counts = new CertificationIdRequirements();
    private boolean valid = false;

    public CertificationIdRequirements getCounts() {
        return this.counts;
    }

    public CertificationIdMetPercentages getPercents() {
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

    public boolean isValid() {
        return this.valid;
    }

    protected abstract boolean onValidate();

    protected abstract boolean isCriteriaValid();

    protected abstract boolean isCqmsValid();

    protected abstract boolean isDomainsValid();

    public boolean validate(List<CertificationCriterion> certDtos, List<CQMMetDTO> cqmDtos) {
        this.collectMetData(certDtos, cqmDtos);
        this.valid = this.onValidate();
        this.calculatePercentages();
        return this.isValid();
    }

    protected void collectMetData(List<CertificationCriterion> certDtos, List<CQMMetDTO> cqmDtos) {

        // Collect criteria met
        if (null != certDtos) {
            criteriaMet = new HashMap<CertificationCriterion, Integer>(certDtos.size());
            for (CertificationCriterion certDetail : certDtos) {
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

    protected void calculatePercentages() {
        this.percents.setCriteriaMet(this.counts.getCriteriaRequired() == 0
                ? 0
                : Math.min((int) Math.floor((this.counts.getCriteriaRequiredMet() * 100.0) / this.counts.getCriteriaRequired()), 100));
        this.percents.setCqmDomains(this.counts.getDomainsRequired() == 0
                ? 0
                : Math.min((int) Math.floor((this.counts.getDomainsRequiredMet() * 100.0) / this.counts.getDomainsRequired()), 100));
        this.percents.setCqmsInpatient(this.counts.getCqmsInpatientRequired() == 0
                ? 0
                : Math.min((int) Math.floor((this.counts.getCqmsInpatientRequiredMet() * 100.0) / this.counts.getCqmsInpatientRequired()), 100));

        this.percents.setCqmsAmbulatory(this.counts.getCqmsAmbulatoryRequired() + this.counts.getCqmsAmbulatoryCoreRequired() == 0
                ? 0
                : Math.min(
                        (int) Math.floor((this.counts.getCqmsAmbulatoryCoreRequiredMet()
                                        + Math.min(this.counts.getCqmsAmbulatoryRequiredMet(), (this.counts.getCqmsAmbulatoryRequired()))
                                / (double) (this.counts.getCqmsAmbulatoryRequired() + this.counts.getCqmsAmbulatoryCoreRequired())) * 100.0), 100));
    }

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

    protected Boolean criteriaMetContainsCriterion(CertificationCriterion criterion) {
        return criteriaMet.keySet().stream()
            .filter(cert -> cert.getId().equals(criterion.getId()))
            .findAny().isPresent();
    }
}
