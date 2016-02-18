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

@Entity
@Table(name = "testing_lab")
public class TestingLabEntity implements Serializable {
	private static final long serialVersionUID = -5332080900089062553L;

	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column( name = "testing_lab_id", nullable = false  )
	private Long id;
	
	@Column(name = "testing_lab_code")
	private String testingLabCode;
	
	@Basic( optional = true )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "address_id", unique=true, nullable = true)
	private AddressEntity address;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "accredidation_number")
	private String accredidationNumber;
	
	@Column(name = "website")
	private String website;
    
	@Basic( optional = false )
	@Column( name = "creation_date", nullable = false  )
	private Date creationDate;
	
	@Basic( optional = false )
	@Column(name = "deleted", nullable = false  )
	private Boolean deleted;
    
	@Basic( optional = false )
	@Column( name = "last_modified_date", nullable = false  )
	private Date lastModifiedDate;
	
	@Basic( optional = false )
	@Column( name = "last_modified_user", nullable = false  )
	private Long lastModifiedUser;
	
	public TestingLabEntity() {
		// Default constructor
	}

	/** Constructor taking a given ID.
	 * @param id to set
	 */
	public TestingLabEntity(Long id) {
		this.id = id;
	} 

 
	/** Return the type of this class. Useful for when dealing with proxies.
	* @return Defining class.
	*/
	@Transient
	public Class<?> getClassType() {
		return TestingLabEntity.class;
	}
 

	 /**
	 * Return the value associated with the column: creationDate.
	 * @return A Date object (this.creationDate)
	 */
	public Date getCreationDate() {
		return this.creationDate;
		
	}
	
	 /**  
	 * Set the value related to the column: creationDate.
	 * @param creationDate the creationDate value you wish to set
	 */
	public void setCreationDate(final Date creationDate) {
		this.creationDate = creationDate;
	}

	 /**
	 * Return the value associated with the column: deleted.
	 * @return A Boolean object (this.deleted)
	 */
	public Boolean isDeleted() {
		return this.deleted;
		
	}
	

  
	 /**  
	 * Set the value related to the column: deleted.
	 * @param deleted the deleted value you wish to set
	 */
	public void setDeleted(final Boolean deleted) {
		this.deleted = deleted;
	}

	 /**
	 * Return the value associated with the column: id.
	 * @return A Long object (this.id)
	 */
	public Long getId() {
		return this.id;
		
	}
	

  
	 /**  
	 * Set the value related to the column: id.
	 * @param id the id value you wish to set
	 */
	public void setId(final Long id) {
		this.id = id;
	}

	 /**
	 * Return the value associated with the column: lastModifiedDate.
	 * @return A Date object (this.lastModifiedDate)
	 */
	public Date getLastModifiedDate() {
		return this.lastModifiedDate;
	}
	

  
	 /**  
	 * Set the value related to the column: lastModifiedDate.
	 * @param lastModifiedDate the lastModifiedDate value you wish to set
	 */
	public void setLastModifiedDate(final Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	 /**
	 * Return the value associated with the column: lastModifiedUser.
	 * @return A Long object (this.lastModifiedUser)
	 */
	public Long getLastModifiedUser() {
		return this.lastModifiedUser;
		
	}
	
	 /**  
	 * Set the value related to the column: lastModifiedUser.
	 * @param lastModifiedUser the lastModifiedUser value you wish to set
	 */
	public void setLastModifiedUser(final Long lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}

	 /**
	 * Return the value associated with the column: name.
	 * @return A String object (this.name)
	 */
	public String getName() {
		return this.name;
		
	}
	
	 /**  
	 * Set the value related to the column: name.
	 * @param name the name value you wish to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	public String getTestingLabCode() {
		return testingLabCode;
	}

	public void setTestingLabCode(String testingLabCode) {
		this.testingLabCode = testingLabCode;
	}

	public AddressEntity getAddress() {
		return address;
	}

	public void setAddress(AddressEntity address) {
		this.address = address;
	}

	public String getAccredidationNumber() {
		return accredidationNumber;
	}

	public void setAccredidationNumber(String accredidationNumber) {
		this.accredidationNumber = accredidationNumber;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public Boolean getDeleted() {
		return deleted;
	}
}
