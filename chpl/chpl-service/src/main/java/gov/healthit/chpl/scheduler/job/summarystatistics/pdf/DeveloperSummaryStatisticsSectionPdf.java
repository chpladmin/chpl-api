package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.layout.element.Table;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailStatistics;


@Component
public class DeveloperSummaryStatisticsSectionPdf extends SummaryStatisticsSectionPdf {

    @Autowired
    public DeveloperSummaryStatisticsSectionPdf(CertificationBodyDAO certificationBodyDAO) {
        super(certificationBodyDAO);
    }

    @Override
    public Table generateTable(LocalDate recent, LocalDate previous, EmailStatistics recentEmailStatistics, EmailStatistics previousEmailStatistics) {
        Table table = new Table(getRelativeColumnWidths());
        table.useAllAvailableWidth();
        table = addHeaders(table, recent, previous);
        table = addTableRow(table, createDataForRow("1. Total # of Unique Developers (Regardless of Edition)",
                                    recentEmailStatistics.getDevelopersForEditionAllAndAllStatuses(),
                                    previousEmailStatistics.getDevelopersForEditionAllAndAllStatuses()));

        table = addTableRow(table, createDataForRow("a. Total # of Developers with 2014 Listings*",
                recentEmailStatistics.getDevelopersForEdition2014WithAllStatuses().getCount(),
                recentEmailStatistics.getDevelopersForEdition2014WithAllStatuses().getCount()), 1);

        table = addAcbRows(table,
                recentEmailStatistics.getDevelopersForEdition2014WithAllStatuses().getAcbStatistics(),
                previousEmailStatistics.getDevelopersForEdition2014WithAllStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("b. Total # of Developers with Active 2014 Listings*",
                recentEmailStatistics.getDevelopersForEdition2014WithActiveStatuses().getCount(),
                recentEmailStatistics.getDevelopersForEdition2014WithActiveStatuses().getCount()), 1);

        table = addAcbRows(table,
                recentEmailStatistics.getDevelopersForEdition2014WithActiveStatuses().getAcbStatistics(),
                previousEmailStatistics.getDevelopersForEdition2014WithActiveStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("c. Total # of Developers with Suspended by ONC-ACB/ONC 2014 Listings*",
                recentEmailStatistics.getDevelopersForEdition2014WithSuspendedStatuses().getCount(),
                recentEmailStatistics.getDevelopersForEdition2014WithSuspendedStatuses().getCount()), 1);

        table = addAcbRows(table,
                recentEmailStatistics.getDevelopersForEdition2014WithSuspendedStatuses().getAcbStatistics(),
                previousEmailStatistics.getDevelopersForEdition2014WithSuspendedStatuses().getAcbStatistics());


        table = addTableRow(table, createDataForRow("d. Total # of Developers with 2015 Listings or 2015 Cures Update Listings (Regardless of Status)*",
                recentEmailStatistics.getDevelopersForEdition2015CuresAndNonCuresWithAllStatuses().getCount(),
                recentEmailStatistics.getDevelopersForEdition2015CuresAndNonCuresWithAllStatuses().getCount()), 1);

        table = addAcbRows(table,
                recentEmailStatistics.getDevelopersForEdition2015CuresAndNonCuresWithAllStatuses().getAcbStatistics(),
                previousEmailStatistics.getDevelopersForEdition2015CuresAndNonCuresWithAllStatuses().getAcbStatistics());


        table = addTableRow(table, createDataForRow("e. Total # of Developers with Active 2015 Listings or 2015 Cures Update Listings*",
                recentEmailStatistics.getDevelopersForEdition2015CuresAndNonCuresWithActiveStatuses().getCount(),
                recentEmailStatistics.getDevelopersForEdition2015CuresAndNonCuresWithActiveStatuses().getCount()), 1);

        table = addAcbRows(table,
                recentEmailStatistics.getDevelopersForEdition2015CuresAndNonCuresWithActiveStatuses().getAcbStatistics(),
                previousEmailStatistics.getDevelopersForEdition2015CuresAndNonCuresWithActiveStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("f.  Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings or 2015 Cures Update Listings*",
                recentEmailStatistics.getDevelopersForEdition2015CuresAndNonCuresWithSuspendedStatuses().getCount(),
                recentEmailStatistics.getDevelopersForEdition2015CuresAndNonCuresWithSuspendedStatuses().getCount()), 1);

        table = addAcbRows(table,
                recentEmailStatistics.getDevelopersForEdition2015CuresAndNonCuresWithSuspendedStatuses().getAcbStatistics(),
                previousEmailStatistics.getDevelopersForEdition2015CuresAndNonCuresWithSuspendedStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("g. Total # of Developers with 2015 Listings (Regardless of Status)*",
                recentEmailStatistics.getDevelopersForEdition2015NonCuresWithAllStatuses().getCount(),
                recentEmailStatistics.getDevelopersForEdition2015NonCuresWithAllStatuses().getCount()), 1);

        table = addAcbRows(table,
                recentEmailStatistics.getDevelopersForEdition2015NonCuresWithAllStatuses().getAcbStatistics(),
                previousEmailStatistics.getDevelopersForEdition2015NonCuresWithAllStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("h. Total # of Developers with Active 2015 Listings*",
                recentEmailStatistics.getDevelopersForEdition2015NonCuresWithActiveStatuses().getCount(),
                recentEmailStatistics.getDevelopersForEdition2015NonCuresWithActiveStatuses().getCount()), 1);

        table = addAcbRows(table,
                recentEmailStatistics.getDevelopersForEdition2015NonCuresWithActiveStatuses().getAcbStatistics(),
                previousEmailStatistics.getDevelopersForEdition2015NonCuresWithActiveStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("i. Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings",
                recentEmailStatistics.getDevelopersForEdition2015NonCuresWithSuspendedStatuses().getCount(),
                recentEmailStatistics.getDevelopersForEdition2015NonCuresWithSuspendedStatuses().getCount()), 1);

        table = addAcbRows(table,
                recentEmailStatistics.getDevelopersForEdition2015NonCuresWithSuspendedStatuses().getAcbStatistics(),
                previousEmailStatistics.getDevelopersForEdition2015NonCuresWithSuspendedStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("j. Total # of Developers with 2015 Cures Update Listings (Regardless of Status)",
                recentEmailStatistics.getDevelopersForEdition2015CuresWithAllStatuses().getCount(),
                recentEmailStatistics.getDevelopersForEdition2015CuresWithAllStatuses().getCount()), 1);

        table = addAcbRows(table,
                recentEmailStatistics.getDevelopersForEdition2015CuresWithAllStatuses().getAcbStatistics(),
                previousEmailStatistics.getDevelopersForEdition2015CuresWithAllStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("k. Total # of Developers with Active 2015 Cures Update Listings",
                recentEmailStatistics.getDevelopersForEdition2015CuresWithActiveStatuses().getCount(),
                recentEmailStatistics.getDevelopersForEdition2015CuresWithActiveStatuses().getCount()), 1);

        table = addAcbRows(table,
                recentEmailStatistics.getDevelopersForEdition2015CuresWithActiveStatuses().getAcbStatistics(),
                previousEmailStatistics.getDevelopersForEdition2015CuresWithActiveStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("l. Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Cures Update Listings",
                recentEmailStatistics.getDevelopersForEdition2015CuresWithSuspendedStatuses().getCount(),
                recentEmailStatistics.getDevelopersForEdition2015CuresWithSuspendedStatuses().getCount()), 1);

        table = addAcbRows(table,
                recentEmailStatistics.getDevelopersForEdition2015CuresWithSuspendedStatuses().getAcbStatistics(),
                previousEmailStatistics.getDevelopersForEdition2015CuresWithSuspendedStatuses().getAcbStatistics());

        return table;
    }



    private Table addHeaders(Table table, LocalDate recent, LocalDate previous) {
        List<String> headers = new ArrayList<String>();
        headers.add("Developer Statistics");
        headers.add(recent.format(DateTimeFormatter.ofPattern("LLLL dd, yyyy")));
        headers.add(previous.format(DateTimeFormatter.ofPattern("LLLL dd, yyyy")));
        headers.add("Delta");
        addTableHeaderRow(table, headers);
        return table;
    }

}
