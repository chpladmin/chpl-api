package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.eclipse.collections.impl.factory.SortedSets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.activity.ActivityExclude;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.domain.comparator.CertificationStatusEventComparator;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.util.CertificationStatusUtil;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import gov.healthit.chpl.util.NullSafeEvaluator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertifiedProductSearchDetails implements Serializable {

    private static final long serialVersionUID = 2903219171127034775L;
    public static final String ACB_ID_KEY = "id";
    public static final String ACB_NAME_KEY = "name";
    public static final String ACB_CODE_KEY = "code";
    public static final String EDITION_ID_KEY = "id";
    public static final String EDITION_NAME_KEY = "name";

    @Schema(description = "The internal ID of the certified product.")
    private Long id;

    @Schema(description = "The unique CHPL ID of the certified product. New uploads to CHPL will use the format: "
            + "CertEdYr.ATL.ACB.Dev.Prod.Ver.ICS.AddS.Date")
    private String chplProductNumber;

    @Schema(description = "A hyperlink to the test results used to certify the Complete EHRs and/or EHR Modules that can be accessed by the public. "
            + "This variable is applicable to 2014 Edition. Fully qualified URL which is reachable via web browser validation and verification.")
    private String reportFileLocation;

    @Schema(description = "Hyperlink to FULL Usability Test Report meeting all the SED requirements. "
            + "Fully qualified URL which is reachable via web browser validation and verification.")
    private String sedReportFileLocation;

    @Schema(description = "For SED testing, a description of the intended users of the Health IT")
    private String sedIntendedUserDescription;

    @Schema(description = "Date all SED testing was concluded for the Health IT. The format for the date is YYYMMDD")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate sedTestingEndDay;

    @JsonIgnore
    private String sedTestingEndDateStr;

    @Schema(description = "The ID used by ONC-ACBs for internal tracking. "
            + "It is a string variable that does not have any restrictions on formatting or values.")
    private String acbCertificationId;

    @Schema(description = "The classification of the certified product (either complete or modular). It is only applicable to 2014 Edition, and takes values "
            + "of either Complete EHR or Modular EHR.")
    private Map<String, Object> classificationType = new HashMap<String, Object>();

    @Schema(description = "If there was previously a different certifying body managing this listing this is their name.")
    private String otherAcb;

    @Schema(description = "The developer or vendor of the certified health IT product listing.")
    private Developer developer;

    @Schema(description = "The product which this listing is under.")
    private Product product;

    @Schema(description = "The version of the product being uploaded.")
    private ProductVersion version;

    @Deprecated
    @DeprecatedResponseField(message = "Please use the 'edition' field.", removalDate = "2024-01-01")
    @Builder.Default
    @Schema(description = "The certification edition. It takes a value of 2011, 2014, 2015, or null.")
    private Map<String, Object> certificationEdition = new HashMap<String, Object>();

    @Schema(description = "The certification edition. It takes a value of 2011, 2014, 2015, or null.")
    private CertificationEdition edition;

    @Schema(description = "For 2014 products, the practice setting for which the certified product is designed. It takes value of Ambulatory or Inpatient.")
    private Map<String, Object> practiceType = new HashMap<String, Object>();

    @Schema(description = "The ONC-ACB responsible for certifying the Health IT Module.")
    private Map<String, Object> certifyingBody = new HashMap<String, Object>();

    @Schema(description = "The ATL responsible for testing the Health IT Module.")
    @Builder.Default
    private List<CertifiedProductTestingLab> testingLabs = new ArrayList<CertifiedProductTestingLab>();

    private Long certificationDate;

    @JsonIgnore
    private String certificationDateStr;

    @Deprecated
    @DeprecatedResponseField(message = "Please use the 'decertificationDay' field.", removalDate = "2024-01-01")
    @Schema(description = "Decertification date represented in milliseconds since epoch")
    private Long decertificationDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @Schema(description = "Decertification day")
    private LocalDate decertificationDay;

    @Schema(description = "Number of certification criteria this listing attests to.")
    private Integer countCerts;

    @Schema(description = "Number of cqms this listing attests to.")
    private Integer countCqms;

    @Schema(description = "Total count of open+closed surveillance for this listing.")
    private Integer countSurveillance;

    @Schema(description = "Total count of open surveillance for this listing.")
    private Integer countOpenSurveillance;

    @Schema(description = "Total count of closed surveillance for this listing.")
    private Integer countClosedSurveillance;

    @Schema(description = "The total number of open (unresolved) non-conformities found for the corresponding listing. For additional information, "
            + "please see 'Understanding Surveillance Information in the CHPL', available in the CHPL Public User Guide")
    private Integer countOpenNonconformities;

    @Schema(description = "Total count of closed nonconformities for this listing.")
    private Integer countClosedNonconformities;

    @Schema(description = "This variable indicates whether or not the certification issued was a result of an inherited certified status request. "
            + "This variable contains the inherited status as well as first-level parents and children.")
    private InheritedCertificationStatus ics;

    @Schema(description = "This variable identifies if Health IT Module was certified to the accessibility-centered "
            + "design certification criterion. It is a binary variable that takes value of true or false.")
    private Boolean accessibilityCertified;

    @JsonIgnore
    private String accessibilityCertifiedStr;

    @Schema(description = "For legacy CHPL listings, any additional software needed.")
    private String productAdditionalSoftware;

    @Schema(description = "A hyperlink to the mandatory disclosures required by 170.523(k)(1) for the Health IT Module")
    private String mandatoryDisclosures;

    @Schema(description = "Any surveillance that has occurred on this listing")
    private List<Surveillance> surveillance = new ArrayList<Surveillance>();

    @Schema(description = "Direct reviews that were conducted against this listing or its developer.")
    private List<DirectReview> directReviews = new ArrayList<DirectReview>();

    private boolean directReviewsAvailable;

    @Schema(description = "A record of CHPL Product Numbers which have been used at some time in the past to reference the listing.")
    private List<CertifiedProductChplProductNumberHistory> chplProductNumberHistory = new ArrayList<CertifiedProductChplProductNumberHistory>();

    @Schema(description = "The standard(s) or lack thereof used to meet the accessibility-centered design certification criterion. "
            + "Please see the 2015 Edition Certification Companion Guide for Accessibility Centered Design for "
            + "example accessibility standards: "
            + "https://www.healthit.gov/test-method/accessibility-centered-design#ccg")
    @Builder.Default
    private List<CertifiedProductAccessibilityStandard> accessibilityStandards = new ArrayList<CertifiedProductAccessibilityStandard>();

    @Schema(description = "Description of the health IT module(s) intended users for the tested capabilities/related criteria. "
            + "This  is a string variable that does not take any restrictions on formatting or values.")
    @Builder.Default
    private List<CertifiedProductTargetedUser> targetedUsers = new ArrayList<CertifiedProductTargetedUser>();

    @Schema(description = "The standard or mapping used to meet the quality management system certification criterion. "
            + "This variable is a string variable that does not take any restrictions on formatting or values.")
    @Builder.Default
    private List<CertifiedProductQmsStandard> qmsStandards = new ArrayList<CertifiedProductQmsStandard>();

    @Schema(description = "This variable indicates if the listing utilizes automated numerator or automated measure required tests.")
    @Builder.Default
    private List<ListingMeasure> measures = new ArrayList<ListingMeasure>();

    @Schema(description = "The criteria to which this listing attests")
    @Singular
    private List<CertificationResult> certificationResults = new ArrayList<CertificationResult>();

    @Schema(description = "The clinical quality measures to which this listing has been certified.")
    @Builder.Default
    private List<CQMResultDetails> cqmResults = new ArrayList<CQMResultDetails>();

    /**
     * This property exists solely to be able to deserialize listing activity events from very old data.
     * Since we care about certification status changes when categorizing listing activity we need to be able to read
     * this value in old listing activity event data. Not all old listing properties need to be present
     * for this reason. This property should not be visible in any response from an API call.
     */
    @JsonProperty(access = Access.WRITE_ONLY)
    private LegacyCertificationStatus certificationStatus;

    @Schema(description = "All current and historical certification status of this listing. The certification statuses take values of Active; Suspended by "
            + "ONC; Suspended by ONC-ACB; Withdrawn by Developer; Withdrawn by Developer Under Surveillance/Review; Withdrawn by ONC-ACB; Terminated by ONC; "
            + "Retired. For a detailed description of each certification status, please see 'Understanding Certification Status in the CHPL', available in "
            + "the CHPL Public User Guide.")
    @Builder.Default
    private List<CertificationStatusEvent> certificationEvents = new ArrayList<CertificationStatusEvent>();

    @Schema(description = "This variable identifies if the listing meets the definition of \"Cures Update\". It is a binary variable that takes value of "
            + "true or false.")
    private Boolean curesUpdate;

    @Schema(description = "All current and historical values of promoting interoperability for this listing along with the dates each user count was valid.")
    @Builder.Default
    private List<PromotingInteroperabilityUser> promotingInteroperabilityUserHistory = new ArrayList<PromotingInteroperabilityUser>();

    @Schema(description = "All data related to safety-enhanced design for this listing.")
    private CertifiedProductSed sed = new CertifiedProductSed();

    @Schema(description = "URL where the listings Real World Testing Plan is located")
    private String rwtPlansUrl;

    @Schema(description = "Date the listings Real World Testing Plan was submitted")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate rwtPlansCheckDate;

    @Schema(description = "URL where the listings Real World Testing Results is located")
    private String rwtResultsUrl;

    @Schema(description = "Date the listings Real World Testing Results was submitted")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate rwtResultsCheckDate;

    @Schema(description = "URL where the Listings SVAP Notice URL is located")
    private String svapNoticeUrl;

    @Builder.Default
    @ActivityExclude
    private Set<String> warningMessages = new LinkedHashSet<String>();

    @Builder.Default
    @ActivityExclude
    private Set<String> errorMessages = new LinkedHashSet<String>();

    @Builder.Default
    @ActivityExclude
    private Set<String> dataErrorMessages = new HashSet<String>();

    @Builder.Default
    @ActivityExclude
    private Set<String> businessErrorMessages = new HashSet<String>();

    public ImmutableSortedSet<String> getWarningMessages() {
        return SortedSets.immutable.withAll(warningMessages);
    }

    public ImmutableSortedSet<String> getErrorMessages() {
        // this forces use of addBusinessError(...) & addDataError(...)
        return SortedSets.immutable.withAll(errorMessages);
    }

    public ImmutableSortedSet<String> getDataErrorMessages() {
        // this forces use of addDataError(...)
        return SortedSets.immutable.withAll(dataErrorMessages);
    }

    public ImmutableSortedSet<String> getBusinessErrorMessages() {
        // this forces use of addBusinessError(...)
        return SortedSets.immutable.withAll(businessErrorMessages);
    }

    public void addDataErrorMessage(String errorMessage) {
        errorMessages.add(errorMessage);
        dataErrorMessages.add(errorMessage);
    }

    public void addBusinessErrorMessage(String errorMessage) {
        errorMessages.add(errorMessage);
        businessErrorMessages.add(errorMessage);
    }

    public void addWarningMessage(String warningMessage) {
        warningMessages.add(warningMessage);
    }

    public void addAllDataErrorMessages(Set<String> dataMessages) {
        this.errorMessages.addAll(dataMessages);
        dataErrorMessages.addAll(dataMessages);
    }

    public void addAllBusinessErrorMessages(Set<String> businessMessages) {
        this.errorMessages.addAll(businessMessages);
        businessErrorMessages.addAll(businessMessages);
    }

    public void addAllWarningMessages(Set<String> warningMessages) {
        this.warningMessages.addAll(warningMessages);
    }

    public void clearAllErrorMessages() {
        errorMessages.clear();
        businessErrorMessages.clear();
        dataErrorMessages.clear();
    }

    public void clearAllWarningMessages() {
        warningMessages.clear();
    }

    public CertificationStatusEvent getCurrentStatus() {
        if (this.getCertificationEvents() == null || this.getCertificationEvents().size() == 0) {
            return null;
        }

        CertificationStatusEvent newest = this.getCertificationEvents().get(0);
        for (CertificationStatusEvent event : this.getCertificationEvents()) {
            if (event.getEventDate() > newest.getEventDate()) {
                newest = event;
            }
        }
        return newest;
    }

    public CertificationStatusEvent getOldestStatus() {
        if (this.getCertificationEvents() == null || this.getCertificationEvents().size() == 0) {
            return null;
        }

        CertificationStatusEvent oldest = this.getCertificationEvents().get(0);
        for (CertificationStatusEvent event : this.getCertificationEvents()) {
            if (event.getEventDate() < oldest.getEventDate()) {
                oldest = event;
            }
        }
        return oldest;
    }

    @Schema(description = "Certification date represented in milliseconds since epoch")
    public Long getCertificationDate() {
        if (CollectionUtils.isEmpty(this.getCertificationEvents())
                || anyCertificationEventIsMissingNameField(this.getCertificationEvents())) {
            return this.certificationDate;
        }

        this.getCertificationEvents().sort(new CertificationStatusEventComparator());
        CertificationStatusEvent result = null;
        for (int i = 0; i < this.getCertificationEvents().size() && result == null; i++) {
            CertificationStatusEvent currEvent = this.getCertificationEvents().get(i);
            if (currEvent.getStatus().getName().equals(CertificationStatusType.Active.getName())) {
                result = currEvent;
            }
        }

        if (result == null) {
            return null;
        }
        return result.getEventDate();
    }

    @Schema(description = "Certification day")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    public LocalDate getCertificationDay() {
        Long certificationDateMillis = getCertificationDate();
        if (certificationDateMillis == null) {
            return null;
        }

        return DateUtil.toLocalDate(certificationDateMillis);
    }

    private boolean anyCertificationEventIsMissingNameField(List<CertificationStatusEvent> certificationStatusEvents) {
        return certificationStatusEvents.stream()
                .filter(event -> event.getStatus() == null
                        || StringUtils.isEmpty(event.getStatus().getName()))
                .findAny().isPresent();
    }

    public CertificationStatusEvent getStatusOnDate(Date date) {
        if (this.getCertificationEvents() == null || this.getCertificationEvents().size() == 0) {
            return null;
        }

        // first we need to make sure the status events are in ascending order
        this.getCertificationEvents().sort(new CertificationStatusEventComparator());

        CertificationStatusEvent result = null;
        for (int i = 0; i < this.getCertificationEvents().size() && result == null; i++) {
            CertificationStatusEvent currEvent = this.getCertificationEvents().get(i);
            if (i < this.getCertificationEvents().size() - 1) {
                CertificationStatusEvent nextEvent = this.getCertificationEvents().get(i + 1);
                // if the passed-in date is between currEvent and nextEvent then the currEvent
                // gives the status on the passed-in date.
                if (currEvent.getEventDate() != null && currEvent.getEventDate().longValue() <= date.getTime()
                        && nextEvent.getEventDate() != null && nextEvent.getEventDate().longValue() > date.getTime()) {
                    result = currEvent;
                }
            } else {
                result = currEvent;
            }
        }
        return result;
    }

    @JsonIgnore
    public boolean isCertificateActive() {
        String currentStatusName = NullSafeEvaluator.eval(() -> getCurrentStatus().getStatus().getName(), "");
        return CertificationStatusUtil.getActiveStatusNames().contains(currentStatusName);
    }
}
