package gov.healthit.chpl.app;

public class Table {
	private String columnOutline;
	private StringBuilder stringBuilder;
	
	public Table(){}
	
	public void setColumnOutline(String columnOutline){
		this.columnOutline = columnOutline;
	}
	
	public String getColumnOutline(){
		return this.columnOutline;
	}
	
	public void printTable(){
		System.out.println(stringBuilder);
	}
	
	public StringBuilder getTable(){
		return this.stringBuilder;
	}
	
	public StringBuilder generateTableHeader(){
		 String dateOutlineSize = "%20s";
		 String totalOutlineDevelopersSize = "%20s";
		 String totalOutlineProductsSize = "%20s";
		 String totalCPsOutlineSize = "%15s";
		 String total2014CPsOutlineSize = "%15s";
		 String total2015CPsOutlineSize = "%15s";
		 
		 String dateHeaderSize = "%22s";
		 String totalDevelopersHeaderSize = "%22s";
		 String totalProductsHeaderSize = "%22s";
		 String totalCPsHeaderSize = "%22s";
		 String total2014CPsHeaderSize = "%22s";
		 String total2015CPsHeaderSize = "%21s";
		 
		 stringBuilder.append(String.format
				 			 (dateOutlineSize + totalOutlineDevelopersSize + totalOutlineProductsSize + totalCPsOutlineSize + total2014CPsOutlineSize + total2015CPsOutlineSize + "\n", 
				 					 columnOutline, columnOutline, columnOutline, columnOutline, columnOutline, columnOutline));
		 stringBuilder.append("|" + String.format(dateHeaderSize + totalDevelopersHeaderSize + totalProductsHeaderSize + totalCPsHeaderSize + 
				 total2014CPsHeaderSize + total2015CPsHeaderSize + "\n", 
				 			"Date |", "Total Developers |", "Total Products |", "Total CPs |", "Total 2014 CPs |", "Total 2015 CPs|"));
		 stringBuilder.append(String.format
				 (dateOutlineSize + totalOutlineDevelopersSize + totalOutlineProductsSize + totalCPsOutlineSize + total2014CPsOutlineSize + total2015CPsOutlineSize + "\n", 
						 columnOutline, columnOutline, columnOutline, columnOutline, columnOutline, columnOutline));
		 return stringBuilder;
	}

	public StringBuilder generateTableDataRow(ActivitiesOutput activitiesOutput, TimePeriod timePeriod){
		 String dateOutput = timePeriod.getEndDate().toString().substring(0, 10);
		 String dateSize = "%20s";
		 String totalDevelopersSize = "%22s";
		 String totalProductsSize = "%22s";
		 String totalCPsSize = "%22s";
		 String total2014CPsSize = "%22s";
		 String total2015CPsSize = "%22s";
		 
		 stringBuilder.append(String.format
				 (dateSize + totalDevelopersSize + totalProductsSize + totalCPsSize + total2014CPsSize + total2015CPsSize + "\n", 
				 dateOutput, activitiesOutput.getTotalDevelopers(), activitiesOutput.getTotalProducts(), activitiesOutput.getTotalCPs(), 
				 activitiesOutput.getTotalCPs_2014(), activitiesOutput.getTotalCPs_2015()));
		 
		 stringBuilder.append(columnOutline + columnOutline + columnOutline + columnOutline + columnOutline + columnOutline + "\n");
		
		return stringBuilder;
	}
	
}
