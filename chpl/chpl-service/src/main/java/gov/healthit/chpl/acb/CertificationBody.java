package gov.healthit.chpl.acb;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="certification_body")
public class CertificationBody {
	
	
	@Id
	@Column(name="id")
	@GeneratedValue( strategy = GenerationType.IDENTITY )
	private Long id;
	
	@Column(name="name")
	private String name;
	
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long long1) {
		this.id = long1;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

}
