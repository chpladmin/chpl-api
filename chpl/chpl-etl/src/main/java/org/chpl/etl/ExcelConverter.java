package org.chpl.etl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.XLSX2CSV;
import org.xml.sax.SAXException;

public class ExcelConverter {

	private File xlsxFile;
	private File csvFile;
	private File csvHashFile;
	private String delimiter = "^";

	public ExcelConverter() {
	}

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
	
	public void calculateHash() {
		FileInputStream fIn = null;
		FileOutputStream fOut = null;
		BufferedReader bIn = null;
		BufferedWriter bOut = null;
		String line, hash;
		
		try {
			fIn = new FileInputStream(csvFile);
			fOut = new FileOutputStream(csvHashFile);
			bIn = new BufferedReader(new InputStreamReader(fIn));
			bOut = new BufferedWriter(new OutputStreamWriter(fOut));
			
			line = bIn.readLine();
			while (line != null) {
				hash = hashString(line);
				bOut.write(line);
				bOut.write(delimiter);;
				bOut.write(hash);
				bOut.newLine();
				line = bIn.readLine();
			}
		} catch (IOException e) {
			Logger.getLogger(ExcelConverter.class.getName()).log(Level.SEVERE, null, e);
		} finally {
			try {
				bIn.close();
				bOut.close();
				fIn.close();
				fOut.close();
			} catch (IOException e) {
				Logger.getLogger(ExcelConverter.class.getName()).log(Level.SEVERE, null, e);
			}
		}
	}
	
	private String hashString(String l) {
		return(DigestUtils.sha1Hex(l));
	}
	
	public void setXlsx(String xlsxName) {
		xlsxFile = new File(xlsxName);
	}
	
	public void setCsv(String csvName) {
		csvFile = new File(csvName);
	}
	
	public void setCsvHash(String csvHashName) {
		csvHashFile = new File(csvHashName);
	}
	
	public void setDelimeter(String delimeter) {
		this.delimiter = delimeter;
	}
	
	public String getDelimeter() {
		return this.delimiter;
	}
}
