package gov.healthit.chpl.scheduler.presenter;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public class CertifiedProduct2014CsvPresenter extends CertifiedProductCsvPresenter {
    @Override
    protected List<String> generateHeaderValues() {
        List<String> result = new ArrayList<String>();
        result.add("Certification Edition");
        result.add("CHPL ID");
        result.add("Listing Database ID");
        result.add("ONC-ACB Certification ID");
        result.add("Certification Date");
        result.add("Inactive As Of Date");
        result.add("Decertified As Of Date");
        result.add("Certification Status");
        result.add("ACB Name");
        result.add("Previous ACB Name");
        result.add("Developer Name");
        result.add("Developer Database ID");
        result.add("Vendor Street Address");
        result.add("Vendor City");
        result.add("Vendor State");
        result.add("Vendor Zip Code");
        result.add("Vendor Website");
        result.add("Self-developer");
        result.add("Vendor Contact Name");
        result.add("Vendor Contact Email");
        result.add("Vendor Contact Phone");
        result.add("Product Name");
        result.add("Product Database ID");
        result.add("Version");
        result.add("Version Database ID");
        result.add("Practice Type");
        result.add("Total Surveillance Activities");
        result.add("Total Surveillance Non-conformities");
        result.add("Open Surveillance Non-conformities");
        if (getApplicableCriteria() != null) {
            for (CertificationCriterion criteria : getApplicableCriteria()) {
                result.add(criteria.getNumber() + ": " + criteria.getTitle());
            }
        }
        return result;
    }

    @Override
    protected List<String> generateRowValue(CertifiedProductSearchDetails listing) {
        List<String> result = new ArrayList<String>();
        result.add(formatEdition(listing));
        result.add(listing.getChplProductNumber());
        result.add(listing.getId().toString());
        result.add(listing.getAcbCertificationId());
        result.add(formatDate(listing.getCertificationDate()));
        result.add(formatInactiveDate(listing));
        result.add(formatDecertificationDate(listing));
        result.add(listing.getCurrentStatus().getStatus().getName());
        result.add(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString());
        result.add(listing.getOtherAcb());
        result.add(listing.getDeveloper().getName());
        result.add(listing.getDeveloper().getId().toString());
        result.addAll(getDeveloperAddressCells(listing));
        result.add(listing.getDeveloper().getWebsite() == null
                ? "" : listing.getDeveloper().getWebsite());
        result.add(formatSelfDeveloper(listing));
        result.addAll(getContactCells(listing));
        result.add(listing.getProduct().getName());
        result.add(listing.getProduct().getId().toString());
        result.add(listing.getVersion().getVersion());
        result.add(listing.getVersion().getId().toString());
        result.add(listing.getPracticeType().get("name").toString());
        result.add(listing.getCountSurveillance().toString());
        result.add((listing.getCountOpenNonconformities() + listing.getCountClosedNonconformities()) + "");
        result.add(listing.getCountOpenNonconformities().toString());
        List<String> criteria = generateCriteriaValues(listing);
        result.addAll(criteria);
        return result;
    }
}
