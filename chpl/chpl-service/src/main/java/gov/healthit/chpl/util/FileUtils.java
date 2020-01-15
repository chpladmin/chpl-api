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

@Component
public final class FileUtils {
    public static final int BUFFER_SIZE = 1024;

    private static final String DOWNLOAD_FOLDER_PROPERTY_NAME = "downloadFolderPath";
    private static final Logger LOGGER = LogManager.getLogger(FileUtils.class);

    private Environment env;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public FileUtils(Environment env, ErrorMessageUtil msgUtil) {
        this.env = env;
        this.msgUtil = msgUtil;
    }

    public String readFileAsString(final MultipartFile file) throws ValidationException {
        // read the file into a string
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

    public File getDownloadFolder() throws IOException {
        String downloadFolderPath = env.getProperty(DOWNLOAD_FOLDER_PROPERTY_NAME);
        File downloadFolder = new File(downloadFolderPath);
        if (!downloadFolder.exists() || !downloadFolder.canRead()) {
            throw new IOException(msgUtil.getMessage("resources.noReadPermission", downloadFolderPath));
        }
        return downloadFolder;
    }

    public File createDownloadFile(final String filename) throws IOException {
        File downloadFolder = getDownloadFolder();
        String absoluteFilename = downloadFolder.getAbsolutePath()
                + File.separator + filename;
        File newDownloadFile = new File(absoluteFilename);
        if (newDownloadFile.exists()) {
            if (!newDownloadFile.delete()) {
                throw new IOException(msgUtil.getMessage("util.file.cannotDelete", absoluteFilename));
            }
        }
        if (!newDownloadFile.createNewFile()) {
            throw new IOException(msgUtil.getMessage("util.file.cannotCreate", absoluteFilename));
        }
        return newDownloadFile;
    }

    public byte[] readDownloadFile(final String filename) throws IOException {
        Path path = Paths.get(env.getProperty(DOWNLOAD_FOLDER_PROPERTY_NAME), filename);
        byte[] data = Files.readAllBytes(path);
        return data;
    }

    public File getDownloadFile(final String filename) throws IOException {
        File downloadFolder = getDownloadFolder();
        File downloadFile = new File(downloadFolder.getAbsolutePath() + File.separator + filename);
        if (!downloadFile.exists() || !downloadFile.canRead()) {
            throw new IOException("Cannot read download file at " + downloadFolder.getAbsolutePath()
                    + File.separator + filename
                    + ". File does not exist or cannot be read.");
        }
        return downloadFile;
    }

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

    public void streamFileAsResponse(File file, String contentType, HttpServletResponse response, String filenameToStreamAs)
            throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file);
                OutputStream outStream = response.getOutputStream();) {

            // set content attributes for the response
            response.setContentType(contentType);
            response.setContentLength((int) file.length());

            // set headers for the response
            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", filenameToStreamAs);
            response.setHeader(headerKey, headerValue);

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;

            // write bytes read from the input stream into the output stream
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        }
    }

    public void streamFileAsResponse(File file, String contentType, HttpServletResponse response) throws IOException {
        streamFileAsResponse(file, contentType, response, file.getName());
    }
}
