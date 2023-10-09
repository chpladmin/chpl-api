package gov.healthit.chpl.upload.listing.normalizer;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ValidationUtils;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class TestingLabNormalizer {
    private TestingLabDAO atlDao;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ValidationUtils validationUtils;

    @Autowired
    public TestingLabNormalizer(TestingLabDAO atlDao, ChplProductNumberUtil chplProductNumberUtil,
            ValidationUtils validationUtils) {
        this.atlDao = atlDao;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.validationUtils = validationUtils;
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (doesListingHaveTestingLabs(listing)) {
            //Update each ATL in the listing with valid data
            listing.getTestingLabs().stream()
                .forEach(testingLab -> populateTestingLab(testingLab));
        } else if (isTestingLabPortionOfChplProductNumberValid(listing)) {
            updateTestingLabFromChplProductNumber(listing);
        }
    }

    private Boolean doesListingHaveTestingLabs(CertifiedProductSearchDetails listing) {
        return listing.getTestingLabs() != null
                && listing.getTestingLabs().size() > 0;
    }

    private Boolean isTestingLabPortionOfChplProductNumberValid(CertifiedProductSearchDetails listing) {
        return !StringUtils.isEmpty(listing.getChplProductNumber())
                && validationUtils.chplNumberPartIsPresentAndValid(listing.getChplProductNumber(), ChplProductNumberUtil.ATL_CODE_INDEX, ChplProductNumberUtil.ATL_CODE_REGEX);
    }

    private void updateTestingLabFromChplProductNumber(CertifiedProductSearchDetails listing) {
        String atlCodeFromChplProductNumber = chplProductNumberUtil.getAtlCode(listing.getChplProductNumber());
        TestingLab testingLab = atlDao.getByCode(atlCodeFromChplProductNumber);

        if (testingLab != null) {
            CertifiedProductTestingLab cpTestingLab = CertifiedProductTestingLab.builder()
                    .testingLab(testingLab)
                    .build();
            listing.setTestingLabs(Stream.of(cpTestingLab).collect(Collectors.toList()));
        }
    }

    private void populateTestingLab(CertifiedProductTestingLab testingLab) {
        if (testingLab == null || testingLab.getTestingLab() == null) {
            return;
        } else if (doesTestingLabNameExist(testingLab)) {
            updateTestingLabBasedOnName(testingLab);
        } else if (doesTestingLabCodeExist(testingLab)) {
            updateTestingLabBasedOnCode(testingLab);
        } else if (doesTestingLabIdExist(testingLab)) {
            updateTestingLabBasedOnId(testingLab);
        }
    }

    private Boolean doesTestingLabNameExist(CertifiedProductTestingLab testingLab) {
        return testingLab.getTestingLab().getId() == null && !StringUtils.isEmpty(testingLab.getTestingLab().getName());
    }

    private Boolean doesTestingLabCodeExist(CertifiedProductTestingLab testingLab) {
        return testingLab.getTestingLab().getId() == null && !StringUtils.isEmpty(testingLab.getTestingLab().getAtlCode());
    }

    private Boolean doesTestingLabIdExist(CertifiedProductTestingLab testingLab) {
        return testingLab.getTestingLab().getId() != null
                && (StringUtils.isEmpty(testingLab.getTestingLab().getName())
                        || StringUtils.isEmpty(testingLab.getTestingLab().getAtlCode()));
    }

    private void updateTestingLabBasedOnName(CertifiedProductTestingLab cpTestingLab) {
        TestingLab testingLab = atlDao.getByName(cpTestingLab.getTestingLab().getName());
        updateTestingLabBasedOnTestingLab(cpTestingLab, testingLab);
    }

    private void updateTestingLabBasedOnCode(CertifiedProductTestingLab cpTestingLab) {
        TestingLab testingLab = atlDao.getByCode(cpTestingLab.getTestingLab().getAtlCode());
        updateTestingLabBasedOnTestingLab(cpTestingLab, testingLab);
    }

    private void updateTestingLabBasedOnId(CertifiedProductTestingLab cpTestingLab) {
        TestingLab testingLab = null;
        try {
            testingLab = atlDao.getById(cpTestingLab.getTestingLab().getId());
        } catch (Exception ex) {
            LOGGER.warn("Could not find Testing Lab with ID " + cpTestingLab.getTestingLab().getId());
        }
        if (testingLab != null) {
            cpTestingLab.setTestingLab(testingLab);
        }
    }

    private void updateTestingLabBasedOnTestingLab(CertifiedProductTestingLab cpTestingLab, TestingLab testingLab) {
        if (testingLab != null) {
            cpTestingLab.setTestingLab(testingLab);
        }
    }

}
