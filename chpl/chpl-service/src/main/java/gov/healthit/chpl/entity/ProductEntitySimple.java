package gov.healthit.chpl.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.contact.PointOfContact;
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
@Table(name = "product")
public class ProductEntitySimple extends EntityAudit {
    private static final long serialVersionUID = -533208090008462551L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "product_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Size(min = 1)
    @Column(name = "name")
    private String name;

    @Basic(optional = true)
    @Column(name = "contact_id")
    private Long contactId;

    @Basic(optional = true)
    @Column(name = "report_file_location", length = 255)
    private String reportFileLocation;

    @Basic(optional = false)
    @Column(name = "vendor_id", nullable = false)
    private Long developerId;

    public Product toDomain() {
        return Product.builder()
                .id(this.getId())
                .name(this.getName())
                .contact(this.getContactId() == null ? null
                        : PointOfContact.builder()
                        .contactId(this.getContactId())
                        .build())
                .lastModifiedDate(this.getLastModifiedDate() + "")
                .owner(this.getDeveloperId() == null ? null
                        : Developer.builder()
                        .id(this.getDeveloperId())
                        .build())
                .reportFileLocation(this.getReportFileLocation())
                .build();
    }
}
