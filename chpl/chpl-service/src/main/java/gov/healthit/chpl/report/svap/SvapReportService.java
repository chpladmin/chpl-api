package gov.healthit.chpl.report.svap;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CriterionStatus;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.report.common.CertificationCriterionWithOrder;
import gov.healthit.chpl.report.criteriaattribute.SvapListingReport;
import gov.healthit.chpl.report.criteriaattribute.SvapReportByCertificationStatus;
import gov.healthit.chpl.report.criteriaattribute.SvapReportDao;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.svap.manager.SvapManager;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class SvapReportService {

    private SvapReportDao svapReportDAO;
    private SvapManager svapManager;
    private CertificationCriterionService certificationCriterionService;
    private CertificationStatusDAO certStatusDao;

    @Autowired
    public SvapReportService(SvapReportDao svapReportDAO,
            SvapManager svapManager,
            CertificationCriterionService certificationCriterionService,
            CertificationStatusDAO certStatusDao) {
        this.svapReportDAO = svapReportDAO;
        this.svapManager = svapManager;
        this.certificationCriterionService = certificationCriterionService;
        this.certStatusDao = certStatusDao;
    }

    @Transactional
    public List<CriteriaWithAnySvap> getCriteriaWithAnySvap() {
        List<CertificationStatus> allCertStatuses = certStatusDao.findAll();
        List<CriteriaWithAnySvap> response = new ArrayList<CriteriaWithAnySvap>();
        allCertStatuses.stream()
        .forEach(certStatus -> {
            List<CriteriaWithAnySvap> criteriaWithAnySvapForStatus = svapReportDAO.getCriteriaWithAnySvap(certStatus);
            if (!CollectionUtils.isEmpty(criteriaWithAnySvapForStatus)) {
                response.addAll(criteriaWithAnySvapForStatus);
            }
        });
        return response;
    }

    @Transactional
    public List<SvapListingReport> getSvapListingReports() {
        return svapReportDAO.getSvapListingReports();
    }

    @Transactional
    public List<SvapReportByCertificationStatus> getSvapReportsByAllCertificationStatuses() {
        List<CertificationStatus> allCertStatuses = certStatusDao.findAll();
        List<SvapReportByCertificationStatus> response = new ArrayList<SvapReportByCertificationStatus>();
        allCertStatuses.stream()
            .forEach(certStatus -> {
                List<SvapReportByCertificationStatus> svapReportsForStatus = svapReportDAO.getSvapReports(certStatus);
                if (!CollectionUtils.isEmpty(svapReportsForStatus)) {
                    response.addAll(svapReportsForStatus);
                }
            });
        return response;
    }

    @Transactional
    public List<CertificationCriterionWithOrder> getCertificationCriteria() {
        return svapManager.getCertificationCriteriaForSvap().stream()
                .filter(cc -> cc.getStatus().equals(CriterionStatus.ACTIVE))
                .map(cc -> {
                    CertificationCriterionWithOrder ccwo = new CertificationCriterionWithOrder(cc);
                    ccwo.setOrder(certificationCriterionService.getCertificationResultSortIndex(cc.getId()).longValue());
                    return ccwo;
                })
                .toList();
    }
}
