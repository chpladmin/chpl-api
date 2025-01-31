package gov.healthit.chpl.scheduler.job.onetime;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.activity.entity.ActivityEntity;
import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.standard.CertificationResultStandard;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.standard.StandardGroupService;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.Util;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "standardsUpdateJobLogger")
public class StandardsUpdateJob implements Job {

    @Autowired
    private ListingActivityUtil listingActivityUtil;

    @Autowired
    private StandardGroupService standardGroupService;

    @Autowired
    private StandardsCheckerActivityDAO standardsCheckerActivityDao;

    private LocalDate startDate = LocalDate.parse("2024-12-18");
    private LocalDate endDate = LocalDate.parse("2025-01-22");

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Standards Checker Job. *********");
        try {
            // get all listing ids that were updated between 2024-12-18 and 2025-01-22
            Set<Long> listingIdsWithUpdates = standardsCheckerActivityDao.getListingsUpdatedBetween(startDate, endDate);
            LOGGER.info("Found " + listingIdsWithUpdates.size() + " listings with activity between " + startDate + " and " + endDate);

            // for each listing
            for (Long listingId : listingIdsWithUpdates) {
                // get all activities for this listing between those dates
                Set<ActivityDTO> listingActivities = standardsCheckerActivityDao.getListingUpdatesBetween(listingId, startDate, endDate);
                LOGGER.info("Inspecting " + listingActivities.size() + " activities for listing " + listingId);

                //sort the activities
                listingActivities.stream()
                    .sorted((a1, a2) -> a1.getActivityDate().compareTo(a2.getActivityDate()));

                //print something for the listing creation/confirmation events
                listingActivities.stream()
                    .filter(activity -> StringUtils.isEmpty(activity.getOriginalData()) || StringUtils.isEmpty(activity.getNewData()))
                    .forEach(activity -> LOGGER.info("\tActivity " + activity.getId() + " from " + activity.getActivityDate() + " is a listing creation event. Nothing else to do."));

                // check each activity originalData vs newData to see if any standards were added to any criteria
                listingActivities.stream()
                    .filter(activity -> !StringUtils.isEmpty(activity.getOriginalData()) && !StringUtils.isEmpty(activity.getNewData()))
                    .forEach(listingUpdateActivity -> {
                        try {
                            CertifiedProductSearchDetails originalListing = listingActivityUtil.getListing(listingUpdateActivity.getOriginalData());
                            CertifiedProductSearchDetails updatedListing = listingActivityUtil.getListing(listingUpdateActivity.getNewData());

                            LOGGER.info("\tInspecting activity ID " + listingUpdateActivity.getId() + " from " + listingUpdateActivity.getActivityDate());
                            List<CertificationResult> originalCertResults = originalListing.getCertificationResults();
                            List<CertificationResult> updatedCertResults = updatedListing.getCertificationResults();

                            //find cert results that were newly attested in this edit that have standards
                            List<CertificationResult> addedCertResultsWithStandards = new ArrayList<CertificationResult>();
                            if (!CollectionUtils.isEmpty(updatedCertResults)) {
                                addedCertResultsWithStandards = updatedCertResults.stream()
                                        .filter(cr -> getMatchingCertResultInList(cr, originalCertResults).isEmpty())
                                        .filter(cr -> !CollectionUtils.isEmpty(cr.getStandards()))
                                        .toList();

                            }

                            //find already attested cert results that had standards added to them in this edit
                            List<CertificationResult> certResultsWithUpdatedStandards = updatedCertResults.stream()
                                    .filter(updatedCr -> {
                                        if (CollectionUtils.isEmpty(updatedCr.getStandards())) {
                                            return false;
                                        }
                                        Optional<CertificationResult> originalCr = getMatchingCertResultInList(updatedCr, originalCertResults);
                                        if (originalCr.isPresent()) {
                                            List<CertificationResultStandard> addedStandards = updatedCr.getStandards().stream()
                                                    .filter(crs -> getMatchingStandardInList(crs, originalCr.get().getStandards()).isEmpty())
                                                    .toList();
                                            return !CollectionUtils.isEmpty(addedStandards);
                                        }
                                        return false;
                                    })
                                    .toList();

                            //log the listing id and criterion number and standards added
                            addedCertResultsWithStandards.stream()
                                .forEach(cr -> {
                                    LOGGER.info("\t\t" + Util.formatCriteriaNumber(cr.getCriterion()) + " was attested with standards: "
                                            + Util.joinListGrammatically(cr.getStandards().stream().map(std -> std.getStandard().getRegulatoryTextCitation()).toList()));
                                    //TODO
                                    // are any of the added standards currently retired?
                                    removeRetiredNonGroupedStandards(cr);
                                        // they should be logged and/or removed
                                    // are any of the added standards part of a group where there is a newer standard also included on that criteria?
                                    removeOldGroupedStandardsIfNewerStandardInGroupIsPresent(cr);
                                        // they should be logged and/or removed
                                });

                            certResultsWithUpdatedStandards.stream()
                                .forEach(cr -> {
                                    Optional<CertificationResult> originalCr = getMatchingCertResultInList(cr, originalCertResults);
                                    List<CertificationResultStandard> addedStandards = cr.getStandards().stream()
                                                .filter(crs -> getMatchingStandardInList(crs, originalCr.get().getStandards()).isEmpty())
                                                .toList();
                                    LOGGER.info("\t\t" + Util.formatCriteriaNumber(cr.getCriterion()) + " added standards: "
                                            + Util.joinListGrammatically(addedStandards.stream().map(std -> std.getStandard().getRegulatoryTextCitation()).toList()));

                                    //TODO
                                    // are any of the added standards currently retired?
                                    removeRetiredNonGroupedStandards(cr);
                                        // they should be logged and/or removed
                                    // are any of the added standards part of a group where there is a newer standard also included on that criteria?
                                    removeOldGroupedStandardsIfNewerStandardInGroupIsPresent(cr);
                                        // they should be logged and/or removed
                                });
                        } catch (Exception ex) {
                            LOGGER.error("Unable to handle activity " + listingUpdateActivity.getId(), ex);
                        }
                    });

            }
        } catch (Exception ex) {
            LOGGER.fatal("Unexpected exception was caught. All listings may not have been processed.", ex);
        }


