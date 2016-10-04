package gov.healthit.chpl.app;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * TableOutline object that can be used to provide an outline between rows of data in a table
 * @author dlucas
 *
 */
@Component("tableOutline")
public class TableOutline extends Table {
	private char outlineStartChar;
	private char outlineMiddleChars;
	private String outline;
	
	public TableOutline(){
	}
	
	/**
	 * Creates a TableOutline object
	 * @param outlineStartChar - the first character for the outline (displayed at the start of each column)
	 * @param outlineMiddleChars - the characters displayed between the start and end of each column
	 * @param tableHeaders - Used to determine the width of each column of the tableOutline
	 * @param tableFormatting - Used to obtain the html formatting for the outline
	 */
	public TableOutline(char outlineStartChar, char outlineMiddleChars, List<TableHeader> tableHeaders, TableFormatting tableFormatting){
		setOutlineStartChar(outlineStartChar);
		setOutlineMiddleChars(outlineMiddleChars);
		setOutline(buildOutline(tableHeaders, tableFormatting));
	}
	
	/**
	 * Create an outline and return the result as a string
	 * @param tableHeaders - Used to determine the width of each column of the tableOutline
	 * @param tableFormatting - Used to obtain the html formatting for the outline
	 * @return
	 */
	private String buildOutline(List<TableHeader> tableHeaders, TableFormatting tableFormatting){
		StringBuilder outlineBuilder = new StringBuilder();
		outlineBuilder.append(tableFormatting.getHtmlPreText());
		for(TableHeader tableHeader : tableHeaders){
			outlineBuilder.append(createOutline(tableHeader.getHeaderWidth()));
		}
		outlineBuilder.append(outlineStartChar + tableFormatting.getHtmlPostText());
		return outlineBuilder.toString();
	}
	
	/**
	 * Creates a column portion of the given line of the outline using the length of the column
	 * @param length
	 * @return
	 */
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

	public String getOutline() {
		return outline;
	}

	public void setOutline(String outline) {
		this.outline = outline;
	}
}
