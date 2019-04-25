package gov.healthit.chpl.scheduler.presenter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;

/**
 * Specific extension for 2014 CSV version of file.
 *
 * @author alarned
 *
 */
public class CertifiedProduct2014CsvPresenter extends CertifiedProductCsvPresenter {
    @Override
    protected List<String> generateHeaderValues() {
        List<String> result = new ArrayList<String>();
        result.add("Certification Edition");
        result.add("CHPL ID");
        result.add("ONC-ACB Certification ID");
        result.add("Certification Date");
        result.add("Certification Status");
        result.add("ACB Name");
        result.add("Previous ACB Name");
        result.add("Developer Name");
        result.add("Vendor Street Address");
        result.add("Vendor City");
        result.add("Vendor State");
        result.add("Vendor Zip Code");
        result.add("Vendor Website");
        result.add("Vendor Contact Name");
        result.add("Vendor Contact Email");
        result.add("Vendor Contact Phone");
        result.add("Product Name");
        result.add("Version");
        result.add("Practice Type");
        result.add("Total Surveillance Activities");
        result.add("Total Nonconformities");
        result.add("Open Nonconformities");
        if (getApplicableCriteria() != null) {
            for (CertificationCriterionDTO criteria : getApplicableCriteria()) {
                result.add(criteria.getNumber());
            }
        }
        return result;
    }

    @Override
    protected List<String> generateRowValue(final CertifiedProductSearchDetails data) {
        List<String> result = new ArrayList<String>();
        result.add(data.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString());
        result.add(data.getChplProductNumber());
        result.add(data.getAcbCertificationId());
        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(data.getCertificationDate()),
                ZoneId.systemDefault());
        result.add(DateTimeFormatter.ISO_LOCAL_DATE.format(date));
        result.add(data.getCurrentStatus().getStatus().getName());
        result.add(data.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString());
        result.add(data.getOtherAcb());
        result.add(data.getDeveloper().getName());
        if (data.getDeveloper().getAddress() != null) {
            if (data.getDeveloper().getAddress().getLine1() != null
                    && data.getDeveloper().getAddress().getLine2() != null) {
                result.add(data.getDeveloper().getAddress().getLine1()
                        + data.getDeveloper().getAddress().getLine2());
            } else {
                result.add(data.getDeveloper().getAddress().getLine1() == null
                        ? "" : data.getDeveloper().getAddress().getLine1());
            }
            result.add(data.getDeveloper().getAddress().getCity() == null
                    ? "" : data.getDeveloper().getAddress().getCity());
            result.add(data.getDeveloper().getAddress().getState() == null
                    ? "" : data.getDeveloper().getAddress().getState());
            result.add(data.getDeveloper().getAddress().getZipcode() == null
                    ? "" : data.getDeveloper().getAddress().getZipcode());
        } else {
            result.add("");
            result.add("");
            result.add("");
            result.add("");
        }
        result.add(data.getDeveloper().getWebsite() == null
                ? "" : data.getDeveloper().getWebsite());
        if (data.getProduct().getContact() != null) {
            result.add(data.getProduct().getContact().getFullName() == null
                    ? "" : data.getProduct().getContact().getFullName());
            result.add(data.getProduct().getContact().getEmail() == null
                    ? "" : data.getProduct().getContact().getEmail());
            result.add(data.getProduct().getContact().getPhoneNumber() == null
                    ? "" : data.getProduct().getContact().getPhoneNumber());
        } else if (data.getDeveloper().getContact() != null) {
            result.add(data.getDeveloper().getContact().getFullName() == null
                    ? "" : data.getDeveloper().getContact().getFullName());
            result.add(data.getDeveloper().getContact().getEmail() == null
                    ? "" : data.getDeveloper().getContact().getEmail());
            result.add(data.getDeveloper().getContact().getPhoneNumber() == null
                    ? "" : data.getDeveloper().getContact().getPhoneNumber());
        } else {
            result.add("");
            result.add("");
            result.add("");
        }
        result.add(data.getProduct().getName());
        result.add(data.getVersion().getVersion());
        result.add(data.getPracticeType().get("name").toString());
        result.add(data.getCountSurveillance().toString());
        result.add((data.getCountOpenNonconformities() + data.getCountClosedNonconformities()) + "");
        result.add(data.getCountOpenNonconformities().toString());
        List<String> criteria = generateCriteriaValues(data);
        result.addAll(criteria);
        return result;
    }
}
