package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CertifiedProductSearchBasicDetails implements Serializable {

    private static final long serialVersionUID = 2903219171135034775L;

    /**
     * The internal ID of the certified product.
     */
    @Schema(description = "The internal ID of the certified product.")
    private Long id;

    /**
     * The unique CHPL ID of the certified product. This variable is applicable to 2014 and 2015 Edition. New uploads to
     * CHPL will use the format: CertEdYr.ATL.ACB.Dev.Prod.Ver.ICS.AddS.Date
     */
    @Schema(description = "The unique CHPL ID of the certified product. This variable is applicable to "
            + "2014 and 2015 Edition. New uploads to CHPL will use the format: "
            + "CertEdYr.ATL.ACB.Dev.Prod.Ver.ICS.AddS.Date")
    private String chplProductNumber;

    /**
     * A hyperlink to the test results used to certify the Complete EHRs and/or EHR Modules that can be accessed by the
     * public. This variable is applicable to 2014 Edition. Fully qualified URL which is reachable via web browser
     * validation and verification.
     */
    @Schema(description = "A hyperlink to the test results used to certify the Complete EHRs and/or EHR Modules "
            + "that can be accessed by the public. This variable is applicable to 2014 Edition. Fully qualified "
            + "URL which is reachable via web browser validation and verification.")
    private String reportFileLocation;

    /**
     * Hyperlink to FULL Usability Test Report meeting all the SED requirements. This variable is applicable for 2014
     * and 2015 Edition. Fully qualified URL which is reachable via web browser validation and verification.
     */
    @Schema(description = "Hyperlink to FULL Usability Test Report meeting all the SED requirements. This variable "
            + "is applicable for 2014 and 2015 Edition. Fully qualified URL which is reachable via web browser "
            + "validation and verification.")
    private String sedReportFileLocation;

    /**
     * For SED testing, a description of the intended users of the Health IT
     */
    private String sedIntendedUserDescription;

    /**
     * Date all SED testing was concluded for the Health IT. The format for the date is YYYMMDD
     */
    @Schema(description = "Date all SED testing was concluded for the Health IT. The format for the date is YYYMMDD")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate sedTestingEndDay;

    /**
     * The ID used by ONC-ACBs for internal tracking for 2014 and 2015 Certification Edition. It is a string variable
     * that does not have any restrictions on formatting or values.
     */
    @Schema(description = "The ID used by ONC-ACBs for internal tracking for 2014 and 2015 Certification Edition. It is "
            + "a string variable that does not have any restrictions on formatting or values.")
    private String acbCertificationId;

    /**
     * The classification of the certified product (either complete or modular). It is only applicable to 2014 Edition,
     * and takes values of either Complete EHR or Modular EHR.
     */
    @Schema(description = "The classification of the certified product (either complete or modular). It is only applicable "
            + "to 2014 Edition, and takes values of either Complete EHR or Modular EHR.")
    private Map<String, Object> classificationType = new HashMap<String, Object>();

    /**
     * If there was previously a different certifying body managing this listing this is their name.
     */
    @Schema(description = "If there was previously a different certifying body managing this listing this is their name.")
    private String otherAcb;

    /**
     * The developer or vendor of the certified health IT product listing.
     */
    @Schema(description = "The developer or vendor of the certified health IT product listing.")
    private Developer developer;

    /**
     * The product which this listing is under.
     */
    @Schema(description = "The product which this listing is under.")
    private Product product;

    /**
     * The version of the product being uploaded. This variable is applicable for 2014 and 2015 Edition.
     */
    @Schema(description = "The version of the product being uploaded. This variable is applicable for 2014 and 2015 Edition.")
    private ProductVersion version;

    /**
     * The certification edition. It takes a value of 2011, 2014 or 2015.
     */
    @Deprecated
    @DeprecatedResponseField(message = "Please use the 'edition' field.", removalDate = "2024-01-01")
    @Schema(description = "The certification edition. It takes a value of 2011, 2014 or 2015.")
    private Map<String, Object> certificationEdition = new HashMap<String, Object>();

    /**
     * The certification edition.
     */
    @Schema(description = "The certification edition.")
    private CertificationEdition edition;

    /**
     * For 2014 products, the practice setting for which the certified product is designed. It takes value of Ambulatory
     * or Inpatient.
     */
    @Schema(description = "For 2014 products, the practice setting for which the certified product is designed. It takes value "
            + "of Ambulatory or Inpatient.")
    private Map<String, Object> practiceType = new HashMap<String, Object>();

    /**
     * The ONC-ACB responsible for certifying the Health IT Module. This variable is applicable to 2014 and 2015
     * Edition, and allowable values are: Drummond Group, ICSA Labs, UL LLC.
     */
    @Schema(description = "The ONC-ACB responsible for certifying the Health IT Module. This variable is applicable to 2014 "
            + "and 2015 Edition, and allowable values are: Drummond Group, ICSA Labs, UL LLC.")
    private Map<String, Object> certifyingBody = new HashMap<String, Object>();

    /**
     * The ATL responsible for testing the Health IT Module. It is applicable for 2014 and 2015 Edition and takes values
     * of: Drummond Group, ICSA Labs, UL LLC, National Technical Systems, SLI Global, CCHIT
     */
    @Schema(description = "The ATL responsible for testing the Health IT Module. It is applicable for 2014 and 2015 Edition and "
            + "takes values of: Drummond Group, ICSA Labs, UL LLC, National Technical Systems, SLI Global, CCHIT")
    private List<CertifiedProductTestingLab> testingLabs = new ArrayList<CertifiedProductTestingLab>();

    /**
     * Certification date represented in milliseconds since epoch
     */
    @Schema(description = "Certification date represented in milliseconds since epoch")
    private Long certificationDate;

    /**
     * Decertification date represented in milliseconds since epoch
     */
    @Deprecated
    @DeprecatedResponseField(message = "Please use the 'decertificationDay' field.", removalDate = "2024-01-01")
    @Schema(description = "Decertification date represented in milliseconds since epoch")
    private Long decertificationDate;

    /**
     * Decertification day
     */
    @Schema(description = "Decertification date represented in milliseconds since epoch")
    private LocalDate decertificationDay;

    /**
     * Number of certification criteria this listing attests to.
     */
    @Schema(description = "Number of certification criteria this listing attests to.")
    private Integer countCerts;

    /**
     * Number of cqms this listing attests to.
     */
    @Schema(description = "Number of cqms this listing attests to.")
    private Integer countCqms;

    /**
     * Total count of open+closed surveillance for this listing.
     */
    @Schema(description = "Total count of open+closed surveillance for this listing.")
    private Integer countSurveillance;

    /**
     * Total count of open surveillance for this listing.
     */
    @Schema(description = "Total count of open surveillance for this listing.")
    private Integer countOpenSurveillance;

    /**
     * Total count of closed surveillance for this listing.
     */
    @Schema(description = "Total count of closed surveillance for this listing.")
    private Integer countClosedSurveillance;

    /**
     * The total number of open (unresolved) non-conformities found for the corresponding listing. For additional
     * information, please see 'Understanding Surveillance Information in the CHPL', available in the CHPL Public User
     * Guide
     */
    @Schema(description = "The total number of open (unresolved) non-conformities found for the corresponding listing. "
            + "For additional information, please see 'Understanding Surveillance Information in the CHPL', available in "
            + "the CHPL Public User Guide")
    private Integer countOpenNonconformities;

    /**
     * Total count of closed nonconformities for this listing.
     */
    @Schema(description = "Total count of closed nonconformities for this listing.")
    private Integer countClosedNonconformities;

    /**
     * This variable indicates whether or not the certification issued was a result of an inherited certified status
     * request. This variable is applicable for 2014 and 2015 Edition and contains the inherited status as well as
     * first-level parents and children.
     */
    @Schema(description = "This variable indicates whether or not the certification issued was a result of an inherited "
            + "certified status request. This variable is applicable for 2014 and 2015 Edition and contains the inherited "
            + "status as well as first-level parents and children.")
    private InheritedCertificationStatus ics;

    /**
     * This variable identifies if Health IT Module was certified to the accessibility-centered design certification
     * criterion for 2015 Edition. It is a binary variable that takes value of true or false.
     */
    @Schema(description = "This variable identifies if Health IT Module was certified to the accessibility-centered design "
            + "certification criterion for 2015 Edition. It is a binary variable that takes value of true or false.")
    private Boolean accessibilityCertified;

    /**
     * For legacy CHPL listings, any additional software needed.
     */
    @Schema(description = "For legacy CHPL listings, any additional software needed.")
    private String productAdditionalSoftware;

    /**
     * A hyperlink to the mandatory disclosures required by 170.523(k)(1) for the Health IT Module
     */
    @Schema(description = "A hyperlink to the mandatory disclosures required by 170.523(k)(1) for the Health IT Module")
    private String mandatoryDisclosures;

    /**
     * The last time this listing was modified in any way given in milliseconds since epoch.
     */
    @Schema(description = "The last time this listing was modified in any way given in milliseconds since epoch.")
    @Deprecated
    @DeprecatedResponseField(message = "This field has been deprecated and will be removed.", removalDate = "2023-10-31")
    private Long lastModifiedDate;

    /**
     * Any surveillance that has occurred on this listing
     */
    @Schema(description = "Any surveillance that has occurred on this listing")
    private List<Surveillance> surveillance = new ArrayList<Surveillance>();

    /**
     * This variable indicates that if there is the standard(s) or lack thereof used to meet the accessibility-centered
     * design certification criterion for 2015 Certification Edtion. It is a string variable that does not have any
     * restrictions on formatting or values.
     */
    @Schema(description = "This variable indicates that if there is the standard(s) or lack thereof used to meet the "
            + "accessibility-centered design certification criterion for 2015 Certification Edtion. It is a string variable "
            + "that does not have any restrictions on formatting or values.")
    private List<CertifiedProductAccessibilityStandard> accessibilityStandards = new ArrayList<CertifiedProductAccessibilityStandard>();

    /**
     * Description of the health IT module(s) intended users for the tested capabilities/related criteria. This variable
     * is applicable only for 2015 Edition, and a string variable that does not take any restrictions on formatting or
     * values.
     */
    @Schema(description = "Description of the health IT module(s) intended users for the tested capabilities/related criteria. This "
            + "variable is applicable only for 2015 Edition, and a string variable that does not take any restrictions on formatting or"
            + " values.")
    private List<CertifiedProductTargetedUser> targetedUsers = new ArrayList<CertifiedProductTargetedUser>();

    /**
     * The standard or mapping used to meet the quality management system certification criterion. This variable is
     * applicable for 2014 and 2015 Edition, and a string variable that does not take any restrictions on formatting or
     * values.
     */
    @Schema(description = "The standard or mapping used to meet the quality management system certification criterion. This variable "
            + "is applicable for 2014 and 2015 Edition, and a string variable that does not take any restrictions on formatting or "
            + "values.")
    private List<CertifiedProductQmsStandard> qmsStandards = new ArrayList<CertifiedProductQmsStandard>();

    /**
     * This property exists solely to be able to deserialize listing activity events from very old data. Since we care
     * about certification status changes when categorizing listing activity we need to be able to read this value in
     * old listing activity event data. Not all old listing properties need to be present for this reason. This property
     * should not be visible in the generated XSD or any response from an API call.
     */
    @Schema(description = "This property exists solely to be able to deserialize listing activity events from very old data. Since we "
            + "care about certification status changes when categorizing listing activity we need to be able to read this value in "
            + "old listing activity event data. Not all old listing properties need to be present for this reason. This property "
            + "should not be visible in the generated XSD or any response from an API call.")
    @JsonProperty(access = Access.WRITE_ONLY)
    private LegacyCertificationStatus certificationStatus;

    /**
     * All current and historical certification status of this listing. The certification statuses take values of
     * Active; Suspended by ONC; Suspended by ONC-ACB; Withdrawn by Developer; Withdrawn by Developer Under
     * Surveillance/Review; Withdrawn by ONC-ACB; Terminated by ONC; Retired. For a detailed description of each
     * certification status, please see 'Understanding Certification Status in the CHPL', available in the CHPL Public
     * User Guide.
     */
    @Schema(description = "All current and historical certification status of this listing. The certification statuses take values of"
            + "Active; Suspended by ONC; Suspended by ONC-ACB; Withdrawn by Developer; Withdrawn by Developer Under Surveillance/Review; "
            + "Withdrawn by ONC-ACB; Terminated by ONC; Retired. For a detailed description of each certification status, please see "
            + "'Understanding Certification Status in the CHPL', available in the CHPL Public User Guide.")
    private List<CertificationStatusEvent> certificationEvents = new ArrayList<CertificationStatusEvent>();

    /**
     * Whether or not the listing meets the definition of "Cures Update".
     */
    @Schema(description = "Whether or not the listing meets the definition of \"Cures Update\".")
    private Boolean curesUpdate;

    /**
     * All current and historical values of promoting interoperability for this listing along with the dates each
     * user count was valid.
     */
    @Schema(description = "All current and historical values of promoting interoperability for this listing along with the dates each user "
            + "count was valid.")
    private List<PromotingInteroperabilityUser> promotingInteroperabilityUserHistory = new ArrayList<PromotingInteroperabilityUser>();

    /**
     * All data related to safety-enhanced design for this listing.
     */
    @Schema(description = "All data related to safety-enhanced design for this listing.")
    private CertifiedProductSed sed;

    /**
     * A hyperlink to SVAP Notice URL.
     */
    @Schema(description = "A hyperlink to SVAP Notice URL.")
    private String svapNoticeUrl;

    /**
     * Direct reviews that were conducted against this listing or its developer.
     */
    @Schema(description = "Direct reviews that were conducted against this listing or its developer.")
    private List<DirectReview> directReviews = new ArrayList<DirectReview>();

    /**
     * Indicates whether the direct reviews were available when the call  was made
     */
    @Schema(description = "Indicates whether the direct reviews were available when the call  was made")
    private boolean directReviewsAvailable;

    /**
     * URL where the listing's Real World Testing Plan is located
     */
    @Schema(description = "URL where the listing's Real World Testing Plan is located")
    private String rwtPlansUrl;

    /**
     * Date the listing's Real World Testing Plan was submitted
     */
    @Schema(description = "Date the listing's Real World Testing Plan was submitted")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate rwtPlansCheckDate;

    /**
     * URL where the listing's Real World Testing Results is located
     */
    @Schema(description = "URL where the listing's Real World Testing Results is located")
    private String rwtResultsUrl;

    /**
     * Date the listing's Real World Testing Results was submitted
     */
    @Schema(description = "Date the listing's Real World Testing Results was submitted")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate rwtResultsCheckDate;

    public CertifiedProductSearchBasicDetails() {
        sed = new CertifiedProductSed();
    }

    public LocalDate getSedTestingEndDay() {
        return sedTestingEndDay;
    }

    public void setSedTestingEndDay(LocalDate sedTestingEndDay) {
        this.sedTestingEndDay = sedTestingEndDay;
    }

    /**
     * Retrieve current status.
     *
     * @return current status
     */
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

    /**
     * Retrieve oldest status.
     *
     * @return the first status of the Listing
     */
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

    /**
     * Retrieve certification status on a specific date.
     *
     * @return certification status
     */
    public CertificationStatusEvent getStatusOnDate(Date date) {
        if (this.getCertificationEvents() == null || this.getCertificationEvents().size() == 0) {
            return null;
        }

        // first we need to make sure the status events are in ascending order
        this.getCertificationEvents().sort(new Comparator<CertificationStatusEvent>() {
            @Override
            public int compare(CertificationStatusEvent o1, CertificationStatusEvent o2) {
                if (o1.getEventDate() == null || o2.getEventDate() == null
                        || o1.getEventDate().equals(o2.getEventDate())) {
                    return 0;
                }
                if (o1.getEventDate() < o2.getEventDate()) {
                    return -1;
                }
                if (o1.getEventDate() > o2.getEventDate()) {
                    return 1;
                }
                return 0;
            }
        });

        CertificationStatusEvent result = null;
        for (int i = 0; i < this.getCertificationEvents().size() && result == null; i++) {
            CertificationStatusEvent currEvent = this.getCertificationEvents().get(i);
            if (i < this.getCertificationEvents().size() - 1) {
                CertificationStatusEvent nextEvent = this.getCertificationEvents().get(i + 1);
                // if the passed-in date is between currEvent and nextEvent then the currEvent
                // gives the status on the passed-in date.
                if (currEvent.getEventDate() != null && currEvent.getEventDate().longValue() <= date
                        .getTime()
                        && nextEvent.getEventDate() != null && nextEvent.getEventDate().longValue() > date
                                .getTime()) {
                    result = currEvent;
                }
            } else {
                result = currEvent;
            }
        }
        return result;
    }

    public PromotingInteroperabilityUser getCurrentPromotingInteroperabilityUsers() {
        if (this.getPromotingInteroperabilityUserHistory() == null
                || this.getPromotingInteroperabilityUserHistory().size() == 0) {
            return null;
        }

        PromotingInteroperabilityUser newest = this.getPromotingInteroperabilityUserHistory().get(0);
        for (PromotingInteroperabilityUser piItem : this.getPromotingInteroperabilityUserHistory()) {
            if (piItem.getUserCountDate().isAfter(newest.getUserCountDate())) {
                newest = piItem;
            }
        }
        return newest;
    }

    public LegacyCertificationStatus getCertificationStatus() {
        return certificationStatus;
    }

    public void setCertificationStatus(LegacyCertificationStatus certificationStatus) {
        this.certificationStatus = certificationStatus;
    }
}
