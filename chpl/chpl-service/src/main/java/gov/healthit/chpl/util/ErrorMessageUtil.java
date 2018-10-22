package gov.healthit.chpl.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;

@Component
public class ErrorMessageUtil {

    @Autowired private MessageSource messageSource;

    public String getMessage(final String messageCode, final Object...inputs) {
        String result = null;
        if(inputs == null || inputs.length == 0) {
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
}
