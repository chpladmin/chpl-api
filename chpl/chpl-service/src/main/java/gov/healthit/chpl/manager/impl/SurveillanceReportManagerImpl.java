package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.surveillance.report.AnnualReportDAO;
import gov.healthit.chpl.dao.surveillance.report.QuarterDAO;
import gov.healthit.chpl.dao.surveillance.report.QuarterlyReportDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.manager.SurveillanceReportManager;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Service
public class SurveillanceReportManagerImpl extends SecuredManager implements SurveillanceReportManager {
    private static final Logger LOGGER = LogManager.getLogger(SurveillanceReportManagerImpl.class);

    private CertifiedProductDetailsManager detailsManager;
    private SurveillanceManager survManager;
    private QuarterlyReportDAO quarterlyDao;
    private AnnualReportDAO annualDao;
    private QuarterDAO quarterDao;
    private CertifiedProductDAO listingDao;
    private ErrorMessageUtil msgUtil;
    private QuarterlyReportBuilderXlsx quarterlyReportBuilder;
    private AnnualReportBuilderXlsx annualReportBuilder;

    @Autowired
    public SurveillanceReportManagerImpl(final CertifiedProductDetailsManager detailsManager,
            final SurveillanceManager survManager, final QuarterlyReportDAO quarterlyDao,
            final AnnualReportDAO annualDao, final QuarterDAO quarterDao,
            final CertifiedProductDAO listingDao, final ErrorMessageUtil msgUtil) {
        this.detailsManager = detailsManager;
        this.survManager = survManager;
        this.quarterlyDao = quarterlyDao;
        this.annualDao = annualDao;
        this.quarterDao = quarterDao;
        this.listingDao = listingDao;
        this.msgUtil = msgUtil;
        this.quarterlyReportBuilder = new QuarterlyReportBuilderXlsx();
        this.annualReportBuilder = new AnnualReportBuilderXlsx();
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
        List<QuarterlyReportDTO> quarterlyReports = 
                getQuarterlyReports(report.getAcb().getId(), report.getYear());
        Map<QuarterlyReportDTO, List<CertifiedProductSearchDetails>> reportListingMap = 
                new HashMap<QuarterlyReportDTO, List<CertifiedProductSearchDetails>>(quarterlyReports.size());
        for (QuarterlyReportDTO quarterlyReport : quarterlyReports) {
            //get all of the surveillance details for the listings relevant to this report
            //the details object included on the quarterly report has some of the data that is needed
            //to build activities and outcomes worksheet but not all of it so we need to do
            //some other work to get the necessary data and put it all together
            reportListingMap.put(quarterlyReport, getRelevantListingDetails(quarterlyReport));
        }
        return annualReportBuilder.buildXlsx(report, reportListingMap);
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
        List<QuarterlyReportDTO> reports = quarterlyDao.getAll();
        //get relevant listings for each report
        for (QuarterlyReportDTO report : reports) {
            report.setRelevantListings(
                    listingDao.findByAcbWithOpenSurveillance(report.getAcb().getId(),
                            report.getStartDate(), report.getEndDate()));
        }
        return reports;
    }

    /**
     * Gets the quarterly reports for a specific ACB and year.
     */
    @Override
    @Transactional
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY, filterObject)")
    public List<QuarterlyReportDTO> getQuarterlyReports(final Long acbId, final Integer year) {
        List<QuarterlyReportDTO> reports = quarterlyDao.getByAcbAndYear(acbId, year);
        //get relevant listings for each report
        for (QuarterlyReportDTO report : reports) {
            report.setRelevantListings(
                    listingDao.findByAcbWithOpenSurveillance(report.getAcb().getId(),
                            report.getStartDate(), report.getEndDate()));
        }
        return reports;
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
        QuarterlyReportDTO report = quarterlyDao.getById(id);
        report.setRelevantListings(
                listingDao.findByAcbWithOpenSurveillance(report.getAcb().getId(),
                        report.getStartDate(), report.getEndDate()));
        return report;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).EXPORT_QUARTERLY, "
            + "#id)")
    public Workbook exportQuarterlyReport(final Long id) throws EntityRetrievalException,
        IOException {
        QuarterlyReportDTO report = getQuarterlyReport(id);
        //get all of the surveillance details for the listings relevant to this report
        //the details object included on the quarterly report has some of the data that is needed
        //to build activities and outcomes worksheet but not all of it so we need to do
        //some other work to get the necessary data and put it all together
        List<CertifiedProductSearchDetails> relevantListingDetails = getRelevantListingDetails(report);
        return quarterlyReportBuilder.buildXlsx(report, relevantListingDetails);
    }

    /**
     * The relevant listings objects in the quarterly report have information about the listing
     * itself but not about each surveillance. This method queries for each surveillance and
     * adds it into a new details object that should have all of the fields needed to fill out
     * the Activities and Outcomes worksheet.
     * @param report
     * @return
     */
    private List<CertifiedProductSearchDetails> getRelevantListingDetails(final QuarterlyReportDTO report) {
        List<CertifiedProductSearchDetails> relevantListingDetails =
                new ArrayList<CertifiedProductSearchDetails>();
        for (CertifiedProductDetailsDTO listingDetails : report.getRelevantListings()) {
            CertifiedProductSearchDetails completeListingDetails = new CertifiedProductSearchDetails();
            completeListingDetails.setId(listingDetails.getId());
            completeListingDetails.setChplProductNumber(listingDetails.getChplProductNumber());
            Map<String, Object> editionMap = new HashMap<String, Object>();
            editionMap.put("id", listingDetails.getCertificationEditionId());
            editionMap.put("name", listingDetails.getYear());
            completeListingDetails.setCertificationEdition(editionMap);
            Developer dev = new Developer();
            dev.setDeveloperId(listingDetails.getDeveloper().getId());
            dev.setName(listingDetails.getDeveloper().getName());
            completeListingDetails.setDeveloper(dev);
            Product prod = new Product();
            prod.setProductId(listingDetails.getProduct().getId());
            prod.setName(listingDetails.getProduct().getName());
            completeListingDetails.setProduct(prod);
            ProductVersion ver = new ProductVersion();
            ver.setVersionId(listingDetails.getVersion().getId());
            ver.setVersion(listingDetails.getVersion().getVersion());
            completeListingDetails.setVersion(ver);
            try {
                completeListingDetails.setCertificationEvents(
                    detailsManager.getCertificationStatusEvents(listingDetails.getId()));
            } catch (EntityRetrievalException ex) {
                LOGGER.error("Could not get certification status events for listing id " + listingDetails.getId(), ex);
            }
            List<Surveillance> surveillances = survManager.getOpenBetweenDatesForCertifiedProduct(
                    listingDetails.getId(), report.getStartDate(), report.getEndDate());
            completeListingDetails.setSurveillance(surveillances);
            relevantListingDetails.add(completeListingDetails);
        }
        return relevantListingDetails;
    }
}
