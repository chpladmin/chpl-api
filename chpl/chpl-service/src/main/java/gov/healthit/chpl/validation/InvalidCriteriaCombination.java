package gov.healthit.chpl.validation;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Value;

import gov.healthit.chpl.util.ErrorMessageUtil;

public abstract class InvalidCriteriaCombination {

    @Value("${criterion.170_315_b_6}")
    protected Integer criteriaB6Id;

    @Value("${criterion.170_315_b_10}")
    protected Integer criteriaB10Id;

    @Value("${criterion.170_315_g_8}")
    protected Integer criteriaG8Id;

    @Value("${criterion.170_315_g_10}")
    protected Integer criteriaG10Id;

    @Value("${criterion.170_315_b_1_old}")
    protected Integer criteriaB1Id;
    @Value("${criterion.170_315_b_1_cures}")
    protected Integer criteriaB1RevisedId;

    @Value("${criterion.170_315_b_2_old}")
    protected Integer criteriaB2Id;
    @Value("${criterion.170_315_b_2_cures}")
    protected Integer criteriaB2RevisedId;

    @Value("${criterion.170_315_b_3_old}")
    protected Integer criteriaB3Id;
    @Value("${criterion.170_315_b_3_cures}")
    protected Integer criteriaB3RevisedId;

    @Value("${criterion.170_315_b_7_old}")
    protected Integer criteriaB7Id;
    @Value("${criterion.170_315_b_7_cures}")
    protected Integer criteriaB7RevisedId;

    @Value("${criterion.170_315_b_8_old}")
    protected Integer criteriaB8Id;
    @Value("${criterion.170_315_b_8_cures}")
    protected Integer criteriaB8RevisedId;

    @Value("${criterion.170_315_b_9_old}")
    protected Integer criteriaB9Id;
    @Value("${criterion.170_315_b_9_cures}")
    protected Integer criteriaB9RevisedId;

    @Value("${criterion.170_315_c_3_old}")
    protected Integer criteriaC3Id;
    @Value("${criterion.170_315_c_3_cures}")
    protected Integer criteriaC3RevisedId;

    @Value("${criterion.170_315_e_1_old}")
    protected Integer criteriaE1Id;
    @Value("${criterion.170_315_e_1_cures}")
    protected Integer criteriaE1RevisedId;

    @Value("${criterion.170_315_f_5_old}")
    protected Integer criteriaF5Id;
    @Value("${criterion.170_315_f_5_cures}")
    protected Integer criteriaF5RevisedId;

    @Value("${criterion.170_315_g_6_old}")
    protected Integer criteriaG6Id;
    @Value("${criterion.170_315_g_6_cures}")
    protected Integer criteriaG6RevisedId;

    @Value("${criterion.170_315_g_9_old}")
    protected Integer criteriaG9Id;
    @Value("${criterion.170_315_g_9_cures}")
    protected Integer criteriaG9RevisedId;

    @Value("${criterion.170_315_d_2_old}")
    protected Integer criteriaD2Id;
    @Value("${criterion.170_315_d_2_cures}")
    protected Integer criteriaD2RevisedId;

    @Value("${criterion.170_315_d_3_old}")
    protected Integer criteriaD3Id;
    @Value("${criterion.170_315_d_3_cures}")
    protected Integer criteriaD3RevisedId;

    @Value("${criterion.170_315_d_10_old}")
    protected Integer criteriaD10Id;
    @Value("${criterion.170_315_d_10_cures}")
    protected Integer criteriaD10RevisedId;

    protected ErrorMessageUtil msgUtil;
    protected FF4j ff4j;

    protected Set<Pair<Integer, Integer>> oldAndNewcriteriaIdPairs;

    public InvalidCriteriaCombination(ErrorMessageUtil msgUtil, FF4j ff4j) {
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
    }

    protected void initializeOldAndNewCriteriaPairs() {
        final Pair<Integer, Integer> b1Pair = Pair.of(criteriaB1Id, criteriaB1RevisedId);
        final Pair<Integer, Integer> b2Pair = Pair.of(criteriaB2Id, criteriaB2RevisedId);
        final Pair<Integer, Integer> b3Pair = Pair.of(criteriaB3Id, criteriaB3RevisedId);
        final Pair<Integer, Integer> b7Pair = Pair.of(criteriaB7Id, criteriaB7RevisedId);
        final Pair<Integer, Integer> b8Pair = Pair.of(criteriaB8Id, criteriaB8RevisedId);
        final Pair<Integer, Integer> b9Pair = Pair.of(criteriaB9Id, criteriaB9RevisedId);
        final Pair<Integer, Integer> c3Pair = Pair.of(criteriaC3Id, criteriaC3RevisedId);
        final Pair<Integer, Integer> e1Pair = Pair.of(criteriaE1Id, criteriaE1RevisedId);
        final Pair<Integer, Integer> f5Pair = Pair.of(criteriaF5Id, criteriaF5RevisedId);
        final Pair<Integer, Integer> g6Pair = Pair.of(criteriaG6Id, criteriaG6RevisedId);
        final Pair<Integer, Integer> g9Pair = Pair.of(criteriaG9Id, criteriaG9RevisedId);
        final Pair<Integer, Integer> d2Pair = Pair.of(criteriaD2Id, criteriaD2RevisedId);
        final Pair<Integer, Integer> d3Pair = Pair.of(criteriaD3Id, criteriaD3RevisedId);
        final Pair<Integer, Integer> d10Pair = Pair.of(criteriaD10Id, criteriaD10RevisedId);
        oldAndNewcriteriaIdPairs = new LinkedHashSet<Pair<Integer, Integer>>(Arrays.asList(b1Pair, b2Pair, b3Pair, b7Pair, b8Pair,
                b9Pair, c3Pair, e1Pair, f5Pair, g6Pair, g9Pair, d2Pair, d3Pair, d10Pair));
    }
}
