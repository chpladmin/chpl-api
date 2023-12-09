package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CertifiedProduct implements Serializable {
    private static final long serialVersionUID = -6634520925641244762L;

    @Schema(description = "Listing internal ID")
    private Long id;

    @Schema(description = "The unique CHPL ID of the certified product. New uploads to CHPL will use "
            + "the format: CertEdYr.ATL.ACB.Dev.Prod.Ver.ICS.AddS.Date")
    private String chplProductNumber;

    @Schema(description = "Edition of the listing. Ex: 2011, 2014, or 2015"
            )
    private String edition;

    @Schema(description = "The date the listing was certified given in milliseconds since epoch")
    private long certificationDate;

    @Schema(description = "The current certification status of the Listing")
    private String certificationStatus;

    @Schema(description = "Whether the Listing is considered \"Cures Update\" or not")
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
        this.edition = dto.getYear();
        this.certificationDate = (dto.getCertificationDate() != null ? dto.getCertificationDate().getTime() : -1);
        this.certificationStatus = dto.getCertificationStatusName();
        this.curesUpdate = dto.getCuresUpdate();
    }

    @Schema(description = "The day the listing was certified.")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    public LocalDate getCertificationDay() {
        return DateUtil.toLocalDate(this.certificationDate);
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
