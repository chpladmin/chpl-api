package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CertifiedProduct implements Serializable {
    private static final long serialVersionUID = -6634520925641244762L;

    /**
     * Listing internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * The unique CHPL ID of the certified product. New uploads to CHPL will use
     * the format: CertEdYr.ATL.ACB.Dev.Prod.Ver.ICS.AddS.Date
     */
    @XmlElement(required = true)
    private String chplProductNumber;

    /**
     * The last time this listing was modified in any way given in milliseconds
     * since epoch.
     */
    @XmlElement(required = false, nillable = true)
    private Long lastModifiedDate;

    /**
     * Edition of the listing. Ex: 2011, 2014, or 2015
     */
    @XmlElement(required = false, nillable = true)
    private String edition;

    /**
     * The date the listing was certified given in milliseconds since epoch
     */
    @XmlElement(required = false, nillable = true)
    private long certificationDate;

    /**
     * The current certification status of the Listing
     */
    @XmlElement(required = false, nillable = true)
    private String certificationStatus;

    /**
     * Whether the Listing is consider "Cures Update" or not
     */
    @XmlElement(required = false, nillable = true)
    private Boolean curesUpdate;

    public CertifiedProduct() {
    }

    public CertifiedProduct(final CertifiedProductDetailsDTO dto) {
        this.id = dto.getId();
        if (!StringUtils.isEmpty(dto.getChplProductNumber())) {
            this.chplProductNumber = dto.getChplProductNumber();
        } else {
            this.chplProductNumber = dto.getYearCode() + "." + dto.getTestingLabCode() + "."
                    + dto.getCertificationBodyCode() + "." + dto.getDeveloper().getDeveloperCode() + "."
                    + dto.getProductCode() + "." + dto.getVersionCode() + "." + dto.getIcsCode() + "."
                    + dto.getAdditionalSoftwareCode() + "." + dto.getCertifiedDateCode();
        }
        this.lastModifiedDate = dto.getLastModifiedDate() != null ? dto.getLastModifiedDate().getTime() : null;
        this.edition = dto.getYear();
        this.certificationDate = (dto.getCertificationDate() != null ? dto.getCertificationDate().getTime() : -1);
        this.certificationStatus = dto.getCertificationStatusName();
        this.curesUpdate = dto.getCuresUpdate();
    }

    /**
     * Check for sameness here by first trying to compare the two IDs
     * or if one of them isn't filled in try comparing the chpl product numbers.
     * Expect one or the other (or both) of those fields always filled in.
     * @param anotherCp
     * @return whether the two certified products are the same
     */
    public boolean matches(final CertifiedProduct anotherCp) {
        if (this.id != null && anotherCp.id != null
                && this.id.longValue() == anotherCp.id.longValue()) {
            return true;
        } else if (!StringUtils.isEmpty(this.chplProductNumber) && !StringUtils.isEmpty(anotherCp.chplProductNumber)
                && this.chplProductNumber.equalsIgnoreCase(anotherCp.chplProductNumber)) {
            return true;
        }
        return false;
    }
}
