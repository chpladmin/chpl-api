package gov.healthit.chpl.certifiedProduct.upload;

import org.apache.commons.csv.CSVRecord;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

public interface CertifiedProductHandler {
	
	public PendingCertifiedProductDTO parseRow() throws EntityCreationException;
	public CSVRecord getRecord();
	public void setRecord(CSVRecord record);
	public CSVRecord getHeading();
	public void setHeading(CSVRecord heading);
}
