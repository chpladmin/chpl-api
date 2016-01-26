package gov.healthit.chpl.certifiedProduct.upload;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.dao.AdditionalSoftwareDAO;
import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.dao.PracticeTypeDAO;
import gov.healthit.chpl.dao.ProductClassificationTypeDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.CertificationStatusEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;

public abstract class CertifiedProductUploadHandlerImpl implements CertifiedProductUploadHandler {
	@Autowired protected PracticeTypeDAO practiceTypeDao;
	@Autowired protected DeveloperDAO developerDao;
	@Autowired protected AddressDAO addressDao;
	@Autowired protected ProductDAO productDao;
	@Autowired protected ProductVersionDAO versionDao;
	@Autowired protected CertificationEditionDAO editionDao;
	@Autowired protected AdditionalSoftwareDAO additionalSoftwareDao;
	@Autowired protected CertificationBodyDAO acbDao;
	@Autowired protected ProductClassificationTypeDAO classificationDao;
	@Autowired protected CertificationCriterionDAO certDao;;
	@Autowired protected CQMCriterionDAO cqmDao;
	@Autowired protected CertificationStatusDAO statusDao;
	
	@Autowired private PendingCertifiedProductDAO pendingCpDao;
	
	private static final String CERTIFICATION_DATE_FORMAT = "M/d/yyyy";
	protected SimpleDateFormat dateFormatter;
	
	private CSVRecord record;
	private CSVRecord heading;
	
	public CertifiedProductUploadHandlerImpl() {
		dateFormatter = new SimpleDateFormat(CERTIFICATION_DATE_FORMAT);
	}
	
	public abstract PendingCertifiedProductEntity handle();
	public abstract List<CQMCriterion> getApplicableCqmCriterion(List<CQMCriterion> allCqms);
	public abstract Long getDefaultStatusId();
	
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
