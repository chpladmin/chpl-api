package gov.healthit.chpl;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Util {
	
    public static String md5(String input) {
        String md5 = null;
        if(null == input) {
        	return null;
        }
         
        try { 
        	//Create MessageDigest object for MD5
        	MessageDigest digest = MessageDigest.getInstance("MD5");
         
	        //Update input string in message digest
	        digest.update(input.getBytes(), 0, input.length());
 
	        //Converts message digest value in base 16 (hex) 
	        md5 = new BigInteger(1, digest.digest()).toString(16);
 
        } catch (NoSuchAlgorithmException e) {
	       	e.printStackTrace();
	    }
        return md5;
    }
    
    public static int myIndexOf(List<HashMap> benefit, Map find) {
	    int i = 0;
	    for (Map map : benefit) {
	        Map tmp = new HashMap(map);
	        tmp.keySet().retainAll(find.keySet());
	        if (tmp.equals(find)) {
	            return i;
	        }
	        i++;
	    }
	    return -1;
	}
	
	public static int getIndex(Set<? extends Object> set, Object value) {
		   int result = 0;
		   for (Object entry:set) {
		     if (entry.equals(value)) return result;
		     result++;
		   }
		   return -1;
		 }

}
