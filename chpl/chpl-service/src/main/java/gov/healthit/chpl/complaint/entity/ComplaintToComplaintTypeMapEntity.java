package gov.healthit.chpl.complaint.entity;

import org.hibernate.annotations.SQLRestriction;

import gov.healthit.chpl.complaint.domain.ComplaintType;
import gov.healthit.chpl.entity.EntityAudit;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "complaint_to_complaint_type_map")
@SQLRestriction("deleted <> true")
public class ComplaintToComplaintTypeMapEntity extends EntityAudit {
    private static final long serialVersionUID = 9116199957815511112L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "complaint_id", nullable = false)
    private Long complaintId;

    @Column(name = "complaint_type_id", nullable = false)
    private Long complaintTypeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_type_id", insertable = false, updatable = false)
    private ComplaintTypeEntity complaintType;

    public ComplaintType buildComplaintType() {
        return this.getComplaintType().buildComplaintType();
    }
}
