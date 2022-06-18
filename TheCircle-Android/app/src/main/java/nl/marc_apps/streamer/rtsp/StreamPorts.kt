package nl.marc_apps.streamer.rtsp

data class StreamPorts(
    val streamPort: Int,
    val streamStateReportPort: Int = streamPort + 1,
    val type: RtspFrame.FrameType
)
