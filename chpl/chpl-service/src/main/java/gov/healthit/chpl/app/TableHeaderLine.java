package gov.healthit.chpl.app;

import java.util.List;

import org.springframework.stereotype.Component;

@Component("tableHeaderLine")
public class TableHeaderLine {
	private String headerLine;
	
	public TableHeaderLine(){}
	
	public TableHeaderLine(List<TableHeader> tableHeaders, TableFormatting tableFormatting){
		setHeaderLine(generateTableHeader(tableHeaders, tableFormatting));
	}
	
	public String generateTableHeader(List<TableHeader> tableHeaders, TableFormatting tableFormatting){
		StringBuilder headerLine = new StringBuilder();
		
		 headerLine.append(tableFormatting.getHtmlPreText());

		 for(TableHeader tableHeader : tableHeaders){
			 String columnWidth = tableHeader.getHeaderWidth().toString();
			 String columnFormat =  "%-" + columnWidth + tableHeader.getTableHeaderTypeAsCharForFormatting();
			 headerLine.append(String.format(columnFormat, tableFormatting.getColumnSeparator() + tableHeader.getHeaderName()));
		 }
		
		headerLine.append(tableFormatting.getColumnSeparator());
		headerLine.append(tableFormatting.getHtmlPostText());

		 return headerLine.toString();
		
	}

	public String getHeaderLine() {
		return headerLine;
	}

	public void setHeaderLine(String headerLine) {
		this.headerLine = headerLine;
	}

}
