package gov.healthit.chpl.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import gov.healthit.chpl.domain.Address;
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
@Table(name = "address")
public class AddressEntity extends EntityAudit {
    private static final long serialVersionUID = 762431901700320834L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "address_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Size(min = 1)
    @Column(name = "street_line_1")
    private String streetLineOne;

    @Column(name = "street_line_2")
    private String streetLineTwo;

    @Basic(optional = false)
    @Size(min = 1)
    @Column(name = "city")
    private String city;

    @Basic(optional = false)
    @Size(min = 1)
    @Column(name = "state")
    private String state;

    @Basic(optional = false)
    @Size(min = 1)
    @Column(name = "zipcode")
    private String zipcode;

    @Basic(optional = false)
    @Size(min = 1)
    @Column(name = "country")
    private String country;

    public Address toDomain() {
        return Address.builder()
        .addressId(this.getId())
        .city(this.getCity())
        .country(this.getCountry())
        .line1(this.getStreetLineOne())
        .line2(this.getStreetLineTwo())
        .state(this.getState())
        .zipcode(this.getZipcode())
        .build();
    }
}
