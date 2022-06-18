package nl.marc_apps.streamer.camera_streamer

import android.content.Context
import android.media.MediaCodec
import com.pedro.encoder.utils.CodecUtil
import com.pedro.rtplibrary.base.Camera1Base
import com.pedro.rtplibrary.view.LightOpenGlView
import com.pedro.rtplibrary.view.OpenGlView
import nl.marc_apps.streamer.rtsp.RtspClient
import nl.marc_apps.streamer.rtsp.VideoCodec
import nl.marc_apps.streamer.rtsp.utils.FrameStatistics
import java.nio.ByteBuffer

class Camera1Emitter : Camera1Base, CameraEmitter {
    private var rtspClient: RtspClient

    constructor(openGlView: OpenGlView?, rtspClient: RtspClient) : super(openGlView) {
        this.rtspClient = rtspClient
    }

    constructor(lightOpenGlView: LightOpenGlView?, rtspClient: RtspClient) : super(lightOpenGlView) {
        this.rtspClient = rtspClient
    }

    constructor(context: Context, rtspClient: RtspClient) : super(context) {
        this.rtspClient = rtspClient
    }

    fun getFrameStatistics(): FrameStatistics {
        return rtspClient.frameStatistics
    }

    fun setVideoCodec(videoCodec: VideoCodec) {
        recordController.setVideoMime(
            if (videoCodec === VideoCodec.H265) CodecUtil.H265_MIME else CodecUtil.H264_MIME
        )
        videoEncoder.type =
            if (videoCodec === VideoCodec.H265) CodecUtil.H265_MIME else CodecUtil.H264_MIME
    }

    override fun setAuthorization(user: String?, password: String?) {}

    override fun prepareAudioRtp(isStereo: Boolean, sampleRate: Int) {
        rtspClient.setAudioInfo(sampleRate, isStereo)
    }

    override fun startStreamRtp(url: String?) {}

    override fun stopStreamRtp() {
        rtspClient.close()
    }

    override fun shouldRetry(reason: String?) = false

    override fun setReTries(reTries: Int) {}

    override fun reConnect(delay: Long, backupUrl: String?) {}

    override fun hasCongestion() = false

    override fun resizeCache(newSize: Int) {}

    override fun getCacheSize() = 0

    override fun getSentAudioFrames() = 0L

    override fun getSentVideoFrames() = 0L

    override fun getDroppedAudioFrames() = 0L

    override fun getDroppedVideoFrames() = 0L

    override fun resetSentAudioFrames() {}

    override fun resetSentVideoFrames() {}

    override fun resetDroppedAudioFrames() {}

    override fun resetDroppedVideoFrames() {}

    override fun getAacDataRtp(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        rtspClient.sendAudio(aacBuffer, info)
    }

    override fun onSpsPpsVpsRtp(sps: ByteBuffer, pps: ByteBuffer, vps: ByteBuffer?) {
        rtspClient.setVideoInfo(sps, pps, vps)
    }

    override fun getH264DataRtp(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        rtspClient.sendVideo(h264Buffer, info)
    }

    override fun setLogs(enable: Boolean) {}

    override fun setCheckServerAlive(enable: Boolean) {}
}
