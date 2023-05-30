package gov.healthit.chpl.questionableactivity.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityCertificationResult;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;

/**
 * Checker for certification criteria questionable activity.
 */
@Component
public class CertificationResultQuestionableActivityProvider {

    public QuestionableActivityCertificationResult checkG1SuccessUpdated(CertificationResult origCertResult,
            CertificationResult newCertResult) {

        QuestionableActivityCertificationResult activity = null;
        if (origCertResult.isG1Success() != null || newCertResult.isG1Success() != null) {
            if ((origCertResult.isG1Success() == null && newCertResult.isG1Success() != null)
                    || (!origCertResult.isG1Success() && newCertResult.isG1Success())) {
                //g1 success changed to true
                activity = new QuestionableActivityCertificationResult();
                activity.setBefore(Boolean.FALSE.toString());
                activity.setAfter(Boolean.TRUE.toString());
            } else if ((origCertResult.isG1Success() != null && newCertResult.isG1Success() == null)
                    || (origCertResult.isG1Success() && !newCertResult.isG1Success())) {
                //g1 success changed to false
                activity = new QuestionableActivityCertificationResult();
                activity.setBefore(Boolean.TRUE.toString());
                activity.setAfter(Boolean.FALSE.toString());
            }
        }
        return activity;
    }

    public QuestionableActivityCertificationResult checkG2SuccessUpdated(CertificationResult origCertResult,
            CertificationResult newCertResult) {

        QuestionableActivityCertificationResult activity = null;
        if (origCertResult.isG2Success() != null || newCertResult.isG2Success() != null) {
            if ((origCertResult.isG2Success() == null && newCertResult.isG2Success() != null)
                    || (!origCertResult.isG2Success() && newCertResult.isG2Success())) {
                //g2 success changed to true
                activity = new QuestionableActivityCertificationResult();
                activity.setBefore(Boolean.FALSE.toString());
                activity.setAfter(Boolean.TRUE.toString());
            } else if ((origCertResult.isG2Success() != null && newCertResult.isG2Success() == null)
                    || (origCertResult.isG2Success() && !newCertResult.isG2Success())) {
                //g2 success changed to false
                activity = new QuestionableActivityCertificationResult();
                activity.setBefore(Boolean.TRUE.toString());
                activity.setAfter(Boolean.FALSE.toString());
            }
        }

        return activity;
    }

    public QuestionableActivityCertificationResult checkGapUpdated(CertificationResult origCertResult,
            CertificationResult newCertResult) {

        QuestionableActivityCertificationResult activity = null;
        if (origCertResult.isGap() != null || newCertResult.isGap() != null) {
            if ((origCertResult.isGap() == null && newCertResult.isGap() != null)
                    ||  (!origCertResult.isGap() && newCertResult.isGap())) {
                //gap changed to true
                activity = new QuestionableActivityCertificationResult();
                activity.setBefore(Boolean.FALSE.toString());
                activity.setAfter(Boolean.TRUE.toString());
            } else if ((origCertResult.isGap() != null && newCertResult.isGap() == null)
                    || (origCertResult.isGap() && !newCertResult.isGap())) {
                //gap changed to false
                activity = new QuestionableActivityCertificationResult();
                activity.setBefore(Boolean.TRUE.toString());
                activity.setAfter(Boolean.FALSE.toString());
            }
        }
        return activity;
    }

    public List<QuestionableActivityCertificationResult> checkReplacedSvapAdded(CertificationResult origCertResult,
            CertificationResult newCertResult) {

        //Get the added SVAPs
        List<CertificationResultSvap> addedSvaps = subtractCertificationResultSvapLists(
                newCertResult.getSvaps() == null ? new ArrayList<CertificationResultSvap>() : newCertResult.getSvaps(),
                origCertResult.getSvaps() == null ? new ArrayList<CertificationResultSvap>() : origCertResult.getSvaps());

        return addedSvaps.stream()
                .filter(crs -> crs.getReplaced())
                .map(crs -> {
                    QuestionableActivityCertificationResult dto = new QuestionableActivityCertificationResult();
                    dto.setAfter(crs.getRegulatoryTextCitation() + ": " + crs.getApprovedStandardVersion());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private List<CertificationResultSvap> subtractCertificationResultSvapLists(List<CertificationResultSvap> listA,
            List<CertificationResultSvap> listB) {

        Predicate<CertificationResultSvap> notInListB = svapFromA -> !listB.stream()
                .anyMatch(svap -> svapFromA.getSvapId().equals(svap.getSvapId()));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }
}
