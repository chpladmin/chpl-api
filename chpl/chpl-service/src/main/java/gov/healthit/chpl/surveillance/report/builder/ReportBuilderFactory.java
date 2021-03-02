package gov.healthit.chpl.surveillance.report.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.surveillance.report.builder2019.AnnualReportBuilder2019;
import gov.healthit.chpl.surveillance.report.builder2019.QuarterlyReportBuilder2019;
import gov.healthit.chpl.surveillance.report.builder2021.AnnualReportBuilder2021;
import gov.healthit.chpl.surveillance.report.builder2021.QuarterlyReportBuilder2021;
import gov.healthit.chpl.surveillance.report.dto.AnnualReportDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;

@Component
public class ReportBuilderFactory {
    private static final int YEAR_2019 = 2019;
    private static final int YEAR_2020 = 2020;

    private AnnualReportBuilder2019 annualReportBuilder2019;
    private AnnualReportBuilder2021 annualReportBuilder2021;
    private QuarterlyReportBuilder2019 quarterlyReportBuilder2019;
    private QuarterlyReportBuilder2021 quarterlyReportBuilder2021;

    @Autowired
    public ReportBuilderFactory(AnnualReportBuilder2019 annualReportBuilder2019,
            AnnualReportBuilder2021 annualReportBuilder2021,
            QuarterlyReportBuilder2019 quarterlyReportBuilder2019,
            QuarterlyReportBuilder2021 quarterlyReportBuilder2021) {
        this.annualReportBuilder2019 = annualReportBuilder2019;
        this.annualReportBuilder2021 = annualReportBuilder2021;
        this.quarterlyReportBuilder2019 = quarterlyReportBuilder2019;
        this.quarterlyReportBuilder2021 = quarterlyReportBuilder2021;
    }

    public QuarterlyReportBuilderXlsx getReportBuilder(QuarterlyReportDTO quarterlyReport)
        throws InvalidArgumentsException {
        if (quarterlyReport == null || quarterlyReport.getYear() == null) {
            throw new InvalidArgumentsException("Invalid quarterly report or year.");
        }

        switch (quarterlyReport.getYear()) {
            case YEAR_2019:
            case YEAR_2020:
                return quarterlyReportBuilder2019;
            default:
                return quarterlyReportBuilder2021;
        }
    }

    public AnnualReportBuilderXlsx getReportBuilder(AnnualReportDTO annualReport)
            throws InvalidArgumentsException {
            if (annualReport == null || annualReport.getYear() == null) {
                throw new InvalidArgumentsException("Invalid annual report or year.");
            }

            switch (annualReport.getYear()) {
                case YEAR_2019:
                case YEAR_2020:
                    return annualReportBuilder2019;
                default:
                    return annualReportBuilder2021;
            }
        }
}
