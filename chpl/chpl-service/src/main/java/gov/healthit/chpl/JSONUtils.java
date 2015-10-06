package gov.healthit.chpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JSONUtils {
	
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final ObjectReader reader = mapper.reader();
	private static final ObjectWriter writer = mapper.writer();

	public static ObjectReader getReader(){
		return reader;
	}

	public static ObjectWriter getWriter(){
		return writer;
	}
	
}
