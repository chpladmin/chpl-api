package gov.healthit.chpl.dto;

import java.util.List;

public class DecertifiedDeveloperDTO {
	private Long developerId;
	private List<Long> acbList;
	private String developerStatus;
	private Long numMeaningfulUse;

	public DecertifiedDeveloperDTO(){}
	
	public DecertifiedDeveloperDTO(Long developerId, List<Long> acbList, String developerStatus, Long numMeaningfulUse){
		this.setDeveloperId(developerId);
		this.acbList=acbList;
		this.developerStatus=developerStatus;
		this.numMeaningfulUse=numMeaningfulUse;
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
			if(numMeaningfulUse != null){
				this.numMeaningfulUse += numMeaningfulUse;
			}
		}
	}
	
	public void addAcb(Long acb){
		this.acbList.add(acb);
	}

	public Long getDeveloperId() {
		return developerId;
	}

	public void setDeveloperId(Long developerId) {
		this.developerId = developerId;
	}
	
	public List<Long> getAcbList(){
		return this.acbList;
	}
	
	public void setAcbList(List<Long> acbList){
		this.acbList = acbList;
	}
	
}
