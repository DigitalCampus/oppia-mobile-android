package org.digitalcampus.oppia.utils;

import androidx.core.util.Pair;
import android.util.Log;

import com.splunk.mint.Mint;

import org.jarjar.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
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

    private CryptoUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static String encryptWithAlgorithm(String password, Pair<String, String> algorithm) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance(algorithm.second);
        byte[] result;
        result = digest.digest(password.getBytes(StandardCharsets.UTF_8));
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
                return encryptWithAlgorithm(password, algorithm);
            } catch (NoSuchAlgorithmException e) {
                Mint.logException(e);
                Log.d(TAG, "NoSuchAlgorithmException:", e);
            }
        }

        return hashed;

    }
}
