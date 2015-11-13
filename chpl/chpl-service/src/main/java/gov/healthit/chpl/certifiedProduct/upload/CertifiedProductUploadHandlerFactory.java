package gov.healthit.chpl.certifiedProduct.upload;

import org.apache.commons.csv.CSVRecord;

import gov.healthit.chpl.web.controller.InvalidArgumentsException;

public interface CertifiedProductUploadHandlerFactory {
	public CertifiedProductUploadHandler getHandler(CSVRecord heading, CSVRecord cpRecord) throws InvalidArgumentsException;
}
