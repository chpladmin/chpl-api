package gov.healthit.chpl.app.statistics;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import gov.healthit.chpl.domain.statistics.Statistics;

public class StatsCsvFileWriter {
	//Delimiter used in CSV file
	private static final String NEW_LINE_SEPARATOR = "\n";

    //CSV file header
    private static final Object [] FILE_HEADER = {"Date","Total Developers","Total Developers With 2014 Listings","Total Developers With 2015 Listings","Total Unique Products",
    		"Total Products With Active 2014 Listings","Total Products With Active 2015 Listings","Total Products With Active Listings",
    		"Total Listings", "Total 2014 Listings", "Total 2015 Listings", "Total 2011 Listings","Total Surveillance Activities","Total Open Surveillance Activities",
    		"Total Closed Surveillance Activities","Total NonConformities","Total Open NonConformities","Total Closed NonConformities"};

    public static void writeCsvFile(String fileName, List<Statistics> statsCsvOutput){
    	FileWriter fileWriter = null;
    	CSVPrinter csvFilePrinter = null;
    	CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
    	SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM dd yyyy");

    	try {
    		// initialize FileWriter object
    		fileWriter = new FileWriter(fileName);

    		// initialize CSVPrinter object
    		csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

    		csvFilePrinter.printRecord(FILE_HEADER);

    		// Write a new StatisticsCSVOutput object list to the CSV file
    		for (Statistics stat : statsCsvOutput){
    			List<String> statRecord = new ArrayList<String>();
   			 	dateFormat.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
   			 	String dateString = dateFormat.format(stat.getDateRange().getEndDate());
    			statRecord.add(dateString);
    			statRecord.add(String.valueOf(stat.getTotalDevelopers()));
    			statRecord.add(String.valueOf(stat.getTotalDevelopersWith2014Listings()));
    			statRecord.add(String.valueOf(stat.getTotalDevelopersWith2015Listings()));
    			statRecord.add(String.valueOf(stat.getTotalCertifiedProducts()));
    			statRecord.add(String.valueOf(stat.getTotalCPsActive2014Listings()));
    			statRecord.add(String.valueOf(stat.getTotalCPsActive2015Listings()));
    			statRecord.add(String.valueOf(stat.getTotalCPsActiveListings()));
    			statRecord.add(String.valueOf(stat.getTotalListings()));
    			statRecord.add(String.valueOf(stat.getTotal2014Listings()));
    			statRecord.add(String.valueOf(stat.getTotal2015Listings()));
    			statRecord.add(String.valueOf(stat.getTotal2011Listings()));
    			statRecord.add(String.valueOf(stat.getTotalSurveillanceActivities()));
    			statRecord.add(String.valueOf(stat.getTotalOpenSurveillanceActivities()));
    			statRecord.add(String.valueOf(stat.getTotalClosedSurveillanceActivities()));
    			statRecord.add(String.valueOf(stat.getTotalNonConformities()));
    			statRecord.add(String.valueOf(stat.getTotalOpenNonconformities()));
    			statRecord.add(String.valueOf(stat.getTotalClosedNonconformities()));
    			csvFilePrinter.printRecord(statRecord);
    		}

    		System.out.println("CSV file was created successfully!");

    	} catch (Exception e){
    		System.out.println("Error in CsvFileWriter!");
    		e.printStackTrace();
    	} finally {
    		try {
    			fileWriter.flush();
    			fileWriter.close();
    			csvFilePrinter.close();
    		} catch (final IOException e) {
    			System.out.println("Error while flushing/closing fileWriter/csvPrinter!");
    			e.printStackTrace();
    		}
    	}
    }

}
