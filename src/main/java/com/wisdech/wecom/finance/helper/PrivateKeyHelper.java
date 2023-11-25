package com.wisdech.wecom.finance.helper;

import org.apache.commons.codec.binary.Base64;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class PrivateKeyHelper {

    public static RSAPrivateKey fromString(String keyString) throws NoSuchAlgorithmException, InvalidKeySpecException {

        keyString = keyString.replace("-----BEGIN PRIVATE KEY-----", "");
        keyString = keyString.replace("-----END PRIVATE KEY-----", "");

        byte[] decoded = Base64.decodeBase64(keyString);

        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

}
