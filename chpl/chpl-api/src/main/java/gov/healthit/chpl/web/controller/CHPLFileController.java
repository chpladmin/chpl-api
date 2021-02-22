package gov.healthit.chpl.web.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.dto.CHPLFileDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.manager.CHPLFileManager;
import gov.healthit.chpl.web.controller.annotation.CacheControl;
import gov.healthit.chpl.web.controller.annotation.CacheMaxAge;
import gov.healthit.chpl.web.controller.annotation.CachePolicy;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "files")
@RestController
@RequestMapping("/files")
@Loggable
public class CHPLFileController {
    private static final String APPLICATION_MS_EXCEL =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private CHPLFileManager chplFileManager;

    @Autowired
    public CHPLFileController(final CHPLFileManager chplFileManager) {
        this.chplFileManager = chplFileManager;
    }

    @ApiOperation(value = "Upload an API Documentation file",
            notes = "Uploads a new current API Documentation file.  Security Restrictions: ROLE_ADMIN or ROLE_ONC")
    @RequestMapping(value = "/api_documentation",
            method = RequestMethod.POST,
            produces = "application/json; charset=utf-8")
    public synchronized ResponseEntity<CHPLFileDTO> uploadApiDocumentation(
            final @RequestParam("file") MultipartFile file,
            final @RequestParam("file_update_date") Long date)
            throws EntityRetrievalException, EntityCreationException, ValidationException,
            MaxUploadSizeExceededException, IOException {

        if (file.isEmpty()) {
            throw new ValidationException("You cannot upload an empty file!");
        }

        CHPLFileDTO newFileDTO = new CHPLFileDTO();
        newFileDTO.setAssociatedDate(new Date(date));
        newFileDTO.setContentType(file.getContentType());
        newFileDTO.setFileName(file.getOriginalFilename());
        newFileDTO.setFileData(file.getBytes());

        CHPLFileDTO fileDTO = chplFileManager.addApiDocumentationFile(newFileDTO);

        return new ResponseEntity<CHPLFileDTO>(fileDTO, HttpStatus.OK);
    }

    @ApiOperation(value = "Retrieve an API Documentation file",
            notes = "Retrieves the current API Documentation file.")
    @RequestMapping(value = "/api_documentation", method = RequestMethod.GET, produces = APPLICATION_MS_EXCEL)
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public synchronized ResponseEntity<byte[]> getApiDocumentationFile() throws EntityRetrievalException {
        CHPLFileDTO fileDTO = chplFileManager.getApiDocumentation();

        String filename = "APIDocData-" + getDateAsYYYYMMDD(fileDTO.getAssociatedDate()) + ".xlsx";

        return ResponseEntity.ok()
                             .contentLength(fileDTO.getFileData().length)
                             .header(HttpHeaders.CONTENT_TYPE, APPLICATION_MS_EXCEL)
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                             .body(fileDTO.getFileData());
    }

    @ApiOperation(value = "Retrieve details about an API Documentation file",
            notes = "Retrieves the details about the current API Documentation file.")
    @RequestMapping(value = "/api_documentation/details",
        method = RequestMethod.GET,
        produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public synchronized ResponseEntity<CHPLFileDTO> getApiDocumentationFileDetails() throws EntityRetrievalException {
        CHPLFileDTO fileDTO = chplFileManager.getApiDocumentation();
        return new ResponseEntity<CHPLFileDTO>(fileDTO, HttpStatus.OK);
    }

    private String getDateAsYYYYMMDD(final Date dateToFormat) {
        DateFormat format = new SimpleDateFormat("yyyyddMM");
        return format.format(dateToFormat);
    }
}
