package gov.healthit.chpl.scheduler.presenter;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.env.Environment;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.RequirementGroupType;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.util.NullSafeEvaluator;

public class SurveillanceAllCsvPresenter extends SurveillanceCsvPresenter {
    private static final String PRESENTER_NAME = "Surveillance All";

    public SurveillanceAllCsvPresenter(Environment env) {
        super(env);
    }

    @Override
    public String getPresenterName() {
        return PRESENTER_NAME;
    }

    @Override
    public String getFileName() {
        return getEnvironment().getProperty("surveillanceAllReportName");
    }

    @Override
    public synchronized void add(final CertifiedProductSearchDetails data) throws IOException {
        getLogger().info("Adding Surveillance to Surveillance All CSV file: " + data.getId());

        if (data.getSurveillance() != null && data.getSurveillance().size() > 0) {
            for (Surveillance currSurveillance : data.getSurveillance()) {
                List<List<String>> rowValues = generateMultiRowValue(data, currSurveillance);
                for (List<String> rowValue : rowValues) {
                    getCsvPrinter().printRecord(rowValue);
                    getCsvPrinter().flush();
                }
            }
        }
    }

    @Override
    public List<String> generateHeaderValues() {
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

    private List<List<String>> generateMultiRowValue(final CertifiedProductSearchDetails data,
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
        result.add(getEnvironment().getProperty("chplUrlBegin") + getEnvironment().getProperty("listingDetailsUrl") + listing.getId());
        result.add(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString());
        result.add(listing.getCurrentStatus().getStatus().getName());
        result.add(listing.getDeveloper().getName());
        result.add(listing.getProduct().getName());
        result.add(listing.getVersion().getVersion());
        result.add(surv.getFriendlyId());
        if (surv.getStartDay() != null) {
            result.add(getDateFormatter().format(surv.getStartDay()));
        } else {
            result.add("");
        }
        if (surv.getEndDay() != null) {
            result.add(getDateFormatter().format(surv.getEndDay()));
        } else {
            result.add("");
        }
        result.add(surv.getType().getName());
        if (surv.getRandomizedSitesUsed() != null) {
            result.add(surv.getRandomizedSitesUsed().toString());
        } else {
            result.add("");
        }
        result.add(surv.getLastModifiedDate().toInstant().atOffset(ZoneOffset.UTC).format(getDateTimeFormatter()));
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
            reqRow.add(req.getRequirementType().getFormattedTitleForReport());
        } else {
            reqRow.add(req.getRequirementType().getTitle());
        }

        reqRow.add(NullSafeEvaluator.eval(() -> req.getResult().getName(), ""));
        return reqRow;
    }

    private List<String> generateNonconformityRowValues(final SurveillanceNonconformity nc) {
        List<String> ncRow = new ArrayList<String>();
        ncRow.add(NullSafeEvaluator.eval(() -> nc.getType().getFormattedTitleForReport(), ""));

        // Derive the status
        if (nc.getNonconformityCloseDay() == null) {
            ncRow.add("Open");
        } else {
            ncRow.add("Closed");
        }
        if (nc.getNonconformityCloseDay() != null) {
            ncRow.add(getDateFormatter().format(nc.getNonconformityCloseDay()));
        } else {
            ncRow.add("");
        }
        if (nc.getDateOfDeterminationDay() != null) {
            ncRow.add(getDateFormatter().format(nc.getDateOfDeterminationDay()));
        } else {
            ncRow.add("");
        }
        if (nc.getCapApprovalDay() != null) {
            ncRow.add(getDateFormatter().format(nc.getCapApprovalDay()));
        } else {
            ncRow.add("");
        }
        if (nc.getCapStartDay() != null) {
            ncRow.add(getDateFormatter().format(nc.getCapStartDay()));
        } else {
            ncRow.add("");
        }
        if (nc.getCapMustCompleteDay() != null) {
            ncRow.add(getDateFormatter().format(nc.getCapMustCompleteDay()));
        } else {
            ncRow.add("");
        }
        if (nc.getCapEndDay() != null) {
            ncRow.add(getDateFormatter().format(nc.getCapEndDay()));
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
        ncRow.add(nc.getLastModifiedDate().toInstant().atOffset(ZoneOffset.UTC).format(getDateTimeFormatter()));
        return ncRow;
    }

}
