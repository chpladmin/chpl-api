package gov.healthit.chpl.scheduler.presenter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.CertificationStatusType;

public class ListingsWithCriterionCSVPresenter {
    private static final String UNKNOWN_VALUE = "?";

    private Logger logger;
    private CertificationCriterion criterion;

    public ListingsWithCriterionCSVPresenter(CertificationCriterion criterion) {
        this.criterion = criterion;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = LogManager.getLogger(CertifiedProductXmlPresenter.class);
        }
        return logger;
    }

    public void presentAsFile(File file, List<CertifiedProductSearchDetails> listings) {
        try (FileWriter writer = new FileWriter(file);
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
            csvPrinter.printRecord(getHeaderValues());
            for (CertifiedProductSearchDetails listing : listings) {
                CertificationResult relevantCertResult = listing.getCertificationResults().stream()
                        .filter(certResult -> certResult.getCriterion().getId().equals(this.criterion.getId()))
                        .findAny().get();
                List<List<String>> rowValues = generateMultiRowValue(listing, relevantCertResult);
                for (List<String> rowValue : rowValues) {
                    csvPrinter.printRecord(rowValue);
                }
            }
        } catch (IOException ex) {
            getLogger().error("Could not write file " + file.getName(), ex);
        }
    }

    private List<String> getHeaderValues() {
        List<String> result = new ArrayList<String>();
        result.add("CHPL ID");
        result.add("ACB Name");
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
        result.add("Certification Date");
        result.add("Certification Status");
        result.add(this.criterion.getNumber() + ": " + this.criterion.getTitle());
        result.add("Privacy and Security Framework");
        result.add("Test Standards");
        result.add("Functionality Tested");
        result.add("Test Procedures");
        return result;
    }

    protected List<List<String>> generateMultiRowValue(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        List<List<String>> result = new ArrayList<List<String>>();
        List<String> certificationResultFirstRow = getSingleValuedCertificationResultData(listing, certResult);
        result.add(certificationResultFirstRow);

        if (!CollectionUtils.isEmpty(certResult.getTestStandards())) {
            for (int i = 0; i < certResult.getTestStandards().size(); i++) {
                CertificationResultTestStandard testStandard = certResult.getTestStandards().get(i);
                if (i == 0) {
                    certificationResultFirstRow.add(testStandard.getTestStandardName());
                } else if (result.size() > i) {
                    result.get(i).add(testStandard.getTestStandardName());
                } else {
                    List<String> newRow = getSingleValuedCertificationResultData(listing, certResult);
                    newRow.add(testStandard.getTestStandardName());
                    result.add(newRow);
                }
            }
        } else {
            certificationResultFirstRow.add("");
        }

        if (!CollectionUtils.isEmpty(certResult.getFunctionalitiesTested())) {
            for (int i = 0; i < certResult.getFunctionalitiesTested().size(); i++) {
                CertificationResultTestFunctionality testFunc = certResult.getFunctionalitiesTested().get(i);
                if (i == 0) {
                    certificationResultFirstRow.add(testFunc.getName());
                } else if (result.size() > i) {
                    result.get(i).add(testFunc.getName());
                } else {
                    List<String> newRow = getSingleValuedCertificationResultData(listing, certResult);
                    newRow.add(""); //blank space for test standards
                    newRow.add(testFunc.getName());
                    result.add(newRow);
                }
            }
        } else {
            certificationResultFirstRow.add("");
        }

        if (!CollectionUtils.isEmpty(certResult.getTestProcedures())) {
            for (int i = 0; i < certResult.getTestProcedures().size(); i++) {
                CertificationResultTestProcedure testProc = certResult.getTestProcedures().get(i);
                if (i == 0) {
                    certificationResultFirstRow.add(testProc.getTestProcedure().getName() + "; " + testProc.getTestProcedureVersion());
                } else if (result.size() > i) {
                    result.get(i).add(testProc.getTestProcedure().getName() + "; " + testProc.getTestProcedureVersion());
                } else {
                    List<String> newRow = getSingleValuedCertificationResultData(listing, certResult);
                    newRow.add(""); //blank space for test standards
                    newRow.add(""); //blank space for test functionality
                    newRow.add(testProc.getTestProcedure().getName() + "; " + testProc.getTestProcedureVersion());
                    result.add(newRow);
                }
            }
        } else {
            certificationResultFirstRow.add("");
        }
        return result;
    }

    private List<String> getSingleValuedCertificationResultData(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        List<String> result = new ArrayList<String>();
        result.add(listing.getChplProductNumber());
        result.add(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString());
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
        result.add(formatDate(listing.getCertificationDate()));
        result.add(listing.getCurrentStatus().getStatus().getName());
        result.add(certResult.isSuccess() ? "Yes" : "No");
        result.add(certResult.getPrivacySecurityFramework());
        return result;
    }

    protected String formatEdition(CertifiedProductSearchDetails listing) {
        String edition = listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString();
        if (listing.getCuresUpdate() != null && listing.getCuresUpdate()) {
            edition = edition + " Cures Update";
        }
        return edition;
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
}
