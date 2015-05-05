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

	private static final String csvFileName = "./src/main/resources/chpl.csv";

	public static void main( String[] args ) {
		String xlsxFileName = "./src/main/resources/chpl-large.xlsx";

		ExcelConverter excelConverter = new ExcelConverter(xlsxFileName,csvFileName);
		excelConverter.convert();

		EtlGraph etlGraph = null;
		try {
			etlGraph = new EtlGraph();
		} catch (URISyntaxException e) {
			Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, e);
		}
		
		etlGraph.setGraph("/vendor-product.grf");
        etlGraph.execute();
	}
}
