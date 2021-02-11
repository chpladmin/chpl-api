package gov.healthit.chpl.caching;

import org.springframework.http.HttpStatus;

import lombok.Data;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.constructs.EhcacheDecoratorAdapter;

@Data
public class HttpStatusAwareCache extends EhcacheDecoratorAdapter {

    private HttpStatus httpStatus;

    public HttpStatusAwareCache(Ehcache underlyingCache) {
        super(underlyingCache);
    }

}
