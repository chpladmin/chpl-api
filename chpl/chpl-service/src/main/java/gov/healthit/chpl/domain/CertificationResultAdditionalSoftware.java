package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class CertificationResultAdditionalSoftware implements Serializable {
    private static final long serialVersionUID = -4131156681875211987L;

    /**
     * Additional software to certification result mapping internal ID
     */
    @Schema(description = "Additional software to certification result mapping internal ID")
    @XmlElement(required = true)
    private Long id;

    /**
     * This variable demonstrates if the additional software relied upon by the
     * Health IT Module is not a certified health IT product, the name of the
     * additional software product relied upon. It is a string variable that
     * does not take any restrictions on formatting or values and is applicable
     * for 2014 and 2015 Edition.
     */
    @Schema(description = "This variable demonstrates if the additional software relied upon by the "
            + "Health IT Module is not a certified health IT product, the name of the "
            + "additional software product relied upon. It is a string variable that "
            + "does not take any restrictions on formatting or values and is applicable "
            + "for 2014 and 2015 Edition.")
    @XmlElement(required = false, nillable = true)
    private String name;

    /**
     * The version of the corresponding non-certified additional software relied
     * upon by the Health IT Module. This variable is for 2014 and 2015 Edition,
     * and a string variable that does not take any restrictions on formatting
     * or values.
     */
    @Schema(description = "The version of the corresponding non-certified additional software relied "
            + "upon by the Health IT Module. This variable is for 2014 and 2015 Edition, "
            + "and a string variable that does not take any restrictions on formatting "
            + "or values.")
    @XmlElement(required = false, nillable = true)
    private String version;

    /**
     * This variable indicates if the additional software relied upon by the
     * Health IT Module is also a certified health IT product, the unique CHPL
     * ID of the additional software relied upon. It is applicable for 2014 and
     * 2015 Edition.It either takes CHIP-XXXXXX or
     * Edition.ATL.ACB.Developer.Product.Version.ICS.AdditionalSoftware.Date.
     * The CHPL ID format,
     * Edition.ATL.ACB.Developer.Product.Version.ICS.AdditionalSoftware.Date, is
     * coded using product-specific information. Edition = certification edition
     * (ex. 2014, 2015); ATL = two digit code for the ONC Authorized Testing
     * Laboratory (ATL); ACB = two digit code for the ONC Authorized Certifying
     * Body (ACB); Developer = four digit code for the health IT product
     * developer; Product = four character alphanumeric reference to the
     * certified product; Version = two character alphanumeric reference to the
     * version of the certified product; ICS = binary code indicating Inherited
     * Certified Status (incremental); AdditionalSoftware = binary code
     * indicating the requirement for additional software to meeting
     * certification requirements (1 = yes; 0 = no); Date = Date of
     * certification (format = YYMMDD).
     */
    @Schema(description = "This variable indicates if the additional software relied upon by the "
            + "Health IT Module is also a certified health IT product, the unique CHPL "
            + "ID of the additional software relied upon. It is applicable for 2014 and "
            + "2015 Edition.It either takes CHIP-XXXXXX or "
            + "Edition.ATL.ACB.Developer.Product.Version.ICS.AdditionalSoftware.Date. "
            + "The CHPL ID format, "
            + "Edition.ATL.ACB.Developer.Product.Version.ICS.AdditionalSoftware.Date, is "
            + "coded using product-specific information. Edition = certification edition "
            + "(ex. 2014, 2015); ATL = two digit code for the ONC Authorized Testing "
            + "Laboratory (ATL); ACB = two digit code for the ONC Authorized Certifying "
            + "Body (ACB); Developer = four digit code for the health IT product "
            + "developer; Product = four character alphanumeric reference to the "
            + "certified product; Version = two character alphanumeric reference to the "
            + "version of the certified product; ICS = binary code indicating Inherited "
            + "Certified Status (incremental); AdditionalSoftware = binary code "
            + "indicating the requirement for additional software to meeting "
            + "certification requirements (1 = yes; 0 = no); Date = Date of "
            + "certification (format = YYMMDD).")
    @XmlElement(required = false, nillable = true)
    private Long certifiedProductId;

    /**
     * If the additional software relied upon by the Health IT Module is also a
     * certified health IT product, the unique CHPL ID of the additional
     * software relied upon.
     */
    @Schema(description = "If the additional software relied upon by the Health IT Module is also a "
            + "certified health IT product, the unique CHPL ID of the additional "
            + "software relied upon.")
    @XmlElement(required = false, nillable = true)
    private String certifiedProductNumber;

    /**
     * Additional software justification
     */
    @Schema(description = "Additional software justification")
    @XmlElement(required = false, nillable = true)
    private String justification;

    @XmlTransient
    private Long certificationResultId;

    /**
     * For 2015 certified products, the concept of a 'grouping' is introduced to
     * allow for sets of alternative additional software. At least one
     * Additional Software within a particular grouping is required to meet a
     * specific certification criteria.
     */
    @Schema(description = "For 2015 certified products, the concept of a 'grouping' is introduced to "
            + "allow for sets of alternative additional software. At least one "
            + "Additional Software within a particular grouping is required to meet a "
            + "specific certification criteria.")
    @XmlElement(required = false, nillable = true)
    private String grouping;

    public CertificationResultAdditionalSoftware() {
        super();
    }

    public CertificationResultAdditionalSoftware(CertificationResultAdditionalSoftwareDTO dto) {
        this.id = dto.getId();
        this.name = dto.getName();
        this.version = dto.getVersion();
        this.certifiedProductId = dto.getCertifiedProductId();
        this.justification = dto.getJustification();
        this.certificationResultId = dto.getCertificationResultId();
        this.certifiedProductNumber = dto.getCertifiedProductNumber();
        this.grouping = dto.getGrouping();
    }

    // not overriding equals becuase i'm not sure if this logic is really right
    // for "equals"
    // but i want to know if two additional software objects are the same as far
    // as a user would think
    public boolean matches(CertificationResultAdditionalSoftware other) {
        if ((StringUtils.isEmpty(this.getGrouping()) && !StringUtils.isEmpty(other.getGrouping()))
            || (!StringUtils.isEmpty(this.getGrouping()) && StringUtils.isEmpty(other.getGrouping()))) {
            return false;
        }
        boolean result = false;
        if ((StringUtils.isEmpty(this.getGrouping()) && StringUtils.isEmpty(other.getGrouping()))
                || this.getGrouping().equals(other.getGrouping())) {
            // if grouping is the same (or not grouped),
            // look for cp id or or software name/version combo to match
            if (this.getCertifiedProductId() != null && other.getCertifiedProductId() != null
                    && this.getCertifiedProductId().longValue() == other.getCertifiedProductId().longValue()) {
                result = true;
            } else if (!StringUtils.isEmpty(this.getName()) && !StringUtils.isEmpty(other.getName())
                    && this.getName().equalsIgnoreCase(other.getName())
                    && ((StringUtils.isEmpty(this.getVersion()) && StringUtils.isEmpty(other.getVersion()))
                            || this.getVersion().equalsIgnoreCase(other.getVersion()))) {
                result = true;
            }
        }
        return result;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(final String justification) {
        this.justification = justification;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public Long getCertifiedProductId() {
        return certifiedProductId;
    }

    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
    }

    public Long getCertificationResultId() {
        return certificationResultId;
    }

    public void setCertificationResultId(final Long certificationResultId) {
        this.certificationResultId = certificationResultId;
    }

    public String getCertifiedProductNumber() {
        return certifiedProductNumber;
    }

    public void setCertifiedProductNumber(final String certifiedProductNumber) {
        this.certifiedProductNumber = certifiedProductNumber;
    }

    public String getGrouping() {
        return grouping;
    }

    public void setGrouping(final String grouping) {
        this.grouping = grouping;
    }

}
