package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DecertifiedDeveloperDTO implements Serializable {
	private static final long serialVersionUID = 5686501038412355764L;
	private Long developerId;
	private List<Long> acbIdList;
	private String developerStatus;
	private Long numMeaningfulUse;

	public DecertifiedDeveloperDTO(){}
	
	public DecertifiedDeveloperDTO(Long developerId, List<Long> acbIdList, String developerStatus, Long numMeaningfulUse){
		this.setDeveloperId(developerId);
		this.acbIdList=acbIdList;
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
	
	public void addAcb(Long acbId){
		if(this.acbIdList != null){
			this.acbIdList.add(acbId);
		}
		else{
			this.acbIdList = new ArrayList<Long>();
			this.acbIdList.add(acbId);
		}
	}

	public Long getDeveloperId() {
		return developerId;
	}

	public void setDeveloperId(Long developerId) {
		this.developerId = developerId;
	}
	
	public List<Long> getAcbIdList(){
		return this.acbIdList;
	}
	
	public void setAcbList(List<Long> acbIdList){
		this.acbIdList = acbIdList;
	}
	
}
