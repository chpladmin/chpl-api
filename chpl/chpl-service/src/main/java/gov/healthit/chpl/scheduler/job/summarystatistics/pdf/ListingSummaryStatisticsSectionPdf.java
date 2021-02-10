package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.layout.element.Table;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailStatistics;

@Component
public class ListingSummaryStatisticsSectionPdf extends SummaryStatisticsSectionPdf {

    @Autowired
    public ListingSummaryStatisticsSectionPdf(CertificationBodyDAO certificationBodyDAO) {
        super(certificationBodyDAO);
    }

    @Override
    public Table generateTable(LocalDate recent, LocalDate previous, EmailStatistics recentEmailStatistics, EmailStatistics previousEmailStatistics) {
        Table table = new Table(getRelativeColumnWidths());
        table.useAllAvailableWidth();
        table = addHeaders(table, "ListingStatistics", recent, previous);
        table = addTableRow(table, createDataForRow("3. Total # of Listings (Regardless of Status or Edition)",
                                    recentEmailStatistics.getListingsForEditionAnyTotal(),
                                    previousEmailStatistics.getListingsForEditionAnyTotal()));

        table = addTableRow(table, createDataForRow("a. Total # of Active (Including Suspended by ONC/ONC-ACB) 2014 Listings",
                recentEmailStatistics.getListingsForEdition2014WithActiveAndSuspendedStatuses().getCount(),
                previousEmailStatistics.getListingsForEdition2014WithActiveAndSuspendedStatuses().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT);

        table = addAcbRows(table,
                recentEmailStatistics.getListingsForEdition2014WithActiveAndSuspendedStatuses().getAcbStatistics(),
                previousEmailStatistics.getListingsForEdition2014WithActiveAndSuspendedStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("b. Total # of Active (Including Suspended) 2015 Listings",
                recentEmailStatistics.getListingsForEdition2015NonCuresWithActiveAndSuspendedStatuses().getCount(),
                previousEmailStatistics.getListingsForEdition2015NonCuresWithActiveAndSuspendedStatuses().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT);

        table = addAcbRows(table,
                recentEmailStatistics.getListingsForEdition2015NonCuresWithActiveAndSuspendedStatuses().getAcbStatistics(),
                previousEmailStatistics.getListingsForEdition2015NonCuresWithActiveAndSuspendedStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("c. Total # of 2015 Listings with Alternative Test Methods",
                recentEmailStatistics.getListingsForEdition2015NonCuresWithAllStatusesAndAltTestMethods().getCount(),
                previousEmailStatistics.getListingsForEdition2015NonCuresWithAllStatusesAndAltTestMethods().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT);

        table = addAcbRows(table,
                recentEmailStatistics.getListingsForEdition2015NonCuresWithAllStatusesAndAltTestMethods().getAcbStatistics(),
                previousEmailStatistics.getListingsForEdition2015NonCuresWithAllStatusesAndAltTestMethods().getAcbStatistics());

        table = addTableRow(table, createDataForRow("d. Total # of Active (Including Suspended by ONC/ONC-ACB 2015 Cures Update Listings)",
                recentEmailStatistics.getListingsForEdition2015CuresWithActiveAndSuspendedStatuses().getCount(),
                previousEmailStatistics.getListingsForEdition2015CuresWithActiveAndSuspendedStatuses().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT);

        table = addAcbRows(table,
                recentEmailStatistics.getListingsForEdition2015CuresWithActiveAndSuspendedStatuses().getAcbStatistics(),
                previousEmailStatistics.getListingsForEdition2015CuresWithActiveAndSuspendedStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("e. Total # of 2015 Cures Update Listings with Alternative Test Methods",
                recentEmailStatistics.getListingsForEdition2015CuresWithAllStatusesAndAltTestMethods().getCount(),
                previousEmailStatistics.getListingsForEdition2015CuresWithAllStatusesAndAltTestMethods().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT);

        table = addAcbRows(table,
                recentEmailStatistics.getListingsForEdition2015CuresWithAllStatusesAndAltTestMethods().getAcbStatistics(),
                previousEmailStatistics.getListingsForEdition2015CuresWithAllStatusesAndAltTestMethods().getAcbStatistics());

        table = addTableRow(table, createDataForRow("f. Total # of 2015 Listings and 2015 Cures Update Listings (Regardless of Status)",
                recentEmailStatistics.getListingsForEdition2015NonCuresAndCuresTotal(),
                previousEmailStatistics.getListingsForEdition2015NonCuresAndCuresTotal()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT);

        table = addTableRow(table, createDataForRow("g. Total # of 2015 Listings (Regardless of Status)",
                recentEmailStatistics.getListingsForEdition2015NonCuresTotal(),
                previousEmailStatistics.getListingsForEdition2015NonCuresTotal()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT);

        table = addTableRow(table, createDataForRow("h. Total # of 2015 Cures Update Listings (Regardless of Status)",
                recentEmailStatistics.getListingsForEdition2015CuresTotal(),
                previousEmailStatistics.getListingsForEdition2015CuresTotal()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT);

        return table;
    }
}
