package gov.healthit.chpl.app.chartdata;

public class ChartData {
	//private ChartDataApplicationEnvironment appEnvironment;
    
	public static void main(String[] args) {
		ChartDataApplicationEnvironment appEnvironment;
		try {
			appEnvironment = new ChartDataApplicationEnvironment();
			SedParticipantsStatisticCount sedParticipantsStatisticCount = new SedParticipantsStatisticCount();
			sedParticipantsStatisticCount.run(appEnvironment);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
		
}
