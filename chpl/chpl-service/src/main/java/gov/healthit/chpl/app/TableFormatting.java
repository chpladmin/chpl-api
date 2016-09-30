package gov.healthit.chpl.app;

import org.springframework.stereotype.Component;

@Component("tableFormatting")
public class TableFormatting extends Table {
	private String htmlPreText;
	private String htmlPostText;
	private char columnSeparator;
	private char fieldDelimiter;
	private Justification justification;
	
	public TableFormatting(){}
	
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
