package gov.healthit.chpl.auth.jwt;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.interfaces.RSAKeyProvider;

public class CognitoRsaKeyProvider implements RSAKeyProvider {
    private final URL awsKidStoreUrl;
    private final JwkProvider provider;

    public CognitoRsaKeyProvider(String awsCognitoRegion, String awsUserPoolId, String tokenizezRsaKeyUrl) {
        String url = String.format(tokenizezRsaKeyUrl, awsCognitoRegion, awsUserPoolId);
        try {
            awsKidStoreUrl = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(String.format("Invalid URL provided, URL=%s", url));
        }
        provider = new JwkProviderBuilder(awsKidStoreUrl).build();
    }


    @Override
    public RSAPublicKey getPublicKeyById(String kid) {
        try {
            return (RSAPublicKey) provider.get(kid).getPublicKey();
        } catch (JwkException e) {
            throw new RuntimeException(String.format("Failed to get JWT kid=%s from aws_kid_store_url=%s", kid, awsKidStoreUrl));
        }
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
        return null;
    }

    @Override
    public String getPrivateKeyId() {
        return null;
    }
}
