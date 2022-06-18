package nl.marc_apps.streamer.rtsp

data class RtspFrame constructor(
    val buffer: ByteArray,
    val timeStamp: Long,
    val length: Int,
    val channelIdentifier: Int,
    val frameType: FrameType
) {
    enum class FrameType {
        AUDIO, VIDEO
    }
}
