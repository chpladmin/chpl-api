package gov.healthit.chpl.surveillance.report.builder;

import java.util.Set;

public class MultiLineWorksheetRecordUtil {

   public static String buildMultiLineString(Set<String> values) {
       String result = "";
       //build the string that goes in the cell
       for (String uniqueVal : values) {
           if (result.length() > 0) {
               result += ", \n";
           }
           result += uniqueVal;
       }
       return result;
   }
}
