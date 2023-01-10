package gov.healthit.chpl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.ProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class UrlCallerTest {

    //I would like to leave this test here in the event we need to diagnose any issues with the Questionable URL Report.
    @Ignore
    @Test
    public void testUrlHttpClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException,
        IOException {
        System.setProperty("javax.net.debug", "all");

        String url = "https://www.exansoftware.com";

        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext != null ? sslContext : SSLContexts.createDefault());
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .setRedirectStrategy(new CustomRedirectStrategy())
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(30000)
                        .setConnectTimeout(30000)
                        .setSocketTimeout(30000)
                        .build())
                .build();
        CloseableHttpResponse response = httpClient.execute(new HttpGet(url));
        String responseBody = EntityUtils.toString(response.getEntity());
        System.out.println(responseBody);
        int statusCode = response.getStatusLine().getStatusCode();
        response.close();
        System.out.println(HttpStatus.valueOf(statusCode));
    }

    private class CustomRedirectStrategy extends DefaultRedirectStrategy {
        protected URI createLocationURI(final String location) throws ProtocolException {
            try {
                System.out.println("Redirecting to " + location);
                //replace spaces with %20
                String encodedLocation = location.replace(" ", "%20");
                if (!location.equals(encodedLocation)) {
                    System.out.println("Encoded " + location + " as " + encodedLocation);
                }
                return new URI(encodedLocation);
            } catch (final URISyntaxException ex) {
                throw new ProtocolException("Invalid redirect URI: " + location, ex);
            }
        }
    }
}
