package gov.healthit.chpl.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Value;

import gov.healthit.chpl.util.ErrorMessageUtil;

public abstract class InvalidCriteriaCombination {
    public static final String CURES_UPDATE_IN_TITLE = "(Cures Update)";

    @Value("${criterion.170_315_b_6}")
    protected Integer criteriaB6Id;

    @Value("${criterion.170_315_b_10}")
    protected Integer criteriaB10Id;

    @Value("${criterion.170_315_g_8}")
    protected Integer criteriaG8Id;

    @Value("${criterion.170_315_g_10}")
    protected Integer criteriaG10Id;

    @Value("${criterion.170_315_b_1}")
    protected Integer criteriaB1Id;
    @Value("${criterion.170_315_b_1_revised}")
    protected Integer criteriaB1RevisedId;

    @Value("${criterion.170_315_b_2}")
    protected Integer criteriaB2Id;
    @Value("${criterion.170_315_b_2_revised}")
    protected Integer criteriaB2RevisedId;

    @Value("${criterion.170_315_b_3}")
    protected Integer criteriaB3Id;
    @Value("${criterion.170_315_b_3_revised}")
    protected Integer criteriaB3RevisedId;

    @Value("${criterion.170_315_b_7}")
    protected Integer criteriaB7Id;
    @Value("${criterion.170_315_b_7_revised}")
    protected Integer criteriaB7RevisedId;

    @Value("${criterion.170_315_b_8}")
    protected Integer criteriaB8Id;
    @Value("${criterion.170_315_b_8_revised}")
    protected Integer criteriaB8RevisedId;

    @Value("${criterion.170_315_b_9}")
    protected Integer criteriaB9Id;
    @Value("${criterion.170_315_b_9_revised}")
    protected Integer criteriaB9RevisedId;

    @Value("${criterion.170_315_c_3}")
    protected Integer criteriaC3Id;
    @Value("${criterion.170_315_c_3_revised}")
    protected Integer criteriaC3RevisedId;

    @Value("${criterion.170_315_e_1}")
    protected Integer criteriaE1Id;
    @Value("${criterion.170_315_e_1_revised}")
    protected Integer criteriaE1RevisedId;

    @Value("${criterion.170_315_f_5}")
    protected Integer criteriaF5Id;
    @Value("${criterion.170_315_f_5_revised}")
    protected Integer criteriaF5RevisedId;

    @Value("${criterion.170_315_g_6}")
    protected Integer criteriaG6Id;
    @Value("${criterion.170_315_g_6_revised}")
    protected Integer criteriaG6RevisedId;

    @Value("${criterion.170_315_g_9}")
    protected Integer criteriaG9Id;
    @Value("${criterion.170_315_g_9_revised}")
    protected Integer criteriaG9RevisedId;

    protected ErrorMessageUtil msgUtil;
    protected FF4j ff4j;

    protected List<Integer> oldCriteriaIds;
    protected List<Integer> newCriteriaIds;

    public InvalidCriteriaCombination(ErrorMessageUtil msgUtil, FF4j ff4j) {
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
    }

    protected void initializeOldAndNewCriteria() {
        oldCriteriaIds = new ArrayList<Integer>(
                Arrays.asList(criteriaB1Id, criteriaB2Id, criteriaB3Id, criteriaB7Id, criteriaB8Id, criteriaB9Id,
                        criteriaC3Id, criteriaE1Id, criteriaF5Id, criteriaG6Id, criteriaG9Id));
        newCriteriaIds = new ArrayList<Integer>(
                Arrays.asList(criteriaB1RevisedId, criteriaB2RevisedId, criteriaB3RevisedId, criteriaB7RevisedId,
                        criteriaB8RevisedId, criteriaB9RevisedId, criteriaC3RevisedId, criteriaE1RevisedId,
                        criteriaF5RevisedId, criteriaG6RevisedId, criteriaG9RevisedId));
    }
}
