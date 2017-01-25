package gov.healthit.chpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.junit.Test;

public class XmlParsingTest {

	static final String URL_PATTERN = "^https?://([\\da-z\\.-]+)\\.([a-z\\.]{2,6})(:[0-9]+)?([\\/\\w \\.\\-\\,=&%#]*)*(\\?([\\/\\w \\.\\-\\,=&%#]*)*)?";

	@Test
	public void test() {
		String id = "14.07.07.1446.ECAS.63.1.1.141113";
		String[] results = id.split("\\.");
		assertEquals(9, results.length);
	}

	@Test
	public void filenameMatcherTest() {
		boolean result = "chpl-2011-20161007-122654.csv".matches("^chpl-" + "2011" + "-.+\\." + "csv"+"$");
		assertTrue(result);
		result = "chpl-2011-20161007-122654.csv".matches("^chpl-" + "2014" + "-.+\\." + "csv"+"$");
		assertFalse(result);
		result = "chpl-2011-20161007-122654.csv".matches("^chpl-" + "2011" + "-.+\\." + "xml"+"$");
		assertFalse(result);
	}
	
	@Test
	public void testProductCodeValidation() {
		boolean result = "ht75_A".matches("^\\w+$");
		assertTrue(result);
		result = "^$THD".matches("^\\w+$");
		assertFalse(result);
		result = "____".matches("^\\w+$");
		assertTrue(result);
		
		result = "4".matches("^\\d+$");
		assertTrue(result);
		result = "55".matches("^\\d+$");
		assertTrue(result);
		result = "t".matches("^\\d+$");
		assertFalse(result);
	}
	
	@Test
	public void testUrlRegex() {
		Pattern urlRegex = Pattern.compile(URL_PATTERN);
		String url = "http://www.greenway.com\\nhttp://www.greenway.com";
		System.out.println(new Date());
		System.out.println(urlRegex.matcher(url).matches());
		System.out.println(new Date());
	}
	
	@Test
	public void parseInt() {
		String intStr = "1.0";
		try {
			Float taskPathDeviationObs = new Float(intStr);
			Integer intVal = Math.round(taskPathDeviationObs);
			System.out.println(intVal);
		} catch(Exception ex) {
			fail("Cannot parse " + intStr + " as integer.");
		}
	}
	
	@Test
	public void parseFloat() {
		String floatStr = "1";
		try {
			Float taskPathDeviationObs = Float.parseFloat(floatStr);
			System.out.println(taskPathDeviationObs);
		} catch(Exception ex) {
			fail("Cannot parse " + floatStr + " as float.");
		}
	}
	
	@Test
	public void coerceCrtierionNumber() {
		String input = "170.315 (a)(1)";
		System.out.println("Testing " + input);
		input = Util.coerceToCriterionNumberFormat(input);
		System.out.println("\tResult: " + input);
		
		input = " 170.315 (A)(1)";
		System.out.println("Testing " + input);
		input = Util.coerceToCriterionNumberFormat(input);
		System.out.println("\tResult: " + input);
		
		input = " 170.315(A)(1)";
		System.out.println("Testing " + input);
		input = Util.coerceToCriterionNumberFormat(input);
		System.out.println("\tResult: " + input);
		
		input = " 170.315  (A)(1)";
		System.out.println("Testing " + input);
		input = Util.coerceToCriterionNumberFormat(input);
		System.out.println("\tResult: " + input);
		
		input = " KATY EKEY";
		System.out.println("Testing " + input);
		input = Util.coerceToCriterionNumberFormat(input);
		System.out.println("\tResult: " + input);
		
		input = " 170.523(a)";
		System.out.println("Testing " + input);
		input = Util.coerceToCriterionNumberFormat(input);
		System.out.println("\tResult: " + input);
	}
	
	@Test
	public void testGmtDateCode() {
		long millis = 1460246400000L; //should be april 10 2016
		String expectedResult = "160410";
		SimpleDateFormat idDateFormat = new SimpleDateFormat("yyMMdd");
		idDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	
		String result = idDateFormat.format(new Date(millis));
		assertEquals(expectedResult, result);
	}
}
