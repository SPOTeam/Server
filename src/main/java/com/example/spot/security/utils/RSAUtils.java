package com.example.spot.security.utils;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.domain.auth.RsaKey;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Component
public class RSAUtils {

    private final KeyPairGenerator generator;
    private final KeyFactory keyFactory;
    private final Cipher cipher;

    public RSAUtils() throws Exception {
        SecureRandom secureRandom = new SecureRandom();
        generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, secureRandom);
        keyFactory = KeyFactory.getInstance("RSA");
        cipher = Cipher.getInstance("RSA");
    }

    /**
     * RSA Public Key와 Private Key를 생성하는 함수
     * @return 생성된 RSA 객체
     */
    public RsaKey createRSA() {

        try {
            // Key Pair 생성
            KeyPair keyPair = generator.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            RSAPublicKeySpec publicKeySpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
            String modulus = publicKeySpec.getModulus().toString(16);           // 16진수 문자열
            String exponent = publicKeySpec.getPublicExponent().toString(16);   // 16진수 문자열

            return RsaKey.builder()
                    .publicKey(getBase64StringFromPublicKey(publicKey))
                    .privateKey(getBase64StringFromPrivateKey(privateKey))
                    .modulus(modulus)
                    .exponent(exponent)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            throw new MemberHandler(ErrorStatus._RSA_ERROR);
        }

    }

    /**
     * Public Key를 Base64 문자열로 변환하는 함수
     * @param publicKey : 암호화에 사용될 publicKey
     * @return Public Key를 Base64 문자열로 변환한 값
     */
    public String getBase64StringFromPublicKey(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Private Key를 Base64 문자열로 변환하는 함수
     * @param privateKey : 암호화에 사용될 privateKey
     * @return Public Key를 Base64 문자열로 변환한 값
     */
    public String getBase64StringFromPrivateKey(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * Base64 문자열을 PrivateKey 객체로 변환하는 함수
     * @param privateKeyString :Private Key를 Base64 문자열로 변환한 값
     * @return PrivateKey 객체
     */
    public PrivateKey getPrivateKeyFromBase64String(String privateKeyString) {

        try {
            // Base64 디코딩
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyString);

            // PrivateKey 객체 생성
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
            throw new MemberHandler(ErrorStatus._RSA_ERROR);
        }

    }

    /**
     * Public Key로 문자열을 암호화하는 함수
     * @param publicKey : 암호화에 사용될 publicKey
     * @param plainText : 암호화되지 않은 문자열
     * @return 암호화된 문자열
     */
    public String getEncryptedText(PublicKey publicKey, String plainText) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] plainBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(plainBytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw new MemberHandler(ErrorStatus._RSA_ERROR);
        }
    }

    /**
     * Private Key로 문자열을 복호화하는 함수
     * @param privateKey : 복호화에 사용될 privateKey
     * @param encryptedText : 암호화된 문자열
     * @return 복호화된 문자열
     */
    public String getDecryptedText(PrivateKey privateKey, String encryptedText) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] plainBytes = cipher.doFinal(encryptedBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            throw new MemberHandler(ErrorStatus._RSA_ERROR);
        }
    }
}
