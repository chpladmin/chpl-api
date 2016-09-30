package gov.healthit.chpl.app;

import org.springframework.stereotype.Component;

/**
 * Represents the header for a table
 * @author dlucas
 *
 */
@Component("tableHeader")
public class TableHeader extends Table{
	public String headerName;
	private Integer headerWidth;
	private Class<?> dataType;
	
	public TableHeader(){
	}
	
	/**
	 * Creates a TableHeader object
	 * @param headerName - the name of the header (i.e. 'Date', 'Total Developers', etc)
	 * @param headerWidth - Width to make this header. Needs to be wide enough to fit the longest content for this column
	 * @param dataType - The datatype of this header to use for reflective methods (i.e. String.class)
	 */
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
	
	/**
	 * Uses the datatype of the TableHeader to determine whether to return a 's' or 'd' for string formatting
	 * @return
	 */
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
