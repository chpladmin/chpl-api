package gov.healthit.chpl.scheduler.job.onetime;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "standardsUpdateJobLogger")
public class StandardsUpdateJob implements Job {

    @Autowired
    private ListingActivityUtil listingActivityUtil;

    @Autowired
    private StandardGroupService standardGroupService;

    @Autowired
    private CertifiedProductDetailsManager cpdManager;

    @Autowired
    private StandardsCheckerActivityDAO standardsCheckerActivityDao;

    private LocalDate startDate = LocalDate.parse("2024-12-18");
    private LocalDate endDate = LocalDate.parse("2025-01-22");

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Standards Update Job. *********");
        try {
            Map<Long, List<ListingCriterionStandardsMap>> listingsWithAddedStandardsDuringTime = new LinkedHashMap<Long, List<ListingCriterionStandardsMap>>();

            // get all listing ids that were updated between 2024-12-18 and 2025-01-22
            Set<Long> listingIdsWithUpdates = standardsCheckerActivityDao.getListingsUpdatedBetween(startDate, endDate);
            listingIdsWithUpdates.stream()
                .forEach(id -> listingsWithAddedStandardsDuringTime.put(id, new ArrayList<ListingCriterionStandardsMap>()));

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
                                    // are any of the added standards currently retired?
                                    List<CertificationResultStandard> addedRetiredNonGroupedStandards
                                        = getAddedRetiredNonGroupedStandards(cr, cr.getStandards());
                                        // they should be logged and/or removed
                                    // are any of the added standards part of a group where there is a newer standard also included on that criteria?
                                    List<CertificationResultStandard> addedOldGroupedStandardsWithNewerStandardsInGroup
                                        = getAddedOldGroupedStandardsIfNewerStandardInGroupIsPresent(cr, cr.getStandards());
                                        // they should be logged and/or removed

                                    if (!CollectionUtils.isEmpty(addedRetiredNonGroupedStandards)
                                            || !CollectionUtils.isEmpty(addedOldGroupedStandardsWithNewerStandardsInGroup)) {
                                        listingsWithAddedStandardsDuringTime.get(updatedListing.getId())
                                            .add(ListingCriterionStandardsMap.builder()
                                                    .listingId(updatedListing.getId())
                                                    .criterion(cr.getCriterion())
                                                    .questionableStandardsAdded(Stream.concat(
                                                            addedRetiredNonGroupedStandards.stream().map(std -> std.getStandard()),
                                                            addedOldGroupedStandardsWithNewerStandardsInGroup.stream().map(std -> std.getStandard()))
                                                            .toList())
                                                    .build());
                                    }
                                });

                            certResultsWithUpdatedStandards.stream()
                                .forEach(cr -> {
                                    Optional<CertificationResult> originalCr = getMatchingCertResultInList(cr, originalCertResults);
                                    List<CertificationResultStandard> addedStandards = cr.getStandards().stream()
                                                .filter(crs -> getMatchingStandardInList(crs, originalCr.get().getStandards()).isEmpty())
                                                .toList();
                                    LOGGER.info("\t\t" + Util.formatCriteriaNumber(cr.getCriterion()) + " added standards: "
                                            + Util.joinListGrammatically(addedStandards.stream().map(std -> std.getStandard().getRegulatoryTextCitation()).toList()));

                                    // are any of the added standards currently retired?
                                    List<CertificationResultStandard> addedRetiredNonGroupedStandards
                                        = getAddedRetiredNonGroupedStandards(cr, addedStandards);
                                        // they should be logged and/or removed
                                    // are any of the added standards part of a group where there is a newer standard also included on that criteria?
                                    List<CertificationResultStandard> addedOldGroupedStandardsWithNewerStandardsInGroup
                                        = getAddedOldGroupedStandardsIfNewerStandardInGroupIsPresent(cr, addedStandards);
                                        // they should be logged and/or removed

                                    if (!CollectionUtils.isEmpty(addedRetiredNonGroupedStandards)
                                            || !CollectionUtils.isEmpty(addedOldGroupedStandardsWithNewerStandardsInGroup)) {
                                        listingsWithAddedStandardsDuringTime.get(updatedListing.getId())
                                            .add(ListingCriterionStandardsMap.builder()
                                                    .listingId(updatedListing.getId())
                                                    .criterion(cr.getCriterion())
                                                    .questionableStandardsAdded(Stream.concat(
                                                            addedRetiredNonGroupedStandards.stream().map(std -> std.getStandard()),
                                                            addedOldGroupedStandardsWithNewerStandardsInGroup.stream().map(std -> std.getStandard()))
                                                            .toList())
                                                    .build());
                                    }
                                });
                        } catch (Exception ex) {
                            LOGGER.error("Unable to handle activity " + listingUpdateActivity.getId(), ex);
                        }
                    });
            }

            LOGGER.info("Listng ID,CHPL Product Number,ONC-ACB,Criterion,Added Questionable Standards,Remaining Questionable Standards");
            listingsWithAddedStandardsDuringTime.keySet().stream()
                .filter(listingId -> !CollectionUtils.isEmpty(listingsWithAddedStandardsDuringTime.get(listingId)))
                .flatMap(listingId -> listingsWithAddedStandardsDuringTime.get(listingId).stream())
                .forEach(item -> {
                    try {
                        CertifiedProductSearchDetails currListing = cpdManager.getCertifiedProductDetailsNoCache(item.getListingId());
                        String output = currListing.getId()
                                + "," + currListing.getChplProductNumber()
                                + "," + currListing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString()
                                + "," + Util.formatCriteriaNumber(item.getCriterion())
                                + ",\"" + Util.joinListGrammatically(item.getQuestionableStandardsAdded().stream().map(std -> std.getRegulatoryTextCitation()).toList()) + "\"";

                        List<Standard> standardsCurrentlyOnCertResult = currListing.getCertificationResults().stream()
                                .filter(cr -> cr.getCriterion().getId().equals(item.getCriterion().getId()))
                                .flatMap(cr -> cr.getStandards().stream())
                                .map(crStd -> crStd.getStandard())
                                .collect(Collectors.toList());

                        List<Standard> remainingQuestionableStandards = item.getQuestionableStandardsAdded().stream()
                                .filter(addedQuestionableStandard -> isStandardInGroup(standardsCurrentlyOnCertResult, addedQuestionableStandard))
                                .collect(Collectors.toList());
                        output += ",\"" + Util.joinListGrammatically(remainingQuestionableStandards.stream().map(std -> std.getRegulatoryTextCitation()).toList()) + "\"";

                        LOGGER.info(output);
                    } catch (Exception ex) {
                        LOGGER.error("Unable to compare listing with ID " + item.getListingId() + " to current details for that listing", ex);
                    }
            });
        } catch (Exception ex) {
            LOGGER.fatal("Unexpected exception was caught. All listings may not have been processed.", ex);
        }


        LOGGER.info("********* Completed the Standards Update job. *********");
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

    private List<CertificationResultStandard> getAddedRetiredNonGroupedStandards(CertificationResult cr, List<CertificationResultStandard> addedStandards) {
        Map<String, List<Standard>> groupedStandards = standardGroupService.getGroupedStandardsForCriteria(cr.getCriterion(), LocalDate.now());
        List<CertificationResultStandard> addedRetiredNonGroupedStandards = addedStandards.stream()
            .filter(std -> !isStandardInAGroup(groupedStandards, std.getStandard()))
            .filter(std -> std.getStandard().getEndDay() != null && std.getStandard().getEndDay().isBefore(LocalDate.now()))
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(addedRetiredNonGroupedStandards)) {
            LOGGER.info("\t\t\tNo retired non-grouped standards were added to " + Util.formatCriteriaNumber(cr.getCriterion()));
        } else {
            LOGGER.info("\t\t\tRetired non-grouped standards were added for : " + Util.formatCriteriaNumber(cr.getCriterion()));
            addedRetiredNonGroupedStandards.stream()
                .forEach(std -> LOGGER.info("\t\t\t" + std.getStandard().getRegulatoryTextCitation()));
        }
        return addedRetiredNonGroupedStandards;
    }

    private Boolean isStandardInAGroup(Map<String, List<Standard>> standardGroups, Standard standard) {
        Boolean isStdInAnyGroup = standardGroups.entrySet().stream()
            .flatMap(mapEntry -> mapEntry.getValue().stream())
            .filter(std -> std.getId().equals(standard.getId()))
            .findAny()
            .isPresent();
        return isStdInAnyGroup;
    }

    private List<CertificationResultStandard> getAddedOldGroupedStandardsIfNewerStandardInGroupIsPresent(CertificationResult cr, List<CertificationResultStandard> addedStandards) {
        Map<String, List<Standard>> todaysGroupedStandardsForCriterion = standardGroupService.getGroupedStandardsForCriteria(cr.getCriterion(), LocalDate.now());
        List<CertificationResultStandard> addedStandardsWithNewerStandardInGroup = addedStandards.stream()
            .filter(std -> isStandardInAGroup(todaysGroupedStandardsForCriterion, std.getStandard()))
            .filter(std -> isNewerStandardInGroup(
                    todaysGroupedStandardsForCriterion.get(getStandardGroupName(todaysGroupedStandardsForCriterion, std.getStandard())),
                    addedStandards.stream().map(crStd -> crStd.getStandard()).toList(),
                    std.getStandard()))
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(addedStandardsWithNewerStandardInGroup)) {
            LOGGER.info("\t\t\tNo added grouped standards with newer standards present from the group for " + Util.formatCriteriaNumber(cr.getCriterion()));
        } else {
            LOGGER.info("\t\t\tGrouped standards added with newer standards present from the group for: " + Util.formatCriteriaNumber(cr.getCriterion()));
            addedStandardsWithNewerStandardInGroup.stream()
                .forEach(std -> LOGGER.info("\t\t\t" + std.getStandard().getRegulatoryTextCitation()));
        }
        return addedStandardsWithNewerStandardInGroup;
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

    private boolean isNewerStandardInGroup(List<Standard> standardsInGroup, List<Standard> allCrStandards, Standard standard) {
        return allCrStandards.stream()
                //is there a standard on the cert result that's
                // a) not this standard
                // b) also in this standard group
                // and c) has a more recent start date
                .filter(crStd -> !crStd.getId().equals(standard.getId())
                        && isStandardInGroup(standardsInGroup, crStd)
                        && crStd.getStartDay().isAfter(standard.getStartDay()))
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static class ListingCriterionStandardsMap {
        private Long listingId;
        private CertificationCriterion criterion;
        private List<Standard> questionableStandardsAdded;
    }
}