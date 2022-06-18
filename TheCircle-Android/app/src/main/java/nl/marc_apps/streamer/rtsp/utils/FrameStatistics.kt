package nl.marc_apps.streamer.rtsp.utils

import nl.marc_apps.streamer.rtsp.RtspFrame

data class FrameStatistics(
    private var audioFramesSent: Int = 0,
    private var videoFramesSent: Int = 0,
    private var droppedAudioFrames: Int = 0,
    private var droppedVideoFrames: Int = 0
) {
    fun getSendFrameCount(type: RtspFrame.FrameType? = null): Int {
        return when (type) {
            RtspFrame.FrameType.AUDIO -> audioFramesSent
            RtspFrame.FrameType.VIDEO -> videoFramesSent
            null -> audioFramesSent + videoFramesSent
        }
    }

    fun onFrameSent(type: RtspFrame.FrameType) {
        when(type) {
            RtspFrame.FrameType.AUDIO -> audioFramesSent++
            RtspFrame.FrameType.VIDEO -> videoFramesSent++
        }
    }

    fun getDroppedFrameCount(type: RtspFrame.FrameType? = null): Int {
        return when (type) {
            RtspFrame.FrameType.AUDIO -> droppedAudioFrames
            RtspFrame.FrameType.VIDEO -> droppedVideoFrames
            null -> droppedAudioFrames + droppedVideoFrames
        }
    }

    fun onFrameDropped(type: RtspFrame.FrameType) {
        when(type) {
            RtspFrame.FrameType.AUDIO -> droppedAudioFrames++
            RtspFrame.FrameType.VIDEO -> droppedVideoFrames++
        }
    }

    fun resetStatistics() {
        resetSendFrames(RtspFrame.FrameType.AUDIO)
        resetSendFrames(RtspFrame.FrameType.VIDEO)
        resetDroppedFrames(RtspFrame.FrameType.AUDIO)
        resetDroppedFrames(RtspFrame.FrameType.VIDEO)
    }

    fun resetSendFrames(type: RtspFrame.FrameType) {
        when (type) {
            RtspFrame.FrameType.AUDIO -> audioFramesSent = 0
            RtspFrame.FrameType.VIDEO -> videoFramesSent = 0
        }
    }

    fun resetDroppedFrames(type: RtspFrame.FrameType) {
        when (type) {
            RtspFrame.FrameType.AUDIO -> droppedAudioFrames = 0
            RtspFrame.FrameType.VIDEO -> droppedVideoFrames = 0
        }
    }
}
