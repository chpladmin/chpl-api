package gov.healthit.chpl.scheduler.presenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.RequirementGroupType;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.util.NullSafeEvaluator;
import gov.healthit.chpl.util.Util;

public class SurveillanceAllCsvPresenter implements CertifiedProductPresenter, AutoCloseable {
    private Environment env;
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter dateTimeFormatter;
    private OutputStreamWriter writer = null;
    private CSVPrinter csvPrinter = null;
    private Logger logger;

    public SurveillanceAllCsvPresenter(Environment env) {
        dateFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd");
        dateTimeFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm Z");
        this.env = env;
    }

    @Override
    public void open(final File file) throws IOException {
        getLogger().info("Opening file, initializing Surveillance All CSV doc.");
        writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        writer.write('\ufeff');
        csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL);
        csvPrinter.printRecord(generateHeaderValues());
        csvPrinter.flush();
    }

    @Override
    public synchronized void add(final CertifiedProductSearchDetails data) throws IOException {
        getLogger().info("Adding Surveillance to Surveillance All CSV file: " + data.getId());

        if (data.getSurveillance() != null && data.getSurveillance().size() > 0) {
            for (Surveillance currSurveillance : data.getSurveillance()) {
                List<List<String>> rowValues = generateMultiRowValue(data, currSurveillance);
                for (List<String> rowValue : rowValues) {
                    csvPrinter.printRecord(rowValue);
                    csvPrinter.flush();
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        getLogger().info("Closing the Surveillance All CSV file.");
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
        result.add("RECORD_STATUS__C");
        result.add("UNIQUE_CHPL_ID__C");
        result.add("URL");
        result.add("ACB_NAME");
        result.add("CERTIFICATION_STATUS");
        result.add("DEVELOPER_NAME");
        result.add("PRODUCT_NAME");
        result.add("PRODUCT_VERSION");
        result.add("SURVEILLANCE_ID");
        result.add("SURVEILLANCE_BEGAN");
        result.add("SURVEILLANCE_ENDED");
        result.add("SURVEILLANCE_TYPE");
        result.add("RANDOMIZED_SITES_USED");
        result.add("SURVEILLANCE_LAST_UPDATED_DATE");
        result.add("SURVEILLED_REQUIREMENT_TYPE");
        result.add("SURVEILLED_REQUIREMENT");
        result.add("SURVEILLANCE_RESULT");
        result.add("NON_CONFORMITY_TYPE");
        result.add("NON_CONFORMITY_STATUS");
        result.add("NON_CONFORMITY_CLOSE_DATE");
        result.add("DATE_OF_DETERMINATION");
        result.add("CAP_APPROVAL_DATE");
        result.add("ACTION_BEGAN_DATE");
        result.add("MUST_COMPLETE_DATE");
        result.add("WAS_COMPLETE_DATE");
        result.add("NON_CONFORMITY_SUMMARY");
        result.add("NON_CONFORMITY_FINDINGS");
        result.add("SITES_PASSED");
        result.add("TOTAL_SITES");
        result.add("DEVELOPER_EXPLANATION");
        result.add("RESOLUTION_DESCRIPTION");
        result.add("NON_CONFORMITY_LAST_UPDATED_DATE");
        return result;
    }

    protected List<List<String>> generateMultiRowValue(final CertifiedProductSearchDetails data,
            final Surveillance surv) {
        List<List<String>> result = new ArrayList<List<String>>();

        List<String> firstRow = new ArrayList<String>();
        firstRow.add("Update");
        List<String> survValues = generateSurveillanceRowValues(data, surv);
        firstRow.addAll(survValues);
        result.add(firstRow);

        if (surv.getRequirements() != null && surv.getRequirements().size() > 0) {
            boolean isFirstSurvRow = true;
            for (SurveillanceRequirement req : surv.getRequirements()) {
                List<String> reqValues = generateSurveilledRequirementRowValues(req);
                List<String> reqRow = null;

                if (isFirstSurvRow) {
                    // put data in firstRow
                    firstRow.addAll(reqValues);
                } else {
                    // make a new row
                    reqRow = new ArrayList<String>();
                    reqRow.add("Subelement");
                    reqRow.addAll(survValues);
                    reqRow.addAll(reqValues);
                    result.add(reqRow);
                }

                if (req.getNonconformities() != null && req.getNonconformities().size() > 0) {
                    boolean isFirstReqRow = true;
                    for (SurveillanceNonconformity nc : req.getNonconformities()) {
                        List<String> ncValues = generateNonconformityRowValues(nc);
                        if (isFirstSurvRow) {
                            isFirstSurvRow = false;
                            firstRow.addAll(ncValues);
                        } else if (reqRow != null && isFirstReqRow) {
                            isFirstReqRow = false;
                            reqRow.addAll(ncValues);
                        } else {
                            List<String> ncRow = new ArrayList<String>();
                            ncRow.add("Subelement");
                            ncRow.addAll(survValues);
                            ncRow.addAll(reqValues);
                            ncRow.addAll(ncValues);
                            result.add(ncRow);
                        }
                    }
                }

                isFirstSurvRow = false;
            }
        }
        return result;
    }

    private List<String> generateSurveillanceRowValues(final CertifiedProductSearchDetails listing,
            final Surveillance surv) {
        List<String> result = new ArrayList<String>();
        result.add(listing.getChplProductNumber());
        result.add(env.getProperty("chplUrlBegin") + env.getProperty("listingDetailsUrl") + listing.getId());
        result.add(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString());
        result.add(listing.getCurrentStatus().getStatus().getName());
        result.add(listing.getDeveloper().getName());
        result.add(listing.getProduct().getName());
        result.add(listing.getVersion().getVersion());
        result.add(surv.getFriendlyId());
        if (surv.getStartDay() != null) {
            result.add(dateFormatter.format(surv.getStartDay()));
        } else {
            result.add("");
        }
        if (surv.getEndDay() != null) {
            result.add(dateFormatter.format(surv.getEndDay()));
        } else {
            result.add("");
        }
        result.add(surv.getType().getName());
        if (surv.getRandomizedSitesUsed() != null) {
            result.add(surv.getRandomizedSitesUsed().toString());
        } else {
            result.add("");
        }
        result.add(surv.getLastModifiedDate().toInstant().atOffset(ZoneOffset.UTC).format(dateTimeFormatter));
        return result;
    }

    private List<String> generateSurveilledRequirementRowValues(final SurveillanceRequirement req) {
        List<String> reqRow = new ArrayList<String>();

        if (req.getRequirementType() == null) {
            reqRow.add(RequirementGroupType.OTHER.toString());
        } else {
            reqRow.add(NullSafeEvaluator.eval(() -> req.getRequirementType().getRequirementGroupType().getName(), ""));
        }

        if (req.getRequirementType() == null) {
            reqRow.add(NullSafeEvaluator.eval(() -> req.getRequirementTypeOther(), ""));
        } else if (req.getRequirementType().getRequirementGroupType().getId().equals(RequirementGroupType.CERTIFIED_CAPABILITY_ID)) {
            reqRow.add(Util.formatCriteriaNumber(req.getRequirementType()));
        } else {
            reqRow.add(req.getRequirementType().getTitle());
        }

        reqRow.add(NullSafeEvaluator.eval(() -> req.getResult().getName(), ""));
        return reqRow;
    }

    private List<String> generateNonconformityRowValues(final SurveillanceNonconformity nc) {
        List<String> ncRow = new ArrayList<String>();
        ncRow.add(NullSafeEvaluator.eval(() -> nc.getType().getFormattedTitle(), ""));

        // Derive the status
        if (nc.getNonconformityCloseDay() == null) {
            ncRow.add("Open");
        } else {
            ncRow.add("Closed");
        }
        if (nc.getNonconformityCloseDay() != null) {
            ncRow.add(dateFormatter.format(nc.getNonconformityCloseDay()));
        } else {
            ncRow.add("");
        }
        if (nc.getDateOfDeterminationDay() != null) {
            ncRow.add(dateFormatter.format(nc.getDateOfDeterminationDay()));
        } else {
            ncRow.add("");
        }
        if (nc.getCapApprovalDay() != null) {
            ncRow.add(dateFormatter.format(nc.getCapApprovalDay()));
        } else {
            ncRow.add("");
        }
        if (nc.getCapStartDay() != null) {
            ncRow.add(dateFormatter.format(nc.getCapStartDay()));
        } else {
            ncRow.add("");
        }
        if (nc.getCapMustCompleteDay() != null) {
            ncRow.add(dateFormatter.format(nc.getCapMustCompleteDay()));
        } else {
            ncRow.add("");
        }
        if (nc.getCapEndDay() != null) {
            ncRow.add(dateFormatter.format(nc.getCapEndDay()));
        } else {
            ncRow.add("");
        }
        if (nc.getSummary() != null) {
            ncRow.add(nc.getSummary());
        } else {
            ncRow.add("");
        }
        if (nc.getFindings() != null) {
            ncRow.add(nc.getFindings());
        } else {
            ncRow.add("");
        }
        if (nc.getSitesPassed() != null) {
            ncRow.add(nc.getSitesPassed().toString());
        } else {
            ncRow.add("");
        }
        if (nc.getTotalSites() != null) {
            ncRow.add(nc.getTotalSites().toString());
        } else {
            ncRow.add("");
        }
        if (nc.getDeveloperExplanation() != null) {
            ncRow.add(nc.getDeveloperExplanation());
        } else {
            ncRow.add("");
        }
        if (nc.getResolution() != null) {
            ncRow.add(nc.getResolution());
        } else {
            ncRow.add("");
        }
        ncRow.add(nc.getLastModifiedDate().toInstant().atOffset(ZoneOffset.UTC).format(dateTimeFormatter));
        return ncRow;
    }

    public final Environment getEnv() {
        return env;
    }

    public final DateTimeFormatter getDateFormatter() {
        return dateFormatter;
    }

    public final DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }
}
