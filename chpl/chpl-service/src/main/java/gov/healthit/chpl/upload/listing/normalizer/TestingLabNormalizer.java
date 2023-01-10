package gov.healthit.chpl.upload.listing.normalizer;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.dto.TestingLabDTO;
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
        TestingLabDTO testingLabDto = atlDao.getByCode(atlCodeFromChplProductNumber);

        if (testingLabDto != null) {
            CertifiedProductTestingLab testingLab = CertifiedProductTestingLab.builder()
                    .testingLabId(testingLabDto.getId())
                    .testingLabName(testingLabDto.getName())
                    .testingLabCode(testingLabDto.getTestingLabCode())
                    .build();
            listing.setTestingLabs(Stream.of(testingLab).collect(Collectors.toList()));
        }
    }

    private void populateTestingLab(CertifiedProductTestingLab testingLab) {
        if (testingLab == null) {
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
        return testingLab.getTestingLabId() == null && !StringUtils.isEmpty(testingLab.getTestingLabName());
    }

    private Boolean doesTestingLabCodeExist(CertifiedProductTestingLab testingLab) {
        return testingLab.getTestingLabId() == null && !StringUtils.isEmpty(testingLab.getTestingLabCode());
    }

    private Boolean doesTestingLabIdExist(CertifiedProductTestingLab testingLab) {
        return testingLab.getTestingLabId() != null
                && (StringUtils.isEmpty(testingLab.getTestingLabName())
                        || StringUtils.isEmpty(testingLab.getTestingLabCode()));
    }

    private void updateTestingLabBasedOnName(CertifiedProductTestingLab testingLab) {
        TestingLabDTO testingLabDto = atlDao.getByName(testingLab.getTestingLabName());
        updateTestingLabBasedOnTestingLabDto(testingLab, testingLabDto);
    }

    private void updateTestingLabBasedOnCode(CertifiedProductTestingLab testingLab) {
        TestingLabDTO testingLabDto = atlDao.getByCode(testingLab.getTestingLabCode());
        updateTestingLabBasedOnTestingLabDto(testingLab, testingLabDto);
    }

    private void updateTestingLabBasedOnId(CertifiedProductTestingLab testingLab) {
        TestingLabDTO testingLabDto = null;
        try {
            testingLabDto = atlDao.getById(testingLab.getTestingLabId());
        } catch (Exception ex) {
            LOGGER.warn("Could not find Testing Lab with ID " + testingLab.getTestingLabId());
        }
        if (testingLabDto != null) {
            testingLab.setTestingLabName(testingLabDto.getName());
            testingLab.setTestingLabCode(testingLabDto.getTestingLabCode());
        }
    }

    private void updateTestingLabBasedOnTestingLabDto(CertifiedProductTestingLab testingLab, TestingLabDTO testingLabDto) {
        if (testingLabDto != null) {
            testingLab.setTestingLabId(testingLabDto.getId());
            testingLab.setTestingLabName(testingLabDto.getName());
            testingLab.setTestingLabCode(testingLabDto.getTestingLabCode());
        }
    }

}
