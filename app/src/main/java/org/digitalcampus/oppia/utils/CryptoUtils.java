package org.digitalcampus.oppia.utils;

import android.support.v4.util.Pair;

import org.jarjar.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class CryptoUtils {

    public static final String TAG = CryptoUtils.class.getSimpleName();
    private static final List<Pair<String, String>> preferred_algorithms = Arrays.asList(
            new Pair<>("sha1", "SHA-1"),
            new Pair<>("md5", "MD5")

    );

    private static String encryptWithAlgorithm(String password, Pair<String, String> algorithm) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance(algorithm.second);
        byte[] result, passBytes;
        try {
            passBytes = password.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            passBytes = password.getBytes();
        }
        result = digest.digest(passBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : result){
            sb.append(String.format("%02x", b));
        }
        return String.format("%s$$%s",
                algorithm.first,
                sb.toString());
    }

    public static String encryptLocalPassword(String password){
        return DigestUtils.sha256Hex(password);
    }

    public static String encryptExternalPassword(String password)  {

        String hashed = "";
        for (Pair<String, String> algorithm : preferred_algorithms){
            try {
                hashed = encryptWithAlgorithm(password, algorithm);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return hashed;

    }

     /*
    //Needs SpongyCastle in gradle

    private static final int SEED_BYTES = 15;
    private static final int ITERATION_COUNT = 4096;

    public static String encryptDjangoPassword(String password){
        String hashed = "";
        SecureRandom rng = new SecureRandom();
        byte[] salt = rng.generateSeed(SEED_BYTES);
        // Django passwords follow this format: <algorithm>$<iterations>$<salt>$<hash>

        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());

        gen.init(password.getBytes(), salt, ITERATION_COUNT);
        KeyParameter dk = (KeyParameter) gen.generateDerivedParameters(256);
        byte[] encoded = dk.getKey();
        String formatted = String.format("%s$%d$%s$%s",
                "pbkdf2_sha256", //Algorithm
                ITERATION_COUNT, //iterations
                Hex.encodeHex(salt), //salt
                Hex.encodeHex(encoded)); //hash

        return hashed;
    }
    */
}
