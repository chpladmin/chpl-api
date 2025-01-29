package gov.healthit.chpl.report.svap;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CriterionStatus;
import gov.healthit.chpl.report.common.CertificationCriterionWithOrder;
import gov.healthit.chpl.report.criteriaattribute.SvapListingReport;
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

    @Autowired
    public SvapReportService(SvapReportDao svapReportDAO, SvapManager svapManager, CertificationCriterionService certificationCriterionService) {
        this.svapReportDAO = svapReportDAO;
        this.svapManager = svapManager;
        this.certificationCriterionService = certificationCriterionService;
    }

    @Transactional
    public List<CriteriaWithAnySvap> getCriteriaWithAnySvap() {
        return svapReportDAO.getCriteriaWithAnySvap();
    }

    @Transactional
    public List<SvapListingReport> getSvapListingReports() {
        return svapReportDAO.getSvapListingReports();
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
