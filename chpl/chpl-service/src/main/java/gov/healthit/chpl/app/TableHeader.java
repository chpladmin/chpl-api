package gov.healthit.chpl.app;

import org.springframework.stereotype.Component;

@Component("tableHeader")
public class TableHeader extends Table{
	private String headerName;
	private Integer headerWidth;
	private Class<?> dataType;
	
	public TableHeader(){
	}
	
	public TableHeader(String headerName, Integer headerWidth, Class<?> dataType){
		setHeaderName(headerName);
		setHeaderWidth(headerWidth);
		setDataType(dataType);
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

	public Object getDataType() {
		return dataType;
	}

	public void setDataType(Class<?> dataType) {
		this.dataType = dataType;
	}
	
	public char getTableHeaderTypeAsCharForFormatting(){
		char fmtChar;
		
		if(dataType.isInstance(String.class)){
			fmtChar = 's';
		}
		else if(dataType.isInstance(Integer.class)){
			fmtChar = 'd';
		}
		else{
			fmtChar = 's';
		}
		return fmtChar;
	}

}
