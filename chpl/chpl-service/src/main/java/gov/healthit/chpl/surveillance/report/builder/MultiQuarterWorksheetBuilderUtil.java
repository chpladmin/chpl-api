package gov.healthit.chpl.surveillance.report.builder;

import java.util.ArrayList;
import java.util.Map;

public class MultiQuarterWorksheetBuilderUtil {

    /** Takes a map where the key is a user-entered string
    * and the value is an array of quarter names that the string applies to.
    * @param valueMap
    * @return a nicely formatted string like:
    * Q1: some value,
    * Q2,Q3: some other value
    * Q4: the last value
    */
   public static String buildStringFromMap(final Map<String, ArrayList<String>> valueMap) {
       String result = "";
     //build the string that goes in the cell
       for (String uniqueVal : valueMap.keySet()) {
           if (result.length() > 0) {
               result += "\n";
           }
           ArrayList<String> quarterNameList = valueMap.get(uniqueVal);
           String quarterNameStr = "";
           for (String quarterName : quarterNameList) {
               if (quarterNameStr.length() > 0) {
                   quarterNameStr += ", ";
               }
               quarterNameStr += quarterName;
           }
           result += quarterNameStr + ": " + uniqueVal;
       }
       return result;
   }
}
