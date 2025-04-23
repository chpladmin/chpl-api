package gov.healthit.chpl.report.attestation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.scheduler.job.report.attestation.AttestationReport;
import gov.healthit.chpl.scheduler.job.report.attestation.AttestationReportDAO;
import gov.healthit.chpl.scheduler.job.report.attestation.AttestationReportDeveloper;

@Component
public class AttestationReportService {

    private AttestationReportDAO attestationReportDAO;

    @Autowired
    public AttestationReportService(AttestationReportDAO attestationReportDAO) {
        this.attestationReportDAO = attestationReportDAO;
    }

    @Transactional
    public List<AttestationReport> getAttestationReports() {
        return attestationReportDAO.getAttestationReportByAttestationPeriod();
    }

    @Transactional
    public List<AttestationReportDeveloper> getAttestationReportDevelopers() {
        return attestationReportDAO.getAttestationReportDeveloperByAttestationPeriod();
    }
}
