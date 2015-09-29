package gov.healthit.chpl.certifiedProduct.upload;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.web.controller.InvalidArgumentsException;

public interface CertifiedProductUploadHandlerFactory {
	public CertifiedProductUploadHandlerImpl getHandler(CSVRecord heading, CSVRecord cpRecord) throws InvalidArgumentsException;
}
