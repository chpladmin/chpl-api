package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import gov.healthit.chpl.util.Util;

/**
 * Testing Lab mapping to database.
 * @author kekey
 *
 */
@Entity
@Table(name = "testing_lab")
public class TestingLabEntity implements Serializable {
    private static final long serialVersionUID = -5332080900089062553L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "testing_lab_id", nullable = false)
    private Long id;

    @Column(name = "testing_lab_code")
    private String testingLabCode;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", unique = true, nullable = true)
    private AddressEntity address;

    @Column(name = "name")
    private String name;

    @Column(name = "accredidation_number")
    private String accredidationNumber;

    @Column(name = "website")
    private String website;

    @Column(name = "retired", nullable = false)
    private Boolean retired;

    @Column(name = "retirement_date", nullable = true)
    private Date retirementDate;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false, insertable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    public TestingLabEntity() {
        // Default constructor
    }

    public TestingLabEntity(final Long id) {
        this.id = id;
    }

    /**
     * Return the type of this class. Useful for when dealing with proxies.
     *
     * @return Defining class.
     */
    @Transient
    public Class<?> getClassType() {
        return TestingLabEntity.class;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return this.lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getTestingLabCode() {
        return testingLabCode;
    }

    public void setTestingLabCode(final String testingLabCode) {
        this.testingLabCode = testingLabCode;
    }

    public AddressEntity getAddress() {
        return address;
    }

    public void setAddress(final AddressEntity address) {
        this.address = address;
    }

    public String getAccredidationNumber() {
        return accredidationNumber;
    }

    public void setAccredidationNumber(final String accredidationNumber) {
        this.accredidationNumber = accredidationNumber;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(final String website) {
        this.website = website;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public Boolean getRetired() {
        return retired;
    }

    public void setRetired(final Boolean retired) {
        this.retired = retired;
    }

    public final Date getRetirementDate() {
        return retirementDate;
    }

    public final void setRetirementDate(final Date retirementDate) {
        this.retirementDate = retirementDate;
    }

    @Override
    public String toString() {
        return "TestingLabEntity [id=" + id + ", testingLabCode=" + testingLabCode + ", address=" + address + ", name="
                + name + ", accredidationNumber=" + accredidationNumber + ", website=" + website + ", retired="
                + retired + ", retirementDate=" + retirementDate + ", creationDate=" + creationDate + ", deleted="
                + deleted + ", lastModifiedDate=" + lastModifiedDate + ", lastModifiedUser=" + lastModifiedUser + "]";
    }
}
