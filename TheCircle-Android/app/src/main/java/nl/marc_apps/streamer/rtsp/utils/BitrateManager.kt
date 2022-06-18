package nl.marc_apps.streamer.rtsp.utils

import kotlin.time.Duration.Companion.milliseconds

open class BitrateManager(
    private val onBitrateChanged: ((Long) -> Unit)? = null
) {
    private var bitrate: Long = 0

    private var timeStamp = System.currentTimeMillis()

    @Synchronized
    fun calculateBitrate(size: Long) {
        bitrate += size
        val timeDiff = (System.currentTimeMillis() - timeStamp).milliseconds
        if (timeDiff >= TIME_DIFFERENCE_CALLBACK_TRIGGER.milliseconds) {
            val calculatedBitrate = bitrate / timeDiff.inWholeSeconds
            onBitrateChanged?.invoke(calculatedBitrate)
            timeStamp = System.currentTimeMillis()
            bitrate = 0
        }
    }

    companion object {
        private const val TIME_DIFFERENCE_CALLBACK_TRIGGER = 1000
    }
}
