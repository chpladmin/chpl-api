package gov.healthit.chpl.app;

import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Creates a table with a string property
 * @author dlucas
 *
 */
@Component("table")
public class Table {
	private String table;
	
	public Table(){}
	
	/**
	 * Generates a table
	 * @param tableHeaderLine - object that contains the full header for the table
	 * @param tableOutline - object that contains the full outline for each row of the table
	 * @param tableRows - object that contains data for each row of the table
	 */
	public Table(TableHeaderLine tableHeaderLine, TableOutline tableOutline, TableRow tableRows){
		setTable(generateTable(tableHeaderLine, tableRows.getFormattedKeyValueTableRows(), tableOutline));
	}
	
	public String getTable(){
		return this.table;
	}
	
	public void setTable(String table){
		this.table = table;
	}
	
	/**
	 * Generates a table using a tableHeaderLine, a Map with keyValueTableRows, and a table outline
	 * @param tableHeaderLine - object that contains the full header for the table
	 * @param formattedKeyValueTableRows - Map of string objects for each row of the table
	 * @param tableOutline - object that contains the full outline for each row of the table
	 * @return
	 */
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
