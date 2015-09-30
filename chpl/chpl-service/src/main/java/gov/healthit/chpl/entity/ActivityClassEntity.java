package gov.healthit.chpl.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name="activity_class")
public class ActivityClassEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "activity_class_activity_class_idGenerator")
	@Basic( optional = false )
	@Column( name = "activity_id", nullable = false )
	@SequenceGenerator(name = "activity_class_activity_class_idGenerator", sequenceName = "activity_class_activity_class_id_seq")
	private Long id;
	
	@Basic( optional = false )
	@Column( name = "class", nullable = false)
	private String className;

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
}
