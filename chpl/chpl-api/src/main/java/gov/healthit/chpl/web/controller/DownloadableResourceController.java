package gov.healthit.chpl.web.controller;

import java.io.File;
import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.compliance.surveillance.SurveillanceManager;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.svap.manager.SvapManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.FileUtils;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "downloadable-resources", description = "Provides access to files generated by CHPL.")
@RestController
@Log4j2
public class DownloadableResourceController {
    private Environment env;
    private ErrorMessageUtil msgUtil;
    private SurveillanceManager survManager;
    private SvapManager svapManager;
    private FileUtils fileUtils;

    @Value("${directReviewsReportName}")
    private String directReviewsReportName;

    @Value("${schemaDirectReviewsName}")
    private String directReviewsSchemaName;

    @Autowired
    public DownloadableResourceController(Environment env,
            ErrorMessageUtil msgUtil,
            SurveillanceManager survManager,
            SvapManager svapManager,
            FileUtils fileUtils) {
        this.env = env;
        this.msgUtil = msgUtil;
        this.survManager = survManager;
        this.svapManager = svapManager;
        this.fileUtils = fileUtils;
    }

    @Operation(summary = "Download all listings of a given type in the specified format.",
            description = "Valid values for 'listingType' are active, inactive, 2011 and 2014."
                    + "Valid values for 'format' are csv and json.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/download/{listingType:2011|2014|active|inactive}", method = RequestMethod.GET, produces = "text/csv")
    public void downloadListings(@PathVariable(value = "listingType", required = true) String listingType,
            @RequestParam(value = "format", defaultValue = "csv", required = false) String formatInput,
            @RequestParam(value = "definition", defaultValue = "false", required = false) Boolean isDefinition,
            HttpServletRequest request, HttpServletResponse response) throws IOException, InvalidArgumentsException {
        String format = normalizeFormat(formatInput);
        if (!format.equalsIgnoreCase("csv") && !format.equalsIgnoreCase("json")) {
            throw new InvalidArgumentsException("Format must be CSV or JSON");
        }
        String responseType = getResponseType(format);

        File toDownload = null;
        if (BooleanUtils.isTrue(isDefinition)) {
            toDownload = getDefinitionDownloadFile(listingType, format);
            if (!toDownload.exists()) {
                response.getWriter()
                        .write(msgUtil.getMessage("resources.schemaFileNotFound", toDownload.getAbsolutePath()));
                return;
            }
        } else {
            File newestFileWithFormat = fileUtils.getNewestFileMatchingName("^chpl-" + listingType + "-.+\\." + format + "$");
            if (newestFileWithFormat != null) {
                toDownload = newestFileWithFormat;
            } else {
                response.getWriter()
                        .write(msgUtil.getMessage("resources.fileWithEditionAndFormatNotFound", listingType, format));
                return;
            }
        }

        LOGGER.info("Downloading " + toDownload.getName());
        fileUtils.streamFileAsResponse(toDownload, responseType, response);
    }

    private String normalizeFormat(String formatInput) {
        String format = formatInput;
        if (!StringUtils.isEmpty(format) && format.equalsIgnoreCase("csv")) {
            format = "csv";
        } else if (!StringUtils.isEmpty(format) && format.equalsIgnoreCase("json")) {
            format = "json";
        }
        return format;
    }

    private String getResponseType(String format) {
        String responseType = "text/plain";
        if (!StringUtils.isEmpty(format) && format.equalsIgnoreCase("csv")) {
            responseType = "text/csv";
        } else if (!StringUtils.isEmpty(format) && format.equalsIgnoreCase("json")) {
            responseType = "application/json";
        }
        return responseType;
    }

    private File getDefinitionDownloadFile(String listingType, String format) throws IOException {
        File toDownload = null;
        if (listingType.equals(CertificationEditionConcept.CERTIFICATION_EDITION_2011.getYear())) {
            toDownload = fileUtils.getDownloadFile(env.getProperty("schemaCsv2011Name"));
        } else if (listingType.equals(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear())) {
            toDownload = fileUtils.getDownloadFile(env.getProperty("schemaCsv2014Name"));
        } else {
            toDownload = fileUtils.getDownloadFile(env.getProperty("schemaCsvListingName"));
        }
        return toDownload;
    }

