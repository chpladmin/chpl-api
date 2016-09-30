package gov.healthit.chpl.app;

public class Justification extends TableFormatting {
	private Boolean isLeftJustified;
	private Boolean isRightJustified;
	private String stringFormatJustification;
	
	public Justification(){}
	
	public Justification(Boolean isLeftJustified, Boolean isRightJustified) throws Exception{
		if(isLeftJustified == true && isRightJustified == true){
			throw new Exception("Cannot have left and right justification set as true");
		}
		else{
			setIsLeftJustified(isLeftJustified);
			setIsRightJustified(isRightJustified);
		}
		
		if(isLeftJustified){
			this.setStringFormatJustification("-");
		}
		else if(isRightJustified){
			this.setStringFormatJustification("");
		}
		else{
			this.setStringFormatJustification("");
		}
	}

	public Boolean getIsLeftJustified() {
		return isLeftJustified;
	}

	public void setIsLeftJustified(Boolean isLeftJustified) {
		this.isLeftJustified = isLeftJustified;
	}

	public Boolean getIsRightJustified() {
		return isRightJustified;
	}

	public void setIsRightJustified(Boolean isRightJustified) {
		this.isRightJustified = isRightJustified;
	}

	public String getStringFormatJustification() {
		return stringFormatJustification;
	}

	public void setStringFormatJustification(String stringFormatJustification) {
		this.stringFormatJustification = stringFormatJustification;
	}
}
