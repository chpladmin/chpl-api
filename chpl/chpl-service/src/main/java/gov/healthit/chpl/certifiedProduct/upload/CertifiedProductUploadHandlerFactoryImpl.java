package gov.healthit.chpl.certifiedProduct.upload;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.web.controller.InvalidArgumentsException;

@Service
public class CertifiedProductUploadHandlerFactoryImpl implements CertifiedProductUploadHandlerFactory {
	private static int NUM_FIELDS_2014 = 276;
	
	@Autowired private NewCertifiedProductHandler2011 handler2011;
	@Autowired private NewCertifiedProductHandler2014 handler2014;
	
	private CertifiedProductUploadHandlerFactoryImpl() {}
	
	@Override
	public CertifiedProductUploadHandlerImpl getHandler(CSVRecord heading, CSVRecord cpRecord) throws InvalidArgumentsException {
		CertifiedProductUploadHandlerImpl handler = null;
		
		//what type of handler do we need?
		CertifiedProductUploadType uploadType = CertifiedProductUploadType.valueOf(cpRecord.get(1).toUpperCase());

		int year = -1;
		if(heading.size() == NUM_FIELDS_2014) {
			//2014
			if(uploadType == CertifiedProductUploadType.NEW) {
				handler = handler2014;
			}
		} else {
			//TODO: we will check other sizes (or whatever distinguishes between upload files for years 2014 and 2015)
			throw new InvalidArgumentsException("Expected " + NUM_FIELDS_2014 + " fields in the record but found " + cpRecord.size());
		}
		
		if(handler != null) {
			handler.setRecord(cpRecord);
			handler.setHeading(heading);
		}
		return handler;
	}
}
