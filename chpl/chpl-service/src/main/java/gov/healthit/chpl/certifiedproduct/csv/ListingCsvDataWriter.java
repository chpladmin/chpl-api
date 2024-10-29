package gov.healthit.chpl.certifiedproduct.csv;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriteriaManager;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionWithAttributes;
import gov.healthit.chpl.codeset.CertificationResultCodeSet;
import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.CertifiedProductUcdProcess;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.standard.CertificationResultStandard;
import gov.healthit.chpl.standard.StandardManager;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.testtool.CertificationResultTestTool;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ListingCsvDataWriter {
    private static final int CHPL_PRODUCT_NUMBER_COL = 0;
    private static final int DEVELOPER_NAME_COL = 1;
    private static final int PRODUCT_NAME_COL = 2;
    private static final int VERSION_NAME_COL = 3;
    private static final int MEASURE_DOMAIN_COL = 4;
    private static final int MEASURE_REQUIRED_TEST_COL = 5;
    private static final int MEASURE_TYPE_COL = 6;
    private static final int MEASURE_ASSOCIATED_CRITERIA_COL = 7;
    private static final int ACB_CERTIFICATION_ID_COL = 8;
    private static final int ACB_NAME_COL = 9;
    private static final int ATL_NAME_COL = 10;
    private static final int CERTIFICATION_DATE_COL = 11;
    private static final int DEVELOPER_ADDRESS_COL_START = 12;
    private static final int DEVELOPER_WEBSITE_COL = 16;
    private static final int SELF_DEVELOPER_COL = 17;
    private static final int DEVELOPER_CONTACT_COL_START = 18;
    private static final int SVAP_NOTICE_URL_COL = 21;
    private static final int RWT_COL_START = 22;
    private static final int TARGETED_USERS_COL = 26;
    private static final int QMS_STD_START_COL = 27;
    private static final int ICS_START_COL = 30;
    private static final int ACCESSIBILITY_STANDARDS_START_COL = 32;
    private static final int K1_URL_COL = 34;
    private static final int CQM_START_COL = 35;
    private static final int SED_REPORT_URL_COL = 38;
    private static final int SED_INTENDED_USERS_COL = 39;
    private static final int SED_TESTING_DATE_COL = 40;
    private static final int PARTICIPANT_START_COL = 41;
    private static final int TASK_START_COL = 50;
    private static final int CRITERIA_START_COL = 65;
    private static final int ADDITIONAL_SOFTWARE_COL_COUNT = 5;
    private static final int UCD_PROCESS_COL_COUNT = 2;
    private static final int SED_TESTING_COL_COUNT = 2;
    private static final int TEST_DATA_COL_COUNT = 4;

    private CertificationCriteriaManager criteriaManager;
    private StandardManager standardManager;
    private DateFormat dateFormat;

    @Autowired
    public ListingCsvDataWriter(CertificationCriteriaManager criteriaManager, StandardManager standardManager) {
        this.criteriaManager = criteriaManager;
        this.standardManager = standardManager;
        this.dateFormat = new SimpleDateFormat(ListingUploadHandlerUtil.UPLOAD_DATE_FORMAT);
    }

    public List<List<String>> getCsvData(CertifiedProductSearchDetails listing, int numHeadings) {
        String[][] csvDataMatrix = new String[getNumberOfRows(listing)][numHeadings];
        csvDataMatrix[0][CHPL_PRODUCT_NUMBER_COL] = listing.getChplProductNumber();
        csvDataMatrix[0][DEVELOPER_NAME_COL] = listing.getDeveloper().getName();
        csvDataMatrix[0][PRODUCT_NAME_COL] = listing.getProduct().getName();
        csvDataMatrix[0][VERSION_NAME_COL] = forceExcelToInterpretAsText(listing.getVersion().getVersion());
        addMeasureData(csvDataMatrix, listing.getMeasures());
        csvDataMatrix[0][ACB_CERTIFICATION_ID_COL] = listing.getAcbCertificationId();
        csvDataMatrix[0][ACB_NAME_COL] = listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString();
        addTestingLabs(csvDataMatrix, listing.getTestingLabs());
        csvDataMatrix[0][CERTIFICATION_DATE_COL] = dateFormat.format(DateUtil.toDate(listing.getCertificationDay()));
        addDeveloperAddress(csvDataMatrix, listing.getDeveloper());
        csvDataMatrix[0][DEVELOPER_WEBSITE_COL] = listing.getDeveloper().getWebsite();
        csvDataMatrix[0][SELF_DEVELOPER_COL] = BooleanUtils.isTrue(listing.getDeveloper().getSelfDeveloper()) ? "1" : "0";
        addDeveloperContact(csvDataMatrix, listing.getDeveloper());
        csvDataMatrix[0][SVAP_NOTICE_URL_COL] = listing.getSvapNoticeUrl();
        addRwtData(csvDataMatrix, listing);
        addTargetedUsers(csvDataMatrix, listing.getTargetedUsers());
        addQmsStandards(csvDataMatrix, listing.getQmsStandards());
        addIcs(csvDataMatrix, listing.getIcs());
        addAccessibilityStandards(csvDataMatrix, listing.getAccessibilityStandards());
        csvDataMatrix[0][K1_URL_COL] = listing.getMandatoryDisclosures();
        addCqms(csvDataMatrix, listing.getCqmResults());
        csvDataMatrix[0][SED_REPORT_URL_COL] = listing.getSedReportFileLocation();
        csvDataMatrix[0][SED_INTENDED_USERS_COL] = listing.getSedIntendedUserDescription();
        csvDataMatrix[0][SED_TESTING_DATE_COL] = listing.getSedTestingEndDay() != null ? dateFormat.format(DateUtil.toDate(listing.getSedTestingEndDay())) : "";
        addParticipants(csvDataMatrix, listing.getSed());
        addTasks(csvDataMatrix, listing.getSed());
        addCertificationResults(csvDataMatrix, getAllAvailableCriteriaAsCertResults(listing), listing.getSed());

        List<List<String>> records = new ArrayList<List<String>>();
        for (int i = 0; i < csvDataMatrix.length; i++) {
            records.add(new ArrayList<String>());
            for (int j = 0; j < csvDataMatrix[i].length; j++) {
                records.get(i).add(StringUtils.isEmpty(csvDataMatrix[i][j]) ? "" : csvDataMatrix[i][j]);
            }
        }
        return records;
    }

    private List<CertificationResult> getAllAvailableCriteriaAsCertResults(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> allCriteriaAvailableToListing = criteriaManager.getCriteriaAvailableToListing(listing);
        List<CertificationResult> allAvailableCriteriaAsCertResults = new ArrayList<CertificationResult>();
        allCriteriaAvailableToListing.stream()
            .forEach(criterion -> {
                if (isAttested(listing, criterion)) {
                    allAvailableCriteriaAsCertResults.add(getCertResult(listing, criterion));
                } else {
                    allAvailableCriteriaAsCertResults.add(CertificationResult.builder()
                            .success(false)
                            .criterion(criterion)
                            .build());
                }
            });
        return allAvailableCriteriaAsCertResults;
    }

    private boolean isAttested(CertifiedProductSearchDetails listing, CertificationCriterion criterion) {
        CertificationResult certResult = listing.getCertificationResults().stream()
                .filter(cr -> cr.getCriterion().getId().equals(criterion.getId()))
                .findAny().orElse(null);
        return certResult != null && BooleanUtils.isTrue(certResult.getSuccess());
    }

    private CertificationResult getCertResult(CertifiedProductSearchDetails listing, CertificationCriterion criterion) {
        return listing.getCertificationResults().stream()
                .filter(cr -> cr.getCriterion().getId().equals(criterion.getId()))
                .findAny().orElse(null);
    }

    private int getNumberOfRows(CertifiedProductSearchDetails listing) {
        int maxOfNonCertResultRows = Stream.of(
                listing.getAccessibilityStandards() != null ? listing.getAccessibilityStandards().size() : 0,
                listing.getCqmResults() != null ? listing.getCqmResults().stream().filter(cqm -> cqm.getSuccess()).toList().size() : 0,
                listing.getIcs() != null && !CollectionUtils.isEmpty(listing.getIcs().getParents()) ? listing.getIcs().getParents().size() : 0,
                listing.getMeasures() != null ? listing.getMeasures().size() : 0,
                listing.getQmsStandards() != null ? listing.getQmsStandards().size() : 0,
                listing.getTargetedUsers() != null ? listing.getTargetedUsers().size() : 0,
                listing.getTestingLabs() != null ? listing.getTestingLabs().size() : 0)
                .mapToInt(size -> size)
                .max().orElse(-1);
        List<CertificationResult> certResults = listing.getCertificationResults();
        int maxAdditionalSoftwareRows = certResults.stream()
            .filter(certResult -> certResult.getAdditionalSoftware() != null)
            .map(certResult -> certResult.getAdditionalSoftware().size())
            .mapToInt(size -> size)
            .max().orElse(-1);
        int maxCodeSetRows = certResults.stream()
                .filter(certResult -> certResult.getCodeSets() != null)
                .map(certResult -> certResult.getCodeSets().size())
                .mapToInt(size -> size)
                .max().orElse(-1);
        int maxConformanceMethodRows = certResults.stream()
                .filter(certResult -> certResult.getConformanceMethods() != null)
                .map(certResult -> certResult.getConformanceMethods().size())
                .mapToInt(size -> size)
                .max().orElse(-1);
        int maxFunctionalitiesTestedRows = certResults.stream()
                .filter(certResult -> certResult.getFunctionalitiesTested() != null)
                .map(certResult -> certResult.getFunctionalitiesTested().size())
                .mapToInt(size -> size)
                .max().orElse(-1);
        int maxOptionalStandardsRows = certResults.stream()
                .filter(certResult -> certResult.getOptionalStandards() != null)
                .map(certResult -> certResult.getOptionalStandards().size())
                .mapToInt(size -> size)
                .max().orElse(-1);
        int maxStandardsRows = certResults.stream()
                .filter(certResult -> certResult.getStandards() != null)
                .map(certResult -> certResult.getStandards().size())
                .mapToInt(size -> size)
                .max().orElse(-1);
        int maxSvapsRows = certResults.stream()
                .filter(certResult -> certResult.getSvaps() != null)
                .map(certResult -> certResult.getSvaps().size())
                .mapToInt(size -> size)
                .max().orElse(-1);
        int maxTestDataRows = certResults.stream()
                .filter(certResult -> certResult.getTestDataUsed() != null)
                .map(certResult -> certResult.getTestDataUsed().size())
                .mapToInt(size -> size)
                .max().orElse(-1);
        int maxTestProcedureRows = certResults.stream()
                .filter(certResult -> certResult.getTestProcedures() != null)
                .map(certResult -> certResult.getTestProcedures().size())
                .mapToInt(size -> size)
                .max().orElse(-1);
        int maxTestStandardsRows = certResults.stream()
                .filter(certResult -> certResult.getTestStandards() != null)
                .map(certResult -> certResult.getTestStandards().size())
                .mapToInt(size -> size)
                .max().orElse(-1);
        int maxTestToolsRows = certResults.stream()
                .filter(certResult -> certResult.getTestToolsUsed() != null)
                .map(certResult -> certResult.getTestToolsUsed().size())
                .mapToInt(size -> size)
                .max().orElse(-1);

        int maxUcdProcesses = 0, maxTestTasks = 0, maxTestParticipants = 0;
        CertifiedProductSed sed = listing.getSed();
        if (sed != null) {
            maxUcdProcesses = listing.getSed().getUcdProcesses() != null ? listing.getSed().getUcdProcesses().size() : 0;
            maxTestTasks = listing.getSed().getTestTasks() != null ? listing.getSed().getTestTasks().size() : 0;
            maxTestParticipants = getAvailableUniqueTestParticipants(listing.getSed()).size();
        }

        return Stream.of(maxOfNonCertResultRows,
                maxAdditionalSoftwareRows,
                maxCodeSetRows,
                maxConformanceMethodRows,
                maxFunctionalitiesTestedRows,
                maxOptionalStandardsRows,
                maxStandardsRows,
                maxSvapsRows,
                maxTestDataRows,
                maxTestProcedureRows,
                maxTestStandardsRows,
                maxTestToolsRows,
                maxUcdProcesses,
                maxTestTasks,
                maxTestParticipants,
                1)
                .mapToInt(val -> val)
                .max()
                .orElse(1);
    }

    private List<TestParticipant> getAvailableUniqueTestParticipants(CertifiedProductSed sed) {
        List<TestTask> tasks = sed.getTestTasks();
        if (CollectionUtils.isEmpty(tasks)) {
            return List.of();
        }
        return tasks.stream()
            .flatMap(task -> task.getTestParticipants().stream())
            .filter(distinctByKey(tp -> tp.getId()))
            .collect(Collectors.toList());
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private void addMeasureData(String[][] csvDataMatrix, List<ListingMeasure> measures) {
        if (CollectionUtils.isEmpty(measures)) {
            return;
        }
        for (int i = 0; i < measures.size(); i++) {
            ListingMeasure measure = measures.get(i);
            csvDataMatrix[i][MEASURE_DOMAIN_COL] = measure.getMeasure().getDomain().getName();
            csvDataMatrix[i][MEASURE_REQUIRED_TEST_COL] = measure.getMeasure().getAbbreviation();
            csvDataMatrix[i][MEASURE_TYPE_COL] = measure.getMeasureType().getName();
            csvDataMatrix[i][MEASURE_ASSOCIATED_CRITERIA_COL] =
                    measure.getAssociatedCriteria()
                        .stream().map(crit -> Util.formatCriteriaNumber(crit))
                        .distinct()
                        .collect(Collectors.joining(";"));
        }
    }

    private void addTestingLabs(String[][] csvDataMatrix, List<CertifiedProductTestingLab> testingLabs) {
        if (CollectionUtils.isEmpty(testingLabs)) {
            return;
        }
        for (int i = 0; i < testingLabs.size(); i++) {
            CertifiedProductTestingLab testingLab = testingLabs.get(i);
            csvDataMatrix[i][ATL_NAME_COL] = testingLab.getTestingLab().getName();
        }
    }

    private void addDeveloperAddress(String[][] csvDataMatrix, Developer developer) {
        if (developer.getAddress() == null) {
            return;
        }
        int col = DEVELOPER_ADDRESS_COL_START;
        csvDataMatrix[0][col++] = developer.getAddress().getLine1();
        csvDataMatrix[0][col++] = developer.getAddress().getCity();
        csvDataMatrix[0][col++] = developer.getAddress().getState();
        csvDataMatrix[0][col++] = developer.getAddress().getZipcode();
    }

    private void addDeveloperContact(String[][] csvDataMatrix, Developer developer) {
        if (developer.getContact() == null) {
            return;
        }
        int col = DEVELOPER_CONTACT_COL_START;
        csvDataMatrix[0][col++] = developer.getContact().getEmail();
        csvDataMatrix[0][col++] = developer.getContact().getPhoneNumber();
        csvDataMatrix[0][col++] = developer.getContact().getFullName();
    }

    private void addRwtData(String[][] csvDataMatrix, CertifiedProductSearchDetails listing) {
        int col = RWT_COL_START;
        csvDataMatrix[0][col++] = !StringUtils.isEmpty(listing.getRwtPlansUrl()) ? listing.getRwtPlansUrl() : "";
        csvDataMatrix[0][col++] = listing.getRwtPlansCheckDate() != null ? dateFormat.format(DateUtil.toDate(listing.getRwtPlansCheckDate())) : "";
        csvDataMatrix[0][col++] = !StringUtils.isEmpty(listing.getRwtResultsUrl()) ? listing.getRwtResultsUrl() : "";
        csvDataMatrix[0][col++] = listing.getRwtResultsCheckDate() != null ? dateFormat.format(DateUtil.toDate(listing.getRwtResultsCheckDate())) : "";
    }

    private void addTargetedUsers(String[][] csvDataMatrix, List<CertifiedProductTargetedUser> targetedUsers) {
        for (int i = 0; i < targetedUsers.size(); i++) {
            CertifiedProductTargetedUser targetedUser = targetedUsers.get(i);
            csvDataMatrix[i][TARGETED_USERS_COL] = targetedUser.getTargetedUserName();
        }
    }

    private void addQmsStandards(String[][] csvDataMatrix, List<CertifiedProductQmsStandard> qmsStandards) {
        for (int i = 0; i < qmsStandards.size(); i++) {
            int col = QMS_STD_START_COL;
            CertifiedProductQmsStandard qmsStandard = qmsStandards.get(i);
            csvDataMatrix[i][col++] = qmsStandard.getQmsStandardName();
            csvDataMatrix[i][col++] = qmsStandard.getApplicableCriteria();
            csvDataMatrix[i][col++] = qmsStandard.getQmsModification();
        }
    }

    private void addIcs(String[][] csvDataMatrix, InheritedCertificationStatus ics) {
        if (ics != null && !CollectionUtils.isEmpty(ics.getParents())) {
            int col = ICS_START_COL;
            csvDataMatrix[0][col++] = "1";
            for (int i = 0; i < ics.getParents().size(); i++) {
                CertifiedProduct parent = ics.getParents().get(i);
                csvDataMatrix[i][col++] = parent.getChplProductNumber();
            }
        }
    }

    private void addAccessibilityStandards(String[][] csvDataMatrix, List<CertifiedProductAccessibilityStandard> accStds) {
        if (!CollectionUtils.isEmpty(accStds)) {
            int col = ACCESSIBILITY_STANDARDS_START_COL;
            csvDataMatrix[0][col++] = "1";
            for (int i = 0; i < accStds.size(); i++) {
                CertifiedProductAccessibilityStandard accStd = accStds.get(i);
                csvDataMatrix[i][col++] = accStd.getAccessibilityStandardName();
            }
        }
    }

    private void addCqms(String[][] csvDataMatrix, List<CQMResultDetails> cqmResults) {
        List<CQMResultDetails> attestedCqms = cqmResults.stream()
            .filter(cqmResult -> BooleanUtils.isTrue(cqmResult.getSuccess()))
            .collect(Collectors.toList());
        for (int i = 0; i < attestedCqms.size(); i++) {
            CQMResultDetails cqm = attestedCqms.get(i);
            int col = CQM_START_COL;
            csvDataMatrix[i][col++] = cqm.getCmsId();
            csvDataMatrix[i][col++] =
                    cqm.getSuccessVersions().stream().collect(Collectors.joining(";"));
            csvDataMatrix[i][col++] =
                    cqm.getCriteria().stream()
                        .map(crit -> Util.formatCriteriaNumber(crit.getCriterion()))
                        .collect(Collectors.joining(";"));
        }
    }

    private void addParticipants(String[][] csvDataMatrix, CertifiedProductSed sed) {
        if (sed == null) {
            return;
        }
        List<TestParticipant> testParticipants = getAvailableUniqueTestParticipants(sed);
        for (int i = 0; i < testParticipants.size(); i++) {
            TestParticipant tp = testParticipants.get(i);
            int col = PARTICIPANT_START_COL;
            if (StringUtils.isEmpty(tp.getFriendlyId())) {
                tp.setFriendlyId("CHPL-PARTICIPANT-" + (i + 1));
                //copy the unique id to all test participant objects that have this same database id
                sed.getTestTasks().stream()
                    .flatMap(tt -> tt.getTestParticipants().stream())
                    .filter(ttParticipant -> ttParticipant.getId().equals(tp.getId()))
                    .forEach(ttParticipant -> ttParticipant.setFriendlyId(tp.getFriendlyId()));
            }
            csvDataMatrix[i][col++] = tp.getFriendlyId();
            csvDataMatrix[i][col++] = tp.getGender();
            csvDataMatrix[i][col++] = tp.getAge() != null ? tp.getAge().getName() : "";
            csvDataMatrix[i][col++] = tp.getEducationType() != null ? tp.getEducationType().getName() : "";
            csvDataMatrix[i][col++] = tp.getOccupation();
            csvDataMatrix[i][col++] = tp.getProfessionalExperienceMonths() != null ? tp.getProfessionalExperienceMonths().toString() : "";
            csvDataMatrix[i][col++] = tp.getComputerExperienceMonths() != null ? tp.getComputerExperienceMonths().toString() : "";
            csvDataMatrix[i][col++] = tp.getProductExperienceMonths() != null ? tp.getProductExperienceMonths().toString() : "";
            csvDataMatrix[i][col++] = tp.getAssistiveTechnologyNeeds();
        }
    }

    private void addTasks(String[][] csvDataMatrix, CertifiedProductSed sed) {
        if (sed == null) {
            return;
        }
        List<TestTask> testTasks = sed.getTestTasks();
        for (int i = 0; i < testTasks.size(); i++) {
            TestTask task = testTasks.get(i);
            int col = TASK_START_COL;
            if (StringUtils.isEmpty(task.getFriendlyId())) {
                task.setFriendlyId("CHPL-TASK-" + (i + 1));
            }
            csvDataMatrix[i][col++] = task.getFriendlyId();
            csvDataMatrix[i][col++] = task.getDescription();
            csvDataMatrix[i][col++] = forceExcelToInterpretAsText(task.getTaskSuccessAverage().toString());
            csvDataMatrix[i][col++] = forceExcelToInterpretAsText(task.getTaskSuccessStddev().toString());
            csvDataMatrix[i][col++] = forceExcelToInterpretAsText(task.getTaskPathDeviationObserved().toString());
            csvDataMatrix[i][col++] = forceExcelToInterpretAsText(task.getTaskPathDeviationOptimal().toString());
            csvDataMatrix[i][col++] = forceExcelToInterpretAsText(task.getTaskTimeAvg().toString());
            csvDataMatrix[i][col++] = forceExcelToInterpretAsText(task.getTaskTimeStddev().toString());
            csvDataMatrix[i][col++] = forceExcelToInterpretAsText(task.getTaskTimeDeviationObservedAvg().toString());
            csvDataMatrix[i][col++] = forceExcelToInterpretAsText(task.getTaskTimeDeviationOptimalAvg().toString());
            csvDataMatrix[i][col++] = forceExcelToInterpretAsText(task.getTaskErrors().toString());
            csvDataMatrix[i][col++] = forceExcelToInterpretAsText(task.getTaskErrorsStddev().toString());
            csvDataMatrix[i][col++] = task.getTaskRatingScale();
            csvDataMatrix[i][col++] = forceExcelToInterpretAsText(task.getTaskRating().toString());
            csvDataMatrix[i][col++] = forceExcelToInterpretAsText(task.getTaskRatingStddev().toString());
        }
    }

    private void addCertificationResults(String[][] csvDataMatrix, List<CertificationResult> certResults, CertifiedProductSed sed) {
        int currCol = CRITERIA_START_COL;
        for (int i = 0; i < certResults.size(); i++) {
            int currCertResultStartCol = currCol;
            CertificationResult certResult = certResults.get(i);
            currCol = addCertificationResult(csvDataMatrix, certResult, sed, currCertResultStartCol);
        }
    }

    private int addCertificationResult(String[][] csvDataMatrix, CertificationResult certResult,
            CertifiedProductSed sed, int currCol) {
        csvDataMatrix[0][currCol++] = certResult.getSuccess() ? "1" : "";

        CertificationCriterion criterion = certResult.getCriterion();
        CertificationCriterionWithAttributes criterionWithAttributes = criteriaManager.getAllWithAttributes().stream()
                .filter(critWithAttr -> critWithAttr.getId().equals(criterion.getId()))
                .findAny().orElse(null);
        if (criterionWithAttributes == null) {
            LOGGER.error("Attributes for criterion with ID " + criterion.getId() + " could not be found. No attributes will be included in the file.");
        } else {
            currCol = addAdditionalSoftware(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addApiDocumentation(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addAttestationAnswer(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addCodeSets(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addConformanceMethods(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addDocumentationUrl(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addExportDocumentation(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addFunctionalitiesTested(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addG1Success(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addG2Success(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addGap(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addOptionalStandards(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addPrivacySecurityFramework(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addRiskManagementSummaryInformation(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addSed(csvDataMatrix, certResult, sed, criterionWithAttributes, currCol);
            currCol = addServiceBaseUrlList(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addStandards(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addSvaps(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addTestData(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addTestProcedures(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addTestTools(csvDataMatrix, certResult, criterionWithAttributes, currCol);
            currCol = addUseCases(csvDataMatrix, certResult, criterionWithAttributes, currCol);
        }
        return currCol;
    }

    private int addAdditionalSoftware(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isAdditionalSoftware()) {
            if (!CollectionUtils.isEmpty(certResult.getAdditionalSoftware())) {
                csvDataMatrix[0][currCol++] = "1";
                for (int i = 0; i < certResult.getAdditionalSoftware().size(); i++) {
                    int additionalSoftwareCol = currCol;
                    CertificationResultAdditionalSoftware as = certResult.getAdditionalSoftware().get(i);
                    csvDataMatrix[i][additionalSoftwareCol++] = !StringUtils.isEmpty(as.getCertifiedProductNumber()) ? as.getCertifiedProductNumber() : "";
                    csvDataMatrix[i][additionalSoftwareCol++] = !StringUtils.isEmpty(as.getCertifiedProductNumber()) ? as.getGrouping() : "";
                    csvDataMatrix[i][additionalSoftwareCol++] = !StringUtils.isEmpty(as.getName()) ? as.getName() : "";
                    csvDataMatrix[i][additionalSoftwareCol++] = !StringUtils.isEmpty(as.getVersion())
                            ? forceExcelToInterpretAsText(as.getVersion()) : "";
                    csvDataMatrix[i][additionalSoftwareCol++] = !StringUtils.isEmpty(as.getName()) ? as.getGrouping() : "";
                }
            } else {
                csvDataMatrix[0][currCol++] = certResult.getSuccess() ? "0" : "";
            }
            currCol += ADDITIONAL_SOFTWARE_COL_COUNT;
        }
        return currCol;
    }

    private int addApiDocumentation(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isApiDocumentation()) {
            csvDataMatrix[0][currCol++] = certResult.getApiDocumentation();
        }
        return currCol;
    }

    private int addAttestationAnswer(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isAttestationAnswer()) {
            csvDataMatrix[0][currCol++] = BooleanUtils.isTrue(certResult.getAttestationAnswer()) ? "Yes" : "No";
        }
        return currCol;
    }

    private int addCodeSets(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isCodeSet()) {
            if (!CollectionUtils.isEmpty(certResult.getCodeSets())) {
                for (int i = 0; i < certResult.getCodeSets().size(); i++) {
                    CertificationResultCodeSet codeSet = certResult.getCodeSets().get(i);
                    csvDataMatrix[i][currCol] = codeSet.getCodeSet().getName();
                }
            }
            currCol++;
        }
        return currCol;
    }

    private int addConformanceMethods(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isConformanceMethod()) {
            if (!CollectionUtils.isEmpty(certResult.getConformanceMethods())) {
                for (int i = 0; i < certResult.getConformanceMethods().size(); i++) {
                    int conformanceMethodCol = currCol;
                    CertificationResultConformanceMethod conformanceMethod = certResult.getConformanceMethods().get(i);
                    csvDataMatrix[i][conformanceMethodCol++] = conformanceMethod.getConformanceMethod().getName();
                    csvDataMatrix[i][conformanceMethodCol++] = conformanceMethod.getConformanceMethodVersion() != null
                            ? forceExcelToInterpretAsText(conformanceMethod.getConformanceMethodVersion()) : "";
                }
            }
            currCol += 2;
        }
        return currCol;
    }

    private int addDocumentationUrl(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isDocumentationUrl()) {
            csvDataMatrix[0][currCol++] = certResult.getDocumentationUrl();
        }
        return currCol;
    }

    private int addExportDocumentation(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isExportDocumentation()) {
            csvDataMatrix[0][currCol++] = certResult.getExportDocumentation();
        }
        return currCol;
    }

    private int addFunctionalitiesTested(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isFunctionalityTested()) {
            if (!CollectionUtils.isEmpty(certResult.getFunctionalitiesTested())) {
                for (int i = 0; i < certResult.getFunctionalitiesTested().size(); i++) {
                    CertificationResultFunctionalityTested funcTested = certResult.getFunctionalitiesTested().get(i);
                    csvDataMatrix[i][currCol] = funcTested.getFunctionalityTested().getRegulatoryTextCitation();
                }
            }
            currCol++;
        }
        return currCol;
    }

    private int addG1Success(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isG1Success()) {
            csvDataMatrix[0][currCol++] = BooleanUtils.isTrue(certResult.getG1Success()) ? "1" : "0";
        }
        return currCol;
    }

    private int addG2Success(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isG2Success()) {
            csvDataMatrix[0][currCol++] = BooleanUtils.isTrue(certResult.getG2Success()) ? "1" : "0";
        }
        return currCol;
    }

    private int addGap(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isGap()) {
            csvDataMatrix[0][currCol++] = BooleanUtils.isTrue(certResult.getGap()) ? "1"
                    : (certResult.getSuccess() ? "0" : "");
        }
        return currCol;
    }

    private int addOptionalStandards(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isOptionalStandard()) {
            if (!CollectionUtils.isEmpty(certResult.getOptionalStandards())) {
                for (int i = 0; i < certResult.getOptionalStandards().size(); i++) {
                    CertificationResultOptionalStandard optionalStandard = certResult.getOptionalStandards().get(i);
                    if (!StringUtils.isEmpty(optionalStandard.getOptionalStandard().getCitation())) {
                        csvDataMatrix[i][currCol] = optionalStandard.getOptionalStandard().getCitation();
                    } else if (!StringUtils.isEmpty(optionalStandard.getOptionalStandard().getDisplayValue())) {
                        csvDataMatrix[i][currCol] = optionalStandard.getOptionalStandard().getDisplayValue();
                    }
                }
            }
            currCol++;
        }
        return currCol;
    }

    private int addPrivacySecurityFramework(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isPrivacySecurityFramework()) {
            csvDataMatrix[0][currCol++] = certResult.getPrivacySecurityFramework() != null ? certResult.getPrivacySecurityFramework() : "";
        }
        return currCol;
    }

    private int addRiskManagementSummaryInformation(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isRiskManagementSummaryInformation()) {
            csvDataMatrix[0][currCol++] = certResult.getRiskManagementSummaryInformation() != null ? certResult.getRiskManagementSummaryInformation() : "";
        }
        return currCol;
    }

    private int addSed(String[][] csvDataMatrix, CertificationResult certResult, CertifiedProductSed sed,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isSed()) {
            List<CertifiedProductUcdProcess> ucdProcesses = getUcdProcessesForCriterion(certResult.getCriterion(), sed);
            if (!CollectionUtils.isEmpty(ucdProcesses)) {
                for (int i = 0; i < ucdProcesses.size(); i++) {
                    int ucdProcessCol = currCol;
                    CertifiedProductUcdProcess ucdProcess = ucdProcesses.get(i);
                    csvDataMatrix[i][ucdProcessCol++] = ucdProcess.getName();
                    csvDataMatrix[i][ucdProcessCol++] = ucdProcess.getDetails();
                }
            }
            currCol += UCD_PROCESS_COL_COUNT;

            List<TestTask> testTasks = getTestTasksForCriterion(certResult.getCriterion(), sed);
            if (!CollectionUtils.isEmpty(testTasks)) {
                for (int i = 0; i < testTasks.size(); i++) {
                    int sedTestingCol = currCol;
                    TestTask testTask = testTasks.get(i);
                    csvDataMatrix[i][sedTestingCol++] = testTask.getFriendlyId();
                    String participants = testTask.getTestParticipants().stream()
                        .map(participant -> participant.getFriendlyId())
                        .collect(Collectors.joining(";"));
                    csvDataMatrix[i][sedTestingCol++] = participants;
                }
            }
            currCol += SED_TESTING_COL_COUNT;
        }
        return currCol;
    }

    private List<CertifiedProductUcdProcess> getUcdProcessesForCriterion(CertificationCriterion criterion, CertifiedProductSed sed) {
        if (sed == null || CollectionUtils.isEmpty(sed.getUcdProcesses())) {
            return List.of();
        }
        return sed.getUcdProcesses().stream()
            .filter(ucdProcess -> ucdProcess.getCriteria().stream().map(ucdCrit -> ucdCrit.getId()).toList().contains(criterion.getId()))
            .collect(Collectors.toList());
    }

    private List<TestTask> getTestTasksForCriterion(CertificationCriterion criterion, CertifiedProductSed sed) {
        if (sed == null || CollectionUtils.isEmpty(sed.getTestTasks())) {
            return List.of();
        }
        return sed.getTestTasks().stream()
            .filter(testTask -> testTask.getCriteria().stream().map(taskCrit -> taskCrit.getId()).toList().contains(criterion.getId()))
            .collect(Collectors.toList());
    }

    private int addServiceBaseUrlList(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isServiceBaseUrlList()) {
            csvDataMatrix[0][currCol++] = certResult.getServiceBaseUrlList() != null ? certResult.getServiceBaseUrlList() : "";
        }
        return currCol;
    }

    private int addStandards(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isStandard()
                //we seem to list all criteria as eligible for standards, so here
                //we are also limiting the column presence by checking whether the criteria
                //has any standards available
                && !CollectionUtils.isEmpty(standardManager.getStandardsByCriteria(certResult.getCriterion().getId()))) {
            if (!CollectionUtils.isEmpty(certResult.getStandards())) {
                for (int i = 0; i < certResult.getStandards().size(); i++) {
                    CertificationResultStandard standard = certResult.getStandards().get(i);
                    csvDataMatrix[i][currCol] = standard.getStandard().getRegulatoryTextCitation();
                }
            }
            currCol++;
        }
        return currCol;
    }

    private int addSvaps(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isSvap()) {
            if (!CollectionUtils.isEmpty(certResult.getSvaps())) {
                for (int i = 0; i < certResult.getSvaps().size(); i++) {
                    CertificationResultSvap svap = certResult.getSvaps().get(i);
                    csvDataMatrix[i][currCol] = svap.getRegulatoryTextCitation();
                }
            }
            currCol++;
        }
        return currCol;
    }

    private int addTestData(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isTestData()) {
            if (!CollectionUtils.isEmpty(certResult.getTestDataUsed())) {
                for (int i = 0; i < certResult.getTestDataUsed().size(); i++) {
                    int testDataCol = currCol;
                    CertificationResultTestData testData = certResult.getTestDataUsed().get(i);
                    csvDataMatrix[i][testDataCol++] = testData.getTestData().getName();
                    csvDataMatrix[i][testDataCol++] = testData.getVersion() != null
                            ? forceExcelToInterpretAsText(testData.getVersion()) : "";
                    csvDataMatrix[i][testDataCol++] = !StringUtils.isEmpty(testData.getAlteration()) ? "1" : "0";
                    csvDataMatrix[i][testDataCol++] = testData.getAlteration() != null ? testData.getAlteration() : "";
                }
            }
            currCol += TEST_DATA_COL_COUNT;
        }
        return currCol;
    }

    private int addTestProcedures(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isTestProcedure()) {
            if (!CollectionUtils.isEmpty(certResult.getTestProcedures())) {
                for (int i = 0; i < certResult.getTestProcedures().size(); i++) {
                    int testProcedureCol = currCol;
                    CertificationResultTestProcedure testProcedure = certResult.getTestProcedures().get(i);
                    csvDataMatrix[i][testProcedureCol++] = testProcedure.getTestProcedure().getName();
                    csvDataMatrix[i][testProcedureCol++] = testProcedure.getTestProcedureVersion() != null
                            ? forceExcelToInterpretAsText(testProcedure.getTestProcedureVersion()) : "";
                }
            }
            currCol += 2;
        }
        return currCol;
    }

    private int addTestTools(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isTestTool()) {
            if (!CollectionUtils.isEmpty(certResult.getTestToolsUsed())) {
                for (int i = 0; i < certResult.getTestToolsUsed().size(); i++) {
                    int testToolCol = currCol;
                    CertificationResultTestTool testTool = certResult.getTestToolsUsed().get(i);
                    csvDataMatrix[i][testToolCol++] = testTool.getTestTool().getValue();
                    csvDataMatrix[i][testToolCol++] = testTool.getVersion() != null ? forceExcelToInterpretAsText(testTool.getVersion()) : "";
                }
            }
            currCol += 2;
        }
        return currCol;
    }

    private int addUseCases(String[][] csvDataMatrix, CertificationResult certResult,
            CertificationCriterionWithAttributes criterionWithAttributes, int currCol) {
        if (criterionWithAttributes.getAttributes().isUseCases()) {
            csvDataMatrix[0][currCol++] = certResult.getUseCases() != null ? certResult.getUseCases() : "";
        }
        return currCol;
    }

    private String forceExcelToInterpretAsText(String value) {
        return "\t" + value + "";
    }
}
