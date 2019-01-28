package gov.healthit.chpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public final class UploadFileUtils {
    private static final Logger LOGGER = LogManager.getLogger(UploadFileUtils.class);

    private static final String UPLOAD_2014 = "2014_V11_hasGAP.csv";
    private static final String UPLOAD_2015_V12 = "2015_Test_ICSA.csv";
    private static final String UPLOAD_2015_V12_TOO_LONG = "2015_Test_ICSA-Participant_TestTask_Too_Long.csv";

    private UploadFileUtils() {}

    public static MultipartFile getUploadFile(final String edition, final String version, final String id) {
        ClassLoader classLoader = UploadFileUtils.class.getClassLoader();
        File file = null;
        Path filePath = null;
        String name =  null;
        String originalFileName = null;
        if (edition.equals("2014")) {
            file = new File(classLoader.getResource(UPLOAD_2014).getFile());
            filePath = Paths.get(file.getPath());
            name = UPLOAD_2014;
            originalFileName = UPLOAD_2014;
        } else if (id == null){
            String resource = UPLOAD_2015_V12;
            file = new File(classLoader.getResource(resource).getFile());
            filePath = Paths.get(file.getPath());
            name = resource;
            originalFileName = resource;
        } else if (id.equals("upLoadCertifiedProduct2015LongTestParticipant")){
            String resource = UPLOAD_2015_V12_TOO_LONG;
            file = new File(classLoader.getResource(resource).getFile());
            filePath = Paths.get(file.getPath());
            name = resource;
            originalFileName = resource;
        }
        String contentType = "text/csv";
        byte[] content = null;
        try {
            content = Files.readAllBytes(filePath);
        } catch (final IOException e) {
            LOGGER.error("Could not read file " + filePath + ". " + e.getMessage(), e);
        }
        MultipartFile result = new MockMultipartFile(name, originalFileName, contentType, content);
        return result;
    }
}
