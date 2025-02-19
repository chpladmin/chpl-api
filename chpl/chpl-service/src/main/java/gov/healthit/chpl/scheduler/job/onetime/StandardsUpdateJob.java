package gov.healthit.chpl.scheduler.job.onetime;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
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
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.scheduler.SchedulerSecurityContextService;
import gov.healthit.chpl.standard.CertificationResultStandard;
import gov.healthit.chpl.standard.Standard;
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
    private CertifiedProductDetailsManager cpdManager;

    @Autowired
    private CertifiedProductManager cpManager;

    @Autowired
    private StandardsCheckerActivityDAO standardsCheckerActivityDao;

    @Autowired
    private SchedulerSecurityContextService securityContextService;

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
                            if (!CollectionUtils.isEmpty(updatedCertResults)) {
                                updatedCertResults.stream()
                                        .filter(cr -> getMatchingCertResultInList(cr, originalCertResults).isEmpty())
                                        .filter(cr -> !CollectionUtils.isEmpty(cr.getStandards()))
                                        .forEach(cr -> {
                                            LOGGER.info("\t\t" + Util.formatCriteriaNumber(cr.getCriterion()) + " was attested with standards: "
                                                    + Util.joinListGrammatically(cr.getStandards().stream().map(std -> std.getStandard().getRegulatoryTextCitation()).toList()));

                                            ListingCriterionStandardsMap existingCriterionMap = getMapForListingAndCriterion(updatedListing.getId(),
                                                    cr.getCriterion().getId(),
                                                    listingsWithAddedStandardsDuringTime);

                                            if (existingCriterionMap != null) {
                                                existingCriterionMap.getStandardsAdded().addAll(
                                                        cr.getStandards().stream().map(std -> std.getStandard()).toList());
                                            } else {
                                                listingsWithAddedStandardsDuringTime.get(updatedListing.getId())
                                                    .add(ListingCriterionStandardsMap.builder()
                                                            .listingId(updatedListing.getId())
                                                            .criterion(cr.getCriterion())
                                                            .standardsAdded(
                                                                    cr.getStandards().stream().map(std -> std.getStandard()).collect(Collectors.toSet()))
                                                            .build());
                                            }
                                        });
                            }

                            //find already attested cert results that had standards added to them in this edit
                            updatedCertResults.stream()
                                    .filter(updatedCr -> !CollectionUtils.isEmpty(updatedCr.getStandards()))
                                    .forEach(updatedCr -> {
                                        Optional<CertificationResult> originalCr = getMatchingCertResultInList(updatedCr, originalCertResults);
                                        if (originalCr.isPresent()) {
                                            List<CertificationResultStandard> addedStandards = updatedCr.getStandards().stream()
                                                    .filter(crs -> getMatchingStandardInList(crs, originalCr.get().getStandards()).isEmpty())
                                                    .toList();

                                            if (!CollectionUtils.isEmpty(addedStandards)) {
                                                LOGGER.info("\t\t" + Util.formatCriteriaNumber(updatedCr.getCriterion()) + " added standards: "
                                                        + Util.joinListGrammatically(addedStandards.stream().map(std -> std.getStandard().getRegulatoryTextCitation()).toList()));

                                                ListingCriterionStandardsMap existingCriterionMap = getMapForListingAndCriterion(updatedListing.getId(),
                                                        updatedCr.getCriterion().getId(),
                                                        listingsWithAddedStandardsDuringTime);

                                                if (existingCriterionMap != null) {
                                                    existingCriterionMap.getStandardsAdded().addAll(
                                                            addedStandards.stream().map(std -> std.getStandard()).toList());
                                                } else {
                                                    listingsWithAddedStandardsDuringTime.get(updatedListing.getId())
                                                        .add(ListingCriterionStandardsMap.builder()
                                                            .listingId(updatedListing.getId())
                                                            .criterion(updatedCr.getCriterion())
                                                            .standardsAdded(
                                                                    addedStandards.stream().map(std -> std.getStandard()).collect(Collectors.toSet()))
                                                            .build());
                                                }
                                            }
                                        }
                                    });

                            //All the listing/cert result/standard info about added standards is in listingsWithAddedStandardsDuringTime
                            //This is to keep track of all the listings and standards that were added during the period of time in question.
                            //I want to be careful about what gets deleted later, so I am tracking here what things we added so that I don't
                            //consider any standards or criterion outside of what we added.
                        } catch (Exception ex) {
                            LOGGER.error("Unable to handle activity " + listingUpdateActivity.getId(), ex);
                        }
                    });
            }

            //log listings with questionable standards before deletion
            LOGGER.info("Listng ID,CHPL Product Number,ONC-ACB,Criterion,Added Questionable Baseline Standards,Added Questionable Grouped Standards,Standard Group");
            listingsWithAddedStandardsDuringTime.keySet().stream()
                .filter(listingId -> !CollectionUtils.isEmpty(listingsWithAddedStandardsDuringTime.get(listingId)))
                .flatMap(listingId -> listingsWithAddedStandardsDuringTime.get(listingId).stream())
                .forEach(listingWithAddedStandards -> {
                    try {
                        CertifiedProductSearchDetails currListing = cpdManager.getCertifiedProductDetailsNoCache(listingWithAddedStandards.getListingId());
                        List<ListingCriterionQuestionableStandardsMap> addedQuestionableStandardsMap
                            = getQuestionableAddedStandardsMap(currListing, listingWithAddedStandards);
                        addedQuestionableStandardsMap.stream()
                            .forEach(addedQuestionableStandardSet -> {
                                String output = currListing.getId()
                                        + "," + currListing.getChplProductNumber()
                                        + "," + currListing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString()
                                        + "," + Util.formatCriteriaNumber(addedQuestionableStandardSet.getCriterion())
                                        + ",\"" + Util.joinListGrammatically(addedQuestionableStandardSet.getRetiredBaselineStandardsAdded().stream().map(std -> std.getRegulatoryTextCitation()).toList()) + "\""
                                        + ",\"" + Util.joinListGrammatically(addedQuestionableStandardSet.getGroupedStandardsAddedWithMultipleInGroup().stream().map(std -> std.getRegulatoryTextCitation()).toList()) + "\""
                                        + "," + (addedQuestionableStandardSet.getStandardGroupName() != null ? addedQuestionableStandardSet.getStandardGroupName() : "");
                                LOGGER.info(output);
                            });
                    } catch (Exception ex) {
                        LOGGER.error("Unable to compare listing with ID " + listingWithAddedStandards.getListingId()
                        + " to current details for that listing", ex);
                    }
            });

            //delete questionable baseline standards
            LOGGER.info("Deleting questionable baseline standards...");
            securityContextService.setAdminSecurityContext();
            listingsWithAddedStandardsDuringTime.keySet().stream()
                .filter(listingId -> !CollectionUtils.isEmpty(listingsWithAddedStandardsDuringTime.get(listingId)))
                .flatMap(listingId -> listingsWithAddedStandardsDuringTime.get(listingId).stream())
                .forEach(listingWithAddedStandards -> {
                    try {
                        AtomicBoolean madeAnyUpdates = new AtomicBoolean(false);
                        LOGGER.info("Getting listing ID " + listingWithAddedStandards.getListingId() + " details (no cache)");
                        CertifiedProductSearchDetails currListing = cpdManager.getCertifiedProductDetailsNoCache(listingWithAddedStandards.getListingId());
                        LOGGER.info("Getting added questionable standards for listing ID " + currListing.getId());
                        List<ListingCriterionQuestionableStandardsMap> addedQuestionableStandardsMap
                            = getQuestionableAddedStandardsMap(currListing, listingWithAddedStandards);
                        LOGGER.info("Determining which standards can be removed from listing ID " + currListing.getId());
                        addedQuestionableStandardsMap.stream()
                            .forEach(addedQuestionableStandardSet -> {
                                if (!CollectionUtils.isEmpty(addedQuestionableStandardSet.getRetiredBaselineStandardsAdded())) {
                                    madeAnyUpdates.set(true);
                                    addedQuestionableStandardSet.getRetiredBaselineStandardsAdded().stream()
                                        .forEach(std -> removeStandardFromListing(std, addedQuestionableStandardSet.getCriterion(), currListing));
                                }
                            });

                        if (madeAnyUpdates.get()) {
                            LOGGER.info("Saving updates to listing ID " + currListing.getId());
                            cpManager.update(ListingUpdateRequest.builder()
                                    .acknowledgeBusinessErrors(false)
                                    .acknowledgeWarnings(true)
                                    .reason("Automated update to remove standards incorrectly added by the system.")
                                    .listing(currListing)
                                    .build());
                            LOGGER.info("Saved updates to listing ID " + currListing.getId());
                        } else {
                            LOGGER.info("No updates made to listing ID " + currListing.getId());
                        }
                    } catch (Exception ex) {
                        LOGGER.error("Unable to delete questionable standards from listing " + listingWithAddedStandards.getListingId(), ex);
                    }
                });
            LOGGER.info("Completed deleting questionable baseline standards...");

        } catch (Exception ex) {
            LOGGER.fatal("Unexpected exception was caught. All listings may not have been processed.", ex);
        }

        LOGGER.info("********* Completed the Standards Update job. *********");
    }

    private void removeStandardFromListing(Standard standard, CertificationCriterion criterion, CertifiedProductSearchDetails listing) {
        CertificationResultStandard stdMappingToRemove = listing.getCertificationResults().stream()
            .filter(cr -> cr.getCriterion().getId().equals(criterion.getId()))
            .flatMap(cr -> cr.getStandards().stream())
            .filter(crstd -> crstd.getStandard().getId().equals(standard.getId()))
            .findAny()
            .orElse(null);

        LOGGER.info("Deleting standard " + stdMappingToRemove.getStandard().getRegulatoryTextCitation() + " for listing " + listing.getId() + " from criterion " + Util.formatCriteriaNumber(criterion));

        listing.getCertificationResults().stream()
            .filter(cr -> cr.getCriterion().getId().equals(criterion.getId()))
            .forEach(cr -> cr.getStandards().remove(stdMappingToRemove));

    }

    private ListingCriterionStandardsMap getMapForListingAndCriterion(Long listingId, Long criterionId, Map<Long, List<ListingCriterionStandardsMap>> listingsWithAddedStandardsDuringTime) {
        List<ListingCriterionStandardsMap> existingMaps = listingsWithAddedStandardsDuringTime.get(listingId);
        return existingMaps.stream()
                .filter(map -> map.getCriterion().getId().equals(criterionId))
                .findAny()
                .orElse(null);
    }

    private List<ListingCriterionQuestionableStandardsMap> getQuestionableAddedStandardsMap(CertifiedProductSearchDetails currentListing,
            ListingCriterionStandardsMap allAddedStandardsForCriterion) {

        List<ListingCriterionQuestionableStandardsMap> result = new ArrayList<ListingCriterionQuestionableStandardsMap>();
        //out of all the added standards for this criterion, determine which ones are still present on the listing today
        ListingCriterionStandardsMap addedStandardsStillPresentOnCriterion = getStandardsStillPresent(currentListing, allAddedStandardsForCriterion);
        if (addedStandardsStillPresentOnCriterion == null) {
            return result;
        }

        //out of those added standards still present, which ones are "questionable"?
        //where questionable is defined as either 1) retired or 2) part of a group where other standards in that group are also present
        List<Standard> questionableBaselineStandards = addedStandardsStillPresentOnCriterion.getStandardsAdded().stream()
                .filter(std -> isStandardBaselineAndRetired(addedStandardsStillPresentOnCriterion.getCriterion(), std))
                .toList();
        if (!CollectionUtils.isEmpty(questionableBaselineStandards)) {
            result.add(ListingCriterionQuestionableStandardsMap.builder()
                .listingId(currentListing.getId())
                .criterion(allAddedStandardsForCriterion.getCriterion())
                .retiredBaselineStandardsAdded(questionableBaselineStandards)
                .groupedStandardsAddedWithMultipleInGroup(new ArrayList<Standard>())
                .standardGroupName(null)
                .build());
        }

        //get distinct groups to check
        Set<String> allGroupsWithAddedStandards = addedStandardsStillPresentOnCriterion.getStandardsAdded().stream()
                .filter(std -> !StringUtils.isEmpty(std.getGroupName()))
                .map(std -> std.getGroupName())
                .collect(Collectors.toSet());

        CertificationResult certResult = currentListing.getCertificationResults().stream()
                .filter(cr -> cr.getCriterion().getId().equals(addedStandardsStillPresentOnCriterion.getCriterion().getId()))
                .findAny()
                .orElse(null);

        if (certResult != null) {
            for (String group : allGroupsWithAddedStandards) {
                List<Standard> questionableGroupedStandards = addedStandardsStillPresentOnCriterion.getStandardsAdded().stream()
                    .filter(std -> StringUtils.equals(std.getGroupName(), group))
                    .filter(std -> areMultipleStandardsFromGroupPresentOnCertResult(certResult, group))
                    .toList();
                if (!CollectionUtils.isEmpty(questionableGroupedStandards)) {
                    result.add(ListingCriterionQuestionableStandardsMap.builder()
                        .listingId(currentListing.getId())
                        .criterion(allAddedStandardsForCriterion.getCriterion())
                        .standardGroupName(group)
                        .groupedStandardsAddedWithMultipleInGroup(questionableGroupedStandards)
                        .retiredBaselineStandardsAdded(new ArrayList<Standard>())
                        .build());
                }
            }
        }

        return result;
    }

    private ListingCriterionStandardsMap getStandardsStillPresent(CertifiedProductSearchDetails currentListing,
            ListingCriterionStandardsMap allAddedStandardsForCriterion) {
        CertificationResult currentCertResult = currentListing.getCertificationResults().stream()
                .filter(currCertResult -> currCertResult.getCriterion().getId().equals(allAddedStandardsForCriterion.getCriterion().getId()))
                .findAny().orElse(null);
        if (currentCertResult == null) {
            return null;
        }
        Set<Standard> addedStandards = allAddedStandardsForCriterion.getStandardsAdded();
        Set<Standard> addedStandardsRemainingOnListing = addedStandards.stream()
            .filter(std -> isStandardInGroup(currentCertResult.getStandards().stream().map(crstd -> crstd.getStandard()).toList(), std))
            .collect(Collectors.toSet());
        return ListingCriterionStandardsMap.builder()
                .listingId(allAddedStandardsForCriterion.getListingId())
                .criterion(allAddedStandardsForCriterion.getCriterion())
                .standardsAdded(addedStandardsRemainingOnListing)
                .build();
    }

    private boolean isStandardBaselineAndRetired(CertificationCriterion criterion, Standard standard) {
        return StringUtils.isEmpty(standard.getGroupName())
                && standard.getEndDay() != null
                && standard.getEndDay().isBefore(LocalDate.now());
    }

    private boolean areMultipleStandardsFromGroupPresentOnCertResult(CertificationResult certResult, String standardGroupName) {
        return certResult.getStandards().stream()
                .filter(std -> StringUtils.equals(std.getStandard().getGroupName(), standardGroupName))
                .count() > 1;
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

    private Boolean isStandardInGroup(List<Standard> standardsInGroup, Standard standard) {
        Boolean isStdInGroup = standardsInGroup.stream()
            .filter(std -> std.getId().equals(standard.getId()))
            .findAny()
            .isPresent();
        return isStdInGroup;
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
        private Set<Standard> standardsAdded;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static class ListingCriterionQuestionableStandardsMap {
        private Long listingId;
        private CertificationCriterion criterion;
        private List<Standard> retiredBaselineStandardsAdded;
        private List<Standard> groupedStandardsAddedWithMultipleInGroup;
        private String standardGroupName;
    }
}