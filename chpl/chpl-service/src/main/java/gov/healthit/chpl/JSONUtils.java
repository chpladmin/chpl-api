package gov.healthit.chpl;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
	
	public static String toJSON(Object obj) throws JsonProcessingException{
		
		String json = null;
		if (obj != null){
			json = getWriter().writeValueAsString(obj);
		}
		return json;
	}
	
	public static <T> T fromJSON(String json, Class<T> type) throws JsonProcessingException, IOException {	
		
		JsonNode node = getReader().readTree(json);
		T obj = getReader().treeToValue(node, type);
		return obj;
	
	}
	
}
