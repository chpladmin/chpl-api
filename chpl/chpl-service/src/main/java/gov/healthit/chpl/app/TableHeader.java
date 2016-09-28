package gov.healthit.chpl.app;

public class TableHeader extends Table{
	private String headerName;
	private Integer headerWidth;
	
	public TableHeader(){
	}
	
	public TableHeader(String headerName, Integer headerWidth){
		setHeaderName(headerName);
		setHeaderWidth(headerWidth);
	}
	
	public String getHeaderName() {
		return headerName;
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	public Integer getHeaderWidth() {
		return headerWidth;
	}

	public void setHeaderWidth(Integer headerWidth) {
		this.headerWidth = headerWidth;
	}
}
