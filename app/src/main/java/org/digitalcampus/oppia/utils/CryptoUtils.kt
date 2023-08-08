package org.digitalcampus.oppia.utils

import android.util.Log
import androidx.core.util.Pair
import org.digitalcampus.oppia.analytics.Analytics
import org.jarjar.apache.commons.codec.digest.DigestUtils
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object CryptoUtils {
    private val TAG = CryptoUtils::class.simpleName
    private val preferredAlgorithms = listOf(
            Pair("sha1", "SHA-1"),
            Pair("md5", "MD5")
    )

    private fun encryptWithAlgorithm(password: String, algorithm: Pair<String, String>): String {
        val digest = MessageDigest.getInstance(algorithm.second)
        val result = digest.digest(password.toByteArray(StandardCharsets.UTF_8))
        val sb = StringBuilder()
        for (b in result) {
            sb.append(String.format("%02x", b))
        }
        return "${algorithm.first}$$$sb"
    }

    @JvmStatic
    fun encryptLocalPassword(password: String): String {
        return DigestUtils.sha256Hex(password)
    }

    @JvmStatic
    fun encryptExternalPassword(password: String): String {
        for (algorithm in preferredAlgorithms) {
            try {
                return encryptWithAlgorithm(password, algorithm)
            } catch (e: NoSuchAlgorithmException) {
                Analytics.logException(e)
                Log.d(TAG, "NoSuchAlgorithmException:", e)
            }
        }
        return ""
    }

}