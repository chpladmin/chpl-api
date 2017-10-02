package gov.healthit.chpl;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util {
	private static final Logger logger = LogManager.getLogger(Util.class);

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

    public static String coerceToCriterionNumberFormat(String input) {
		String formatRegex = "^\\d{3}\\.\\d{3}\\s{1}\\([a-z]{1}\\)(\\([0-9]{1,2}\\))?$";
		if(input.matches(formatRegex)) {
			logger.debug("\tMatches required format. Not changing input.");
			return input;
		}

		String adjustedInput = input.toLowerCase();
		adjustedInput = adjustedInput.trim();
		if(adjustedInput.matches(formatRegex)) {
			logger.debug("\tTrimmed space and made lower case: " + adjustedInput);
			return adjustedInput;
		}

		//check for the middle space
		int openParenIndex = adjustedInput.indexOf('(');
		if(openParenIndex > 0) {
			int currIndex = openParenIndex;
			boolean foundNonspaceChar = false;
			while(currIndex > 0 && !foundNonspaceChar) {
				currIndex--;
				String currChar = adjustedInput.charAt(currIndex) + "";
				if(currChar.matches("\\S")) {
					foundNonspaceChar = true;
				}
			}

			if(currIndex >= 0) {
				adjustedInput = adjustedInput.substring(0, currIndex+1) + " " + adjustedInput.substring(openParenIndex);
			}
		}

		if(adjustedInput.matches(formatRegex)) {
			logger.debug("\tAdjusted spaces in the middle of the criterion: " + adjustedInput);
			return adjustedInput;
		}

		return input;
	}
}
