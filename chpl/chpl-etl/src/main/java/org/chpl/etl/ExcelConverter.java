package org.chpl.etl;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.XLSX2CSV;
import org.xml.sax.SAXException;

public class ExcelConverter {

	private File xlsxFile;
	private File csvFile;

	public ExcelConverter(String xlsxName, String csvName) {
		xlsxFile = new File(xlsxName);
		csvFile = new File(csvName);		
	}

	public void convert() {
		OPCPackage p;
		try {
			PrintStream fOut = new PrintStream(csvFile);
			p = OPCPackage.open(xlsxFile.getPath(), PackageAccess.READ);
			XLSX2CSV xlsx2csv = new XLSX2CSV(p, fOut, -1);
			xlsx2csv.process();
		} catch ( IOException | OpenXML4JException |
				ParserConfigurationException | SAXException e) {
			Logger.getLogger(ExcelConverter.class.getName()).log(Level.SEVERE, null, e);
		}
	}
}