    @Operation(summary = "Download a summary of SVAP activity as a CSV.",
            description = "Once per day, a summary of SVAP activity is written out to a CSV "
                    + "file on the CHPL servers. This method allows any user to download that file.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/svap/download", method = RequestMethod.GET, produces = "text/csv")
    public void downloadSvapSummary(
            @RequestParam(value = "definition", defaultValue = "false", required = false) Boolean isDefinition,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        File downloadFile = null;
        if (isDefinition != null && isDefinition.booleanValue()) {
            try {
                downloadFile = svapManager.getSvapSummaryDefinitionFile();
            } catch (IOException ex) {
                response.getWriter().append(ex.getMessage());
                return;
            }
        } else {
            try {
                downloadFile = svapManager.getSvapSummaryFile();
            } catch (IOException ex) {
                response.getWriter().append(ex.getMessage());
                return;
            }
        }

        if (downloadFile == null) {
            response.getWriter().append(msgUtil.getMessage("resources.schemaFileGeneralError"));
            return;
        }
        if (!downloadFile.exists()) {
            response.getWriter().append(msgUtil.getMessage("resources.schemaFileNotFound", downloadFile.getAbsolutePath()));
            return;
        }

        LOGGER.info("Streaming " + downloadFile.getName());
        fileUtils.streamFileAsResponse(downloadFile, "text/csv", response);
    }

    @Operation(summary = "Download all SED details for Listings that are certified to 170.315(g)(3).",
            description = "Download a specific file that is generated overnight.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/certified_products/sed_details", method = RequestMethod.GET)
    public void streamSEDDetailsDocumentContents(HttpServletResponse response)
            throws EntityRetrievalException, IOException {
        File downloadFile = fileUtils.getNewestFileMatchingName("^" + env.getProperty("SEDDownloadName") + "-.+\\.csv$");
        fileUtils.streamFileAsResponse(downloadFile, "text/csv", response);
    }

    @Operation(summary = "Download all direct reviews as a CSV.",
            description = "Once per day, all direct reviews are written out to a CSV "
                    + "file on the CHPL servers. This method allows any user to download that file.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/developers/direct-reviews/download", method = RequestMethod.GET, produces = "text/csv")
    public void downloadDirectReviews(
            @RequestParam(value = "definition", defaultValue = "false", required = false) Boolean isDefinition,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        File downloadFile = null;
        if (isDefinition != null && isDefinition.booleanValue()) {
            try {
                downloadFile = fileUtils.getDownloadFile(directReviewsSchemaName);
            } catch (IOException ex) {
                response.getWriter().append(ex.getMessage());
                return;
            }
        } else {
            try {
                downloadFile = fileUtils.getNewestFileMatchingName("^" + directReviewsReportName + "-.+\\.csv$");
            } catch (IOException ex) {
                response.getWriter().append(ex.getMessage());
                return;
            }
        }

        if (downloadFile == null) {
            response.getWriter().append(msgUtil.getMessage("resources.schemaFileGeneralError"));
            return;
        }
        if (!downloadFile.exists()) {
            response.getWriter().append(msgUtil.getMessage("resources.schemaFileNotFound", downloadFile.getAbsolutePath()));
            return;
        }

        LOGGER.info("Streaming " + downloadFile.getName());
        fileUtils.streamFileAsResponse(downloadFile, "text/csv", response);
    }

    @Operation(summary = "Download surveillance as CSV.",
            description = "Once per day, all surveillance and nonconformities are written out to CSV "
                    + "files on the CHPL servers. This method allows any user to download those files.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/surveillance/download", method = RequestMethod.GET, produces = "text/csv")
    public void downloadSurveillance(@RequestParam(value = "type", required = false, defaultValue = "") final String type,
            @RequestParam(value = "definition", defaultValue = "false", required = false) final Boolean isDefinition,
            final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, EntityRetrievalException {

        File downloadFile = null;
        if (isDefinition != null && isDefinition.booleanValue()) {
            if (type.equalsIgnoreCase("basic")) {
                downloadFile = survManager.getBasicReportDownloadDefinitionFile();
            } else {
                downloadFile = fileUtils.getDownloadFile(env.getProperty("schemaSurveillanceName"));
            }
        } else {
            try {
                if (type.equalsIgnoreCase("all")) {
                    downloadFile = survManager.getAllSurveillanceDownloadFile();
                } else if (type.equalsIgnoreCase("basic")) {
                    downloadFile = survManager.getBasicReportDownloadFile();
                } else {
                    downloadFile = survManager.getSurveillanceWithNonconformitiesDownloadFile();
                }
            } catch (final IOException ex) {
                response.getWriter().append(ex.getMessage());
                return;
            }
        }

        if (downloadFile == null) {
            response.getWriter()
                    .append(msgUtil.getMessage("resources.schemaFileGeneralError"));
            return;
        }
        if (!downloadFile.exists()) {
            response.getWriter()
                    .write(msgUtil.getMessage("resources.schemaFileNotFound", downloadFile.getAbsolutePath()));
            return;
        }

        LOGGER.info("Downloading " + downloadFile.getName());
        fileUtils.streamFileAsResponse(downloadFile, "text/csv", response);
    }
}
