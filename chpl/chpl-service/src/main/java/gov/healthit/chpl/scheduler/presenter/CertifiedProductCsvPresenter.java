package gov.healthit.chpl.scheduler.presenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.compliance.DirectReviewNonConformity;
import gov.healthit.chpl.entity.CertificationStatusType;

public class CertifiedProductCsvPresenter implements CertifiedProductPresenter, AutoCloseable {
    private static final String OPEN_STATUS = "open";
    private static final String UNKNOWN_VALUE = "?";

    private Logger logger;
    private List<CertificationCriterion> applicableCriteria = new ArrayList<CertificationCriterion>();
    private OutputStreamWriter writer = null;
    private CSVPrinter csvPrinter = null;

    /**
     * Required to setCriteriaNames before calling this function.
     */
    @Override
    public void open(final File file) throws IOException {
        getLogger().info("Opening file, initializing CSV doc.");
        writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        writer.write('\ufeff');
        csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL);
        csvPrinter.printRecord(generateHeaderValues());
        csvPrinter.flush();
    }

    @Override
    public synchronized void add(final CertifiedProductSearchDetails data) throws IOException {
        getLogger().info("Adding CP to CSV file: " + data.getId());
        List<String> rowValue = generateRowValue(data);
        if (rowValue != null) { // a subclass could return null to skip a row
            csvPrinter.printRecord(rowValue);
            csvPrinter.flush();
        }
    }

    @Override
    public void close() throws IOException {
        getLogger().info("Closing the CSV file.");
        csvPrinter.close();
        writer.close();
    }

    public void setLogger(final Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = LogManager.getLogger(CertifiedProductCsvPresenter.class);
        }
        return logger;
    }

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
        result.add("Real World Testing Plans URL");
        result.add("Real World Testing Results URL");
        result.add("Total Surveillance Activities");
        result.add("Total Surveillance Non-conformities");
        result.add("Open Surveillance Non-conformities");
        result.add("Total Direct Review Activities");
        result.add("Total Direct Review Non-conformities");
        result.add("Open Direct Review Non-conformities");

        if (applicableCriteria != null) {
            for (CertificationCriterion criteria : applicableCriteria) {
                result.add(criteria.getNumber() + ": " + criteria.getTitle());
            }
        }
        return result;
    }

    @SuppressWarnings("checkstyle:linelength")
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
                ? ""
                : listing.getDeveloper().getWebsite());
        result.add(formatSelfDeveloper(listing));
        result.addAll(getContactCells(listing));
        result.add(listing.getProduct() != null ? listing.getProduct().getName() : UNKNOWN_VALUE);
        result.add(ObjectUtils.allNotNull(listing.getProduct(), listing.getProduct().getId())
                ? listing.getProduct().getId().toString() : UNKNOWN_VALUE);
        result.add(listing.getVersion() != null ? listing.getVersion().getVersion() : UNKNOWN_VALUE);
        result.add(ObjectUtils.allNotNull(listing.getVersion(), listing.getVersion().getId())
                ? listing.getVersion().getId().toString() : UNKNOWN_VALUE);
        result.add(listing.getRwtPlansUrl());
        result.add(listing.getRwtResultsUrl());
        result.add(listing.getCountSurveillance() != null ? listing.getCountSurveillance().toString() : UNKNOWN_VALUE);
        result.add(ObjectUtils.allNotNull(listing.getCountOpenNonconformities(), listing.getCountClosedNonconformities())
                ? (listing.getCountOpenNonconformities() + listing.getCountClosedNonconformities()) + "" : UNKNOWN_VALUE);
        result.add(listing.getCountOpenNonconformities() != null ? listing.getCountOpenNonconformities().toString() : UNKNOWN_VALUE);
        result.add(listing.getDirectReviews() != null ? listing.getDirectReviews().size() + "" : UNKNOWN_VALUE);
        result.add(getCountOfDirectReviewNonconformitiesForListing(listing) + "");
        result.add(getCountOfOpenDirectReviewNonconformitiesForListing(listing) + "");
        List<String> criteria = generateCriteriaValues(listing);
        result.addAll(criteria);
        return result;
    }

    protected List<String> generateCriteriaValues(final CertifiedProductSearchDetails data) {
        List<String> result = new ArrayList<String>();

        for (CertificationCriterion criteria : applicableCriteria) {
            boolean criteriaMatch = false;
            for (int i = 0; i < data.getCertificationResults().size() && !criteriaMatch; i++) {
                CertificationResult currCriteria = data.getCertificationResults().get(i);
                if (currCriteria.getCriterion().getId().equals(criteria.getId())) {
                    criteriaMatch = true;
                    result.add(currCriteria.isSuccess().toString());
                }
            }
            if (!criteriaMatch) {
                result.add(Boolean.FALSE.toString());
            }
        }
        return result;
    }

    protected String formatEdition(CertifiedProductSearchDetails listing) {
        if (listing.getEdition() == null) {
            return "";
        } else {
            String edition = listing.getEdition().getName();
            if (listing.getCuresUpdate() != null && listing.getCuresUpdate()) {
                edition = edition + CertificationEdition.CURES_SUFFIX;
            }
            return edition;
        }
    }

    protected String formatInactiveDate(CertifiedProductSearchDetails listing) {
        if (listing.getDecertificationDate() != null && listing.getCurrentStatus().getStatus().getName()
                    .equals(CertificationStatusType.WithdrawnByDeveloper.getName())) {
            return formatDate(listing.getDecertificationDate());
        }
        return "";
    }

    protected String formatDecertificationDate(CertifiedProductSearchDetails listing) {
        if (listing.getDecertificationDate() != null
                && (listing.getCurrentStatus().getStatus().getName()
                        .equals(CertificationStatusType.WithdrawnByDeveloperUnderReview.getName())
                    || listing.getCurrentStatus().getStatus().getName()
                        .equals(CertificationStatusType.WithdrawnByAcb.getName())
                    || listing.getCurrentStatus().getStatus().getName()
                        .equals(CertificationStatusType.TerminatedByOnc.getName()))) {
                return formatDate(listing.getDecertificationDate());
        }
        return "";
    }

    protected String formatSelfDeveloper(CertifiedProductSearchDetails listing) {
        if (ObjectUtils.allNotNull(listing.getDeveloper(), listing.getDeveloper().getSelfDeveloper())) {
            return BooleanUtils.isTrue(listing.getDeveloper().getSelfDeveloper()) ? "Yes" : "No";
        }
        return UNKNOWN_VALUE;
    }

    protected String formatDate(Long dateInMillis) {
        LocalDateTime localDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateInMillis), ZoneId.systemDefault());
        return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate);
    }

    private long getCountOfDirectReviewNonconformitiesForListing(CertifiedProductSearchDetails listing) {
        long count = 0;
        if (listing.getDirectReviews() != null) {
            count = listing.getDirectReviews().stream()
                    .flatMap(dr -> dr.getNonConformities().stream())
                    .filter(nc -> isNonconformityAssociatedWithListing(listing, nc))
                    .count();
        }
        return count;
    }

    private long getCountOfOpenDirectReviewNonconformitiesForListing(CertifiedProductSearchDetails listing) {
        long count = 0;
        count = listing.getDirectReviews().stream()
                .filter(dr -> dr.getNonConformities() != null && dr.getNonConformities().size() > 0)
                .flatMap(dr -> dr.getNonConformities().stream())
                .filter(nc -> isNonconformityAssociatedWithListing(listing, nc))
                .filter(nc -> nc.getNonConformityStatus() != null && nc.getNonConformityStatus().equalsIgnoreCase(OPEN_STATUS))
                .count();
        return count;
    }

    private boolean isNonconformityAssociatedWithListing(CertifiedProductSearchDetails listing, DirectReviewNonConformity nc) {
        if (nc == null || nc.getDeveloperAssociatedListings() == null
                || nc.getDeveloperAssociatedListings().size() == 0) {
            return false;
        }

        return nc.getDeveloperAssociatedListings().stream()
                .filter(dal -> dal.getId() != null && dal.getId().equals(listing.getId()))
                .findAny().isPresent();
    }

    protected List<String> getDeveloperAddressCells(CertifiedProductSearchDetails listing) {
        List<String> result = new ArrayList<String>();
        if (listing.getDeveloper().getAddress() != null) {
            if (!StringUtils.isEmpty(listing.getDeveloper().getAddress().getLine1())
                    && !StringUtils.isEmpty(listing.getDeveloper().getAddress().getLine2())) {
                result.add(listing.getDeveloper().getAddress().getLine1()
                        + ", " + listing.getDeveloper().getAddress().getLine2());
            } else {
                result.add(listing.getDeveloper().getAddress().getLine1() == null
                        ? ""
                        : listing.getDeveloper().getAddress().getLine1());
            }
            result.add(listing.getDeveloper().getAddress().getCity() == null
                    ? ""
                    : listing.getDeveloper().getAddress().getCity());
            result.add(listing.getDeveloper().getAddress().getState() == null
                    ? ""
                    : listing.getDeveloper().getAddress().getState());
            result.add(listing.getDeveloper().getAddress().getZipcode() == null
                    ? ""
                    : listing.getDeveloper().getAddress().getZipcode());
        } else {
            result.add("");
            result.add("");
            result.add("");
            result.add("");
        }
        return result;
    }

    protected List<String> getContactCells(CertifiedProductSearchDetails listing) {
        List<String> result = new ArrayList<String>();
        if (listing.getProduct().getContact() != null) {
            result.add(listing.getProduct().getContact().getFullName() == null
                    ? ""
                    : listing.getProduct().getContact().getFullName());
            result.add(listing.getProduct().getContact().getEmail() == null
                    ? ""
                    : listing.getProduct().getContact().getEmail());
            result.add(listing.getProduct().getContact().getPhoneNumber() == null
                    ? ""
                    : listing.getProduct().getContact().getPhoneNumber());
        } else if (listing.getDeveloper().getContact() != null) {
            result.add(listing.getDeveloper().getContact().getFullName() == null
                    ? ""
                    : listing.getDeveloper().getContact().getFullName());
            result.add(listing.getDeveloper().getContact().getEmail() == null
                    ? ""
                    : listing.getDeveloper().getContact().getEmail());
            result.add(listing.getDeveloper().getContact().getPhoneNumber() == null
                    ? ""
                    : listing.getDeveloper().getContact().getPhoneNumber());
        } else {
            result.add("");
            result.add("");
            result.add("");
        }
        return result;
    }

    public List<CertificationCriterion> getApplicableCriteria() {
        return applicableCriteria;
    }

    public void setApplicableCriteria(final List<CertificationCriterion> applicableCriteria) {
        this.applicableCriteria = applicableCriteria;
    }
}
