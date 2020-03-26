package gov.healthit.chpl.scheduler.presenter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.util.Util;

/**
 * Writes out all surveillance for all products in the system.
 *
 * @author kekey
 *
 */
public class SurveillanceCsvPresenter {
    private static final Logger LOGGER = LogManager.getLogger(SurveillanceCsvPresenter.class);
    private Environment env;
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter dateTimeFormatter;

    /**
     * Constructor with properties.
     *
     * @param props
     *            the properties
     */
    public SurveillanceCsvPresenter(final Environment env) {
        dateFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd");
        dateTimeFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm Z");
        this.env = env;
    }

    /**
     * Write out surveillance details to CSV file.
     *
     * @param file
     *            the output file
     * @param cpList
     *            list of Certified Products
     */
    public void presentAsFile(final File file, final List<CertifiedProductSearchDetails> cpList) {
        try (FileWriter writer = new FileWriter(file);
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
            writer.write('\ufeff');
            csvPrinter.printRecord(generateHeaderValues());
            for (CertifiedProductSearchDetails cp : cpList) {
                if (cp.getSurveillance() != null && cp.getSurveillance().size() > 0) {
                    for (Surveillance currSurveillance : cp.getSurveillance()) {
                        List<List<String>> rowValues = generateMultiRowValue(cp, currSurveillance);
                        for (List<String> rowValue : rowValues) {
                            csvPrinter.printRecord(rowValue);
                        }
                    }
                }
            }
        } catch (final IOException ex) {
            LOGGER.error("Could not write file " + file.getName(), ex);
        }
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

    protected List<String> generateSurveillanceRowValues(final CertifiedProductSearchDetails listing,
            final Surveillance surv) {
        List<String> result = new ArrayList<String>();
        result.add(listing.getChplProductNumber());
        result.add(env.getProperty("chplUrlBegin") + "#/product/" + listing.getId());
        result.add(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString());
        result.add(listing.getCurrentStatus().getStatus().getName());
        result.add(listing.getDeveloper().getName());
        result.add(listing.getProduct().getName());
        result.add(listing.getVersion().getVersion());
        result.add(surv.getFriendlyId());
        if (surv.getStartDate() != null) {
            LocalDateTime survStartDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(surv.getStartDate().getTime()),
                    ZoneId.systemDefault());
            result.add(dateFormatter.format(survStartDate));
        } else {
            result.add("");
        }
        if (surv.getEndDate() != null) {
            LocalDateTime survEndDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(surv.getEndDate().getTime()),
                    ZoneId.systemDefault());
            result.add(dateFormatter.format(survEndDate));
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

    protected List<String> generateSurveilledRequirementRowValues(final SurveillanceRequirement req) {
        List<String> reqRow = new ArrayList<String>();

        if (req.getType() != null) {
            reqRow.add(req.getType().getName());
        } else {
            reqRow.add("");
        }
        if (req.getCriterion() != null) {
            reqRow.add(Util.formatCriteriaNumber(req.getCriterion()));
        } else if (req.getRequirement() != null) {
            reqRow.add(req.getRequirement());
        } else {
            reqRow.add("");
        }
        if (req.getResult() != null) {
            reqRow.add(req.getResult().getName());
        } else {
            reqRow.add("");
        }
        return reqRow;
    }

    protected List<String> generateNonconformityRowValues(final SurveillanceNonconformity nc) {
        List<String> ncRow = new ArrayList<String>();
        if (nc.getCriterion() != null) {
            ncRow.add(Util.formatCriteriaNumber(nc.getCriterion()));
        } else if (nc.getNonconformityType() != null) {
            ncRow.add(nc.getNonconformityType());
        } else {
            ncRow.add("");
        }
        if (nc.getStatus() != null) {
            ncRow.add(nc.getStatus().getName());
        } else {
            ncRow.add("");
        }
        if (nc.getDateOfDetermination() != null) {
            LocalDateTime ncDeterminationDate = LocalDateTime
                    .ofInstant(Instant.ofEpochMilli(nc.getDateOfDetermination().getTime()), ZoneId.systemDefault());
            ncRow.add(dateFormatter.format(ncDeterminationDate));
        } else {
            ncRow.add("");
        }
        if (nc.getCapApprovalDate() != null) {
            LocalDateTime ncApprovalDate = LocalDateTime
                    .ofInstant(Instant.ofEpochMilli(nc.getCapApprovalDate().getTime()), ZoneId.systemDefault());
            ncRow.add(dateFormatter.format(ncApprovalDate));
        } else {
            ncRow.add("");
        }
        if (nc.getCapStartDate() != null) {
            LocalDateTime ncStartDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(nc.getCapStartDate().getTime()),
                    ZoneId.systemDefault());
            ncRow.add(dateFormatter.format(ncStartDate));
        } else {
            ncRow.add("");
        }
        if (nc.getCapMustCompleteDate() != null) {
            LocalDateTime ncMustCompleteDate = LocalDateTime
                    .ofInstant(Instant.ofEpochMilli(nc.getCapMustCompleteDate().getTime()), ZoneId.systemDefault());
            ncRow.add(dateFormatter.format(ncMustCompleteDate));
        } else {
            ncRow.add("");
        }
        if (nc.getCapEndDate() != null) {
            LocalDateTime ncEndDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(nc.getCapEndDate().getTime()),
                    ZoneId.systemDefault());
            ncRow.add(dateFormatter.format(ncEndDate));
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
