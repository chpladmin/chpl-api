package gov.healthit.chpl.app;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TableRow extends Table {
	private Map<String, String> keyValueTableRows = new LinkedHashMap<String, String>();
	private Map<String, String> formattedKeyValueTableRows = new LinkedHashMap<String, String>();
	private TableFormatting tableFormatting = new TableFormatting();
	
	public TableRow(){}
	
	public TableRow(Map<String, String> keyValueTableRows, TableFormatting tableFormatting){
		setKeyValueTableRows(keyValueTableRows);
		setFormattedKeyValueTableRows(generateFormattedOutputRows());
	}
	
	public Map<String, String> generateFormattedOutputRows(){
		String htmlPreText = "<pre><br>";
		String htmlPostText = "</br></pre>";
		List<String> fmtOutputRows = new ArrayList<String>();
		for(String row : commaSeparatedOutputList){
			StringBuilder outputRow = new StringBuilder();
			int i = 0;
			for(String field : row.split(",")){
				//headerNamesWithColumnWidth.entrySet().;
				//headerNamesWithColumnWidth.entrySet().
				//Integer headerLength = super.headerNames.get(i).length();
				String headerLengthString = headerLength.toString();
				String headerFormat = "|%" + headerLengthString + "s";
				outputRow.append(String.format(headerFormat, field));
				i++;
		}
			outputRow.append("|");
			fmtOutputRows.add(htmlPreText + outputRow + htmlPostText);
		}
		
		return fmtOutputRows;
	}

	public Map<String, String> getKeyValueTableRows() {
		return keyValueTableRows;
	}

	public void setKeyValueTableRows(Map<String, String> keyValueTableRows) {
		this.keyValueTableRows = keyValueTableRows;
	}

	public Map<String, String> getFormattedKeyValueTableRows() {
		return formattedKeyValueTableRows;
	}

	public void setFormattedKeyValueTableRows(Map<String, String> formattedKeyValueTableRows) {
		this.formattedKeyValueTableRows = formattedKeyValueTableRows;
	}
}
