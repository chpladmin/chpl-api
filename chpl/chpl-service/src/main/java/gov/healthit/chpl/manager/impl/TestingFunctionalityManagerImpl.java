package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.domain.TestFunctionality;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.manager.TestingFunctionalityManager;

@Service
public class TestingFunctionalityManagerImpl implements TestingFunctionalityManager {

    @Autowired
    private TestFunctionalityDAO testFunctionalityDAO;

    @Override
    public List<TestFunctionality> getTestFunctionalities(
            final String criteriaNumber, final String certificationEdition, final Long practiceTypeId) {

        if (certificationEdition.equals("2014")) {
            return get2014TestFunctionalities(criteriaNumber, certificationEdition, practiceTypeId);
        } else if (certificationEdition.equals("2015")) {
            return get2015TestFunctionalities(certificationEdition);
        } else {
            return new ArrayList<TestFunctionality>();
        }
    }

    private List<TestFunctionality> get2014TestFunctionalities(
            final String criteriaNumber, final String edition, final Long practiceTypeId) {
        List<TestFunctionality> allTestFunctionalities = getAllTestFunctionalities();
        List<TestFunctionality> allowedTestFunctionalities = new ArrayList<TestFunctionality>();

        for (TestFunctionality tf : allTestFunctionalities) {
            if (tf.getYear().equals(edition)) {
                if (tf.getPracticeType() != null) {
                    if (tf.getPracticeType().getId().equals(practiceTypeId)) {
                        if (tf.getCertificationCriterion() != null) {
                            if (tf.getCertificationCriterion().getNumber().equals(criteriaNumber)) {
                                allowedTestFunctionalities.add(tf);
                            }
                        }
                    }
                } else {
                    if (tf.getCertificationCriterion() != null) {
                        if (tf.getCertificationCriterion().getNumber().equals(criteriaNumber)) {
                            allowedTestFunctionalities.add(tf);
                        }
                    }
                }
            }
        }

        return allowedTestFunctionalities;
    }

    private List<TestFunctionality> get2015TestFunctionalities(final String edition) {
        List<TestFunctionality> allTestFunctionalities = getAllTestFunctionalities();
        List<TestFunctionality> allowedTestFunctionalities = new ArrayList<TestFunctionality>();

        for (TestFunctionality tf : allTestFunctionalities) {
            if (tf.getYear().equals(edition)) {
                allowedTestFunctionalities.add(tf);
            }
        }

        return allowedTestFunctionalities;
    }

    private List<TestFunctionality> getAllTestFunctionalities() {
        List<TestFunctionality> allTfs = new ArrayList<TestFunctionality>();
        List<TestFunctionalityDTO> tfs = testFunctionalityDAO.findAll();
        for (TestFunctionalityDTO item : tfs) {
            TestFunctionality tf = new TestFunctionality(item);
            allTfs.add(tf);
        }
        return allTfs;
    }

}
