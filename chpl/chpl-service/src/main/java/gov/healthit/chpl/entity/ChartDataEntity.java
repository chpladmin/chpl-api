package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "chart_data")
public class ChartDataEntity implements Serializable{
	
	private static final long serialVersionUID = -1184965899001788559L;

	@Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column( name = "chart_data_id", nullable = false  )
	private Long id;
	
	@Basic( optional = false )
	@Column( name = "data_date", nullable = false)
	private Date dataDate;
	
	@Basic(optional = true)
	@Column(name="json_data_object", nullable = false)
	private String jsonDataObject;
	
	@Basic( optional = false )
	@Column( name = "chart_data_stat_type_id", nullable = false  )
	private Long typeOfStatId;
	
	@Basic( optional = false )
	@Column( name = "last_modified_date", nullable = false)
	private Date lastModifiedDate;
	
	@Basic( optional = false )
	@Column( name = "last_modified_user", nullable = false  )
	private Long lastModifiedUser;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDataDate() {
		return dataDate;
	}

	public void setDataDate(Date dataDate) {
		this.dataDate = dataDate;
	}

	public String getJsonDataObject() {
		return jsonDataObject;
	}

	public void setJsonDataObject(String jsonDataObject) {
		this.jsonDataObject = jsonDataObject;
	}

	public Long getTypeOfStatId() {
		return typeOfStatId;
	}

	public void setTypeOfStatId(Long typeOfStatId) {
		this.typeOfStatId = typeOfStatId;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(Long lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}
}
