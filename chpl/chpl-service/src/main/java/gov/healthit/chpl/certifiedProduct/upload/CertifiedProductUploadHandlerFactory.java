package gov.healthit.chpl.certifiedProduct.upload;

import java.util.List;

import org.apache.commons.csv.CSVRecord;

import gov.healthit.chpl.web.controller.InvalidArgumentsException;

public interface CertifiedProductUploadHandlerFactory {
	public CertifiedProductUploadHandler getHandler(CSVRecord heading, List<CSVRecord> cpRecords) throws InvalidArgumentsException;
}
