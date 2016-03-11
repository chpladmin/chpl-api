package gov.healthit.chpl.certifiedProduct.upload;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.ContactDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.dao.PracticeTypeDAO;
import gov.healthit.chpl.dao.ProductClassificationTypeDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.dao.UcdProcessDAO;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

public abstract class CertifiedProductUploadHandlerImpl implements CertifiedProductUploadHandler {
	@Autowired protected CertifiedProductDAO certifiedProductDao;
	@Autowired protected PracticeTypeDAO practiceTypeDao;
	@Autowired protected DeveloperDAO developerDao;
	@Autowired protected AddressDAO addressDao;
	@Autowired protected ContactDAO contactDao;
	@Autowired protected ProductDAO productDao;
	@Autowired protected ProductVersionDAO versionDao;
	@Autowired protected CertificationEditionDAO editionDao;
	@Autowired protected CertificationBodyDAO acbDao;
	@Autowired protected TestingLabDAO atlDao;
	@Autowired protected ProductClassificationTypeDAO classificationDao;
	@Autowired protected CertificationCriterionDAO certDao;;
	@Autowired protected CQMCriterionDAO cqmDao;
	@Autowired protected CertificationStatusDAO statusDao;
	@Autowired protected QmsStandardDAO qmsDao;
	@Autowired protected TestFunctionalityDAO testFunctionalityDao;
	@Autowired protected TestProcedureDAO testProcedureDao;
	@Autowired protected TestStandardDAO testStandardDao;
	@Autowired protected TestToolDAO testToolDao;
	@Autowired protected UcdProcessDAO ucdDao;
	
	@Autowired private PendingCertifiedProductDAO pendingCpDao;
	
	private static final String CERTIFICATION_DATE_FORMAT = "yyyyMMdd";
	protected SimpleDateFormat dateFormatter;
	
	private List<CSVRecord> record;
	private CSVRecord heading;
	private int lastDataIndex;
	
	public CertifiedProductUploadHandlerImpl() {
		dateFormatter = new SimpleDateFormat(CERTIFICATION_DATE_FORMAT);
	}
	
	public abstract PendingCertifiedProductEntity handle();
	public abstract List<CQMCriterion> getApplicableCqmCriterion(List<CQMCriterion> allCqms);
	public abstract Long getDefaultStatusId();
	
	@Override
	public List<CSVRecord> getRecord() {
		return record;
	}

	@Override
	public void setRecord(List<CSVRecord> record) {
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

	public int getLastDataIndex() {
		return lastDataIndex;
	}

	public void setLastDataIndex(int lastDataIndex) {
		this.lastDataIndex = lastDataIndex;
	}
}
