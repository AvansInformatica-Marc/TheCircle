package nl.marc_apps.streamer.rtsp.utils

import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object AuthUtil {
    @JvmStatic
    fun getMd5Hash(buffer: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            return bytesToHex(md.digest(buffer.toByteArray()))
        } catch (ignore: NoSuchAlgorithmException) {
            ""
        } catch (ignore: UnsupportedEncodingException) {
            ""
        }
    }

    private fun bytesToHex(raw: ByteArray): String {
        return raw.joinToString("") { "%02x".format(it) }
    }
}
