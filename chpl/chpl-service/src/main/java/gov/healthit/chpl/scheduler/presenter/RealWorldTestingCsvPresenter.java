package gov.healthit.chpl.scheduler.presenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.BooleanUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingReport;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingReport.CriterionAndSvapData;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.service.realworldtesting.RealWorldTestingCriteriaService;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "realWorldTestingReportEmailJobLogger")
public class RealWorldTestingCsvPresenter {
    private CertificationCriterionService criteriaService;
    private List<CertificationCriterion> rwtResultsRequiredCriteria;

    @Autowired
    public RealWorldTestingCsvPresenter(RealWorldTestingCriteriaService rwtCriteriaService,
            CertificationCriterionService criteriaService,
            CertificationCriterionComparator criteriaComparator,
            FF4j ff4j) {
        this.criteriaService = criteriaService;
        Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (ff4j.check(FeatureList.HTI_5_ERD)) {
            rwtResultsRequiredCriteria = rwtCriteriaService.getEligibleCriteria(currentYear);
        } else {
            rwtResultsRequiredCriteria = Stream.of(criteriaService.get(Criteria2015.G_7),
                    criteriaService.get(Criteria2015.G_9_CURES),
                    criteriaService.get(Criteria2015.G_10)).collect(Collectors.toList());
        }
        rwtResultsRequiredCriteria.stream().sorted(criteriaComparator);
    }

    public void presentAsFile(List<RealWorldTestingReport> rwtReports, File outputFile) {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile),
                Charset.forName("UTF-8").newEncoder());
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
            writer.write('\ufeff');
            csvPrinter.printRecord(generateHeaderValues());
            for (RealWorldTestingReport rwtReport : rwtReports) {
                csvPrinter.printRecord(generateRowValue(rwtReport));
            }
        } catch (IOException ex) {
            LOGGER.error("Could not write file " + outputFile.getName(), ex);
        }
    }

    protected List<String> generateHeaderValues() {
        List<String> results = new ArrayList<String>();
        results.add("ONC-ACB Name");
        results.add("CHPL ID");
        results.add("Current Status");
        results.add("Certification Date");
        results.add("Product Name");
        results.add("Product DBID");
        results.add("Developer Name");
        results.add("Developer DBID");
        results.add("Developer Users");
        results.add("Initial RWT Year");
        results.add("ICS");
        results.add("RWT Plans URL");
        results.add("RWT Plans Submission Confirmed");
        results.add("RWT Results URL");
        results.add("RWT Results Submission Confirmed");
        results.add("RWT Plans Message");
        results.add("RWT Results Message");
        rwtResultsRequiredCriteria.stream()
            .forEach(criterion -> {
                if (criteriaService.isGCriterion(criterion)) {
                    results.add(Util.formatCriteriaNumber(criterion));
                } else {
                    results.add(Util.formatCriteriaNumber(criterion) + "SVAP");
                }
        });
        return results;
    }

    protected List<String> generateRowValue(RealWorldTestingReport rwtReport) {
        List<String> results = new ArrayList<String>();
        results.add(rwtReport.getAcbName());
        results.add(rwtReport.getChplProductNumber());
        results.add(rwtReport.getCurrentStatus());
        results.add(DateUtil.format(rwtReport.getCertificationDate()));
        results.add(rwtReport.getProductName());
        results.add(rwtReport.getProductId() == null ? null : rwtReport.getProductId().toString());
        results.add(rwtReport.getDeveloperName());
        results.add(rwtReport.getDeveloperId() == null ? null : rwtReport.getDeveloperId().toString());
        results.add(!CollectionUtils.isEmpty(rwtReport.getDeveloperUsers())
                ? rwtReport.getDeveloperUsers().stream().collect(Collectors.joining("; "))
                        : "");
        results.add(rwtReport.getRwtEligibilityYear() == null ? null : rwtReport.getRwtEligibilityYear().toString());
        results.add(BooleanUtils.isTrue(rwtReport.getIcs()) ? "Yes" : "");
        results.add(rwtReport.getRwtPlansUrl());
        results.add(rwtReport.getRwtPlansCheckDate() == null ? null : rwtReport.getRwtPlansCheckDate().toString());
        results.add(rwtReport.getRwtResultsUrl());
        results.add(rwtReport.getRwtResultsCheckDate() == null ? null : rwtReport.getRwtResultsCheckDate().toString());
        results.add(rwtReport.getRwtPlansMessage());
        results.add(rwtReport.getRwtResultsMessage());
        rwtResultsRequiredCriteria.stream()
            .forEach(criterion -> results.add(determineCriteriaReportValue(rwtReport.getCriterionAndSvapData(), criterion)));
        return results;
    }

    private String determineCriteriaReportValue(List<CriterionAndSvapData> criteriaAndSvapData, CertificationCriterion criterion) {
        CriterionAndSvapData criterionAndSvapData = criteriaAndSvapData.stream()
                .filter(item -> item.getCriterion().getId().equals(criterion.getId()))
                .findAny()
                .orElse(null);
        if (criterionAndSvapData == null) {
            return "FALSE";
        } else if (criterionAndSvapData.isGCriterion()) {
            return criterionAndSvapData.isAttested() ? "TRUE" : "FALSE";
        } else {
            return criterionAndSvapData.isAttested() && criterionAndSvapData.isUsesSvap() ? "TRUE" : "FALSE";
        }
    }
}
