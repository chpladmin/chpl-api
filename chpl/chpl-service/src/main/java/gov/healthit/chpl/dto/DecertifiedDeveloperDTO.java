package gov.healthit.chpl.dto;

import java.util.List;

public class DecertifiedDeveloperDTO {
	private String developerName;
	private List<String> oncacb;
	private String developerStatus;
	private Long numMeaningfulUse;

	public DecertifiedDeveloperDTO(){}
	
	public DecertifiedDeveloperDTO(String developerName, List<String> oncacb, String developerStatus, Long numMeaningfulUse){
		this.developerName=developerName;
		this.oncacb=oncacb;
		this.developerStatus=developerStatus;
		this.numMeaningfulUse=numMeaningfulUse;
	}

	public String getDeveloperName() {
		return developerName;
	}

	public void setDeveloperName(String developerName) {
		this.developerName = developerName;
	}
	
	public List<String> getOncacb() {
		return oncacb;
	}

	public void setOncacb(List<String> oncacb) {
		this.oncacb = oncacb;
	}

	public String getDeveloperStatus() {
		return developerStatus;
	}

	public void setDeveloperStatus(String developerStatus) {
		this.developerStatus = developerStatus;
	}

	public Long getNumMeaningfulUse() {
		return numMeaningfulUse;
	}

	public void setNumMeaningfulUse(Long numMeaningfulUse) {
		if(this.numMeaningfulUse == null){
			this.numMeaningfulUse = numMeaningfulUse;
		}
		else{
			this.numMeaningfulUse += numMeaningfulUse;
		}
		
	}
	
	public void addAcb(String acb){
		this.oncacb.add(acb);
	}
	
	public void addNumMeaningfulUse(Long numMeaningfulUse){
		if(this.numMeaningfulUse != null){
			this.numMeaningfulUse+= numMeaningfulUse;
		}
		else{
			this.numMeaningfulUse = numMeaningfulUse;
		}
		
	}
	
}
