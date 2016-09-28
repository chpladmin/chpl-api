package gov.healthit.chpl.app;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component("tableRow")
public class TableRow extends Table {
	private Map<String, String> keyValueTableRows = new LinkedHashMap<String, String>();
	private Map<String, String> formattedKeyValueTableRows = new LinkedHashMap<String, String>();
	
	public TableRow(){}
	
	public TableRow(Map<String, String> keyValueTableRows, TableFormatting tableFormatting, List<TableHeader> tableHeaders){
		setKeyValueTableRows(keyValueTableRows);
		setFormattedKeyValueTableRows(generateFormattedOutputRows(tableFormatting, tableHeaders));
	}
	
	public Map<String, String> generateFormattedOutputRows(TableFormatting tableFormatting, List<TableHeader> tableHeaders){
		Map<String, String> fmtOutputRows = new LinkedHashMap<String, String>();
		
		for(Map.Entry<String, String> entry : keyValueTableRows.entrySet()){
			StringBuilder outputRow = new StringBuilder();
			int i = 0;
			// add key to outputRow
			Integer keyWidth = tableHeaders.get(i).getHeaderWidth();
			String keyWidthString = keyWidth.toString();
			String keyColumnFormat = "%-" + keyWidthString + tableHeaders.get(i).getTableHeaderTypeAsCharForFormatting();
			outputRow.append(String.format(keyColumnFormat, tableFormatting.getColumnSeparator() + entry.getKey()));
			
			for(String field : entry.getValue().split(String.valueOf(tableFormatting.getFieldDelimiter()))){
				++i;
				// add each field in Map to outputRow
				Integer columnWidth = tableHeaders.get(i).getHeaderWidth();
				String columnWidthString = columnWidth.toString();
				String columnFormat =  "%-" + columnWidthString + tableHeaders.get(i).getTableHeaderTypeAsCharForFormatting();
				outputRow.append(String.format(columnFormat, tableFormatting.getColumnSeparator() + field));
			}
			outputRow.append(tableFormatting.getColumnSeparator());
			fmtOutputRows.put(entry.getKey(), tableFormatting.getHtmlPreText() + outputRow + tableFormatting.getHtmlPostText());
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
