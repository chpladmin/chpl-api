package gov.healthit.chpl.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component("table")
public class Table {
	//private Map<TableHeader, List<String>> headerWithCommaSeparatedOutputList = new LinkedHashMap();
	private List<TableHeader> tableHeaders = new ArrayList<TableHeader>();
	private TableOutline tableOutline = new TableOutline();
	//private String tableOutline;
	private List<String> formattedOutputRows = new ArrayList<String>();
	private StringBuilder tableBuilder = new StringBuilder();
	private String table;
	private TableFormatting tableFormatting = new TableFormatting();
	private List<Map<String, String>> tableRows = new LinkedList<Map<String, String>>();
	//private char outlineStartChar;
	//private char outlineMiddleChars;
	//Map<String, Integer> headerNamesWithColumnWidth = new LinkedHashMap<String, Integer>();
	//List<String> commaSeparatedOutputList = new ArrayList<String>();
	private String tableHeader;
	
	public Table(){}
	
	public Table(List<TableHeader> tableHeaders, TableOutline tableOutline, ){
		
	}
	
	public Table(List<String> commaSeparatedOutputList, List<TableHeader> tableHeaders, TableOutline tableOutline){
		setTableHeaders(tableHeaders);
//		setTableHeaders(tableHeaders);
//		setCommaSeparatedOutputList(commaSeparatedOutputList);
//		setHeaderNamesWithColumnWidth(headerNamesWithColumnWidth);
//		setOutlineStartChar(outlineStartChar);
//		setOutlineMiddleChars(outlineMiddleChars);
//		setTableOutline(generateTableOutline());
//		setTableHeader(generateTableHeader());
//		setFormattedOutputRows(generateFormattedOutputRows());
//		setTable(generateTable());
	}
	
	public List<TableHeader> getTableHeaders() {
		return tableHeaders;
	}

	public void setTableHeaders(List<TableHeader> tableHeaders) {
		this.tableHeaders = tableHeaders;
	}
	
	public char getOutlineStartChar() {
		return outlineStartChar;
	}

	public void setOutlineStartChar(char outlineStartChar) {
		this.outlineStartChar = outlineStartChar;
	}

	public char getOutlineMiddleChars() {
		return outlineMiddleChars;
	}

	public void setOutlineMiddleChars(char outlineMiddleChars) {
		this.outlineMiddleChars = outlineMiddleChars;
	}
	
	public List<String> getCommaSeparatedOutputList() {
		return commaSeparatedOutputList;
	}

	public void setCommaSeparatedOutputList(List<String> commaSeparatedOutputList) {
		this.commaSeparatedOutputList = commaSeparatedOutputList;
	}
	
	public Map<String, Integer> getHeaderNamesWithColumnWidth(){
		return this.headerNamesWithColumnWidth;
	}
	
	public void setHeaderNamesWithColumnWidth(Map<String, Integer> headerNamesWithColumnWidth){
		this.headerNamesWithColumnWidth = headerNamesWithColumnWidth;
	}
	
	public String getTable(){
		return this.tableBuilder.toString();
	}
	
	public void setTable(String table){
		this.table = table;
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
	
	public String generateTableHeader(){
		StringBuilder headerLine = new StringBuilder();
		 String htmlPreText = "<pre><br>";
		 String htmlPostText = "</br></pre>";
		 headerLine.append(htmlPreText);
		 
		 for(Map.Entry<String, Integer> entry : headerNamesWithColumnWidth.entrySet()){
			 String header = entry.getKey();
			 headerLine.append("|" + header);
		 }
		
		headerLine.append("|");
		headerLine.append(htmlPostText);

		 return headerLine.toString();
		
	}
	
	public String generateTable(){
		tableBuilder.append(tableOutline);
		tableBuilder.append(tableHeader);
		tableBuilder.append(tableOutline);
		for(String row : formattedOutputRows){
			tableBuilder.append(row);
			tableBuilder.append(tableOutline);
		}
		
		table = tableBuilder.toString();
		
		return table;
	}
	
	
	
	public String generateTableOutline(){
		StringBuilder outline = new StringBuilder();
		String htmlPreText = "<pre><br>";
		String htmlPostText = "</br></pre>";
		outline.append(htmlPreText);
		
		for(Map.Entry<String, Integer> entry : headerNamesWithColumnWidth.entrySet()){
			 Integer width = entry.getValue();
			 outline.append(createOutline(width));
		 }
		
		 outline.append(htmlPostText);

		 return outline.toString();
	}
	
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

	public TableFormatting getTableFormatting() {
		return tableFormatting;
	}

	public void setTableFormatting(TableFormatting tableFormatting) {
		this.tableFormatting = tableFormatting;
	}
	
}
