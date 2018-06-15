package gov.healthit.chpl.scheduler.job;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.springframework.context.support.AbstractApplicationContext;

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
    private CertifiedProductDetailsManager cpdManager;
    private CertifiedProductDAO certifiedProductDao;
    private CertificationCriterionDAO criteriaDao;
    private CertificationResultDAO certificationResultDao;
    private CertificationResultDetailsDAO certificationResultDetailsDao;
    private AbstractApplicationContext applicationContext;

    /**
     * Default constructor; creates time stamp format.
     */
    public DownloadableResourceCreatorJob() {
        timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    }

    @Override
    protected void initiateSpringBeans(final AbstractApplicationContext context) throws IOException {
        this.setCpdManager((CertifiedProductDetailsManager) context.getBean("certifiedProductDetailsManager"));
        this.setCertifiedProductDao((CertifiedProductDAO) context.getBean("certifiedProductDAO"));
        this.setCriteriaDao((CertificationCriterionDAO) context.getBean("certificationCriterionDAO"));
        this.setCertificationResultDao((CertificationResultDAO) context.getBean("certificationResultDAO"));
        this.setCertificationResultDetailsDao((CertificationResultDetailsDAO)
                context.getBean("certificationResultDetailsDAO"));

        setApplicationContext(context);
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

    public AbstractApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(final AbstractApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
