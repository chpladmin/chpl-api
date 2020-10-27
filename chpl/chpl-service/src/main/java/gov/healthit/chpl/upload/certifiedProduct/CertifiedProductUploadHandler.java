package gov.healthit.chpl.upload.certifiedProduct;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.AgeRangeDAO;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchDAO;
import gov.healthit.chpl.dao.ContactDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.EducationTypeDAO;
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dao.PracticeTypeDAO;
import gov.healthit.chpl.dao.ProductClassificationTypeDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.dao.TargetedUserDAO;
import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.dao.UcdProcessDAO;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.dto.UploadTemplateVersionDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.listing.mipsMeasure.ListingMipsMeasureDAO;
import gov.healthit.chpl.listing.mipsMeasure.MipsMeasureDAO;

public abstract class CertifiedProductUploadHandler {
    @Autowired
    protected CertifiedProductDAO certifiedProductDao;
    @Autowired
    protected CertifiedProductSearchDAO cpSearchDao;
    @Autowired
    protected PracticeTypeDAO practiceTypeDao;
    @Autowired
    protected DeveloperDAO developerDao;
    @Autowired
    protected AddressDAO addressDao;
    @Autowired
    protected ContactDAO contactDao;
    @Autowired
    protected ProductDAO productDao;
    @Autowired
    protected ProductVersionDAO versionDao;
    @Autowired
    protected CertificationEditionDAO editionDao;
    @Autowired
    protected CertificationBodyDAO acbDao;
    @Autowired
    protected TestingLabDAO atlDao;
    @Autowired
    protected ProductClassificationTypeDAO classificationDao;
    @Autowired
    protected CertificationCriterionDAO certDao;
    @Autowired
    protected CQMCriterionDAO cqmDao;
    @Autowired
    protected QmsStandardDAO qmsDao;
    @Autowired
    protected AccessibilityStandardDAO stdDao;
    @Autowired
    protected TargetedUserDAO tuDao;
    @Autowired
    protected TestFunctionalityDAO testFunctionalityDao;
    @Autowired
    protected TestProcedureDAO testProcedureDao;
    @Autowired
    protected TestDataDAO testDataDao;
    @Autowired
    protected TestStandardDAO testStandardDao;
    @Autowired
    protected TestToolDAO testToolDao;
    @Autowired
    protected UcdProcessDAO ucdDao;
    @Autowired
    protected EducationTypeDAO educationDao;
    @Autowired
    protected AgeRangeDAO ageDao;
    @Autowired
    protected MipsMeasureDAO mipsMeasureDao;
    @Autowired
    protected MacraMeasureDAO macraMeasureDao;
    @Autowired
    protected ListingMipsMeasureDAO listingMipsDao;

    private static final String CERTIFICATION_DATE_FORMAT = "yyyyMMdd";
    protected SimpleDateFormat dateFormatter;

    private List<CSVRecord> record;
    private CSVRecord heading;
    private UploadTemplateVersionDTO uplodTemplateVersion;

    public CertifiedProductUploadHandler() {
        dateFormatter = new SimpleDateFormat(CERTIFICATION_DATE_FORMAT);
    }

    public abstract PendingCertifiedProductEntity handle() throws InvalidArgumentsException;

    public abstract List<CQMCriterion> getApplicableCqmCriterion(List<CQMCriterion> allCqms);

    public List<CSVRecord> getRecord() {
        return record;
    }

    public void setRecord(final List<CSVRecord> record) {
        this.record = record;
    }

    public CSVRecord getHeading() {
        return heading;
    }

    public void setHeading(final CSVRecord heading) {
        this.heading = heading;
    }

    public UploadTemplateVersionDTO getUploadTemplateVersion() {
        return uplodTemplateVersion;
    }

    public void setUploadTemplateVersion(final UploadTemplateVersionDTO template) {
        this.uplodTemplateVersion = template;
    }
}
