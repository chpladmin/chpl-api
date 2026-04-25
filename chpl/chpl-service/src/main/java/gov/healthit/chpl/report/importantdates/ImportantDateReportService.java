package gov.healthit.chpl.report.importantdates;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.dao.AttestationDAO;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationId.CertificationIdYearCalculator;
import gov.healthit.chpl.codeset.CodeSet;
import gov.healthit.chpl.codeset.CodeSetDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.functionalitytested.FunctionalityTested;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedDAO;
import gov.healthit.chpl.realworldtesting.manager.RealWorldTestingReportService;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.surveillance.report.QuarterDAO;
import gov.healthit.chpl.surveillance.report.domain.Quarter;
import gov.healthit.chpl.util.Util;
import jakarta.transaction.Transactional;

@Component
public class ImportantDateReportService {

    private CertificationCriterionDAO criteriaDao;
    private StandardDAO standardDao;
    private CodeSetDAO codeSetDao;
    private FunctionalityTestedDAO functionalityTestedDao;
    private QuarterDAO quarterDao;
    private AttestationDAO attestationDao;
    private RealWorldTestingReportService rwtReportService;
    private CertificationIdYearCalculator certIdYearCalculator;
    private Integer rolloffDays;

    @Autowired
    public ImportantDateReportService(CertificationCriterionDAO criteriaDao, StandardDAO standardDao,
            CodeSetDAO codeSetDao, FunctionalityTestedDAO functionalityTestedDao,
            QuarterDAO quarterDao, AttestationDAO attestationDao,
            RealWorldTestingReportService rwtReportService,
            CertificationIdYearCalculator certIdYearCalculator,
            @Value("${importantDateRolloffDays}") Integer rolloffDays) {
        this.criteriaDao = criteriaDao;
        this.standardDao = standardDao;
        this.codeSetDao = codeSetDao;
        this.functionalityTestedDao = functionalityTestedDao;
        this.quarterDao = quarterDao;
        this.attestationDao = attestationDao;
        this.rwtReportService = rwtReportService;
        this.certIdYearCalculator = certIdYearCalculator;
        this.rolloffDays = rolloffDays;
    }

