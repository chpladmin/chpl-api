package gov.healthit.chpl.util;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * ReloadableResourceBundleMessageSource that always resolves its bundles through the
 * classloader that loaded this class (the webapp's), rather than the calling thread's
 * context classloader.
 *
 * The ApplicationContext injected via ResourceLoaderAware resolves classpath: resources
 * through the current thread's context classloader. ForkJoinPool worker threads
 * (new ForkJoinPool(n), parallelStream()) carry the system classloader, which cannot see
 * WEB-INF/lib - and because a missing bundle is cached for the life of the app, one
 * lookup from such a thread makes every error code unresolvable until restart.
 */
public class ClassLoaderMessageSource extends ReloadableResourceBundleMessageSource {

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        //ClassLoaderMessageSource.class.getClassLoader() is the webapp's classloader
        //because the chpl-service jar is in WEB-INF/lib alongside chpl-resources.
        super.setResourceLoader(new DefaultResourceLoader(ClassLoaderMessageSource.class.getClassLoader()));
    }
}

