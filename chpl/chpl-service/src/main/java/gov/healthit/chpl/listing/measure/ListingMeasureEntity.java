package gov.healthit.chpl.listing.measure;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.domain.ListingMeasure;
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
@Table(name = "certified_product_measure")
public class ListingMeasureEntity extends EntityAudit {
    private static final long serialVersionUID = -893200800638747496L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "certified_product_id")
    private Long listingId;

    @Column(name = "measure_id")
    private Long measureId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "measure_id", unique = true, nullable = true, insertable = false, updatable = false)
    private MeasureEntity measure;

    @Column(name = "measure_type_id")
    private Long typeId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "measure_type_id", unique = true, nullable = true, insertable = false, updatable = false)
    @Where(clause = " deleted = false ")
    private ListingMeasureTypeEntity type;

    @Basic(optional = false)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "listingMeasureMapId")
    @Column(name = "certified_product_measure_map_id", nullable = false)
    @Where(clause = " deleted = false ")
    private Set<ListingMeasureCriterionMapEntity> associatedCriteria
        = new LinkedHashSet<ListingMeasureCriterionMapEntity>();

    public ListingMeasure convert() {
        ListingMeasure listingMeasure = new ListingMeasure();
        listingMeasure.setId(getId());
        if (getMeasure() != null) {
            listingMeasure.setMeasure(getMeasure().convert());
        }
        if (getType() != null) {
            listingMeasure.setMeasureType(getType().convert());
        }
        if (getAssociatedCriteria() != null) {
            listingMeasure.setAssociatedCriteria(
                    getAssociatedCriteria().stream()
                    .map(assocCriterion -> assocCriterion.convert())
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }
        return listingMeasure;
    }
}
