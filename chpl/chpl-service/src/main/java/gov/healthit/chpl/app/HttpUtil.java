package gov.healthit.chpl.app;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists; 

public class HttpUtil {
	private static final Logger logger = LogManager.getLogger(HttpUtil.class);
	
	private static final Function<Map.Entry<String, String>, NameValuePair> nameValueTransformFunction = new Function<Map.Entry<String, String>, NameValuePair>() { 
        public NameValuePair apply(final Map.Entry<String, String> input) { 
            return new BasicNameValuePair(input.getKey(), input.getValue()); 
        } 
    }; 
 
    /**
     * handle response's entity to utf8 text 
     */ 
    public static final ResponseHandler<String> UTF8_CONTENT_HANDLER = new ResponseHandler<String>() { 
        public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException { 
            final StatusLine statusLine = response.getStatusLine(); 
            if (statusLine.getStatusCode() >= 300) { 
                throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase()); 
            } 
 
            final HttpEntity entity = response.getEntity(); 
            if (entity != null) { 
                return EntityUtils.toString(entity, "UTF-8"); 
            } 
 
            return StringUtils.EMPTY; 
        } 
    }; 
 
    public static URI buildURI(String url, Map<String, String> paramMap) { 
      List<NameValuePair> nameValuePairs = Lists.newArrayList(); 
         if (paramMap != null) { 
             Iterables.addAll(nameValuePairs, Iterables.transform(paramMap.entrySet(), nameValueTransformFunction)); 
             try { 
                 return new URIBuilder(url).setParameters(nameValuePairs).build(); 
             } catch (Exception e) { 
              logger.error("build URI ERROR: url:{},params:{}",url,nameValuePairs,e); 
                 return null; 
             }
         }
         try {
			return new URI(url);
		} catch (URISyntaxException e) {
			logger.error("Could not build URI. url:{}", url);
			e.printStackTrace();
		}
        return null;
    }
     
    public static String getAuthenticatedRequest(String url, Map<String, String> paramMap, Properties props, Token token) { 
        URI uri = buildURI(url, paramMap); 
        try { 
            String content = Request.Get(uri) 
            		.version(HttpVersion.HTTP_1_1)
            		.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
            		.addHeader("API-key", props.getProperty("apiKey"))
            		.addHeader("Authorization", "Bearer " + token.getValidToken(token, props).getToken())
                    .execute().returnContent().asString(); 
            logger.debug("{},result:{}",uri,content); 
            return content; 
        } catch (Exception e) { 
         logger.error("getAuthenticatedRequest:{},error:{}",uri,e); 
         return null; 
        } 
    } 
    
    public static String getRequest(String url, Map<String, String> paramMap, Properties props) { 
        URI uri = buildURI(url, paramMap); 
        try { 
            String content = Request.Get(uri) 
            		.version(HttpVersion.HTTP_1_1)
            		.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
            		.addHeader("API-key", props.getProperty("apiKey"))
                    .execute().returnContent().asString(); 
            logger.debug("{},result:{}",uri,content); 
            return content; 
        } catch (Exception e) { 
         logger.error("getRequest:{},error:{}",uri,e); 
         return null; 
        } 
    } 
     
    public static String postAuthenticatedRequest(String url, Map<String, String> paramMap, Properties props, Token token) { 
        URI uri = buildURI(url, paramMap); 
        try { 
        	String authenticatedToken = token.getValidToken(token, props).getToken();
            String content = Request.Post(uri) 
            		.version(HttpVersion.HTTP_1_1)
            		.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
            		.addHeader("API-key", props.getProperty("apiKey"))
            		.addHeader("Authorization", "Bearer " + authenticatedToken)
                    .execute().returnContent().asString(); 
            logger.debug("{},result:{}",uri,content); 
            return content; 
        } catch (Exception e) { 
         logger.error("postRequest:{},error:{}",uri,e); 
         return null; 
        } 
    } 
    
    public static String postRequest(String url, Map<String, String> paramMap, Properties props) { 
        URI uri = buildURI(url, paramMap); 
        try { 
            String content = Request.Post(uri) 
            		.version(HttpVersion.HTTP_1_1)
            		.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
            		.addHeader("API-key", props.getProperty("apiKey"))
                    .execute().returnContent().asString(); 
            logger.debug("{},result:{}",uri,content); 
            return content; 
        } catch (Exception e) { 
         logger.error("postRequest:{},error:{}",uri,e); 
         return null; 
        } 
    } 
     
    public static String postAuthenticatedBodyRequest(String url, Map<String, String> paramMap, Properties props, Token token, String body) { 
        URI uri = buildURI(url, paramMap); 
        try { 
        	String authenticatedToken = token.getValidToken(token, props).getToken();
            String content = Request.Post(uri) 
            		.version(HttpVersion.HTTP_1_1)
            		.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
            		.addHeader("API-key", props.getProperty("apiKey"))
            		.addHeader("Authorization", "Bearer " + authenticatedToken)
                    .bodyString(body, ContentType.APPLICATION_JSON) 
                    .execute().returnContent().asString(); 
            logger.debug("{},result:{}",uri,content); 
            return content; 
        } catch (Exception e) { 
         logger.error("postBodyRequest:{},error:{}",uri,e); 
         return null; 
        } 
    } 
    
    public static String postBodyRequest(String url, Map<String, String> paramMap, Properties props, String body) { 
        URI uri = buildURI(url, paramMap); 
        try { 
            String content = Request.Post(uri) 
            		.version(HttpVersion.HTTP_1_1)
            		.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
            		.addHeader("API-key", props.getProperty("apiKey"))
                    .bodyString(body, ContentType.APPLICATION_JSON) 
                    .execute().returnContent().asString(); 
            logger.debug("{},result:{}",uri,content); 
            return content; 
        } catch (Exception e) { 
         logger.error("postBodyRequest:{},error:{}",uri,e); 
         return null; 
        } 
    } 
}
