package gov.healthit.chpl.upload.surveillance;

import java.util.List;

import org.apache.commons.csv.CSVRecord;

import gov.healthit.chpl.web.controller.InvalidArgumentsException;

public interface SurveillanceUploadHandlerFactory {
	public SurveillanceUploadHandler getHandler(CSVRecord heading, List<CSVRecord> survRecords)
			throws InvalidArgumentsException;
}
