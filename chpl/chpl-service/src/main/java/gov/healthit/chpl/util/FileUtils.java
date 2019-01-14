package gov.healthit.chpl.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.exception.ValidationException;

/**
 * Utility methods for finding and reading files.
 * @author kekey
 *
 */
@Component
public final class FileUtils {
    /**
     * Global buffer size.
     */
    public static final int BUFFER_SIZE = 1024;

    private static final String DOWNLOAD_FOLDER_PROPERTY_NAME = "downloadFolderPath";
    private static final Logger LOGGER = LogManager.getLogger(FileUtils.class);

    @Autowired private Environment env;
    @Autowired private ErrorMessageUtil msgUtil;

    private FileUtils() {}

    /**
     * Given a file returns the contents as a string.
     * @param file the input file
     * @return the contents of that file as a string
     * @throws ValidationException if the file could not be read
     */
    public String readFileAsString(final MultipartFile file) throws ValidationException {
        //read the file into a string
        StringBuffer data = new StringBuffer();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
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

    /**
     * Reads the contents of a file in the download folder into a byte array.
     * @param filename the name of the file in the download folder
     * @return the contents of the file as a byte array
     * @throws IOException if the file does not exist or cannot be read
     */
    public byte[] readDownloadFile(final String filename) throws IOException {
        Path path = Paths.get(env.getProperty(DOWNLOAD_FOLDER_PROPERTY_NAME), filename);
        byte[] data = Files.readAllBytes(path);
        return data;
    }

    /**
     * Find a file by name in the download directory.
     * @param filename the name of the file to find
     * @return the file in the download directory
     * @throws IOException if no file with the provided name is
     * found under downloadFolderPath or if the file exists but cannot be read.
     */
    public File getDownloadFile(final String filename) throws IOException {
        String downloadFolderPath = env.getProperty(DOWNLOAD_FOLDER_PROPERTY_NAME);
        File downloadFolder = new File(downloadFolderPath);
        if (!downloadFolder.exists() || !downloadFolder.canRead()) {
            throw new IOException(msgUtil.getMessage("resources.noReadPermission", downloadFolderPath));
        }

        File downloadFile = new File(downloadFolderPath + File.separator + filename);
        if (!downloadFile.exists() || !downloadFile.canRead()) {
            throw new IOException("Cannot read download file at " + downloadFolderPath
                    + File.separator + filename
                    + ". File does not exist or cannot be read.");
        }
        return downloadFile;
    }

    /**
     * There are many files with a similar name in the download folder because they are generated
     * daily with a timestamp appended to their name. Finds the most recently written
     * of the files in the download folder matching a specific pattern.
     * @param filenamePattern the pattern of filename to look for. Must work with matches()
     * @return the file
     * @throws IOException if the download folder does not exist or cannot be read
     */
    public File getNewestFileMatchingName(final String filenamePattern) throws IOException {
        String downloadFolderPath = env.getProperty(DOWNLOAD_FOLDER_PROPERTY_NAME);
        File downloadFolder = new File(downloadFolderPath);
        if (!downloadFolder.exists() || !downloadFolder.canRead()) {
            throw new IOException(msgUtil.getMessage("resources.noReadPermission", downloadFolderPath));
        }

        File[] children = downloadFolder.listFiles();
        File newestFileWithFormat = null;
        for (int i = 0; i < children.length; i++) {
            if (children[i].getName().matches(filenamePattern)) {
                if (newestFileWithFormat == null) {
                    newestFileWithFormat = children[i];
                } else {
                    if (children[i].lastModified() > newestFileWithFormat.lastModified()) {
                        newestFileWithFormat = children[i];
                    }
                }
            }
        }
        return newestFileWithFormat;
    }

    /**
     * Writes the contents of a file out to the response stream.
     * @param file the file to write
     * @param contentType content type of response like text/plain, text/csv, etc
     * @param response the http response object
     * @throws IOException if the stream cannot be written
     */
    public void streamFileAsResponse(final File file, final String contentType, final HttpServletResponse response)
            throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file);
                OutputStream outStream = response.getOutputStream();) {

            // set content attributes for the response
            response.setContentType("text/csv");
            response.setContentLength((int) file.length());

            // set headers for the response
            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", file.getName());
            response.setHeader(headerKey, headerValue);

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;

            // write bytes read from the input stream into the output stream
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        }
    }
}
