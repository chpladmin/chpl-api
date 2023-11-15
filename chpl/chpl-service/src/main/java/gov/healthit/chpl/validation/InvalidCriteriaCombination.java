package gov.healthit.chpl.validation;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class InvalidCriteriaCombination {
    private Set<Pair<Integer, Integer>> oldAndNewcriteriaIdPairs;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public InvalidCriteriaCombination(@Value("${criterion.170_315_a_9}") Integer criteriaA9Id,
            @Value("${criterion.170_315_b_1_old}") Integer criteriaB1Id,
            @Value("${criterion.170_315_b_1_cures}") Integer criteriaB1RevisedId,
            @Value("${criterion.170_315_b_2_old}") Integer criteriaB2Id,
            @Value("${criterion.170_315_b_2_cures}") Integer criteriaB2RevisedId,
            @Value("${criterion.170_315_b_3_old}") Integer criteriaB3Id,
            @Value("${criterion.170_315_b_3_cures}") Integer criteriaB3RevisedId,
            @Value("${criterion.170_315_b_6}") Integer criteriaB6Id,
            @Value("${criterion.170_315_b_7_old}") Integer criteriaB7Id,
            @Value("${criterion.170_315_b_7_cures}") Integer criteriaB7RevisedId,
            @Value("${criterion.170_315_b_8_old}") Integer criteriaB8Id,
            @Value("${criterion.170_315_b_8_cures}") Integer criteriaB8RevisedId,
            @Value("${criterion.170_315_b_9_old}") Integer criteriaB9Id,
            @Value("${criterion.170_315_b_9_cures}") Integer criteriaB9RevisedId,
            @Value("${criterion.170_315_b_10}") Integer criteriaB10Id,
            @Value("${criterion.170_315_b_11}") Integer criteriaB11Id,
            @Value("${criterion.170_315_c_3_old}") Integer criteriaC3Id,
            @Value("${criterion.170_315_c_3_cures}") Integer criteriaC3RevisedId,
            @Value("${criterion.170_315_d_2_old}") Integer criteriaD2Id,
            @Value("${criterion.170_315_d_2_cures}") Integer criteriaD2RevisedId,
            @Value("${criterion.170_315_d_3_old}") Integer criteriaD3Id,
            @Value("${criterion.170_315_d_3_cures}") Integer criteriaD3RevisedId,
            @Value("${criterion.170_315_d_10_old}") Integer criteriaD10Id,
            @Value("${criterion.170_315_d_10_cures}") Integer criteriaD10RevisedId,
            @Value("${criterion.170_315_e_1_old}") Integer criteriaE1Id,
            @Value("${criterion.170_315_e_1_cures}") Integer criteriaE1RevisedId,
            @Value("${criterion.170_315_f_5_old}") Integer criteriaF5Id,
            @Value("${criterion.170_315_f_5_cures}") Integer criteriaF5RevisedId,
            @Value("${criterion.170_315_g_6_old}") Integer criteriaG6Id,
            @Value("${criterion.170_315_g_6_cures}") Integer criteriaG6RevisedId,
            @Value("${criterion.170_315_g_8}") Integer criteriaG8Id,
            @Value("${criterion.170_315_g_9_old}") Integer criteriaG9Id,
            @Value("${criterion.170_315_g_9_cures}") Integer criteriaG9RevisedId,
            @Value("${criterion.170_315_g_10}") Integer criteriaG10Id) {
        final Pair<Integer, Integer> a9Pair = Pair.of(criteriaA9Id, criteriaB11Id);
        final Pair<Integer, Integer> b1Pair = Pair.of(criteriaB1Id, criteriaB1RevisedId);
        final Pair<Integer, Integer> b2Pair = Pair.of(criteriaB2Id, criteriaB2RevisedId);
        final Pair<Integer, Integer> b3Pair = Pair.of(criteriaB3Id, criteriaB3RevisedId);
        final Pair<Integer, Integer> b6Pair = Pair.of(criteriaB6Id, criteriaB10Id);
        final Pair<Integer, Integer> b7Pair = Pair.of(criteriaB7Id, criteriaB7RevisedId);
        final Pair<Integer, Integer> b8Pair = Pair.of(criteriaB8Id, criteriaB8RevisedId);
        final Pair<Integer, Integer> b9Pair = Pair.of(criteriaB9Id, criteriaB9RevisedId);
        final Pair<Integer, Integer> c3Pair = Pair.of(criteriaC3Id, criteriaC3RevisedId);
        final Pair<Integer, Integer> e1Pair = Pair.of(criteriaE1Id, criteriaE1RevisedId);
        final Pair<Integer, Integer> f5Pair = Pair.of(criteriaF5Id, criteriaF5RevisedId);
        final Pair<Integer, Integer> g6Pair = Pair.of(criteriaG6Id, criteriaG6RevisedId);
        final Pair<Integer, Integer> g8Pair = Pair.of(criteriaG8Id, criteriaG10Id);
        final Pair<Integer, Integer> g9Pair = Pair.of(criteriaG9Id, criteriaG9RevisedId);
        final Pair<Integer, Integer> d2Pair = Pair.of(criteriaD2Id, criteriaD2RevisedId);
        final Pair<Integer, Integer> d3Pair = Pair.of(criteriaD3Id, criteriaD3RevisedId);
        final Pair<Integer, Integer> d10Pair = Pair.of(criteriaD10Id, criteriaD10RevisedId);
        oldAndNewcriteriaIdPairs = new LinkedHashSet<Pair<Integer, Integer>>(
                Arrays.asList(a9Pair, b1Pair, b2Pair, b3Pair, b6Pair, b7Pair, b8Pair, b9Pair,
                        c3Pair, e1Pair, f5Pair, g6Pair, g8Pair, g9Pair, d2Pair, d3Pair, d10Pair));
    }
}
