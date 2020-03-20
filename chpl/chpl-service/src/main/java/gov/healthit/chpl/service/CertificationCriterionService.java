package gov.healthit.chpl.service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;

@Component
public class CertificationCriterionService {

    private CertificationCriterionDAO certificationCriterionDAO;
    private Environment environment;

    private Map<Long, CertificationCriterion> criteriaMap = new HashMap<Long, CertificationCriterion>();

    @Autowired
    public CertificationCriterionService(CertificationCriterionDAO certificationCriterionDAO, Environment environment) {
        this.certificationCriterionDAO = certificationCriterionDAO;
        this.environment = environment;
    }

    @PostConstruct
    public void postConstruct() {
        criteriaMap = certificationCriterionDAO.findAll().stream()
                .map(criterion -> new CertificationCriterion(criterion))
                .collect(Collectors.toMap(CertificationCriterion::getId, cc -> cc));
    }

    public CertificationCriterion get(Long certificationCriterionId) {
        return criteriaMap.containsKey(certificationCriterionId) ? criteriaMap.get(certificationCriterionId) : null;
    }

    public CertificationCriterion get(String certificationCriterionDescriptor) {
        try {
            return get(Long.parseLong(environment.getProperty(certificationCriterionDescriptor)));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
