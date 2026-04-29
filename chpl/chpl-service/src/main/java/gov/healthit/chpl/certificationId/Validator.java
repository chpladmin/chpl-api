package gov.healthit.chpl.certificationId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.cqm.CQMResultDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public abstract class Validator {
    private Set<CertificationCriterion> criteriaMet = new LinkedHashSet<CertificationCriterion>();
    private Map<String, Integer> cqmsMet = new HashMap<String, Integer>();
    private Map<String, Integer> domainsMet = new HashMap<String, Integer>();
    private List<CertifiedProductSearchDetails> listings = new ArrayList<CertifiedProductSearchDetails>();

    // missing criteria where all in the set are required
    private ArrayList<String> missingAnd = new ArrayList<String>();
    // criteria are present but not up-to-date
    private ArrayList<String> missingUpToDate = new ArrayList<String>();
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

    public Set<CertificationCriterion> getCriteriaMet() {
        return this.criteriaMet;
    }

    public List<CertifiedProductSearchDetails> getListings() {
        return this.listings;
    }

    public Map<String, Integer> getCqmsMet() {
        return this.cqmsMet;
    }

    public ArrayList<String> getMissingAnd() {
        return missingAnd;
    }

    public ArrayList<String> getMissingUpToDate() {
        return missingUpToDate;
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

    public boolean validate() {
        this.collectMetData();
        this.valid = this.onValidate();
        this.calculatePercentages();
        return this.isValid();
    }

    protected void collectMetData() {
        // Collect criteria met
        if (!CollectionUtils.isEmpty(listings)) {
            criteriaMet = listings.stream()
                    .flatMap(listing -> listing.getCertificationResults().stream())
                    .filter(certResult -> BooleanUtils.isTrue(certResult.getSuccess()))
                    .map(certResult -> certResult.getCriterion())
                    .collect(Collectors.toSet());
        }

        // Collect cqms and domains met
        if (!CollectionUtils.isEmpty(listings)) {
            List<CQMResultDetails> attestedCqms = listings.stream()
                .flatMap(listing -> listing.getCqmResults().stream())
                .filter(cqmResult -> cqmResult.getSuccess() || !CollectionUtils.isEmpty(cqmResult.getSuccessVersions()))
                .collect(Collectors.toList());

            cqmsMet = new HashMap<String, Integer>(attestedCqms.size());
            for (CQMResultDetails cqmDetail : attestedCqms) {
                // Store the attested versions
                Integer highestVersion = cqmDetail.getSuccessVersions().stream()
                    .map(ver -> Integer.parseInt(ver.substring(1)))
                    .max(Integer::compareTo)
                    .orElse(null);

                cqmsMet.put(cqmDetail.getCmsId(), highestVersion);

                if (!StringUtils.isEmpty(cqmDetail.getDomain())) {
                    domainsMet.put(cqmDetail.getDomain(), 1);
                }
            }
        }
    }

    protected void calculatePercentages() {
        this.percents.setCriteriaMet(this.counts.getCriteriaRequired() == 0
                ? 0
                : Math.min((int) Math.floor((this.counts.getCriteriaRequiredMet() * 100.0) / this.counts.getCriteriaRequired()), 100));
        this.percents.setCriteriaUpToDate(this.counts.getCriteriaUpToDateRequired() == 0
                ? 0
                : Math.min((int) Math.floor((this.counts.getCriteriaUpToDateMet() * 100.0) / this.counts.getCriteriaUpToDateRequired()), 100));
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
        return criteriaMet.stream()
                .filter(cert -> cert.getNumber().equals(criterion))
                .findAny().isPresent();
    }

    protected Boolean criteriaMetContainsCriterion(CertificationCriterion criterion) {
        return criteriaMet.stream()
            .filter(cert -> cert.getId().equals(criterion.getId()))
            .findAny().isPresent();
    }
}
