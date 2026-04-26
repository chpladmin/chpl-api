package gov.healthit.chpl.report.importantdates;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public ImportantDateReportService(CertificationCriterionDAO criteriaDao, StandardDAO standardDao,
            CodeSetDAO codeSetDao, FunctionalityTestedDAO functionalityTestedDao,
            QuarterDAO quarterDao, AttestationDAO attestationDao,
            RealWorldTestingReportService rwtReportService,
            CertificationIdYearCalculator certIdYearCalculator) {
        this.criteriaDao = criteriaDao;
        this.standardDao = standardDao;
        this.codeSetDao = codeSetDao;
        this.functionalityTestedDao = functionalityTestedDao;
        this.quarterDao = quarterDao;
        this.attestationDao = attestationDao;
        this.rwtReportService = rwtReportService;
        this.certIdYearCalculator = certIdYearCalculator;
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
        List<CertificationCriterion> allCriteria = criteriaDao.findAll();
        List<ImportantDate> expiringCriteria = allCriteria.stream()
            .map(criterion -> ImportantDate.builder()
                                .eventDescription(String.format(ImportantDateType.CRITERIA_EXPIRING.getUnformattedDisplay(), Util.formatCriteriaNumber(criterion)))
                                .date(criterion.getEndDay())
                                .build())
            .collect(Collectors.toList());

        List<ImportantDate> availableCriteria = allCriteria.stream()
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
                .map(std -> ImportantDate.builder()
                                    .eventDescription(String.format(ImportantDateType.ATTRIBUTE_AVAILABLE.getUnformattedDisplay(), "Standard", std.getValue()))
                                    .date(std.getStartDay())
                                    .build())
                .collect(Collectors.toList());

        List<ImportantDate> expiringStandards = allStandards.stream()
            .map(std -> ImportantDate.builder()
                                .eventDescription(String.format(ImportantDateType.ATTRIBUTE_EXPIRING.getUnformattedDisplay(), "Standard", std.getValue()))
                                .date(std.getEndDay())
                                .build())
            .collect(Collectors.toList());

        List<ImportantDate> requiredStandards = allStandards.stream()
            .map(std -> ImportantDate.builder()
                                .eventDescription(String.format(ImportantDateType.ATTRIBUTE_REQUIRED.getUnformattedDisplay(), "Standard", std.getValue()))
                                .date(std.getRequiredDay())
                                .build())
            .collect(Collectors.toList());

        List<ImportantDate> extensionEndingStandards = allStandards.stream()
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
            .map(codeSet -> ImportantDate.builder()
                                .eventDescription(String.format(ImportantDateType.ATTRIBUTE_AVAILABLE.getUnformattedDisplay(), "Code Set", codeSet.getName()))
                                .date(codeSet.getStartDay())
                                .build())
            .collect(Collectors.toList());

        List<ImportantDate> requiredCodeSets = allCodeSets.stream()
            .map(codeSet -> ImportantDate.builder()
                                .eventDescription(String.format(ImportantDateType.ATTRIBUTE_REQUIRED.getUnformattedDisplay(), "Code Set", codeSet.getName()))
                                .date(codeSet.getRequiredDay())
                                .build())
            .collect(Collectors.toList());

        List<ImportantDate> extensionEndingCodeSets = allCodeSets.stream()
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
                .map(ft -> ImportantDate.builder()
                                    .eventDescription(String.format(ImportantDateType.ATTRIBUTE_AVAILABLE.getUnformattedDisplay(), "Functionality Tested", ft.getValue()))
                                    .date(ft.getStartDay())
                                    .build())
                .collect(Collectors.toList());

        List<ImportantDate> expiringFunctionalityTested = allFunctionalityTested.stream()
            .map(ft -> ImportantDate.builder()
                                .eventDescription(String.format(ImportantDateType.ATTRIBUTE_EXPIRING.getUnformattedDisplay(), "Functionality Tested", ft.getValue()))
                                .date(ft.getEndDay())
                                .build())
            .collect(Collectors.toList());

        List<ImportantDate> requiredFunctionalityTested = allFunctionalityTested.stream()
            .map(ft -> ImportantDate.builder()
                                .eventDescription(String.format(ImportantDateType.ATTRIBUTE_REQUIRED.getUnformattedDisplay(), "Functionality Tested", ft.getValue()))
                                .date(ft.getRequiredDay())
                                .build())
            .collect(Collectors.toList());

        List<ImportantDate> extensionEndingFunctionalityTested = allFunctionalityTested.stream()
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
                .map(period -> ImportantDate.builder()
                                    .eventDescription(ImportantDateType.ATTESTATION_SUBMISSIONS_OPEN.getUnformattedDisplay())
                                    .date(period.getSubmissionStart())
                                    .build())
                .collect(Collectors.toList());

        List<ImportantDate> attestationSubmissionsClosing = attestationPeriods.stream()
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
                .toList();
    }
}
