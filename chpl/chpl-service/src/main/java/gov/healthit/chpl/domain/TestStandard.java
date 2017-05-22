package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.TestStandardDTO;

public class TestStandard implements Serializable {
	private static final long serialVersionUID = 620315627813875501L;
	private Long id;
	private String name;
	private String description;
	private String year;
	
	public TestStandard() {}
	public TestStandard(TestStandardDTO dto) {
		this.id = dto.getId();
		this.name = dto.getName();
		this.description = dto.getDescription();
		this.year = dto.getYear();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
}
