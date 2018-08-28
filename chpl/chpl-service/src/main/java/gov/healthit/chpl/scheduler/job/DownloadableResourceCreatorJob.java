package gov.healthit.chpl.scheduler.job;

import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

/**
 * Basic class for any Job to create downloadable files.
 * @author alarned
 *
 */
public abstract class DownloadableResourceCreatorJob extends QuartzJob {
//    private static final Logger LOGGER = LogManager.getLogger(DownloadableResourceCreatorJob.class);

    
    private SimpleDateFormat timestampFormat;
    
    @Autowired
    private CertifiedProductDetailsManager cpdManager;
    
    @Autowired
    private CertifiedProductDAO certifiedProductDao;
    
    @Autowired
    private CertificationCriterionDAO criteriaDao;
    
    @Autowired
    private CertificationResultDAO certificationResultDao;
    
    @Autowired
    private CertificationResultDetailsDAO certificationResultDetailsDao;
    
    /**
     * Default constructor; creates time stamp format.
     */
    public DownloadableResourceCreatorJob() {
        timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    }

    public CertifiedProductDAO getCertifiedProductDao() {
        return certifiedProductDao;
    }

    public void setCertifiedProductDao(final CertifiedProductDAO certifiedProductDao) {
        this.certifiedProductDao = certifiedProductDao;
    }

    public SimpleDateFormat getTimestampFormat() {
        return timestampFormat;
    }

    public void setTimestampFormat(final SimpleDateFormat timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    public CertifiedProductDetailsManager getCpdManager() {
        return cpdManager;
    }

    public void setCpdManager(final CertifiedProductDetailsManager cpdManager) {
        this.cpdManager = cpdManager;
    }

    public CertificationCriterionDAO getCriteriaDao() {
        return criteriaDao;
    }

    public void setCriteriaDao(final CertificationCriterionDAO criteriaDao) {
        this.criteriaDao = criteriaDao;
    }

    public CertificationResultDAO getCertificationResultDao() {
        return certificationResultDao;
    }

    public void setCertificationResultDao(
            final CertificationResultDAO certificationResultDao) {
        this.certificationResultDao = certificationResultDao;
    }

    public CertificationResultDetailsDAO getCertificationResultDetailsDao() {
        return certificationResultDetailsDao;
    }

    public void setCertificationResultDetailsDao(
            final CertificationResultDetailsDAO certificationResultDetailsDao) {
        this.certificationResultDetailsDao = certificationResultDetailsDao;
    }

}
