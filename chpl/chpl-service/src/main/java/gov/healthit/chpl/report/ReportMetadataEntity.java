package gov.healthit.chpl.report;

import gov.healthit.chpl.entity.EntityAudit;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "report_metadata")
public class ReportMetadataEntity extends EntityAudit {
    private static final long serialVersionUID = -1269371158605085957L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "environment")
    private String environment;

    @Column(name = "title")
    private String title;

    @Column(name = "report_key")
    private String reportKey;

    @Column(name = "report_group")
    private String reportGroup;

    @Column(name = "url")
    private String url;

    @Column(name = "height")
    private String height;

    @Column(name = "display_order")
    private String displayOrder;

    public ReportMetadata toDomain() {
        return ReportMetadata.builder()
                .id(id)
                .environment(environment)
                .title(title)
                .reportKey(reportKey)
                .reportGroup(reportGroup)
                .url(url)
                .height(height)
                .displayOrder(displayOrder)
                .build();
    }
}
