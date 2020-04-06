package old.gov.healthit.chpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.Ignore;
import org.junit.Test;

public class XmlParsingTest {

    static final String URL_PATTERN = "^https?://([\\da-z\\.-]+)\\.([a-z\\.]{2,6})(:[0-9]+)?([\\/\\w \\.\\-\\,=&%#]*)*(\\?([\\/\\w \\.\\-\\,=&%#]*)*)?";

    @Test
    public void csvWritingTest() {
        List<String> vals = new ArrayList<String>();
        vals.add("Test_VAL_1");
        vals.add("Test_VAL_2");
        vals.add("Test_VAL_3");
        StringBuffer buf = new StringBuffer();
        CSVPrinter writer = null;
        try {
            writer = new CSVPrinter(buf, CSVFormat.EXCEL);
            writer.printRecord(vals);
        } catch (IOException ex) {
            fail("IOException: " + ex.getMessage());
        } finally {
            try {
                writer.close();
            } catch (Exception ignore) {
            }
        }
        String csv = buf.toString();
        System.out.println(csv);
        assertTrue(!csv.contains(" "));
    }

    @Test
    public void test() {
        String id = "14.07.07.1446.ECAS.63.1.1.141113";
        String[] results = id.split("\\.");
        assertEquals(9, results.length);
    }

    @Test
    public void testIntegerParsing() {
        String toParse = "00";
        Integer parsed = Integer.valueOf(toParse);
        assertTrue(parsed.intValue() == 0);
    }

    @Test
    public void filenameMatcherTest() {
        boolean result = "chpl-2011-20161007-122654.csv".matches("^chpl-" + "2011" + "-.+\\." + "csv" + "$");
        assertTrue(result);
        result = "chpl-2011-20161007-122654.csv".matches("^chpl-" + "2014" + "-.+\\." + "csv" + "$");
        assertFalse(result);
        result = "chpl-2011-20161007-122654.csv".matches("^chpl-" + "2011" + "-.+\\." + "xml" + "$");
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
        } catch (Exception ex) {
            fail("Cannot parse " + intStr + " as integer.");
        }
    }

    @Test
    public void parseFloat() {
        String floatStr = "1";
        try {
            Float taskPathDeviationObs = Float.parseFloat(floatStr);
            System.out.println(taskPathDeviationObs);
        } catch (Exception ex) {
            fail("Cannot parse " + floatStr + " as float.");
        }
    }

    @Ignore
    @Test
    public void coerceCrtierionNumber() {
        String input = "170.315 (a)(1)";
        System.out.println("Testing " + input);
        // input = Util.coerceToCriterionNumberFormat(input);
        System.out.println("\tResult: " + input);

        input = " 170.315 (A)(1)";
        System.out.println("Testing " + input);
        // input = Util.coerceToCriterionNumberFormat(input);
        System.out.println("\tResult: " + input);

        input = " 170.315(A)(1)";
        System.out.println("Testing " + input);
        // input = Util.coerceToCriterionNumberFormat(input);
        System.out.println("\tResult: " + input);

        input = " 170.315  (A)(1)";
        System.out.println("Testing " + input);
        // input = Util.coerceToCriterionNumberFormat(input);
        System.out.println("\tResult: " + input);

        input = " KATY EKEY";
        System.out.println("Testing " + input);
        /// input = Util.coerceToCriterionNumberFormat(input);
        System.out.println("\tResult: " + input);

        input = " 170.523(a)";
        System.out.println("Testing " + input);
        // input = Util.coerceToCriterionNumberFormat(input);
        System.out.println("\tResult: " + input);
    }

    @Test
    public void testGmtDateCode() {
        long millis = 1460246400000L; // should be april 10 2016
        String expectedResult = "160410";
        SimpleDateFormat idDateFormat = new SimpleDateFormat("yyMMdd");
        idDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String result = idDateFormat.format(new Date(millis));
        assertEquals(expectedResult, result);
    }
}
