package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.builder.AnnualReportBuilderXlsx;
import gov.healthit.chpl.builder.QuarterlyReportBuilderXlsx;
import gov.healthit.chpl.dao.surveillance.report.AnnualReportDAO;
import gov.healthit.chpl.dao.surveillance.report.QuarterDAO;
import gov.healthit.chpl.dao.surveillance.report.QuarterlyReportDAO;
import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.manager.SurveillanceReportManager;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Service
public class SurveillanceReportManagerImpl extends SecuredManager implements SurveillanceReportManager {
    private QuarterlyReportDAO quarterlyDao;
    private AnnualReportDAO annualDao;
    private QuarterDAO quarterDao;
    private ErrorMessageUtil msgUtil;
    private QuarterlyReportBuilderXlsx quarterlyReportBuilder;
    private AnnualReportBuilderXlsx annualReportBuilder;

    @Autowired
    public SurveillanceReportManagerImpl(final QuarterlyReportDAO quarterlyDao,
            final AnnualReportDAO annualDao, final QuarterDAO quarterDao,
            final ErrorMessageUtil msgUtil, final QuarterlyReportBuilderXlsx quarterlyReportBuilder,
            final AnnualReportBuilderXlsx annualReportBuilder) {
        this.quarterlyDao = quarterlyDao;
        this.annualDao = annualDao;
        this.quarterDao = quarterDao;
        this.msgUtil = msgUtil;
        this.quarterlyReportBuilder = quarterlyReportBuilder;
        this.annualReportBuilder = annualReportBuilder;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).CREATE_ANNUAL, #toCreate)")
    public AnnualReportDTO createAnnualReport(final AnnualReportDTO toCreate)
    throws EntityCreationException, InvalidArgumentsException {
        //Annual report has to be associated with a year and an ACB

        if (toCreate == null || toCreate.getYear() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.annualSurveillance.missingYear"));
        } else if (toCreate.getAcb() == null || toCreate.getAcb().getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.annualSurveillance.missingAcb"));
        }

        //make sure there's not already an annual report for this acb and year
        AnnualReportDTO existingAnnualReport =
                annualDao.getByAcbAndYear(toCreate.getAcb().getId(), toCreate.getYear());
        if (existingAnnualReport != null) {
            throw new EntityCreationException(msgUtil.getMessage("report.annualSurveillance.exists"));
        }

        AnnualReportDTO created = annualDao.create(toCreate);
        return created;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).UPDATE_ANNUAL, #toUpdate)")
    public AnnualReportDTO updateAnnualReport(final AnnualReportDTO toUpdate)
    throws EntityRetrievalException {
        AnnualReportDTO updated = annualDao.update(toUpdate);
        return updated;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).DELETE_ANNUAL, #id)")
    public void deleteAnnualReport(final Long id) throws EntityRetrievalException {
        annualDao.delete(id);
    }

    /**
     * Returns all the annual reports the current user has access to.
     */
    @Override
    @Transactional
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_ANNUAL, filterObject)")
    public List<AnnualReportDTO> getAnnualReports() {
        return annualDao.getAll();
    }

    /**
     * Gets the quarterly report by ID if the user has access.
     */
    @Override
    @Transactional
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_ANNUAL,"
            + "returnObject)")
    public AnnualReportDTO getAnnualReport(final Long id) throws EntityRetrievalException {
        return annualDao.getById(id);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).EXPORT_ANNUAL, "
            + "#id)")
    public Workbook exportAnnualReport(final Long id) throws EntityRetrievalException, IOException {
        AnnualReportDTO report = getAnnualReport(id);
        return annualReportBuilder.buildXlsx(report);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).CREATE_QUARTERLY, #toCreate)")
    public QuarterlyReportDTO createQuarterlyReport(final QuarterlyReportDTO toCreate)
    throws EntityCreationException, InvalidArgumentsException {
        //Quarterly report has to have an ACB, year, and quarter
        if (toCreate.getYear() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.quarterlySurveillance.missingYear"));
        }
        if (toCreate.getAcb() == null || toCreate.getAcb().getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.quarterlySurveillance.missingAcb"));
        } 
        if (toCreate.getQuarter() == null
                || (toCreate.getQuarter().getId() == null && StringUtils.isEmpty(toCreate.getQuarter().getName()))) {
            throw new InvalidArgumentsException("report.quarterlySurveillance.missingQuarter");
        } else if (toCreate.getQuarter().getId() == null && toCreate.getQuarter().getName() != null) {
            QuarterDTO quarter = quarterDao.getByName(toCreate.getQuarter().getName());
            if (quarter == null) {
                throw new InvalidArgumentsException(
                        msgUtil.getMessage("report.quarterlySurveillance.badQuarter", toCreate.getQuarter().getName()));
            }
            toCreate.setQuarter(quarter);
        }

        //make sure there's not already a quarterly report for this acb and year and quarter
        QuarterlyReportDTO existingQuarterlyReport =
                quarterlyDao.getByQuarterAndAcbAndYear(toCreate.getQuarter().getId(),
                        toCreate.getAcb().getId(),
                        toCreate.getYear());
        if (existingQuarterlyReport != null) {
            throw new EntityCreationException(msgUtil.getMessage("report.quarterlySurveillance.exists"));
        }

        QuarterlyReportDTO created = quarterlyDao.create(toCreate);
        return created;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).UPDATE_QUARTERLY, #toUpdate)")
    public QuarterlyReportDTO updateQuarterlyReport(final QuarterlyReportDTO toUpdate)
    throws EntityRetrievalException {
        QuarterlyReportDTO updated = quarterlyDao.update(toUpdate);
        return updated;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).DELETE_QUARTERLY, #id)")
    public void deleteQuarterlyReport(final Long id) throws EntityRetrievalException {
        quarterlyDao.delete(id);
    }

    /**
     * Returns all the quarterly reports the current user has access to.
     */
    @Override
    @Transactional
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY, filterObject)")
    public List<QuarterlyReportDTO> getQuarterlyReports() {
        return quarterlyDao.getAll();
    }

    /**
     * Gets the quarterly report by ID if the user has access.
     */
    @Override
    @Transactional
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY,"
            + "returnObject)")
    public QuarterlyReportDTO getQuarterlyReport(final Long id) throws EntityRetrievalException {
        return quarterlyDao.getById(id);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).EXPORT_QUARTERLY, "
            + "#id)")
    public Workbook exportQuarterlyReport(final Long id) throws EntityRetrievalException,
        IOException {
        QuarterlyReportDTO report = getQuarterlyReport(id);
        return quarterlyReportBuilder.buildXlsx(report);
    }
}
