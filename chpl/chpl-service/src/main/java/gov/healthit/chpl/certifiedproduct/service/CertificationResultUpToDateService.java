package gov.healthit.chpl.certifiedproduct.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationResultDAO;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CertificationResultUpToDateService {
    private CertificationResultDAO certResultDao;

    @Autowired
    public CertificationResultUpToDateService(CertificationResultDAO certResultDao) {
        this.certResultDao = certResultDao;
    }

    @Transactional
    public boolean isUpToDate(Long certResultId, LocalDate asOfDate) {
        System.out.println("Checking whether cert result " + certResultId + " is up to date as of " + asOfDate);
        return certResultDao.isUpToDate(certResultId, asOfDate);
    }

    @Transactional
    public boolean isUpToDateAsOfToday(Long certResultId) {
        System.out.println("Checking whether cert result " + certResultId + " is up to date as of today");
        return isUpToDate(certResultId, LocalDate.now());
    }

    @Transactional
    public boolean isUpToDate(Long criterionId,
            List<Long> certResultStandards,
            List<Long> certResultFunctionalitiesTested,
            List<Long> certResultCodeSets,
            LocalDate asOfDate) {
        return certResultDao.isUpToDate(criterionId, certResultStandards, certResultFunctionalitiesTested, certResultCodeSets, asOfDate);
    }

    @Transactional
    public boolean isUpToDateAsOfToday(Long criterionId,
            List<Long> certResultStandards,
            List<Long> certResultFunctionalitiesTested,
            List<Long> certResultCodeSets) {
        return isUpToDate(criterionId, certResultStandards, certResultFunctionalitiesTested, certResultCodeSets, LocalDate.now());
    }
}
