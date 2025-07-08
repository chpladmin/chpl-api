package gov.healthit.chpl.sed;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationCriterionAttributeDAO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class SedManager {

    private AgeRangeDAO ageRangeDao;
    private EducationTypeDAO educationTypeDao;
    private CertificationCriterionAttributeDAO certificationCriterionAttributeDao;

    @Autowired
    public SedManager(AgeRangeDAO ageRangeDao,
            EducationTypeDAO educationTypeDao,
            CertificationCriterionAttributeDAO certificationCriterionAttributeDao) {
        this.ageRangeDao = ageRangeDao;
        this.educationTypeDao = educationTypeDao;
        this.certificationCriterionAttributeDao = certificationCriterionAttributeDao;
    }

    @Transactional
    public List<AgeRange> getAllAgeRanges() {
        return ageRangeDao.getAll();
    }

    @Transactional
    public List<EducationType> getAllEducationTypes() {
        return educationTypeDao.getAll();
    }

    @Transactional
    public List<CertificationCriterion> getCertificationCriteriaForSed() {
        return certificationCriterionAttributeDao.getCriteriaForSed();
    }

}
