package gov.healthit.chpl.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component("csv")
public class CSV {
	private File file;
	private PrintWriter pw;
	private StringBuilder sb;
	
	public CSV(){}
	
	/**
	 * Creates a CSV from a comma separated string and the File
	 * Note that if multiple rows are desired in the CSV, the string should have newline characters at the end of each line
	 * @param formattedStringToWriteToCSV
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public CSV(String formattedStringToWriteToCSV, File file) throws FileNotFoundException, IllegalArgumentException, IllegalAccessException{
		setFile(file);
		PrintWriter printWriter = new PrintWriter(file);
		setPrintWriter(printWriter);
		writeToCSV(formattedStringToWriteToCSV);
		closePrintWriter();
	}
	
	private void writeToCSV(String stringToWrite){
		pw.write(stringToWrite);
	}
	
	private void closePrintWriter(){
		pw.close();
	}
	
//	/**
//	 * Takes list of comma separated value strings and appends them to the CSV
//	 * @param strList
//	 * @throws FileNotFoundException
//	 * @throws IllegalArgumentException
//	 * @throws IllegalAccessException
//	 */
//	private void appendCommaSeparatedStringsToCSV(List<String> strList) throws FileNotFoundException, IllegalArgumentException, IllegalAccessException{	
//		int i = 0;
//		for(String str : strList){
//			if(i == 0){	
//				sb.append(str);
//			}
//			else{
//				sb.append("\n" + str);
//			}
//			i++;
//		}
//
//        writeToCSV(sb.toString());
//	}
	
	
	/**
	 * Gets a list of comma separated lines using a list of objects with a common field name for those objects. 
	 * Each line is an object, and for each object the provided field name is output as comma separated.
	 * @param List<T> list
	 * @param fieldName
	 * @return
	 */
	public static <T> List<String> getCommaSeparatedList(List<T> list, String fieldName){
		List<String> commaSeparatedList = new LinkedList<String>();
		int i = 0;
		for(Object obj : list){
			String fieldValue = ReflectiveHelper.get(obj, fieldName);
			if(i == 0){
				commaSeparatedList.add(fieldValue);
			}
			else{
				if(!fieldValue.isEmpty()){
					commaSeparatedList.add(fieldValue.toString());
				}
			}
			i++;	
		}
		
		return commaSeparatedList;
	}
	
	/**
	 * Gets a list of comma separated lines using a list of objects with a common field name for those objects. 
	 * Each line is an object, and for each object the provided field name is output as comma separated.
	 * A newline character is added to the end of each line.
	 * @param List<T> list
	 * @param fieldName
	 * @return
	 */
	public static <T> List<String> getCommaSeparatedList(List<T> list, List<String> fieldNames){
		List<String> commaSeparatedList = new LinkedList<String>();
		
		for(Object obj : list){
			int i = 0;
			for(String fieldName : fieldNames){
				String fieldValue = ReflectiveHelper.get(obj, fieldName);
				if(i == 0){
					commaSeparatedList.add(fieldValue);
				}
				else{
					commaSeparatedList.add(',' + fieldValue.toString());
				}
				i++;	
			}
			commaSeparatedList.add("\n");
		}
		
		return commaSeparatedList;
	}
	
	/**
	 * Gets a list of comma separated lines using a list of objects with a common field name for those objects. 
	 * Each line is an object, and for each object the provided field name is output as comma separated.
	 * A newline character is added to the end of each line.
	 * @param List<T> list
	 * @param fieldName
	 * @return
	 */
	public static <T> List<String> getCommaSeparatedListWithFields(List<T> list, List<Field> fields){
		List<String> commaSeparatedList = new LinkedList<String>();
		int objCounter = 0;
		for(Object obj : list){
			int i = 0;
			for(Field field : fields){
				String fieldValue = ReflectiveHelper.get(obj, field.getName()).toString();
				if(i == 0 && objCounter > 0){
					commaSeparatedList.add("\n" + fieldValue);
				}
				else{
					commaSeparatedList.add(fieldValue);
				}
				i++;
			}
			objCounter++;
		}
		
		return commaSeparatedList;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public PrintWriter getPrintWriter() {
		return pw;
	}

	public void setPrintWriter(PrintWriter pw) {
		this.pw = pw;
	}

	public StringBuilder getSb() {
		return sb;
	}

	public void setSb(StringBuilder sb) {
		this.sb = sb;
	}
}
