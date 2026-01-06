package gov.healthit.chpl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.Assert;

// We really probably should not need this class, but what is happening is that Jira is sending back a content-length header
// on some requests. When initially loading all of the direct reviews, the responses from Jira are large and do NOT specify a
// content-length header, so the StringHttpMessageConverter as supplied by Spring just reads all of the bytes in the response.
// When requesting direct reviews for a specific developer that has none, the Jira response does include a response header
// of content-length. When there are no direct reviews the length is given as 111 bytes, but it is actually 116 bytes.
// The provided StringHttpMessageConverter respects the content-length header and only reads 111 bytes which ends up being an
// incomplete JSON string and gives an error.
// This class overrides that behavior by just always reading the entire response.
public class FullStringHttpMessageConverter extends StringHttpMessageConverter {
    private static final MediaType APPLICATION_PLUS_JSON = new MediaType("application", "*+json");

    public FullStringHttpMessageConverter() {
        super();
    }

    public FullStringHttpMessageConverter(Charset defaultCharset) {
        super(defaultCharset);
    }

    @Override
    protected String readInternal(Class<? extends String> clazz, HttpInputMessage inputMessage) throws IOException {
        Charset charset = getContentTypeCharset(inputMessage.getHeaders().getContentType());
        byte[] bytes = inputMessage.getBody().readAllBytes();
        return new String(bytes, charset);
    }

    private Charset getContentTypeCharset(@Nullable MediaType contentType) {
        if (contentType != null) {
            Charset charset = contentType.getCharset();
            if (charset != null) {
                return charset;
            } else if (contentType.isCompatibleWith(MediaType.APPLICATION_JSON)
                    || contentType.isCompatibleWith(APPLICATION_PLUS_JSON)) {
                // Matching to AbstractJackson2HttpMessageConverter#DEFAULT_CHARSET
                return StandardCharsets.UTF_8;
            }
        }
        Charset charset = getDefaultCharset();
        Assert.state(charset != null, "No default charset");
        return charset;
    }
}
