package gov.healthit.chpl.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CertificationCriterionService {
    private static final String CURES_TITLE = "Cures Update";
    public static final String CURES_SUFFIX = " (" + CURES_TITLE + ")";

    private CertificationCriterionDAO certificationCriterionDAO;
    private Environment environment;

    private Map<Long, CertificationCriterion> criteriaMap = new HashMap<Long, CertificationCriterion>();
    private List<String> referenceSortingCriteriaList = new ArrayList<String>();

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
        referenceSortingCriteriaList = getReferenceSortingCriteriaList();
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

    public int sortCriteria(CertificationCriterionDTO c1, CertificationCriterionDTO c2) {
        String valueA = formatCriteriaNumber(c1);
        String valueB = formatCriteriaNumber(c2);
        return getCertificationResultSortIndex(valueA) - getCertificationResultSortIndex(valueB);
    }

    public static boolean hasCuresInTitle(CertificationCriterion criterion) {
        return criterion.getTitle() != null && criterion.getTitle().contains(CURES_TITLE);
    }

    public boolean hasCuresInTitle(CertificationCriterionDTO criterion) {
        return hasCuresInTitle(new CertificationCriterion(criterion));
    }

    public static String formatCriteriaNumber(CertificationCriterion criterion) {
        String result = criterion.getNumber();
        if (hasCuresInTitle(criterion)) {
            result += CURES_SUFFIX;
        }
        return result;
    }

    public static String formatCriteriaNumber(CertificationCriterionDTO criterion) {
        return formatCriteriaNumber(new CertificationCriterion(criterion));
    }

    public String coerceToCriterionNumberFormat(String input) {
        String formatRegex = "^\\d{3}\\.\\d{3}\\s{1}\\([a-z]{1}\\)(\\([0-9]{1,2}\\))?$";
        if (input.matches(formatRegex)) {
            LOGGER.debug("\tMatches required format. Not changing input.");
            return input;
        }

        String adjustedInput = input.toLowerCase();
        adjustedInput = adjustedInput.trim();
        if (adjustedInput.matches(formatRegex)) {
            LOGGER.debug("\tTrimmed space and made lower case: " + adjustedInput);
            return adjustedInput;
        }

        // check for the middle space
        int openParenIndex = adjustedInput.indexOf('(');
        if (openParenIndex > 0) {
            int currIndex = openParenIndex;
            boolean foundNonspaceChar = false;
            while (currIndex > 0 && !foundNonspaceChar) {
                currIndex--;
                String currChar = adjustedInput.charAt(currIndex) + "";
                if (currChar.matches("\\S")) {
                    foundNonspaceChar = true;
                }
            }

            if (currIndex >= 0) {
                adjustedInput = adjustedInput.substring(0, currIndex + 1) + " "
                        + adjustedInput.substring(openParenIndex);
            }
        }

        if (adjustedInput.matches(formatRegex)) {
            LOGGER.debug("\tAdjusted spaces in the middle of the criterion: " + adjustedInput);
            return adjustedInput;
        }

        return input;
    }

    private Integer getCertificationResultSortIndex(String criteriaNumber) {
        Integer index = referenceSortingCriteriaList.indexOf(criteriaNumber);
        if (index.equals(-1)) {
            // This is case when the criteria number is not in the array, just make it last...
            index = Integer.MAX_VALUE;
        }
        return index;
    }

    private List<String> getReferenceSortingCriteriaList() {
        String commaDelimitedProperyValue = environment.getProperty("criteria.sortOrder");
        return Stream.of(commaDelimitedProperyValue.split(","))
                .collect(Collectors.toList());
    }

    public static class Criteria2015 {
        public static final String A_1 = "criterion.170_315_a_1";
        public static final String A_2 = "criterion.170_315_a_2";
        public static final String A_3 = "criterion.170_315_a_3";
        public static final String A_4 = "criterion.170_315_a_4";
        public static final String A_5 = "criterion.170_315_a_5";
        public static final String A_6 = "criterion.170_315_a_6";
        public static final String A_7 = "criterion.170_315_a_7";
        public static final String A_8 = "criterion.170_315_a_8";
        public static final String A_9 = "criterion.170_315_a_9";
        public static final String A_10 = "criterion.170_315_a_10";
        public static final String A_11 = "criterion.170_315_a_11";
        public static final String A_12 = "criterion.170_315_a_12";
        public static final String A_13 = "criterion.170_315_a_13";
        public static final String A_14 = "criterion.170_315_a_14";
        public static final String A_15 = "criterion.170_315_a_15";
        public static final String B_1_OLD = "criterion.170_315_b_1_old";
        public static final String B_1_CURES = "criterion.170_315_b_1_cures";
        public static final String B_2_OLD = "criterion.170_315_b_2_old";
        public static final String B_2_CURES = "criterion.170_315_b_2_cures";
        public static final String B_3_OLD = "criterion.170_315_b_3_old";
        public static final String B_3_CURES = "criterion.170_315_b_3_cures";
        public static final String B_4 = "criterion.170_315_b_4";
        public static final String B_5 = "criterion.170_315_b_5";
        public static final String B_6 = "criterion.170_315_b_6";
        public static final String B_7_OLD = "criterion.170_315_b_7_old";
        public static final String B_7_CURES = "criterion.170_315_b_7_cures";
        public static final String B_8_OLD = "criterion.170_315_b_8_old";
        public static final String B_8_CURES = "criterion.170_315_b_8_cures";
        public static final String B_9_OLD = "criterion.170_315_b_9_old";
        public static final String B_9_CURES = "criterion.170_315_b_9_cures";
        public static final String B_10 = "criterion.170_315_b_10";
        public static final String C_1 = "criterion.170_315_c_1";
        public static final String C_2 = "criterion.170_315_c_2";
        public static final String C_3_OLD = "criterion.170_315_c_3_old";
        public static final String C_3_CURES = "criterion.170_315_c_3_cures";
        public static final String C_4 = "criterion.170_315_c_4";
        public static final String D_1 = "criterion.170_315_d_1";
        public static final String D_2_OLD = "criterion.170_315_d_2_old";
        public static final String D_2_CURES = "criterion.170_315_d_2_cures";
        public static final String D_3_OLD = "criterion.170_315_d_3_old";
        public static final String D_3_CURES = "criterion.170_315_d_3_cures";
        public static final String D_4 = "criterion.170_315_d_4";
        public static final String D_5 = "criterion.170_315_d_5";
        public static final String D_6 = "criterion.170_315_d_6";
        public static final String D_7 = "criterion.170_315_d_7";
        public static final String D_8 = "criterion.170_315_d_8";
        public static final String D_9 = "criterion.170_315_d_9";
        public static final String D_10_OLD = "criterion.170_315_d_10_old";
        public static final String D_10_CURES = "criterion.170_315_d_10_cures";
        public static final String D_11 = "criterion.170_315_d_11";
        public static final String D_12 = "criterion.170_315_d_12";
        public static final String D_13 = "criterion.170_315_d_13";
        public static final String E_1_OLD = "criterion.170_315_e_1_old";
        public static final String E_1_CURES = "criterion.170_315_e_1_cures";
        public static final String E_2 = "criterion.170_315_e_2";
        public static final String E_3 = "criterion.170_315_e_3";
        public static final String F_1 = "criterion.170_315_f_1";
        public static final String F_2 = "criterion.170_315_f_2";
        public static final String F_3 = "criterion.170_315_f_3";
        public static final String F_4 = "criterion.170_315_f_4";
        public static final String F_5_OLD = "criterion.170_315_f_5_old";
        public static final String F_5_CURES = "criterion.170_315_f_5_cures";
        public static final String F_6 = "criterion.170_315_f_6";
        public static final String F_7 = "criterion.170_315_f_7";
        public static final String G_1 = "criterion.170_315_g_1";
        public static final String G_2 = "criterion.170_315_g_2";
        public static final String G_3 = "criterion.170_315_g_3";
        public static final String G_4 = "criterion.170_315_g_4";
        public static final String G_5 = "criterion.170_315_g_5";
        public static final String G_6_OLD = "criterion.170_315_g_6_old";
        public static final String G_6_CURES = "criterion.170_315_g_6_cures";
        public static final String G_7 = "criterion.170_315_g_7";
        public static final String G_8 = "criterion.170_315_g_8";
        public static final String G_9_OLD = "criterion.170_315_g_9_old";
        public static final String G_9_CURES = "criterion.170_315_g_9_cures";
        public static final String G_10 = "criterion.170_315_g_10";
        public static final String H_1 = "criterion.170_315_h_1";
        public static final String H_2 = "criterion.170_315_h_2";
    }
}
