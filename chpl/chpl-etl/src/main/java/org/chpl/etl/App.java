package org.chpl.etl;

import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Hello world!
 *
 */
public class App 
{

	private static final String csvRawFileName = "./src/main/resources/chpl-raw.csv";
	private static final String csvChecksummedFileName = "./src/main/resources/chpl-wChecksum.csv";

	public static void main( String[] args ) {
		String fileBase = "./src/main/resources/chpl-small-";
		for (int i = 0; i < 6; i++) {
			parseFile(fileBase + i + ".xlsx");
		}
	}
	
	public static void parseFile(String filename) {
		ExcelConverter excelConverter = new ExcelConverter(filename,csvRawFileName);
		excelConverter.convert();
		excelConverter.setCsvHash(csvChecksummedFileName);
		excelConverter.calculateHash();

		EtlGraph etlGraph = null;
		try {
			etlGraph = new EtlGraph();
		} catch (URISyntaxException e) {
			Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, e);
		}
		
		etlGraph.setGraph("/graphs/checksum-analysis.grf");
        etlGraph.execute();
		etlGraph.setGraph("/graphs/create_vendor-product.grf");
        etlGraph.execute();
		etlGraph.setGraph("/graphs/create_certified-product.grf");
        etlGraph.execute();		
	}
}
