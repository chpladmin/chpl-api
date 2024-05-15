package gov.healthit.chpl.entity.developer;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "developer_search")
public class DeveloperSearchResultEntity implements Serializable {
    private static final long serialVersionUID = -1396979009490864864L;
    private static final String ACB_SEPARATOR_CHAR = "\\|";
    private static final String ID_NAME_SEPARATOR_CHAR = ":";

    @Id
    @Column(name = "developer_id")
    private Long id;

    @Column(name = "developer_code")
    private String developerCode;

    @Column(name = "developer_name")
    private String developerName;

    @Column(name = "developer_website")
    private String website;

    @Column(name = "self_developer")
    private Boolean selfDeveloper;

    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "street_line_1")
    private String addressStreetLine1;

    @Column(name = "street_line_2")
    private String addressStreetLine2;

    @Column(name = "city")
    private String addressCity;

    @Column(name = "state")
    private String addressState;

    @Column(name = "zipcode")
    private String addressZipcode;

    @Column(name = "contact_id")
    private Long contactId;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone_number")
    private String contactPhoneNumber;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "current_status_id")
    private Long currentStatusId;

    @Column(name = "current_status_name")
    private String currentStatusName;

    @Column(name = "last_developer_status_change")
    private Date lastStatusChangeDate;

    @Column(name = "current_active_listing_count")
    private Integer currentActiveListingCount;

    @Column(name = "most_recent_past_attestation_period_active_listing_count")
    private Integer mostRecentPastAttestationPeriodActiveListingCount;

    @Column(name = "published_attestation_submission_id")
    private Long mostRecentPastAttestationPeriodPublishedSubmissionId;

    @Column(name = "attestation_submission_change_request_id")
    private Long mostRecentPastAttestationPeriodChangeRequestSubmissionId;

    @Column(name = "acbs_for_developer_active_listings")
    private String acbsForDeveloperActiveListings;

    @Column(name = "acbs_for_developer_all_listings")
    private String acbsForDeveloperAllListings;

    @Column(name = "creation_date")
    private Date creationDate;

    @Column(name = "deleted")
    private Boolean deleted;

    public DeveloperSearchResult toDomain() {
        return DeveloperSearchResult.builder()
                .id(this.getId())
                .code(this.getDeveloperCode())
                .name(this.getDeveloperName())
                .website(this.getWebsite())
                .selfDeveloper(this.getSelfDeveloper())
                .address(Address.builder()
                        .addressId(this.getAddressId())
                        .line1(this.getAddressStreetLine1())
                        .line2(this.getAddressStreetLine2())
                        .city(this.getAddressCity())
                        .state(this.getAddressState())
                        .zipcode(this.getAddressZipcode())
                        .build())
                .contact(PointOfContact.builder()
                        .contactId(this.getContactId())
                        .fullName(this.getContactName())
                        .email(this.getContactEmail())
                        .phoneNumber(this.getContactPhoneNumber())
                        .build())
                .status(IdNamePair.builder()
                        .id(this.getCurrentStatusId())
                        .name(this.getCurrentStatusName())
                        .build())
                .mostRecentStatusEvent(this.getLastStatusChangeDate())
                .decertificationDate(calculateDecertificationDate(this.getCurrentStatusName(), this.getLastStatusChangeDate()))
                .currentActiveListingCount(this.getCurrentActiveListingCount())
                .mostRecentPastAttestationPeriodActiveListingCount(this.getMostRecentPastAttestationPeriodActiveListingCount())
                .submittedAttestationsForMostRecentPastPeriod(this.getMostRecentPastAttestationPeriodChangeRequestSubmissionId() != null)
                .publishedAttestationsForMostRecentPastPeriod(this.getMostRecentPastAttestationPeriodPublishedSubmissionId() != null)
                .acbsForAllListings(buildSetOfIdNamePairs(this.getAcbsForDeveloperAllListings()))
                .acbsForActiveListings(buildSetOfIdNamePairs(this.getAcbsForDeveloperActiveListings()))
                .creationDate(this.getCreationDate())
                .build();
    }

    private LocalDate calculateDecertificationDate(String statusName, Date statusChangeDate) {
        if (statusName.equals(DeveloperStatusType.UnderCertificationBanByOnc.getName())) {
            return DateUtil.toLocalDate(statusChangeDate.getTime());
        }
        return null;
    }

    private Set<IdNamePair> buildSetOfIdNamePairs(String acbIdNamePairs) {
        if (StringUtils.isEmpty(acbIdNamePairs)) {
            return new HashSet<IdNamePair>();
        }

        String[] acbs = acbIdNamePairs.split(ACB_SEPARATOR_CHAR);
        return Stream.of(acbs)
            .map(acbIdNamePair -> acbIdNamePair.split(ID_NAME_SEPARATOR_CHAR))
            .map(idNameArr -> IdNamePair.builder()
                    .id(Long.parseLong(idNameArr[0]))
                    .name(idNameArr[1])
                    .build())
            .collect(Collectors.toSet());
    }

}
