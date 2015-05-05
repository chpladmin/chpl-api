package org.chpl.etl;

import java.net.URISyntaxException;


/**
 * Hello world!
 *
 */
public class App 
{
	public static void main( String[] args )
	{
	private static final String csvFileName = "./src/main/resources/chpl.csv";

	public static void main( String[] args ) {
		String xlsxFileName = "./src/main/resources/chpl-large.xlsx";

		ExcelConverter excelConverter = new ExcelConverter(xlsxFileName,csvFileName);
		excelConverter.convert();

		etlGraph.setGraph("/vendor-product.grf");
        etlGraph.execute();
	}
}
