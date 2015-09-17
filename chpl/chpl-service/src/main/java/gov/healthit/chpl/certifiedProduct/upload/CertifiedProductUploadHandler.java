package gov.healthit.chpl.certifiedProduct.upload;

import java.util.List;

import org.apache.commons.csv.CSVRecord;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

public interface CertifiedProductUploadHandler {
	public List<CQMCriterion> getApplicableCqmCriterion(List<CQMCriterion> allCqms);
	public PendingCertifiedProductDTO parseRow() throws EntityCreationException;
	public CSVRecord getRecord();
	public void setRecord(CSVRecord record);
	public CSVRecord getHeading();
	public void setHeading(CSVRecord heading);
}
