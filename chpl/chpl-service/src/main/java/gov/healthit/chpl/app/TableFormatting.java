package gov.healthit.chpl.app;

import org.springframework.stereotype.Component;

/**
 * Contains properties for a table's formatting
 * @author dlucas
 *
 */
@Component("tableFormatting")
public class TableFormatting extends Table {
	private String htmlPreText;
	private String htmlPostText;
	private char columnSeparator;
	private char fieldDelimiter;
	private Justification justification;
	
	public TableFormatting(){}
	
	/**
	 * Creates a TableFormatting object with the given properties
	 * @param htmlPreText - inserted before each row of the table
	 * @param htmlPostText - inserted after each row of the table
	 * @param columnSeparator - inserted before each field for each row of the table (i.e. |field1|field2)
	 * @param fieldDelimiter - The character that delimits each field of the table (i.e. comma separated)
	 * @param justification - determines the justification of the output of the table (i.e. left or right justified)
	 */
	public TableFormatting(String htmlPreText, String htmlPostText, char columnSeparator, char fieldDelimiter, Justification justification){
		setHtmlPreText(htmlPreText);
		setHtmlPostText(htmlPostText);
		setColumnSeparator(columnSeparator);
		setFieldDelimiter(fieldDelimiter);
		setJustification(justification);
	}

	public String getHtmlPreText() {
		return htmlPreText;
	}

	public void setHtmlPreText(String htmlPreText) {
		this.htmlPreText = htmlPreText;
	}

	public String getHtmlPostText() {
		return htmlPostText;
	}

	public void setHtmlPostText(String htmlPostText) {
		this.htmlPostText = htmlPostText;
	}

	public char getColumnSeparator() {
		return columnSeparator;
	}

	public void setColumnSeparator(char columnSeparator) {
		this.columnSeparator = columnSeparator;
	}

	public char getFieldDelimiter() {
		return fieldDelimiter;
	}

	public void setFieldDelimiter(char fieldDelimiter) {
		this.fieldDelimiter = fieldDelimiter;
	}

	public Justification getJustification() {
		return justification;
	}

	public void setJustification(Justification justification) {
		this.justification = justification;
	}
}
