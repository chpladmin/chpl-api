package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.domain.Address;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
//NOTE: This class is ONLY kept in order to retrieve pending listing activity
public class PendingCertifiedProductDTO implements Serializable {
    private static final long serialVersionUID = 8778880570983282001L;
    private Long id;
    private Long practiceTypeId;
    private Long developerId;
    private Address developerAddress;
    private Long productId;
    private Long productVersionId;
    private Long certificationEditionId;
    private Long certificationBodyId;
    private Long productClassificationId;
    private Boolean deleted;
    private Long lastModifiedUser;
    private String uniqueId;
    private String recordStatus;
    private String practiceType;
    private String developerName;
    private String productName;
    private String productVersion;
    private String certificationEdition;
    private String acbCertificationId;
    private String certificationBodyName;
    private String productClassificationName;
    private Date certificationDate;
    private String developerStreetAddress;
    private String developerCity;
    private String developerState;
    private String developerZipCode;
    private String developerWebsite;
    private String developerEmail;
    private String developerContactName;
    private String developerPhoneNumber;
    private Boolean selfDeveloper;
    private Long developerContactId;
    private String reportFileLocation;
    private String sedReportFileLocation;
    private String sedIntendedUserDescription;
    private Date sedTestingEnd;
    private Boolean ics;
    private Boolean hasQms;
    private Boolean accessibilityCertified;
    private String mandatoryDisclosures;
    private String svapNoticeUrl;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Date uploadDate;
}
