package gov.healthit.chpl.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.web.controller.exception.ValidationException;


public class FileUtils {
    private static final Logger LOGGER = LogManager.getLogger(FileUtils.class);

    public static String readFileAsString(MultipartFile file) throws ValidationException {
     // read the file into a string
        StringBuffer data = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (data.length() > 0) {
                    data.append(System.getProperty("line.separator"));
                }
                data.append(line);
            }
        } catch (final IOException ex) {
            String msg = "Could not read file: " + ex.getMessage();
            LOGGER.error(msg);
            throw new ValidationException(msg);
        }
        return data.toString();
    }
}
