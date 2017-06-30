package gov.healthit.chpl.web.controller;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.domain.TabularValues;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="resources")
@RestController
@RequestMapping("/resources")
public class ResourceController {
	private static final Logger logger = LogManager.getLogger(ResourceController.class);

	@ApiOperation(value="Create a downloadable resource from the POST body data.")
	@RequestMapping(value="/create", method=RequestMethod.POST,
			produces="text/csv; charset=utf-8")
	public void createResourceForDownload(@RequestBody(required=true) TabularValues values,
			HttpServletRequest request, HttpServletResponse response) {
	
		String filename = !StringUtils.isEmpty(values.getName()) ? values.getName() : "file.csv";
		StringBuffer buf = new StringBuffer();
		CSVPrinter csvPrinter = null;
		try {
			csvPrinter = new CSVPrinter(buf, CSVFormat.EXCEL);
			if(values.getHeadings() != null && values.getHeadings().size() > 0) {
				csvPrinter.printRecord(values.getHeadings());
			}
			if(values.getValues() != null && values.getValues().size() > 0) {
				for(List<String> value : values.getValues()) {
					csvPrinter.printRecord(value);
				}
			}
		} catch(IOException ex) {
			logger.error("Could not write csv data to buffer : " + ex.getMessage(), ex);
		} finally {
			try {
				csvPrinter.flush();
				csvPrinter.close();
			} catch(Exception ignore) {}
		}

		// set content attributes for the response
		response.setContentType("application/csv");
		response.setContentLength(buf.length());
	 
		// set headers for the response
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", filename);
		response.setHeader(headerKey, headerValue);
	 
		InputStream inputStream = null;
		OutputStream outStream = null;

		try {
			inputStream = new ByteArrayInputStream(buf.toString().getBytes(Charset.forName("UTF-8")));
			outStream = response.getOutputStream();

			// get output stream of the response
			byte[] buffer = new byte[1024];
			int bytesRead = -1;
		 
			// write bytes read from the input stream into the output stream
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
			}
		} catch(IOException ex) {
			logger.error("Cannot write response stream " + ex.getMessage(), ex);
		} finally {
			try { inputStream.close(); } catch(IOException ignore) {}
			try { outStream.close(); } catch(IOException ignore) {}
		}
	}
}