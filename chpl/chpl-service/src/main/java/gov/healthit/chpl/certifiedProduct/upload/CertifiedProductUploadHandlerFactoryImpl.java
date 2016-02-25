package gov.healthit.chpl.certifiedProduct.upload;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.web.controller.InvalidArgumentsException;

@Service
public class CertifiedProductUploadHandlerFactoryImpl implements CertifiedProductUploadHandlerFactory {
	public static int NUM_FIELDS_2014 = 712;
	
	@Autowired private CertifiedProductHandler2014 handler2014;
	
	private CertifiedProductUploadHandlerFactoryImpl() {}
	
	@Override
	public CertifiedProductUploadHandler getHandler(CSVRecord heading, List<CSVRecord> cpRecords) throws InvalidArgumentsException {
		CertifiedProductUploadHandler handler = null;
		
		int lastDataIndex = -1;
		for(int i = 0; i < heading.size() && lastDataIndex < 0; i++) {
			String headingValue = heading.get(i);
			if(StringUtils.isEmpty(headingValue)) {
				lastDataIndex = i-1;
			} else if(i == heading.size()-1) {
				lastDataIndex = i;
			}
		}
		
		if((lastDataIndex+1) == NUM_FIELDS_2014) {
			handler = handler2014;
		} else {
			throw new InvalidArgumentsException("Expected " + NUM_FIELDS_2014 + " fields in the record but found " + (lastDataIndex+1));
		}
		
		handler.setRecord(cpRecords);
		handler.setHeading(heading);
		handler.setLastDataIndex(lastDataIndex);
		return handler;
	}
}
