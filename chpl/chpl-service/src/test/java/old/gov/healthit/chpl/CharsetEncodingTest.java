package old.gov.healthit.chpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.util.ValidationUtils;
import junit.framework.TestCase;

//
// https://en.wikipedia.org/wiki/Windows-1252
//

public class CharsetEncodingTest extends TestCase {
    private static final String INVALID_UTF8_FILENAME = "nonutf8text.txt";
    private static final String VALID_UTF8_FILENAME = "validutf8text.txt";

    @Test
    public void testDetectInvalidUtf8TextInSystemDefaultCharset() {
        System.out.println("DEFAULT CHARSET IS: " + Charset.defaultCharset().name());

        File file = new File(getClass().getClassLoader().getResource(INVALID_UTF8_FILENAME).getFile());
        BufferedReader reader = null;
        try {
            Path filePath = Paths.get(file.getPath());
            String name = INVALID_UTF8_FILENAME;
            String originalFileName = INVALID_UTF8_FILENAME;

            String contentType = "text/csv";
            byte[] content = null;
            try {
                content = Files.readAllBytes(filePath);
            } catch (final IOException e) {
            }
            MultipartFile multipartFile = new MockMultipartFile(name, originalFileName, contentType, content);

            System.out.println("Reading the file " + originalFileName + " as " + Charset.defaultCharset().name());
            // reads using the default Win 1252 charset which recognizes the
            // windows-specific characters
            reader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream()));
            StringBuffer readContentsBuf = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                readContentsBuf.append(line);
            }

            String readContents = readContentsBuf.toString();
            System.out.println("Read: " + readContents);

            boolean result = ValidationUtils.isValidUtf8(Charset.defaultCharset(), readContents);
            System.out.println("Valid UTF-8? " + result);
            assertFalse(result);
        } catch (IOException ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception ignore) {
            }
        }
    }

    @Test
    public void testDetectInvalidUtf8TextInUtf8Charset() {
        System.out.println("DEFAULT CHARSET IS: " + Charset.defaultCharset().name());

        File file = new File(getClass().getClassLoader().getResource(INVALID_UTF8_FILENAME).getFile());
        BufferedReader reader = null;
        try {
            Path filePath = Paths.get(file.getPath());
            String name = INVALID_UTF8_FILENAME;
            String originalFileName = INVALID_UTF8_FILENAME;

            String contentType = "text/csv";
            byte[] content = null;
            try {
                content = Files.readAllBytes(filePath);
            } catch (final IOException e) {
            }
            MultipartFile multipartFile = new MockMultipartFile(name, originalFileName, contentType, content);

            // reads using the UTF-8 charset which matches what the database
            // supports.
            // does not recognize windows-specific characters
            System.out.println("Reading the file " + originalFileName + " as UTF-8.");
            reader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream(), StandardCharsets.UTF_8));

            StringBuffer readContentsBuf = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                readContentsBuf.append(line);
            }

            String readContents = readContentsBuf.toString();
            System.out.println("Read: " + readContents);

            boolean result = ValidationUtils.isValidUtf8(StandardCharsets.UTF_8, readContents);
            System.out.println("Valid UTF-8? " + result);
            assertFalse(result);
            reader.close();
        } catch (IOException ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception ignore) {
            }
        }
    }

    @Test
    public void testDetectValidUtf8TextInSystemDefaultCharset() {
        System.out.println("DEFAULT CHARSET IS: " + Charset.defaultCharset().name());

        File file = new File(getClass().getClassLoader().getResource(VALID_UTF8_FILENAME).getFile());
        BufferedReader reader = null;
        try {
            Path filePath = Paths.get(file.getPath());
            String name = VALID_UTF8_FILENAME;
            String originalFileName = VALID_UTF8_FILENAME;

            String contentType = "text/csv";
            byte[] content = null;
            try {
                content = Files.readAllBytes(filePath);
            } catch (final IOException e) {
            }
            MultipartFile multipartFile = new MockMultipartFile(name, originalFileName, contentType, content);

            System.out.println("Reading the file " + originalFileName + " as " + Charset.defaultCharset().name());
            // reads using the default Win 1252 charset which recognizes the
            // windows-specific characters
            reader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream()));
            StringBuffer readContentsBuf = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                readContentsBuf.append(line);
            }

            String readContents = readContentsBuf.toString();
            System.out.println("Read: " + readContents);

            boolean result = ValidationUtils.isValidUtf8(Charset.defaultCharset(), readContents);
            System.out.println("Valid UTF-8? " + result);
            assertTrue(result);
        } catch (IOException ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception ignore) {
            }
        }
    }

    @Test
    public void testDetectValidUtf8TextInUtf8Charset() {
        System.out.println("DEFAULT CHARSET IS: " + Charset.defaultCharset().name());

        File file = new File(getClass().getClassLoader().getResource(VALID_UTF8_FILENAME).getFile());
        BufferedReader reader = null;
        try {
            Path filePath = Paths.get(file.getPath());
            String name = VALID_UTF8_FILENAME;
            String originalFileName = VALID_UTF8_FILENAME;

            String contentType = "text/csv";
            byte[] content = null;
            try {
                content = Files.readAllBytes(filePath);
            } catch (final IOException e) {
            }
            MultipartFile multipartFile = new MockMultipartFile(name, originalFileName, contentType, content);

            // reads using the UTF-8 charset which matches what the database
            // supports.
            // does not recognize windows-specific characters
            System.out.println("Reading the file " + originalFileName + " as UTF-8.");
            reader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream(), StandardCharsets.UTF_8));

            StringBuffer readContentsBuf = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                readContentsBuf.append(line);
            }

            String readContents = readContentsBuf.toString();
            System.out.println("Read: " + readContents);

            boolean result = ValidationUtils.isValidUtf8(StandardCharsets.UTF_8, readContents);
            System.out.println("Valid UTF-8? " + result);
            assertTrue(result);
            reader.close();
        } catch (IOException ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception ignore) {
            }
        }
    }
}
