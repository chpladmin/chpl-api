package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductSystemUpdateEntity;

public class PendingCertifiedProductSystemUpdateDTO {
	
	private Long id;
    private Long pendingCertifiedProductId;
    private String changeMade;
    
    public PendingCertifiedProductSystemUpdateDTO(PendingCertifiedProductSystemUpdateEntity entity){
    	this.id = entity.getId();
    	this.pendingCertifiedProductId = entity.getPendingCertifiedProductId();
    	this.changeMade = entity.getChangeMade();
    }
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getPendingCertifiedProductId() {
		return pendingCertifiedProductId;
	}
	public void setPendingCertifiedProductId(Long pendingCertifiedProductId) {
		this.pendingCertifiedProductId = pendingCertifiedProductId;
	}
	public String getChangeMade() {
		return changeMade;
	}
	public void setChange_made(String changeMade) {
		this.changeMade = changeMade;
	}
}
