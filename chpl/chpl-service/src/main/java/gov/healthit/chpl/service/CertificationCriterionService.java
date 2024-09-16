package gov.healthit.chpl.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CertificationCriterionService {
    private static final String HEADINGS_PROPERTY_SUFFIX = ".headings";
    private static final String HEADING_DELIMITER = ",";

    private CertificationCriterionDAO certificationCriterionDAO;
    private Environment environment;

    private Map<Long, CertificationCriterion> criteriaByIdMap = new HashMap<Long, CertificationCriterion>();
    private Map<String, List<CertificationCriterion>> criteriaByNumberMap = new HashMap<String, List<CertificationCriterion>>();
    private Map<CertificationCriterion, CertificationCriterion> originalToCuresCriteriaMap = new HashMap<CertificationCriterion, CertificationCriterion>();
    private Map<Long, List<String>> criterionHeadingsByIdMap = new HashMap<Long, List<String>>();
    private List<Long> referenceSortingCriteriaList = new ArrayList<Long>();

    @Autowired
    public CertificationCriterionService(CertificationCriterionDAO certificationCriterionDAO, Environment environment) {
        this.certificationCriterionDAO = certificationCriterionDAO;
        this.environment = environment;
    }

    @PostConstruct
    public void postConstruct() {
        criteriaByIdMap = certificationCriterionDAO.findAll().stream()
                .collect(Collectors.toMap(CertificationCriterion::getId, cc -> cc));
        criteriaByNumberMap = criteriaByIdMap.values().stream()
                .collect(Collectors.groupingBy(CertificationCriterion::getNumber));
        referenceSortingCriteriaList = getReferenceSortingCriteriaList();
        initOriginalToCuresCriteriaMap();
        initCriterionHeadingByIdMap();
    }

    private void initOriginalToCuresCriteriaMap() {
        originalToCuresCriteriaMap.put(get("criterion.170_315_b_1_old"), get("criterion.170_315_b_1_cures"));
        originalToCuresCriteriaMap.put(get("criterion.170_315_b_2_old"), get("criterion.170_315_b_2_cures"));
        originalToCuresCriteriaMap.put(get("criterion.170_315_b_3_old"), get("criterion.170_315_b_3_cures"));
        originalToCuresCriteriaMap.put(get("criterion.170_315_b_7_old"), get("criterion.170_315_b_7_cures"));
        originalToCuresCriteriaMap.put(get("criterion.170_315_b_8_old"), get("criterion.170_315_b_8_cures"));
        originalToCuresCriteriaMap.put(get("criterion.170_315_b_9_old"), get("criterion.170_315_b_9_cures"));
        originalToCuresCriteriaMap.put(get("criterion.170_315_b_6"), get("criterion.170_315_b_10"));
        originalToCuresCriteriaMap.put(get("criterion.170_315_c_3_old"), get("criterion.170_315_c_3_cures"));
        originalToCuresCriteriaMap.put(get("criterion.170_315_d_2_old"), get("criterion.170_315_d_2_cures"));
        originalToCuresCriteriaMap.put(get("criterion.170_315_d_3_old"), get("criterion.170_315_d_3_cures"));
        originalToCuresCriteriaMap.put(get("criterion.170_315_d_10_old"), get("criterion.170_315_d_10_cures"));
        originalToCuresCriteriaMap.put(get("criterion.170_315_e_1_old"), get("criterion.170_315_e_1_cures"));
        originalToCuresCriteriaMap.put(get("criterion.170_315_f_5_old"), get("criterion.170_315_f_5_cures"));
        originalToCuresCriteriaMap.put(get("criterion.170_315_g_6_old"), get("criterion.170_315_g_6_cures"));
        originalToCuresCriteriaMap.put(get("criterion.170_315_g_9_old"), get("criterion.170_315_g_9_cures"));
        originalToCuresCriteriaMap.put(get("criterion.170_315_g_8"), get("criterion.170_315_g_10"));
    }

    private void initCriterionHeadingByIdMap() {
        //2015 criteria
        Field[] declaredFields2015AndBeyond = Criteria2015.class.getDeclaredFields();
        List<Field> staticFields2015AndBeyond = new ArrayList<Field>();
        for (Field field : declaredFields2015AndBeyond) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                staticFields2015AndBeyond.add(field);
            }
        }
        staticFields2015AndBeyond.stream()
            .map(field -> getFieldValue(field))
            .filter(fieldValue -> !StringUtils.isBlank(fieldValue) && get(fieldValue) != null)
            .forEach(fieldValue -> criterionHeadingsByIdMap.put(get(fieldValue).getId(), getCriterionHeadings(fieldValue)));

        //2014 criteria
        Field[] declaredFields2014 = Criteria2014.class.getDeclaredFields();
        List<Field> staticFields2014 = new ArrayList<Field>();
        for (Field field : declaredFields2014) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                staticFields2014.add(field);
            }
        }
        staticFields2014.stream()
            .map(field -> getFieldValue(field))
            .filter(fieldValue -> !StringUtils.isBlank(fieldValue) && get(fieldValue) != null)
            .forEach(fieldValue -> criterionHeadingsByIdMap.put(get(fieldValue).getId(), getCriterionHeadings(fieldValue)));

      //2011 criteria
        Field[] declaredFields2011 = Criteria2014.class.getDeclaredFields();
        List<Field> staticFields2011 = new ArrayList<Field>();
        for (Field field : declaredFields2011) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                staticFields2011.add(field);
            }
        }
        staticFields2011.stream()
            .map(field -> getFieldValue(field))
            .filter(fieldValue -> !StringUtils.isBlank(fieldValue) && get(fieldValue) != null)
            .forEach(fieldValue -> criterionHeadingsByIdMap.put(get(fieldValue).getId(), getCriterionHeadings(fieldValue)));
    }

    public CertificationCriterion get(Long certificationCriterionId) {
        return criteriaByIdMap.get(certificationCriterionId);
    }

    public CertificationCriterion get(String certificationCriterionDescriptor) {
        try {
            return get(Long.parseLong(environment.getProperty(certificationCriterionDescriptor)));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<String> getCriterionHeadings(String certificationCriterionDescriptor) {
        String delimitedHeadings = environment.getProperty(certificationCriterionDescriptor + HEADINGS_PROPERTY_SUFFIX);
        if (StringUtils.isEmpty(delimitedHeadings)) {
            return new ArrayList<String>();
        }
        return Stream.of(delimitedHeadings.split(HEADING_DELIMITER))
                .map(heading -> heading.toUpperCase())
                .collect(Collectors.toList());
    }

    public List<String> getCriterionHeadings(Long criterionId) {
        return criterionHeadingsByIdMap.get(criterionId);
    }

    public List<String> getEquivalentCriterionHeadings(String criterionHeading) {
        Long keyForCriterionHeading = criterionHeadingsByIdMap.keySet().stream()
            .filter(key -> criterionHeadingsByIdMap.get(key).contains(criterionHeading.toUpperCase()))
            .findAny().orElse(null);
        if (keyForCriterionHeading != null) {
            return List.of();
        }
        return criterionHeadingsByIdMap.get(keyForCriterionHeading);
    }

    public List<CertificationCriterion> getByNumber(String certificationCriterionNumber) {
        return criteriaByNumberMap.get(certificationCriterionNumber);
    }

    public Map<CertificationCriterion, CertificationCriterion> getOriginalToCuresCriteriaMap() {
        return originalToCuresCriteriaMap;
    }

    public List<String> getAllowedCriterionHeadingsForNewListing() {
        Field[] declaredFields = Criteria2015.class.getDeclaredFields();
        List<Field> staticFields = new ArrayList<Field>();
        for (Field field : declaredFields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                staticFields.add(field);
            }
        }
        return staticFields.stream()
            .map(field -> getFieldValue(field))
            .filter(fieldValue -> !StringUtils.isBlank(fieldValue))
            .flatMap(fieldValue -> getCriterionHeadings(fieldValue).stream())
            .collect(Collectors.toList());
    }

    private String getFieldValue(Field field) {
        Object val = null;
        try {
            val = field.get(null);
        } catch (IllegalAccessException ex) {
            LOGGER.error("Unable to get the value of the field " + field.getName());
        }

        if (val == null) {
            return "";
        }
        return val.toString();
    }

    public int sortCriteria(CertificationCriterion c1, CertificationCriterion c2) {
        return getCertificationResultSortIndex(c1.getId()) - getCertificationResultSortIndex(c2.getId());
    }

    public static String formatCriteriaNumber(String number) {
        return number;
    }

    public static String formatCriteriaNumber(CertificationCriterion criterion) {
        return formatCriteriaNumber(criterion.getNumber());
    }

    public static String formatCriteriaNumber(CertificationCriterion criterion, boolean formatForRemoved) {
        String result = formatCriteriaNumber(criterion);
        if (formatForRemoved && Objects.nonNull(criterion.isRemoved()) && criterion.isRemoved()) {
            result = "Removed | " + result;
        }
        return result;
    }

    public boolean isCriteriaNumber(String input) {
        String criteriaNumber = coerceToCriterionNumberFormat(input);
        return !CollectionUtils.isEmpty(getByNumber(criteriaNumber));
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

    private Integer getCertificationResultSortIndex(Long criterionId) {
        Integer index = referenceSortingCriteriaList.indexOf(criterionId);
        if (index.equals(-1)) {
            // This is case when the criteria ID is not in the array, just make it last...
            index = Integer.MAX_VALUE;
        }
        return index;
    }

    private List<Long> getReferenceSortingCriteriaList() {
        String commaDelimitedProperyValue = environment.getProperty("criteria.sortOrder");
        return Stream.of(commaDelimitedProperyValue.split(","))
                .map(idAsString -> StringUtils.trim(idAsString))
                .map(idAsString -> Long.parseLong(idAsString))
                .collect(Collectors.toList());
    }

    public List<CertificationCriterion> getUscdiCriteria() {
        return Arrays.asList(
                get(Criteria2015.B_1_CURES),
                get(Criteria2015.B_2_CURES),
                get(Criteria2015.E_1_CURES),
                get(Criteria2015.F_5_CURES),
                get(Criteria2015.G_6_CURES),
                get(Criteria2015.G_9_CURES),
                get(Criteria2015.G_10));
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
        public static final String B_11 = "criterion.170_315_b_11";
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

    public static class Criteria2014 {
        public static final String A_1 = "criterion.170_314_a_1";
        public static final String A_2 = "criterion.170_314_a_2";
        public static final String A_3 = "criterion.170_314_a_3";
        public static final String A_4 = "criterion.170_314_a_4";
        public static final String A_5 = "criterion.170_314_a_5";
        public static final String A_6 = "criterion.170_314_a_6";
        public static final String A_7 = "criterion.170_314_a_7";
        public static final String A_8 = "criterion.170_314_a_8";
        public static final String A_9 = "criterion.170_314_a_9";
        public static final String A_10 = "criterion.170_314_a_10";
        public static final String A_11 = "criterion.170_314_a_11";
        public static final String A_12 = "criterion.170_314_a_12";
        public static final String A_13 = "criterion.170_314_a_13";
        public static final String A_14 = "criterion.170_314_a_14";
        public static final String A_15 = "criterion.170_314_a_15";
        public static final String A_16 = "criterion.170_314_a_16";
        public static final String A_17 = "criterion.170_314_a_17";
        public static final String A_18 = "criterion.170_314_a_18";
        public static final String A_19 = "criterion.170_314_a_19";
        public static final String A_20 = "criterion.170_314_a_20";
        public static final String B_1 = "criterion.170_314_b_1";
        public static final String B_2 = "criterion.170_314_b_2";
        public static final String B_3 = "criterion.170_314_b_3";
        public static final String B_4 = "criterion.170_314_b_4";
        public static final String B_5_A = "criterion.170_314_b_5_A";
        public static final String B_5_B = "criterion.170_314_b_5_B";
        public static final String B_6 = "criterion.170_314_b_6";
        public static final String B_7 = "criterion.170_314_b_7";
        public static final String B_8 = "criterion.170_314_b_8";
        public static final String B_9 = "criterion.170_314_b_9";
        public static final String C_1 = "criterion.170_314_c_1";
        public static final String C_2 = "criterion.170_314_c_2";
        public static final String C_3 = "criterion.170_314_c_3";
        public static final String D_1 = "criterion.170_314_d_1";
        public static final String D_2 = "criterion.170_314_d_2";
        public static final String D_3 = "criterion.170_314_d_3";
        public static final String D_4 = "criterion.170_314_d_4";
        public static final String D_5 = "criterion.170_314_d_5";
        public static final String D_6 = "criterion.170_314_d_6";
        public static final String D_7 = "criterion.170_314_d_7";
        public static final String D_8 = "criterion.170_314_d_8";
        public static final String D_9 = "criterion.170_314_d_9";
        public static final String E_1 = "criterion.170_314_e_1";
        public static final String E_2 = "criterion.170_314_e_2";
        public static final String E_3 = "criterion.170_314_e_3";
        public static final String F_1 = "criterion.170_314_f_1";
        public static final String F_2 = "criterion.170_314_f_2";
        public static final String F_3 = "criterion.170_314_f_3";
        public static final String F_4 = "criterion.170_314_f_4";
        public static final String F_5 = "criterion.170_314_f_5";
        public static final String F_6 = "criterion.170_314_f_6";
        public static final String F_7 = "criterion.170_314_f_7";
        public static final String G_1 = "criterion.170_314_g_1";
        public static final String G_2 = "criterion.170_314_g_2";
        public static final String G_3 = "criterion.170_314_g_3";
        public static final String G_4 = "criterion.170_314_g_4";
        public static final String H_1 = "criterion.170_314_h_1";
        public static final String H_2 = "criterion.170_314_h_2";
        public static final String H_3 = "criterion.170_314_h_3";
    }

    public static class Criteria2011 {
        public static final String A_302 = "criterion.170_302_a";
        public static final String B_302 = "criterion.170_302_b";
        public static final String C_302 = "criterion.170_302_c";
        public static final String D_302 = "criterion.170_302_d";
        public static final String E_302 = "criterion.170_302_e";
        public static final String F_1_302 = "criterion.170_302_f_1";
        public static final String F_2_302 = "criterion.170_302_f_2";
        public static final String F_3_302 = "criterion.170_302_f_3";
        public static final String G_302 = "criterion.170_302_g";
        public static final String H_302 = "criterion.170_302_h";
        public static final String I_302 = "criterion.170_302_i";
        public static final String J_302 = "criterion.170_302_j";
        public static final String K_302 = "criterion.170_302_k";
        public static final String L_302 = "criterion.170_302_l";
        public static final String M_302 = "criterion.170_302_m";
        public static final String N_302 = "criterion.170_302_n";
        public static final String O_302 = "criterion.170_302_o";
        public static final String P_302 = "criterion.170_302_p";
        public static final String Q_302 = "criterion.170_302_q";
        public static final String R_302 = "criterion.170_302_r";
        public static final String S_302 = "criterion.170_302_s";
        public static final String T_302 = "criterion.170_302_t";
        public static final String U_302 = "criterion.170_302_u";
        public static final String V_302 = "criterion.170_302_v";
        public static final String W_302 = "criterion.170_302_w";
        public static final String A_304 = "criterion.170_304_a";
        public static final String B_304 = "criterion.170_304_b";
        public static final String C_304 = "criterion.170_304_c";
        public static final String D_304 = "criterion.170_304_d";
        public static final String E_304 = "criterion.170_304_e";
        public static final String F_304 = "criterion.170_304_f";
        public static final String G_304 = "criterion.170_304_g";
        public static final String H_304 = "criterion.170_304_h";
        public static final String I_304 = "criterion.170_304_i";
        public static final String J_304 = "criterion.170_304_j";
        public static final String A_306 = "criterion.170_306_a";
        public static final String B_306 = "criterion.170_306_b";
        public static final String C_306 = "criterion.170_306_c";
        public static final String D_1_306 = "criterion.170_306_d_1";
        public static final String D_2_306 = "criterion.170_306_d_2";
        public static final String E_306 = "criterion.170_306_e";
        public static final String F_306 = "criterion.170_306_f";
        public static final String G_306 = "criterion.170_306_g";
        public static final String H_306 = "criterion.170_306_h";
        public static final String I_306 = "criterion.170_306_i";
    }
}
