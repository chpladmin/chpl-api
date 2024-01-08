package gov.healthit.chpl.complaint.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.compliance.surveillance.entity.SurveillanceBasicEntity;
import gov.healthit.chpl.domain.ComplaintSurveillanceMap;
import gov.healthit.chpl.entity.EntityAudit;
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
@Table(name = "complaint_surveillance_map")
public class ComplaintSurveillanceMapEntity extends EntityAudit {
    private static final long serialVersionUID = 5402427083505515536L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_surveillance_map_id", nullable = false)
    private Long id;

    @Column(name = "complaint_id", nullable = false)
    private Long complaintId;

    @Column(name = "surveillance_id", nullable = false)
    private Long surveillanceId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "surveillance_id", insertable = false, updatable = false)
    @Where(clause = "deleted <> 'true'")
    private SurveillanceBasicEntity surveillance;

        public ComplaintSurveillanceMap buildComplaintSurveillanceMap() {
        return ComplaintSurveillanceMap.builder()
            .complaintId(this.getComplaintId())
            .id(this.getId())
            .surveillanceId(this.getSurveillanceId())
            .surveillance(this.getSurveillance() != null ? this.getSurveillance().buildSurveillanceBasic() : null)
            .build();
    }
}
