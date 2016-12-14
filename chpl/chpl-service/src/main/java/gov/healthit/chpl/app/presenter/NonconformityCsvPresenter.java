package gov.healthit.chpl.app.presenter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.domain.CertifiedProductDownloadResponse;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceRequirement;

/**
 * writes out only surveillance records that resulted in nonconformities
 * @author kekey
 *
 */
public class NonconformityCsvPresenter extends SurveillanceCsvPresenter {
	private static final Logger logger = LogManager.getLogger(NonconformityCsvPresenter.class);
	
	public void presentAsFile(File file, CertifiedProductDownloadResponse cpList) {
		FileWriter writer = null;
		CSVPrinter csvPrinter = null;
		try {
			writer = new FileWriter(file);
			csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL);
			csvPrinter.printRecord(generateHeaderValues());
			
			for(CertifiedProductSearchDetails cp : cpList.getProducts()) {
				if(cp.getSurveillance() != null && cp.getSurveillance().size() > 0) {
					for(Surveillance currSurveillance : cp.getSurveillance()) {
						//note if this surveillance has any nonconformities
						boolean hasNc = false;
						if(currSurveillance.getRequirements() != null && currSurveillance.getRequirements().size() > 0) {
							//marks requirements for removal if they have no nonconformities
							List<SurveillanceRequirement> reqsToRemove = new ArrayList<SurveillanceRequirement>();
							for(SurveillanceRequirement req : currSurveillance.getRequirements()) {
								if(req.getNonconformities() != null && req.getNonconformities().size() > 0) {
									hasNc = true;
								} else {
									reqsToRemove.add(req);
								}
							}
							
							//remove requirements without nonconformities
							for(SurveillanceRequirement reqToRemove : reqsToRemove) {
								currSurveillance.getRequirements().remove(reqToRemove);
							}
						}
						
						if(hasNc) {
							//write out surveillance with nonconformities only
							List<List<String>> rowValues = generateMultiRowValue(cp, currSurveillance);
							for(List<String> rowValue : rowValues) {
								csvPrinter.printRecord(rowValue);
							}
						}
					}
				}
			}
		} catch(IOException ex) {
			logger.error("Could not write file " + file.getName(), ex);
		} finally {
			try {
				writer.flush();
				writer.close();
				csvPrinter.flush();
				csvPrinter.close();
			} catch(Exception ignore) {}
		}
	}
}
