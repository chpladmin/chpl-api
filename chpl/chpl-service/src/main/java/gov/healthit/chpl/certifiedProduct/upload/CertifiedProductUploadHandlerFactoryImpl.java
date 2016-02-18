package gov.healthit.chpl.certifiedProduct.upload;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.web.controller.InvalidArgumentsException;

@Service
public class CertifiedProductUploadHandlerFactoryImpl implements CertifiedProductUploadHandlerFactory {
	public static int NUM_FIELDS_2014 = 276;
	public static int NUM_FIELDS_2014_EXTENDED = 279;
	
	@Autowired private CertifiedProductHandler2014 handler2014;
	
	private CertifiedProductUploadHandlerFactoryImpl() {}
	
	@Override
	public CertifiedProductUploadHandler getHandler(CSVRecord heading, List<CSVRecord> cpRecords) throws InvalidArgumentsException {
		CertifiedProductUploadHandler handler = null;
		
		if(heading.size() == NUM_FIELDS_2014 || heading.size() == NUM_FIELDS_2014_EXTENDED) {
			handler = handler2014;
		} else {
			throw new InvalidArgumentsException("Expected " + NUM_FIELDS_2014 + " or " + NUM_FIELDS_2014_EXTENDED + " fields in the record but found " + heading.size());
		}
		
		handler.setRecord(cpRecords);
		handler.setHeading(heading);
		return handler;
	}
}
