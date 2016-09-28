package gov.healthit.chpl.app;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component("table")
public class Table {
	private String table;
	
	public Table(){}
	
	public Table(TableHeaderLine tableHeaderLine, TableOutline tableOutline, TableRow tableRows){
		setTable(generateTable(tableHeaderLine, tableRows.getFormattedKeyValueTableRows(), tableOutline));
	}
	
	public String getTable(){
		return this.table;
	}
	
	public void setTable(String table){
		this.table = table;
	}
	
	public String generateTable(TableHeaderLine tableHeaderLine, Map<String, String> formattedKeyValueTableRows, TableOutline tableOutline){
		StringBuilder tableBuilder = new StringBuilder();
		tableBuilder.append(tableOutline.getOutline());
		tableBuilder.append(tableHeaderLine.getHeaderLine());
		tableBuilder.append(tableOutline.getOutline());
		
		for(Map.Entry<String, String> entry : formattedKeyValueTableRows.entrySet()){
			tableBuilder.append(entry.getValue());
			tableBuilder.append(tableOutline.getOutline());
		}
		
		return tableBuilder.toString();
	}
	
}
