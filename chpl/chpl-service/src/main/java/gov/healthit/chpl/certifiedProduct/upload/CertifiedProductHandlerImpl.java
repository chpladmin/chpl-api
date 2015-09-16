package gov.healthit.chpl.certifiedProduct.upload;

import java.text.SimpleDateFormat;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.PendingCertifiedProductDao;
import gov.healthit.chpl.dao.PracticeTypeDAO;
import gov.healthit.chpl.dao.ProductClassificationTypeDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.dao.VendorDAO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;

public abstract class CertifiedProductHandlerImpl implements CertifiedProductHandler {
	@Autowired protected PracticeTypeDAO practiceTypeDao;
	@Autowired protected VendorDAO vendorDao;
	@Autowired protected AddressDAO addressDao;
	@Autowired protected ProductDAO productDao;
	@Autowired protected ProductVersionDAO versionDao;
	@Autowired protected CertificationEditionDAO editionDao;
	@Autowired protected CertificationBodyDAO acbDao;
	@Autowired protected ProductClassificationTypeDAO classificationDao;
	@Autowired protected CertificationCriterionDAO certDao;;
	@Autowired protected CQMCriterionDAO cqmDao;
	
	@Autowired private PendingCertifiedProductDao pendingCpDao;
	
	private static final String CERTIFICATION_DATE_FORMAT = "M/d/yyyy";
	protected SimpleDateFormat dateFormatter;
	
	private CSVRecord record;
	private CSVRecord heading;
	
	public CertifiedProductHandlerImpl() {
		dateFormatter = new SimpleDateFormat(CERTIFICATION_DATE_FORMAT);
	}
	
	protected abstract PendingCertifiedProductEntity handle();

	@Override
	public PendingCertifiedProductDTO parseRow() throws EntityCreationException {
		PendingCertifiedProductEntity entity = handle();
		PendingCertifiedProductDTO dto = pendingCpDao.create(entity);
		return dto;
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