    @Transactional
    public List<ImportantDate> getAll() {
        return Stream.of(getCriteriaDates(), getStandardDates(), getCodeSetDates(),
                getFunctionalityTestedDates(), Stream.of(getNextQuarterEndingDate()).toList(),
                getAttestationSubmissionDates(), getRwtResultsDates(),
                getCmsIdDates())
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    private List<ImportantDate> getCriteriaDates() {
        LocalDate today = LocalDate.now();
        List<CertificationCriterion> allCriteria = criteriaDao.findAll();
        List<ImportantDate> expiringCriteria = allCriteria.stream()
            .filter(criterion -> isDateImportant(criterion.getEndDay()))
            .map(criterion -> ImportantDate.builder()
                                .eventDescription(String.format(ImportantDateType.CRITERIA_EXPIRING.getUnformattedDisplay(), Util.formatCriteriaNumber(criterion)))
                                .date(criterion.getEndDay())
                                .build())
            .collect(Collectors.toList());

        List<ImportantDate> availableCriteria = allCriteria.stream()
            .filter(criterion -> isDateImportant(criterion.getStartDay()))
            .map(criterion -> ImportantDate.builder()
                                .eventDescription(String.format(ImportantDateType.CRITERIA_AVAILABLE.getUnformattedDisplay(), Util.formatCriteriaNumber(criterion)))
                                .date(criterion.getStartDay())
                                .build())
            .collect(Collectors.toList());
        return Stream.of(expiringCriteria, availableCriteria).flatMap(List::stream).toList();
    }

    private List<ImportantDate> getStandardDates() {
        List<Standard> allStandards = standardDao.findAll();

        List<ImportantDate> availableStandards = allStandards.stream()
                .filter(std -> isDateImportant(std.getStartDay()))
                .map(std -> ImportantDate.builder()
                                    .eventDescription(String.format(ImportantDateType.ATTRIBUTE_AVAILABLE.getUnformattedDisplay(), "Standard", std.getValue()))
                                    .date(std.getStartDay())
                                    .build())
                .collect(Collectors.toList());

        List<ImportantDate> expiringStandards = allStandards.stream()
            .filter(std -> isDateImportant(std.getEndDay()))
            .map(std -> ImportantDate.builder()
                                .eventDescription(String.format(ImportantDateType.ATTRIBUTE_EXPIRING.getUnformattedDisplay(), "Standard", std.getValue()))
                                .date(std.getEndDay())
                                .build())
            .collect(Collectors.toList());

        List<ImportantDate> requiredStandards = allStandards.stream()
            .filter(std -> isDateImportant(std.getRequiredDay()))
            .map(std -> ImportantDate.builder()
                                .eventDescription(String.format(ImportantDateType.ATTRIBUTE_REQUIRED.getUnformattedDisplay(), "Standard", std.getValue()))
                                .date(std.getRequiredDay())
                                .build())
            .collect(Collectors.toList());

        List<ImportantDate> extensionEndingStandards = allStandards.stream()
                .filter(std -> isDateImportant(std.getExtensionEndDay()))
                .map(std -> ImportantDate.builder()
                                    .eventDescription(String.format(ImportantDateType.ATTRIBUTE_EXTENSION_ENDS.getUnformattedDisplay(), "Standard", std.getValue()))
                                    .date(std.getExtensionEndDay())
                                    .build())
                .collect(Collectors.toList());
        return Stream.of(availableStandards, expiringStandards, requiredStandards, extensionEndingStandards).flatMap(List::stream).toList();
    }

    private List<ImportantDate> getCodeSetDates() {
        List<CodeSet> allCodeSets = codeSetDao.findAll();
        List<ImportantDate> availableCodeSets = allCodeSets.stream()
            .filter(codeSet -> isDateImportant(codeSet.getStartDay()))
            .map(codeSet -> ImportantDate.builder()
                                .eventDescription(String.format(ImportantDateType.ATTRIBUTE_AVAILABLE.getUnformattedDisplay(), "Code Set", codeSet.getName()))
                                .date(codeSet.getStartDay())
                                .build())
            .collect(Collectors.toList());

        List<ImportantDate> requiredCodeSets = allCodeSets.stream()
            .filter(codeSet -> isDateImportant(codeSet.getRequiredDay()))
            .map(codeSet -> ImportantDate.builder()
                                .eventDescription(String.format(ImportantDateType.ATTRIBUTE_REQUIRED.getUnformattedDisplay(), "Code Set", codeSet.getName()))
                                .date(codeSet.getRequiredDay())
                                .build())
            .collect(Collectors.toList());

        List<ImportantDate> extensionEndingCodeSets = allCodeSets.stream()
                .filter(codeSet -> isDateImportant(codeSet.getExtensionEndDay()))
                .map(codeSet -> ImportantDate.builder()
                                    .eventDescription(String.format(ImportantDateType.ATTRIBUTE_EXTENSION_ENDS.getUnformattedDisplay(), "Code Set", codeSet.getName()))
                                    .date(codeSet.getExtensionEndDay())
                                    .build())
                .collect(Collectors.toList());
        return Stream.of(availableCodeSets, requiredCodeSets, extensionEndingCodeSets).flatMap(List::stream).toList();
    }

    private List<ImportantDate> getFunctionalityTestedDates() {
        List<FunctionalityTested> allFunctionalityTested = functionalityTestedDao.findAll();

        List<ImportantDate> availableFunctionalityTested = allFunctionalityTested.stream()
                .filter(ft -> isDateImportant(ft.getStartDay()))
                .map(ft -> ImportantDate.builder()
                                    .eventDescription(String.format(ImportantDateType.ATTRIBUTE_AVAILABLE.getUnformattedDisplay(), "Functionality Tested", ft.getValue()))
                                    .date(ft.getStartDay())
                                    .build())
                .collect(Collectors.toList());

        List<ImportantDate> expiringFunctionalityTested = allFunctionalityTested.stream()
            .filter(ft -> isDateImportant(ft.getEndDay()))
            .map(ft -> ImportantDate.builder()
                                .eventDescription(String.format(ImportantDateType.ATTRIBUTE_EXPIRING.getUnformattedDisplay(), "Functionality Tested", ft.getValue()))
                                .date(ft.getEndDay())
                                .build())
            .collect(Collectors.toList());

        List<ImportantDate> requiredFunctionalityTested = allFunctionalityTested.stream()
            .filter(ft -> isDateImportant(ft.getRequiredDay()))
            .map(ft -> ImportantDate.builder()
                                .eventDescription(String.format(ImportantDateType.ATTRIBUTE_REQUIRED.getUnformattedDisplay(), "Functionality Tested", ft.getValue()))
                                .date(ft.getRequiredDay())
                                .build())
            .collect(Collectors.toList());

        List<ImportantDate> extensionEndingFunctionalityTested = allFunctionalityTested.stream()
                .filter(ft -> isDateImportant(ft.getExtensionEndDay()))
                .map(ft -> ImportantDate.builder()
                                    .eventDescription(String.format(ImportantDateType.ATTRIBUTE_EXTENSION_ENDS.getUnformattedDisplay(), "Functionality Tested", ft.getValue()))
                                    .date(ft.getExtensionEndDay())
                                    .build())
                .collect(Collectors.toList());
        return Stream.of(availableFunctionalityTested, expiringFunctionalityTested, requiredFunctionalityTested, extensionEndingFunctionalityTested).flatMap(List::stream).toList();
    }

    private ImportantDate getNextQuarterEndingDate() {
        //return only which quarter ends next
        List<Quarter> quarters = quarterDao.getAll();
        MonthDay today = MonthDay.from(LocalDate.now());
        Quarter currentQuarter = quarters.stream()
            .filter(quarter -> (quarter.getStart().equals(today) || quarter.getStart().isBefore(today))
                    && (quarter.getEnd().isAfter(today) || quarter.getEnd().equals(today)))
            .findAny() // there should only be 1 match
            .orElse(null);
        if (currentQuarter != null) {
            return ImportantDate.builder()
                    .eventDescription(String.format(ImportantDateType.QUARTER_END.getUnformattedDisplay(), currentQuarter.getName()))
                    .date(currentQuarter.getEnd().atYear(LocalDate.now().getYear()))
                    .build();
        }
        return null;
    }

    private List<ImportantDate> getAttestationSubmissionDates() {
        List<AttestationPeriod> attestationPeriods = attestationDao.getAllPeriods();

        List<ImportantDate> attestationSubmissionsOpening = attestationPeriods.stream()
                .filter(period -> isDateImportant(period.getSubmissionStart()))
                .map(period -> ImportantDate.builder()
                                    .eventDescription(ImportantDateType.ATTESTATION_SUBMISSIONS_OPEN.getUnformattedDisplay())
                                    .date(period.getSubmissionStart())
                                    .build())
                .collect(Collectors.toList());

        List<ImportantDate> attestationSubmissionsClosing = attestationPeriods.stream()
                .filter(period -> isDateImportant(period.getSubmissionEnd()))
                .map(period -> ImportantDate.builder()
                                    .eventDescription(ImportantDateType.ATTESTATION_SUBMISSIONS_CLOSE.getUnformattedDisplay())
                                    .date(period.getSubmissionEnd())
                                    .build())
                .collect(Collectors.toList());
        return Stream.of(attestationSubmissionsOpening, attestationSubmissionsClosing).flatMap(List::stream).toList();
    }

    private List<ImportantDate> getRwtResultsDates() {
        LocalDate today = LocalDate.now();

        ImportantDate rwtResultsStart = ImportantDate.builder()
                .eventDescription(ImportantDateType.RWT_RESULTS_SUBMISSION_BEGIN.getUnformattedDisplay())
                .date(rwtReportService.getResultsStartDate(today.getYear()))
                .build();
        ImportantDate rwtResultsEnd = ImportantDate.builder()
                .eventDescription(ImportantDateType.RWT_RESULTS_SUBMISSION_END.getUnformattedDisplay())
                .date(rwtReportService.getResultsLateDate(today.getYear()))
                .build();
        return Stream.of(rwtResultsStart, rwtResultsEnd)
                .filter(item -> isDateImportant(item.getDate()))
                .toList();
    }

    private List<ImportantDate> getCmsIdDates() {
        ImportantDate nextCmsIdYearStart = ImportantDate.builder()
                .eventDescription(String.format(ImportantDateType.CMS_ID_CREATION.getUnformattedDisplay(), certIdYearCalculator.getNextCertIdYear()))
                .date(certIdYearCalculator.getStartDateOfNextCmsIdYear())
                .build();
        ImportantDate nextCmsIdOverlapEnd = ImportantDate.builder()
                .eventDescription(String.format(ImportantDateType.CMS_ID_OVERLAP_ENDS.getUnformattedDisplay(), certIdYearCalculator.getCurrentCertIdYear()))
                .date(certIdYearCalculator.getEndDateOfThisCmsIdYearOverlap())
                .build();
        return Stream.of(nextCmsIdYearStart, nextCmsIdOverlapEnd)
                .filter(item -> isDateImportant(item.getDate()))
                .toList();
    }

    private boolean isDateImportant(LocalDate eventDay) {
        return eventDay != null
                && (isDateInPastButBeforeRolloff(eventDay)
                        || isDateTodayOrFuture(eventDay));
    }

    private boolean isDateInPastButBeforeRolloff(LocalDate eventDay) {
        LocalDate rolloffDay = eventDay.plusDays(rolloffDays);
        return isDateTodayOrFuture(rolloffDay);
    }

    private boolean isDateTodayOrFuture(LocalDate eventDay) {
        LocalDate today = LocalDate.now();
        return eventDay.isEqual(today) || eventDay.isAfter(today);
    }
}
