package gov.healthit.chpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@Log4j2
@UtilityClass
public class PasswordUtil {
    private static final int MIN_PASSWORD_STRENGTH = 3;
    private static final int MIN_UPPER_CASE_LETTER = 65;
    private static final int MAX_UPPER_CASE_LETTER = 90;
    private static final int MIN_LOWER_CASE_LETTER = 97;
    private static final int MAX_LOWER_CASE_LETTER = 122;
    private static final int MIN_SPECIAL_CHAR = 33;
    private static final int MAX_SPECIAL_CHAR = 47;

    public final String generate() {
        String upperCaseLetters = RandomStringUtils.random(2, MIN_UPPER_CASE_LETTER, MAX_UPPER_CASE_LETTER, true, true);
        String lowerCaseLetters = RandomStringUtils.random(2, MIN_LOWER_CASE_LETTER, MAX_LOWER_CASE_LETTER, true, true);
        String numbers = RandomStringUtils.randomNumeric(2);
        String specialChar = RandomStringUtils.random(2, MIN_SPECIAL_CHAR, MAX_SPECIAL_CHAR, false, false);        String totalChars = RandomStringUtils.randomAlphanumeric(2);
        String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
          .concat(numbers)
          .concat(specialChar)
          .concat(totalChars);
        List<Character> pwdChars = combinedChars.chars()
          .mapToObj(c -> (char) c)
          .collect(Collectors.toList());
        Collections.shuffle(pwdChars);
        String password = pwdChars.stream()
          .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
          .toString();
        return password;
    }

    public Boolean validatePaswordStrength(String username, String password) {
        ArrayList<String> badWords = new ArrayList<String>();
        badWords.add("chpl");
        badWords.add(username);

        Zxcvbn zxcvbn = new Zxcvbn();
        Strength strength = zxcvbn.measure(password, badWords);

        if (strength.getScore() < MIN_PASSWORD_STRENGTH) {
            LOGGER.info("Strength results: [warning: {}] [suggestions: {}] [score: {}] [worst case crack time: {}]",
                    strength.getFeedback().getWarning(), strength.getFeedback().getSuggestions().toString(),
                    strength.getScore(), strength.getCrackTimesDisplay().getOfflineFastHashing1e10PerSecond());
            return false;
        }
        return true;
    }

}
