package gov.healthit.chpl.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;

/**
 * Utility functions for dealing with error messages.
 */
@Component
public class ErrorMessageUtil {

    private MessageSource messageSource;

    @Autowired
    public ErrorMessageUtil(final MessageSource messageSource) {
        this.messageSource = messageSource;
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
