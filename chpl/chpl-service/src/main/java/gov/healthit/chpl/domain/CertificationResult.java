package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.dto.CertificationResultDetailsDTO;

public class CertificationResult {
	private String number;
	private String title;
	private boolean success;
	private boolean gap;
	private boolean sed;
	private boolean g1Success;
	private boolean g2Success;
	private String ucdProcessSelected;
	private String ucdProcessDetails;
	
	private List<CertificationResultTestFunctionality> testFunctionality;
	private List<CertificationResultTestProcedure> testProcedures;
	private List<CertificationResultTestData> testDataUsed;
	private List<CertificationResultAdditionalSoftware> additionalSoftware;
	private List<CertificationResultTestStandard> testStandards;
	private List<CertificationResultTestTool> testToolsUsed;
	
	public CertificationResult(){
		 additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
		 testStandards = new ArrayList<CertificationResultTestStandard>();
		 testToolsUsed = new ArrayList<CertificationResultTestTool>();
		 testDataUsed = new ArrayList<CertificationResultTestData>();
		 testProcedures = new ArrayList<CertificationResultTestProcedure>();
		 testFunctionality = new ArrayList<CertificationResultTestFunctionality>();
	}
	
	public List<CertificationResultTestProcedure> getTestProcedures() {
		return testProcedures;
	}

	public void setTestProcedures(List<CertificationResultTestProcedure> testProcedures) {
		this.testProcedures = testProcedures;
	}

	public CertificationResult(CertificationResultDetailsDTO certResult) {
		this();
		this.setNumber(certResult.getNumber());
		this.setSuccess(certResult.getSuccess());
		this.setTitle(certResult.getTitle());
		if(certResult.getGap() == null) {
			this.gap = false;
		} else {
			this.gap = certResult.getGap().booleanValue();
		}
		
		if(certResult.getSed() == null) {
			this.sed = false;
		} else {
			this.sed = certResult.getSed().booleanValue();
		}
		
		if(certResult.getG1Success() == null) {
			this.g1Success = false;
		} else {
			this.g1Success = certResult.getG1Success();
		}
		
		if(certResult.getG2Success() == null) {
			this.g2Success = false;
		} else {
			this.g2Success = certResult.getG2Success();
		}
		
		this.ucdProcessSelected = certResult.getUcdProcessSelected();
		this.ucdProcessDetails = certResult.getUcdProcessDetails();
	}
	
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean successful) {
		this.success = successful;
	}
	public List<CertificationResultAdditionalSoftware> getAdditionalSoftware() {
		return additionalSoftware;
	}
	public void setAdditionalSoftware(List<CertificationResultAdditionalSoftware> additionalSoftware) {
		this.additionalSoftware = additionalSoftware;
	}
	public boolean isGap() {
		return gap;
	}
	public void setGap(boolean gap) {
		this.gap = gap;
	}
	public boolean isSed() {
		return sed;
	}
	public void setSed(boolean sed) {
		this.sed = sed;
	}
	public boolean isG1Success() {
		return g1Success;
	}
	public void setG1Success(boolean g1Success) {
		this.g1Success = g1Success;
	}
	public boolean isG2Success() {
		return g2Success;
	}
	public void setG2Success(boolean g2Success) {
		this.g2Success = g2Success;
	}
	public String getUcdProcessSelected() {
		return ucdProcessSelected;
	}
	public void setUcdProcessSelected(String ucdProcessSelected) {
		this.ucdProcessSelected = ucdProcessSelected;
	}
	public String getUcdProcessDetails() {
		return ucdProcessDetails;
	}
	public void setUcdProcessDetails(String ucdProcessDetails) {
		this.ucdProcessDetails = ucdProcessDetails;
	}

	public List<CertificationResultTestTool> getTestToolsUsed() {
		return testToolsUsed;
	}

	public void setTestToolsUsed(List<CertificationResultTestTool> testToolsUsed) {
		this.testToolsUsed = testToolsUsed;
	}

	public List<CertificationResultTestStandard> getTestStandards() {
		return testStandards;
	}

	public void setTestStandards(List<CertificationResultTestStandard> testStandards) {
		this.testStandards = testStandards;
	}
	
	public List<CertificationResultTestData> getTestDataUsed() {
		return testDataUsed;
	}

	public void setTestDataUsed(List<CertificationResultTestData> testDataUsed) {
		this.testDataUsed = testDataUsed;
	}

	public List<CertificationResultTestFunctionality> getTestFunctionality() {
		return testFunctionality;
	}

	public void setTestFunctionality(List<CertificationResultTestFunctionality> testFunctionality) {
		this.testFunctionality = testFunctionality;
	}
}
