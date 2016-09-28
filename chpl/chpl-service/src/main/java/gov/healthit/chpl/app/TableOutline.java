package gov.healthit.chpl.app;

public class TableOutline extends Table {
	private char outlineStartChar;
	private char outlineMiddleChars;
	
	public TableOutline(){
	}
	
	public TableOutline(char outlineStartChar, char outlineMiddleChars){
		setOutlineStartChar(outlineStartChar);
		setOutlineMiddleChars(outlineMiddleChars);
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
}
