package gov.healthit.chpl.certifiedProduct.upload;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;

@Component("updateOrDeleteCertifiedProductHandler")
public class UpdateOrDeleteCertifiedProductHandler implements CertifiedProductUploadHandler {

	private CSVRecord record;
	private CSVRecord heading;
	
	@Override
	public PendingCertifiedProductEntity handle() {
		PendingCertifiedProductEntity entity = new PendingCertifiedProductEntity();
		String uniqueId = getRecord().get(0);
		entity.setUniqueId(uniqueId);
		String recordStatus = getRecord().get(1);
		entity.setRecordStatus(recordStatus);
		return entity;
	}

	@Override
	public List<CQMCriterion> getApplicableCqmCriterion(List<CQMCriterion> allCqms) {
		return null;
	}

	@Override
	public CSVRecord getRecord() {
		return record;
	}

	@Override
	public void setRecord(CSVRecord record) {
		this.record = record;
	}

	@Override
	public CSVRecord getHeading() {
		return heading;
	}

	@Override
	public void setHeading(CSVRecord heading) {
		this.heading = heading;
	}

}
