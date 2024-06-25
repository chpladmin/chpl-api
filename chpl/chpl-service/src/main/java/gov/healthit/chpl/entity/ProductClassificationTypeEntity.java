package gov.healthit.chpl.entity;

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
@Table(name = "product_classification_type")
public class ProductClassificationTypeEntity extends EntityAudit {
    private static final long serialVersionUID = 1890518450950966171L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "product_classification_type_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(nullable = false, length = 250)
    private String description;

    @Basic(optional = false)
    @Column(nullable = false, length = 50)
    private String name;

}
