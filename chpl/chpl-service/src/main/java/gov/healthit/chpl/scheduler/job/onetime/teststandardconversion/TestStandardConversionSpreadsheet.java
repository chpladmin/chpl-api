package gov.healthit.chpl.scheduler.job.onetime.teststandardconversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.TestStandard;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.optionalStandard.dao.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class TestStandardConversionSpreadsheet {
    private static final Integer CRITERIA_NUMBER_IDX = 0;
    private static final Integer CRITERIA_TITLE_IDX = 1;
    private static final Integer TEST_STANDARD_NUMBER_IDX = 2;
    private static final Integer OPTIONAL_STANDARD_1_IDX = 4;
    private static final Integer OPTIONAL_STANDARD_2_IDX = 5;
    private static final Integer OPTIONAL_STANDARD_3_IDX = 6;
    private static final Integer OPTIONAL_STANDARD_4_IDX = 7;
    private static final Integer OPTIONAL_STANDARD_5_IDX = 8;


    private CertificationCriterionDAO certificationCriterionDAO;
    private TestStandardDAO testStandardDAO;
    private OptionalStandardDAO optionalStandardDAO;

    @Autowired
    public TestStandardConversionSpreadsheet(CertificationCriterionDAO certificationCriterionDAO, TestStandardDAO testStandardDAO, OptionalStandardDAO optionalStandardDAO) {
        this.certificationCriterionDAO = certificationCriterionDAO;
        this.testStandardDAO = testStandardDAO;
        this.optionalStandardDAO = optionalStandardDAO;
    }

    public Map<Pair<CertificationCriterion, TestStandard>, TestStandard2OptionalStandardsMapping> getTestStandard2OptionalStandardsMap() {

        return getDataFromSheet(null);
    }

    private Map<Pair<CertificationCriterion, TestStandard>, TestStandard2OptionalStandardsMapping> getDataFromSheet(Sheet sheet) {
        Map<Pair<CertificationCriterion, TestStandard>, TestStandard2OptionalStandardsMapping> map = new HashMap<Pair<CertificationCriterion, TestStandard>, TestStandard2OptionalStandardsMapping>();

        Row currentRow = sheet.getRow(1);

        String criteriaNumber = currentRow.getCell(CRITERIA_NUMBER_IDX).getStringCellValue();
        String criteriaTitle = currentRow.getCell(CRITERIA_TITLE_IDX).getStringCellValue();
        String testStandardNumber = currentRow.getCell(TEST_STANDARD_NUMBER_IDX).getStringCellValue();

        while (!StringUtils.isEmpty(criteriaTitle) && !StringUtils.isEmpty(criteriaNumber) && !StringUtils.isEmpty(testStandardNumber)) {
            CertificationCriterion criterion = getCriterion(criteriaNumber, criteriaTitle);
            TestStandard testStandard = getTestStandard(testStandardNumber);
            List<OptionalStandard> optionalStandards = getOptionalStandardsFromRow(currentRow);

            TestStandard2OptionalStandardsMapping mapping = new TestStandard2OptionalStandardsMapping(criterion, testStandard, optionalStandards);
            map.put(mapping.getKey(), mapping);
            LOGGER.info(mapping.toString());

            currentRow = sheet.getRow(currentRow.getRowNum() + 1);
            criteriaNumber = currentRow.getCell(CRITERIA_NUMBER_IDX).getStringCellValue();
            criteriaTitle = currentRow.getCell(CRITERIA_TITLE_IDX).getStringCellValue();
            testStandardNumber = currentRow.getCell(TEST_STANDARD_NUMBER_IDX).getStringCellValue();
        }

        return map;
    }

    private List<OptionalStandard> getOptionalStandardsFromRow(Row row) {
        List<OptionalStandard> optionalStandards = new ArrayList<OptionalStandard>();

        Optional<OptionalStandard> optionalStandard = getOptionalStandardFromCell(row.getCell(OPTIONAL_STANDARD_1_IDX));
        if (optionalStandard.isPresent()) {
            optionalStandards.add(optionalStandard.get());
        }

        optionalStandard = getOptionalStandardFromCell(row.getCell(OPTIONAL_STANDARD_2_IDX));
        if (optionalStandard.isPresent()) {
            optionalStandards.add(optionalStandard.get());
        }

        optionalStandard = getOptionalStandardFromCell(row.getCell(OPTIONAL_STANDARD_3_IDX));
        if (optionalStandard.isPresent()) {
            optionalStandards.add(optionalStandard.get());
        }

        optionalStandard = getOptionalStandardFromCell(row.getCell(OPTIONAL_STANDARD_4_IDX));
        if (optionalStandard.isPresent()) {
            optionalStandards.add(optionalStandard.get());
        }

        optionalStandard = getOptionalStandardFromCell(row.getCell(OPTIONAL_STANDARD_5_IDX));
        if (optionalStandard.isPresent()) {
            optionalStandards.add(optionalStandard.get());
        }

        return optionalStandards;
   }

    private Optional<OptionalStandard> getOptionalStandardFromCell(Cell cell) {
        String optionalStandard = cell.getStringCellValue();

        if (!StringUtils.isEmpty(optionalStandard) && optionalStandard.toUpperCase().trim().equals("DELETE")) {
            return Optional.of(optionalStandardDAO.getByCitation(optionalStandard));
        } else {
            return Optional.empty();
        }
    }

    private CertificationCriterion getCriterion(String number, String title) {
        return new CertificationCriterion(certificationCriterionDAO.getByNumberAndTitle(number, title));
    }

    private TestStandard getTestStandard(String number) {
        return new TestStandard(testStandardDAO.getByNumberAndEdition(number, CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId()));
    }
}
