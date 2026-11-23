package gov.healthit.chpl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Resolves message codes against the real errors.properties bundle, wired the same way
 * CHPLServiceConfig.messageSource() wires it. Every other test in the suite mocks
 * MessageSource, so a code that exists in Java but not in the bundle - or a bundle the
 * configured basenames cannot actually load - is invisible until it throws in production.
 */
public class ErrorMessageUtilResolutionTest {

    private ErrorMessageUtil errorMessageUtil;

    @BeforeEach
    public void setup() {
        //Mirrors CHPLServiceConfig.messageSource() exactly.
        ReloadableResourceBundleMessageSource messageSource = new ClassLoaderMessageSource();
        messageSource.setBasenames("classpath:errors-override", "classpath:errors");
        messageSource.setDefaultEncoding("UTF-8");

        errorMessageUtil = new ErrorMessageUtil(messageSource);

        //The failing jobs run under en_US, per the NoSuchMessageException they threw.
        LocaleContextHolder.setLocale(Locale.US);
    }

    @AfterEach
    public void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    public void surveillanceNewlineCharacterFound_resolves() {
        String message = errorMessageUtil.getMessage("surveillance.newlineCharacterFound", "Surveillance Type");

        assertNotNull(message);
        assertFalse(message.isBlank());
    }

    @Test
    public void surveillanceBadCharacterFound_resolves() {
        String message = errorMessageUtil.getMessage("surveillance.badCharacterFound", "Surveillance Type");

        assertNotNull(message);
        assertFalse(message.isBlank());
    }

    @Test
    public void listingNewlineCharacterFound_resolves() {
        String message = errorMessageUtil.getMessage("listing.newlineCharacterFound", "Product Name");

        assertNotNull(message);
        assertFalse(message.isBlank());
    }

    @Test
    public void firstLookupFromThreadWithForeignContextClassLoader_stillResolves() throws Exception {
        ClassLoaderMessageSource messageSource = new ClassLoaderMessageSource();
        messageSource.setResourceLoader(null); //what ResourceLoaderAware does in a live context
        messageSource.setBasenames("classpath:errors-override", "classpath:errors");
        messageSource.setDefaultEncoding("UTF-8");
        ErrorMessageUtil util = new ErrorMessageUtil(messageSource);

        //Bootstrap-parented loader with no URLs: sees no application resources at all, like
        //the system classloader on a Tomcat ForkJoinPool worker.
        ClassLoader foreign = new URLClassLoader(new URL[0], null);
        AtomicReference<Object> result = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            LocaleContextHolder.setLocale(Locale.US);
            try {
                result.set(util.getMessage("maxLength.certificationEdition"));
            } catch (RuntimeException e) {
                result.set(e);
            }
        });
        worker.setContextClassLoader(foreign);
        worker.start();
        worker.join();

        assertEquals("10", result.get());
        //And the bundle is still usable afterwards, i.e. a "not found" wasn't cached.
        assertEquals("10", util.getMessage("maxLength.certificationEdition"));
    }
}
