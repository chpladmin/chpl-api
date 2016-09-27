package gov.healthit.chpl.app;

import java.util.ArrayList;
import java.util.List;

public class Table {
	private String tableOutline;
	private List<String> formattedOutputRows = new ArrayList<String>();
	private StringBuilder tableBuilder = new StringBuilder();
	private char outlineStartChar;
	private char outlineMiddleChars;
	List<String> headerNames = new ArrayList<String>();
	List<String> commaSeparatedOutputList = new ArrayList<String>();
	private String tableHeader;
	
	public Table(){}
	
	public Table(List<String> commaSeparatedOutputList, List<String> headerNames, char outlineStartChar, char outlineMiddleChars){
		this.commaSeparatedOutputList = commaSeparatedOutputList;
		this.headerNames = headerNames;
		this.outlineStartChar = outlineStartChar;
		this.outlineMiddleChars = outlineMiddleChars;
		setTableOutline(generateTableOutline());
		for(String header : headerNames){
			headerNames.add(header);
		}
		setTableHeader(generateTableHeader());
		setFormattedOutputRows(generateFormattedOutputRows());
		
//		for(Object obj : outputList){
//			tableBuilder.append(tableOutline);
//		}
	}
	
	public String getTable(){
		return this.tableBuilder.toString();
	}
	
	public void setTableOutline(String tableOutline){
		this.tableOutline = tableOutline;
	}
	
	public void setTableHeader(String tableHeader){
		this.tableHeader = tableHeader;
	}
	
	public void setFormattedOutputRows(List<String> formattedOutputRows){
		this.formattedOutputRows = formattedOutputRows;
	}
	
	public List<String> generateFormattedOutputRows(){
		String htmlPreText = "<pre><br>";
		String htmlPostText = "</br></pre>";
		List<String> fmtOutputRows = new ArrayList<String>();
		for(String row : commaSeparatedOutputList){
			StringBuilder outputRow = new StringBuilder();
			int i = 0;
			for(String field : row.split(",")){
				int headerLength = headerNames.get(i).length();
				outputRow.append(String.format("|%" + headerLength + "d" + htmlPostText, field));
			}
			outputRow.append("|");
			fmtOutputRows.add(htmlPreText + outputRow + htmlPostText);
		}
		
		return fmtOutputRows;
	}
	
	public String generateTableHeader(){
		StringBuilder headerLine = new StringBuilder();
		 String htmlPreText = "<pre><br>";
		 String htmlPostText = "</br></pre>";
		 headerLine.append(htmlPreText);
		
		for(String header : headerNames){
			headerLine.append("|" + header);
		}
		headerLine.append("|");
		headerLine.append(htmlPostText);

		 return headerLine.toString();
		
	}
	
	public String generateTableOutline(){
		StringBuilder outline = new StringBuilder();
		String htmlPreText = "<pre><br>";
		String htmlPostText = "</br></pre>";
		outline.append(htmlPreText);
		
		for(String header : headerNames){
			// Create top outline for this column header
			outline.append(createOutline(header.length() + 1));
		}
		 outline.append(htmlPostText);

		 return outline.toString();
	}

//	public String generateTableDataRow(ActivitiesOutput activitiesOutput, TimePeriod timePeriod){
//		 String dateOutput = timePeriod.getEndDate().toString().substring(0, 10);
//		 String dateSize = "%20s";
//		 String totalDevelopersSize = "%22s";
//		 String totalProductsSize = "%22s";
//		 String totalCPsSize = "%22s";
//		 String total2014CPsSize = "%22s";
//		 String total2015CPsSize = "%22s";
//		 
//		 tableBuilder.append(String.format
//				 ("<pre><br>" + dateSize + totalDevelopersSize + totalProductsSize + totalCPsSize + total2014CPsSize + total2015CPsSize + "</br></pre>\n", 
//				 dateOutput, activitiesOutput.getTotalDevelopers(), activitiesOutput.getTotalProducts(), activitiesOutput.getTotalCPs(), 
//				 activitiesOutput.getTotalCPs_2014(), activitiesOutput.getTotalCPs_2015()));
//		 
//		 //tableBuilder.append("<pre><br>" + columnOutline + columnOutline + columnOutline + columnOutline + columnOutline + columnOutline + "</br></pre>\n");
//		
//		return tableBuilder.toString();
//	}
	
	private StringBuilder createOutline(Integer length){
		StringBuilder outline = new StringBuilder();
		
		for(int i = 0; i < length; i++){
			if(i == 0){
				outline.append(outlineStartChar);
			}
			else{
				outline.append(outlineMiddleChars);
			}
		}
		return outline;
	}
	
}
