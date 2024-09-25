package gov.healthit.chpl.entity.listing;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gov.healthit.chpl.domain.CQMResultDetails;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "cqm_result_details", schema = "openchpl")
public class CQMResultDetailsEntity {

    @Id
    @Column(name = "cqm_result_id", nullable = false)
    private Long id;

    @Column(name = "certified_product_id")
    private Long certifiedProductId;

    @Basic(optional = false)
    @Column(name = "success", nullable = false)
    private Boolean success;

    @Basic(optional = false)
    @Column(name = "cqm_criterion_id", nullable = false)
    private Long cqmCriterionId;

    @Column(name = "number")
    private String number;

    @Column(name = "cms_id")
    private String cmsId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "nqf_number")
    private String nqfNumber;

    @Column(name = "cqm_criterion_type_id")
    private Long cqmCriterionTypeId;

    @Basic(optional = true)
    @Column(name = "cqm_version_id", nullable = true)
    private Long cqmVersionId;

    @Basic(optional = true)
    @Column(name = "cqm_domain", nullable = true)
    private String domain;

    @Basic(optional = true)
    @Column(name = "version")
    private String version;

    @Column(name = "cqm_id")
    private String cqmId;

    @Column(name = "deleted")
    private Boolean deleted;

    public CQMResultDetails toDomain() {
        return CQMResultDetails.builder()
                .cmsId(this.getCmsId())
                .cqmCriterionId(this.getCqmCriterionId())
                .description(this.getDescription())
                .domain(this.getDomain())
                .id(this.getId())
                .nqfNumber(this.getNqfNumber())
                .number(this.getNumber())
                .title(this.getTitle())
                .typeId(this.getCqmCriterionTypeId())
                .success(this.getSuccess())
                .successVersions(Stream.of(this.getVersion()).collect(Collectors.toCollection(LinkedHashSet::new)))
                .allVersions(new LinkedHashSet<String>())
                .build();
    }
}
