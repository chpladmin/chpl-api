package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.layout.element.Table;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailStatistics;

@Component
public class ProductSummaryStatisticsSectionPdf extends SummaryStatisticsSectionPdf {

    @Autowired
    public ProductSummaryStatisticsSectionPdf(CertificationBodyDAO certificationBodyDAO) {
        super(certificationBodyDAO);
    }

    @Override
    public Table generateTable(LocalDate recent, LocalDate previous, EmailStatistics recentEmailStatistics, EmailStatistics previousEmailStatistics) {
        Table table = new Table(getRelativeColumnWidths());
        table.useAllAvailableWidth();
        table = addHeaders(table, "Product Statistics", recent, previous);
        table = addTableRow(table, createDataForRow("2. Total # of Certified Unique Products (Regardless of Status or Edition –Including 2011)",
                                    recentEmailStatistics.getProductsForEditionAllAndAllStatuses(),
                                    previousEmailStatistics.getProductsForEditionAllAndAllStatuses()), true);

        table = addTableRow(table, createDataForRow("a. Total # of Unique Products with 2014 Listings*",
                recentEmailStatistics.getProductsForEdition2014WithAllStatuses().getCount(),
                recentEmailStatistics.getProductsForEdition2014WithAllStatuses().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addAcbRows(table,
                recentEmailStatistics.getProductsForEdition2014WithAllStatuses().getAcbStatistics(),
                previousEmailStatistics.getProductsForEdition2014WithAllStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("b. Total # of Unique Products with Active 2014 Listings*",
                recentEmailStatistics.getProductsForEdition2014WithActiveStatuses().getCount(),
                recentEmailStatistics.getProductsForEdition2014WithActiveStatuses().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addAcbRows(table,
                recentEmailStatistics.getProductsForEdition2014WithActiveStatuses().getAcbStatistics(),
                previousEmailStatistics.getProductsForEdition2014WithActiveStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("c. Total # of Unique Products with Suspended by ONC-ACB/ONC 2014 Listings*",
                recentEmailStatistics.getProductsForEdition2014WithSuspendedStatuses().getCount(),
                recentEmailStatistics.getProductsForEdition2014WithSuspendedStatuses().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addAcbRows(table,
                recentEmailStatistics.getProductsForEdition2014WithSuspendedStatuses().getAcbStatistics(),
                previousEmailStatistics.getProductsForEdition2014WithSuspendedStatuses().getAcbStatistics());


        table = addTableRow(table, createDataForRow("d. Total # of Unique Products with 2015 Listings or 2015 Cures Update Listings (Regardless of Status)",
                recentEmailStatistics.getProductsForEdition2015CuresAndNonCuresWithAllStatuses().getCount(),
                recentEmailStatistics.getProductsForEdition2015CuresAndNonCuresWithAllStatuses().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addAcbRows(table,
                recentEmailStatistics.getProductsForEdition2015CuresAndNonCuresWithAllStatuses().getAcbStatistics(),
                previousEmailStatistics.getProductsForEdition2015CuresAndNonCuresWithAllStatuses().getAcbStatistics());


        table = addTableRow(table, createDataForRow("e. Total # of Unique Products with Active 2015 Listings or 2015 Cures Update Listings",
                recentEmailStatistics.getProductsForEdition2015CuresAndNonCuresWithActiveStatuses().getCount(),
                recentEmailStatistics.getProductsForEdition2015CuresAndNonCuresWithActiveStatuses().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addAcbRows(table,
                recentEmailStatistics.getProductsForEdition2015CuresAndNonCuresWithActiveStatuses().getAcbStatistics(),
                previousEmailStatistics.getProductsForEdition2015CuresAndNonCuresWithActiveStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("f.  Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Listings or 2015 Cures Update Listings",
                recentEmailStatistics.getProductsForEdition2015CuresAndNonCuresWithSuspendedStatuses().getCount(),
                recentEmailStatistics.getProductsForEdition2015CuresAndNonCuresWithSuspendedStatuses().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addAcbRows(table,
                recentEmailStatistics.getProductsForEdition2015CuresAndNonCuresWithSuspendedStatuses().getAcbStatistics(),
                previousEmailStatistics.getProductsForEdition2015CuresAndNonCuresWithSuspendedStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("g. Total # of Unique Products with 2015 Listings (Regardless of Status)*",
                recentEmailStatistics.getProductsForEdition2015NonCuresWithAllStatuses().getCount(),
                recentEmailStatistics.getProductsForEdition2015NonCuresWithAllStatuses().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addAcbRows(table,
                recentEmailStatistics.getProductsForEdition2015NonCuresWithAllStatuses().getAcbStatistics(),
                previousEmailStatistics.getProductsForEdition2015NonCuresWithAllStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("h. Total # of Unique Products with Active 2015 Listings*",
                recentEmailStatistics.getProductsForEdition2015NonCuresWithActiveStatuses().getCount(),
                recentEmailStatistics.getProductsForEdition2015NonCuresWithActiveStatuses().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addAcbRows(table,
                recentEmailStatistics.getProductsForEdition2015NonCuresWithActiveStatuses().getAcbStatistics(),
                previousEmailStatistics.getProductsForEdition2015NonCuresWithActiveStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("i. Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Listings*",
                recentEmailStatistics.getProductsForEdition2015NonCuresWithSuspendedStatuses().getCount(),
                recentEmailStatistics.getProductsForEdition2015NonCuresWithSuspendedStatuses().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addAcbRows(table,
                recentEmailStatistics.getProductsForEdition2015NonCuresWithSuspendedStatuses().getAcbStatistics(),
                previousEmailStatistics.getProductsForEdition2015NonCuresWithSuspendedStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("j. Total # of Unique Products with 2015 Cures Update Listings (Regardless of Status)",
                recentEmailStatistics.getProductsForEdition2015CuresWithAllStatuses().getCount(),
                recentEmailStatistics.getProductsForEdition2015CuresWithAllStatuses().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addAcbRows(table,
                recentEmailStatistics.getProductsForEdition2015CuresWithAllStatuses().getAcbStatistics(),
                previousEmailStatistics.getProductsForEdition2015CuresWithAllStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("k. Total # of Unique Products with Active 2015 Cures Update Listings",
                recentEmailStatistics.getProductsForEdition2015CuresWithActiveStatuses().getCount(),
                recentEmailStatistics.getProductsForEdition2015CuresWithActiveStatuses().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addAcbRows(table,
                recentEmailStatistics.getProductsForEdition2015CuresWithActiveStatuses().getAcbStatistics(),
                previousEmailStatistics.getProductsForEdition2015CuresWithActiveStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("l. Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Cures Update Listings",
                recentEmailStatistics.getProductsForEdition2015CuresWithSuspendedStatuses().getCount(),
                recentEmailStatistics.getProductsForEdition2015CuresWithSuspendedStatuses().getCount()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        table = addAcbRows(table,
                recentEmailStatistics.getProductsForEdition2015CuresWithSuspendedStatuses().getAcbStatistics(),
                previousEmailStatistics.getProductsForEdition2015CuresWithSuspendedStatuses().getAcbStatistics());

        table = addTableRow(table, createDataForRow("m. Total # of Unique Products with Active Listings (Regardless of Edition)",
                recentEmailStatistics.getProductsForEditionAllAndActiveStatuses(),
                recentEmailStatistics.getProductsForEditionAllAndActiveStatuses()),
                NUMBER_OF_INDENTS_SUMMARY_LEVEL_STAT, true);

        return table;
    }
}