        LOGGER.info("********* Completed the Standards Checker job. *********");
    }

    private Optional<CertificationResult> getMatchingCertResultInList(CertificationResult cr, List<CertificationResult> certificationResults) {
        if (CollectionUtils.isEmpty(certificationResults)) {
            return Optional.empty();
        }
        return certificationResults.stream()
                .filter(certificationResult ->
                certificationResult != null && certificationResult.getCriterion() != null
                            ? certificationResult.getCriterion().getId().equals(cr.getCriterion().getId())
                            : false)
                .findAny();
    }

    private Optional<CertificationResultStandard> getMatchingStandardInList(CertificationResultStandard crs, List<CertificationResultStandard> certificationResultStandards) {
        if (CollectionUtils.isEmpty(certificationResultStandards)) {
            return Optional.empty();
        }
        return certificationResultStandards.stream()
                .filter(certificationResultStandard ->
                        certificationResultStandard != null ? certificationResultStandard.getStandard().getId().equals(crs.getStandard().getId()) : false)
                .findAny();
    }

    private void removeRetiredNonGroupedStandards(CertificationResult cr) {
        Map<String, List<Standard>> groupedStandards = standardGroupService.getGroupedStandardsForCriteria(cr.getCriterion(), LocalDate.now());
        List<CertificationResultStandard> retiredNonGroupedStandards = cr.getStandards().stream()
            .filter(std -> !isStandardInAGroup(groupedStandards, std.getStandard()))
            .filter(std -> std.getStandard().getEndDay() != null && std.getStandard().getEndDay().isBefore(LocalDate.now()))
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(retiredNonGroupedStandards)) {
            LOGGER.info("\t\t\tNo retired non-grouped standards are present on " + Util.formatCriteriaNumber(cr.getCriterion()));
        } else {
            LOGGER.info("\t\t\tRetired non-grouped standards for : " + Util.formatCriteriaNumber(cr.getCriterion()));
            retiredNonGroupedStandards.stream()
                .forEach(std -> LOGGER.info("\t\t\t" + std.getStandard().getRegulatoryTextCitation()));
        }
    }

    private Boolean isStandardInAGroup(Map<String, List<Standard>> standardGroups, Standard standard) {
        Boolean isStdInAnyGroup = standardGroups.entrySet().stream()
            .flatMap(mapEntry -> mapEntry.getValue().stream())
            .filter(std -> std.getId().equals(standard.getId()))
            .findAny()
            .isPresent();
        return isStdInAnyGroup;
    }

    private void removeOldGroupedStandardsIfNewerStandardInGroupIsPresent(CertificationResult cr) {
        Map<String, List<Standard>> groupedStandards = standardGroupService.getGroupedStandardsForCriteria(cr.getCriterion(), LocalDate.now());
        List<CertificationResultStandard> standardsWithNewerStandardInGroup = cr.getStandards().stream()
            .filter(std -> isStandardInAGroup(groupedStandards, std.getStandard()))
            .filter(std -> isNewerStandardInGroup(groupedStandards.get(getStandardGroupName(groupedStandards, std.getStandard())), std.getStandard()))
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(standardsWithNewerStandardInGroup)) {
            LOGGER.info("\t\t\tNo grouped standards with newer standards present from the group for " + Util.formatCriteriaNumber(cr.getCriterion()));
        } else {
            LOGGER.info("\t\t\tGrouped standards with newer standards present from the group for: " + Util.formatCriteriaNumber(cr.getCriterion()));
            standardsWithNewerStandardInGroup.stream()
                .forEach(std -> LOGGER.info("\t\t\t" + std.getStandard().getRegulatoryTextCitation()));
        }
    }

    private String getStandardGroupName(Map<String, List<Standard>> standardGroups, Standard standard) {
        return standardGroups.keySet().stream()
            .filter(key -> isStandardInGroup(standardGroups.get(key), standard))
            .findAny()
            .orElse(null);
    }

    private Boolean isStandardInGroup(List<Standard> standardsInGroup, Standard standard) {
        Boolean isStdInGroup = standardsInGroup.stream()
            .filter(std -> std.getId().equals(standard.getId()))
            .findAny()
            .isPresent();
        return isStdInGroup;
    }

    private boolean isNewerStandardInGroup(List<Standard> standardsInGroup, Standard standard) {
        return standardsInGroup.stream()
            .filter(std -> std.getStartDay().isAfter(standard.getStartDay()))
            .findAny()
            .isPresent();
    }

    @Component
    private static class StandardsCheckerActivityDAO extends BaseDAOImpl {

        @Autowired
        StandardsCheckerActivityDAO() {
            super();
        }

        @Transactional
        public Set<Long> getListingsUpdatedBetween(LocalDate startDate, LocalDate endDate) {
            String hql = "SELECT activityObjectId "
                    + "FROM ActivityEntity ae "
                    + "WHERE activityObjectConceptId = 1 "
                    + "AND activityDate > :startDate "
                    + "AND activityDate < :endDate ";

            Query query = entityManager.createQuery(hql);
            query.setParameter("startDate", DateUtil.toDate(startDate));
            query.setParameter("endDate", DateUtil.toDate(endDate));
            List<Long> allIds = query.getResultList();
            return allIds.stream().collect(Collectors.toSet());
        }

        @Transactional
        public Set<ActivityDTO> getListingUpdatesBetween(Long listingId, LocalDate startDate, LocalDate endDate) {
            String hql = "SELECT ae "
                    + "FROM ActivityEntity ae "
                    + "WHERE activityObjectId = :listingId "
                    + "AND activityObjectConceptId = 1 "
                    + "AND activityDate > :startDate "
                    + "AND activityDate < :endDate ";

            Query query = entityManager.createQuery(hql);
            query.setParameter("listingId", listingId);
            query.setParameter("startDate", DateUtil.toDate(startDate));
            query.setParameter("endDate", DateUtil.toDate(endDate));
            List<ActivityEntity> entities = query.getResultList();
            return entities.stream()
                    .map(entity -> entity.toDomain())
                    .collect(Collectors.toSet());
        }
    }
}