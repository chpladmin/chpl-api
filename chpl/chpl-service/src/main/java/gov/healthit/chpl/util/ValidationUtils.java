package gov.healthit.chpl.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;


public class ValidationUtils {
    private static final Logger LOGGER = LogManager.getLogger(ValidationUtils.class);

    public static boolean hasNewline(String input) {
        //check both windows and unix line separator chars
        if(input == null || StringUtils.isEmpty(input)) {
            return false;
        }
        return input.contains("\n") || input.contains("\r\n");
    }
    
    public static boolean isValidUtf8(String input) {
        return isValidUtf8(Charset.defaultCharset(), input);
    }
    
    public static boolean isValidUtf8(Charset inputCharset, String input) {
        if(input == null || StringUtils.isEmpty(input)) {
            return true;
        }
        
        if (inputCharset.name().equals(StandardCharsets.UTF_8.name())) {
            LOGGER.debug("Looking for UTF-8 Replacement Character in " + input);
            return !hasUtf8ReplacementCharacter(input);
        } else {
            LOGGER.debug("Looking for non UTF-8 character in " + input);
            return hasNonUtf8Character(input.getBytes());
        }
    }

    /**
     * This method could be called if the encoding in which input is received IS
     * UTF-8.
     * 
     * @param input
     * @return
     */
    public static boolean hasUtf8ReplacementCharacter(String input) {
        if (input.contains("\uFFFD")) {
            return true;
        }
        return false;
    }

    /**
     * This method could be called if the encoding in which input is received is
     * NOT already UTF-8
     * 
     * @param input
     * @return
     */
    public static boolean hasNonUtf8Character(byte[] input) {
        int i = 0;
        // Check for BOM
        if (input.length >= 3 && (input[0] & 0xFF) == 0xEF && (input[1] & 0xFF) == 0xBB & (input[2] & 0xFF) == 0xBF) {
            i = 3;
        }

        int end;
        for (int j = input.length; i < j; ++i) {
            int octet = input[i];
            if ((octet & 0x80) == 0) {
                continue; // ASCII
            }

            // Check for UTF-8 leading byte
            if ((octet & 0xE0) == 0xC0) {
                end = i + 1;
            } else if ((octet & 0xF0) == 0xE0) {
                end = i + 2;
            } else if ((octet & 0xF8) == 0xF0) {
                end = i + 3;
            } else {
                // Java only supports BMP so 3 is max
                return false;
            }

            while (i < end) {
                i++;
                octet = input[i];
                if ((octet & 0xC0) != 0x80) {
                    // Not a valid trailing byte
                    return false;
                }
            }
        }
        return true;
    }
}
