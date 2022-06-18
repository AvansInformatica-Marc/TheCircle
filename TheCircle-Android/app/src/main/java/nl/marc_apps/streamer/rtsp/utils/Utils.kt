package nl.marc_apps.streamer.rtsp.utils

import android.util.Base64
import java.nio.ByteBuffer
import java.util.*
import java.util.regex.Matcher

fun ByteArray.setLong(n: Long, range: IntRange) {
    var value = n
    for (i in range.reversed()) {
        this[i] = (value % 256).toByte()
        value = value shr 8
    }
}

fun ByteArray.encodeToString(flags: Int = Base64.DEFAULT): String {
    return Base64.encodeToString(this, flags)
}

fun ByteBuffer.getVideoStartCodeSize(): Int {
    val areFirstTwoBytesZero = this[0].toInt() == 0x00 && this[1].toInt() == 0x00
    return when {
        areFirstTwoBytesZero && this[2].toInt() == 0x00 && this[3].toInt() == 0x01 -> 4
        areFirstTwoBytesZero && this[2].toInt() == 0x01 -> 3
        else -> 0
    }
}

operator fun Matcher.get(index: Int): String? {
    return if (find()) group(index) else null
}

const val NTP_UNIX_TIMESTAMP_DIFFERENCE = -2208988800000L

val Date.ntpSeconds: UInt
    get() = ((time - NTP_UNIX_TIMESTAMP_DIFFERENCE) / 1000).toUInt()
