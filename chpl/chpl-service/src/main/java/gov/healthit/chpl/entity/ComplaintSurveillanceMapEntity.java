package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import gov.healthit.chpl.domain.ComplaintSurveillanceMap;
import gov.healthit.chpl.entity.surveillance.SurveillanceBasicEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "complaint_surveillance_map")
public class ComplaintSurveillanceMapEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_surveillance_map_id", nullable = false)
    private Long id;

    @Column(name = "complaint_id", nullable = false)
    private Long complaintId;

    @Column(name = "surveillance_id", nullable = false)
    private Long surveillanceId;

    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Transient
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
