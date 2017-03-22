package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusHistoryDTO;

public class Developer implements Serializable {
	private static final long serialVersionUID = 7341544844577617247L;
	private Long developerId;
	private String developerCode;
	private String name;
	private String website;
	private Address address;
	private Contact contact;
	private String lastModifiedDate;
	private Boolean deleted;
	private List<TransparencyAttestationMap> transparencyAttestations;
	private List<DeveloperStatusHistory> statusHistory;
	
	public Developer() {
		this.transparencyAttestations = new ArrayList<TransparencyAttestationMap>();
		this.statusHistory = new ArrayList<DeveloperStatusHistory>();
	}
	
	public Developer(DeveloperDTO dto) {
		this();
		this.developerId = dto.getId();
		this.developerCode = dto.getDeveloperCode();
		this.name = dto.getName();
		this.website = dto.getWebsite();
		this.deleted = dto.getDeleted();
		if(dto.getAddress() != null) {
			this.address = new Address(dto.getAddress());
		}
		if(dto.getContact() != null) {
			this.contact = new Contact(dto.getContact());
		}
		
		if(dto.getLastModifiedDate() != null) {
			this.lastModifiedDate = dto.getLastModifiedDate().getTime()+"";
		}
		
		if(dto.getTransparencyAttestationMappings() != null && dto.getTransparencyAttestationMappings().size() > 0) {
			for(DeveloperACBMapDTO map : dto.getTransparencyAttestationMappings()) {
				TransparencyAttestationMap toAdd = new TransparencyAttestationMap();
				toAdd.setAcbId(map.getAcbId());
				toAdd.setAcbName(map.getAcbName());
				toAdd.setAttestation(map.getTransparencyAttestation());
				this.transparencyAttestations.add(toAdd);
			}
		}
		
		if(dto.getStatusHistory() != null && dto.getStatusHistory().size() > 0) {
			for(DeveloperStatusHistoryDTO historyItem : dto.getStatusHistory()) {
				DeveloperStatusHistory toAdd = new DeveloperStatusHistory(historyItem);
				this.statusHistory.add(toAdd);
			}
		}
	}
	public Long getDeveloperId() {
		return developerId;
	}
	public void setDeveloperId(Long developerId) {
		this.developerId = developerId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public String getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getDeveloperCode() {
		return developerCode;
	}

	public void setDeveloperCode(String developerCode) {
		this.developerCode = developerCode;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public List<TransparencyAttestationMap> getTransparencyAttestations() {
		return transparencyAttestations;
	}

	public void setTransparencyAttestations(List<TransparencyAttestationMap> transparencyAttestations) {
		this.transparencyAttestations = transparencyAttestations;
	}
	
	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public List<DeveloperStatusHistory> getStatusHistory() {
		return statusHistory;
	}

	public void setStatusHistory(List<DeveloperStatusHistory> statusHistory) {
		this.statusHistory = statusHistory;
	}
}
