package gov.healthit.chpl.app;

public class TableFormatting extends Table {
	private String htmlPreText;
	private String htmlPostText;
	
	public TableFormatting(){}
	
	public TableFormatting(String htmlPreText, String htmlPostText){
		setHtmlPreText(htmlPreText);
		setHtmlPostText(htmlPostText);
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
}
