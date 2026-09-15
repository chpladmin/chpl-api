package gov.healthit.chpl.util;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

/**
 * Utility functions for dealing with error messages.
 */
@Component
@Log4j2
public class ErrorMessageUtil {

    //Codes probed at startup by logInjectedMessageSource(). The first three are
    //spread across errors.properties (lines 16, 167 and 722) so a bundle that
    //failed to load shows up as every probe unresolved, rather than looking like
    //one bad key. The last one is the code that throws on DEV.
    private static final String[] PROBE_CODES = {
        "access.denied",
        "listing.newlineCharacterFound",
        "surveillance.badCharacterFound",
        "surveillance.newlineCharacterFound"
    };

    private MessageSource messageSource;

    @Autowired
    public ErrorMessageUtil(final MessageSource messageSource) {
        this.messageSource = messageSource;
        logInjectedMessageSource();
    }

    //Temporary diagnostic. surveillance.newlineCharacterFound throws
    //NoSuchMessageException on DEV even though the key is present in the
    //deployed chpl-resources jar, that file parses, and the exact bean
    //configuration below resolves the code both standalone and from inside the
    //running container. That leaves the live Spring context as the only
    //remaining difference, so record what it actually injected. Uses the
    //defaultMessage overload so a miss cannot throw. Remove once root-caused.
    private void logInjectedMessageSource() {
        LOGGER.info("Injected MessageSource implementation: {}", messageSource.getClass().getName());
        LOGGER.info("Injected MessageSource classloader:    {}", messageSource.getClass().getClassLoader());
        LOGGER.info("LocaleContextHolder locale: {}, JVM default locale: {}",
                LocaleContextHolder.getLocale(), Locale.getDefault());
        for (String code : PROBE_CODES) {
            String resolved = messageSource.getMessage(code, null, "**UNRESOLVED**", Locale.US);
            LOGGER.info("  probe {} -> {}", code, resolved);
        }
    }

    /**
     * Retrieve the local encoded message for a given error code.
     * @param messageCode the error code
     * @param inputs values that will go into the error string
     * @return the encoded message, with values inserted
     */
    public String getMessage(final String messageCode, final Object...inputs) {
        String result = null;
        if (inputs == null || inputs.length == 0) {
            result = String.format(
                messageSource.getMessage(new DefaultMessageSourceResolvable(messageCode),
                        LocaleContextHolder.getLocale()));
        } else {
            result = String.format(
                    messageSource.getMessage(new DefaultMessageSourceResolvable(messageCode),
                            LocaleContextHolder.getLocale()), inputs);
        }
        return result;
    }

    public int getMessageAsInteger(final String field) {
        return Integer.parseInt(String.format(
                messageSource.getMessage(new DefaultMessageSourceResolvable(field), LocaleContextHolder.getLocale())));
    }
}
