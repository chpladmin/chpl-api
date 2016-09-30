package gov.healthit.chpl.app;

import java.util.List;

import org.springframework.stereotype.Component;

@Component("tableOutline")
public class TableOutline extends Table {
	private char outlineStartChar;
	private char outlineMiddleChars;
	private String outline;
	
	public TableOutline(){
	}
	
	public TableOutline(char outlineStartChar, char outlineMiddleChars, List<TableHeader> tableHeaders, TableFormatting tableFormatting){
		setOutlineStartChar(outlineStartChar);
		setOutlineMiddleChars(outlineMiddleChars);
		setOutline(buildOutline(tableHeaders, tableFormatting));
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
	
	private String buildOutline(List<TableHeader> tableHeaders, TableFormatting tableFormatting){
		StringBuilder outlineBuilder = new StringBuilder();
		outlineBuilder.append(tableFormatting.getHtmlPreText());
		for(TableHeader tableHeader : tableHeaders){
			outlineBuilder.append(createOutline(tableHeader.getHeaderWidth(), tableFormatting));
		}
		outlineBuilder.append(outlineStartChar + tableFormatting.getHtmlPostText());
		return outlineBuilder.toString();
	}
	
	private StringBuilder createOutline(Integer length, TableFormatting tableFormatting){
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

	public String getOutline() {
		return outline;
	}

	public void setOutline(String outline) {
		this.outline = outline;
	}
}
