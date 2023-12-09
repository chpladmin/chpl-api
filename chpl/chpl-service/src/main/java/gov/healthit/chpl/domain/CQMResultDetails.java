package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CQMResultCriteriaDTO;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@Data
public class CQMResultDetails implements Serializable {
    private static final long serialVersionUID = -7077008682408284325L;

    @Schema(description = "CQM internal ID")
    private Long id;

    @JsonIgnore
    private Long cqmCriterionId;

    @Schema(description = "The CMS ID clinical quality measures to which the Health IT Module has been certified. "
            + "For a list of the clinical quality measures, please reference the CMS eCQM library.")
    private String number;

    @Schema(description = "The CMS ID clinical quality measures to which the Health IT Module has been certified.")
    private String cmsId;

    @Schema(description = "The title of the clinical quality measure.")
    private String title;

    @Schema(description = "The description of the clinical quality measure.")
    private String description;

    @Schema(description = "The NQF Number of the clinical quality measure")
    private String nqfNumber;

    @Schema(description = "Type of CQM. 1 for Ambulatory, 2 for Inpatient")
    private Long typeId;

    @Schema(description = "Category of the clinial quality measure. Examples include "
            + "\"Population/Public Health\" or \"Patient and Family Engagement\"")
    private String domain;

    @Schema(description = "This variable indicates whether or not the clinical quality measure has "
            + "been certified to the related listing. It is a binary variable that takes either true or false value.")
    private Boolean success;

    @Schema(description = "The corresponding version of the clinical quality measures to which the "
            + "Health IT Module has been certified. For a list of clinical quality measures and their viable "
            + "versions, please reference the CMS eCQM library.")
    @Builder.Default
    private LinkedHashSet<String> successVersions = new LinkedHashSet<String>();

    @Schema(description = "All possible versions of the clinical quality measure. For a list of "
            + "clinical quality measures and their viable versions, please reference the "
            + "CMS eCQM library.")
    @Builder.Default
    private LinkedHashSet<String> allVersions = new LinkedHashSet<String>();

    @Schema(description = "The certification criteria to which a given clinical quality measure applies. "
            + "It takes values including: c1, c2, c3, c4,c1;c2, c1;c3, c1;c4, c2;c3, c2;c4, c3;c4, c1;c2;c3, "
            + "c2;c3;c4, c1;c2;c3;c4")
    @Builder.Default
    private List<CQMResultCertification> criteria = new ArrayList<CQMResultCertification>();

    public CQMResultDetails() {
        this.successVersions = new LinkedHashSet<String>();
        this.allVersions = new LinkedHashSet<String>();
        this.criteria = new ArrayList<CQMResultCertification>();
    }

    public CQMResultDetails(CQMResultDetailsDTO dto) {
        this();
        this.id = dto.getId();
        this.number = dto.getNumber();
        this.cmsId = dto.getCmsId();
        this.title = dto.getTitle();
        this.description = dto.getDescription();
        this.nqfNumber = dto.getNqfNumber();
        this.typeId = dto.getCqmCriterionTypeId();
        this.domain = dto.getDomain();

        if (!StringUtils.isEmpty(dto.getCmsId())) {
            this.getSuccessVersions().add(dto.getVersion());
        } else if (!StringUtils.isEmpty(dto.getNqfNumber())) {
            this.setSuccess(dto.getSuccess());
        }

        if (dto.getCriteria() != null && dto.getCriteria().size() > 0) {
            for (CQMResultCriteriaDTO criteriaDTO : dto.getCriteria()) {
                CQMResultCertification cqmCriteria = new CQMResultCertification(criteriaDTO);
                this.criteria.add(cqmCriteria);
            }
        }
    }
}
