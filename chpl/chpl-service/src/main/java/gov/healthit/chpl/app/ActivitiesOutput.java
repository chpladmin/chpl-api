package gov.healthit.chpl.app;

/** Description: Provides objects used in output of ParseActivities application
 * 
 * @author dlucas
 *
 */
public class ActivitiesOutput {
	private String date;
	private Integer totalDevelopers;
	private Integer totalProducts;
	private Integer totalCPs;
	private Integer totalCPs_2014;
	private Integer totalCPs_2015;
	private Integer totalNumSurvActivities;
	private Integer numOpenSurvActivities;
	private Integer numClosedSurvActivities;
	private Integer totalNumNonConformities;
	private Integer numOpenNonConformities;
	private Integer numClosedNonConformities;
	
	public ActivitiesOutput(){
	}
	
	public ActivitiesOutput(String date, Integer totalDevelopers, Integer totalProducts, Integer totalCPs, Integer totalCPs_2014, Integer totalCPs_2015,
			Integer totalNumSurvActivities, Integer numOpenSurvActivities, Integer numClosedSurvActivities, Integer totalNumNonConformities, Integer numOpenNonConformities,
			Integer numClosedNonConformities){
		this.date = date;
		this.totalDevelopers = totalDevelopers;
		this.totalProducts = totalProducts;
		this.totalCPs = totalCPs;
		this.totalCPs_2014 = totalCPs_2014;
		this.totalCPs_2015 = totalCPs_2015;
		this.totalNumSurvActivities = totalNumSurvActivities;
		this.numOpenSurvActivities = numOpenSurvActivities;
		this.numClosedSurvActivities = numClosedSurvActivities;
		this.totalNumNonConformities = totalNumNonConformities;
		this.numOpenNonConformities = numOpenNonConformities;
		this.numClosedNonConformities = numClosedNonConformities;
	}
	
	public String getDate(){
		return this.date;
	}
	
	public void setDate(String date){
		this.date = date;
	}
	
	public Integer getTotalDevelopers(){
		return this.totalDevelopers;
	}
	
	public void setTotalDevelopers(Integer totalDevelopers){
		this.totalDevelopers = totalDevelopers;
	}
	
	public Integer getTotalProducts(){
		return this.totalProducts;
	}
	
	public void setTotalProducts(Integer totalProducts){
		this.totalProducts = totalProducts;
	}
	
	public Integer getTotalCPs(){
		return this.totalCPs;
	}
	
	public void setTotalCPs(Integer totalCPs){
		this.totalCPs = totalCPs;
	}
	
	public Integer getTotalCPs_2014(){
		return this.totalCPs_2014;
	}
	
	public void setTotalCPs_2014(Integer totalCPs_2014){
		this.totalCPs_2014 = totalCPs_2014;
	}
		
	public Integer getTotalCPs_2015(){
		return this.totalCPs_2015;
	}
	
	public void setTotalCPs_2015(Integer totalCPs_2015){
		this.totalCPs_2015 = totalCPs_2015;
	}

	public Integer getTotalNumSurvActivities() {
		return totalNumSurvActivities;
	}

	public void setTotalNumSurvActivities(Integer totalNumSurvActivities) {
		this.totalNumSurvActivities = totalNumSurvActivities;
	}

	public Integer getNumOpenSurvActivities() {
		return numOpenSurvActivities;
	}

	public void setNumOpenSurvActivities(Integer numOpenSurvActivities) {
		this.numOpenSurvActivities = numOpenSurvActivities;
	}

	public Integer getNumClosedSurvActivities() {
		return numClosedSurvActivities;
	}

	public void setNumClosedSurvActivities(Integer numClosedSurvActivities) {
		this.numClosedSurvActivities = numClosedSurvActivities;
	}

	public Integer getTotalNumNonConformities() {
		return totalNumNonConformities;
	}

	public void setTotalNumNonConformities(Integer totalNumNonConformities) {
		this.totalNumNonConformities = totalNumNonConformities;
	}

	public Integer getNumOpenNonConformities() {
		return numOpenNonConformities;
	}

	public void setNumOpenNonConformities(Integer numOpenNonConformities) {
		this.numOpenNonConformities = numOpenNonConformities;
	}

	public Integer getNumClosedNonConformities() {
		return numClosedNonConformities;
	}

	public void setNumClosedNonConformities(Integer numClosedNonConformities) {
		this.numClosedNonConformities = numClosedNonConformities;
	}
	
}