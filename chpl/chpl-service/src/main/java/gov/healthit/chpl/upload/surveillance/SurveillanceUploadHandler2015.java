package gov.healthit.chpl.upload.surveillance;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

@Component("surveillanceUploadHandler2015")
public class SurveillanceUploadHandler2015 implements SurveillanceUploadHandler {
	private static final String DATE_FORMAT = "yyyyMMdd";
	protected SimpleDateFormat dateFormatter;
	
	private List<CSVRecord> record;
	private CSVRecord heading;
	private int lastDataIndex;
	
	public SurveillanceUploadHandler2015() {
		dateFormatter = new SimpleDateFormat(DATE_FORMAT);
	}
	
	public Surveillance handle() throws InvalidArgumentsException {
		return null;
	}

	
	public List<CSVRecord> getRecord() {
		return record;
	}
	public void setRecord(List<CSVRecord> record) {
		this.record = record;
	}
	public CSVRecord getHeading() {
		return heading;
	}
	public void setHeading(CSVRecord heading) {
		this.heading = heading;
	}
	public int getLastDataIndex() {
		return lastDataIndex;
	}
	public void setLastDataIndex(int lastDataIndex) {
		this.lastDataIndex = lastDataIndex;
	}
}
