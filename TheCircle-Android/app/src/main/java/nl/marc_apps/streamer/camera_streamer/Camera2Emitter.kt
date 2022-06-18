package nl.marc_apps.streamer.camera_streamer

import android.content.Context
import android.media.MediaCodec
import com.pedro.encoder.utils.CodecUtil
import com.pedro.rtplibrary.view.LightOpenGlView
import com.pedro.rtplibrary.view.OpenGlView
import nl.marc_apps.streamer.rtsp.RtspClient
import nl.marc_apps.streamer.rtsp.VideoCodec
import nl.marc_apps.streamer.rtsp.utils.FrameStatistics
import java.nio.ByteBuffer

class Camera2Emitter : Camera2Base, CameraEmitter {
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

    override fun getFrameStatistics(): FrameStatistics {
        return rtspClient.frameStatistics
    }

    fun setVideoCodec(videoCodec: VideoCodec) {
        recordController.setVideoMime(
            if (videoCodec === VideoCodec.H265) CodecUtil.H265_MIME else CodecUtil.H264_MIME
        )
        videoEncoder.type =
            if (videoCodec === VideoCodec.H265) CodecUtil.H265_MIME else CodecUtil.H264_MIME
    }

    override fun prepareAudioRtp(isStereo: Boolean, sampleRate: Int) {
        rtspClient.setAudioInfo(sampleRate, isStereo)
    }

    override fun stopStreamRtp() {
        rtspClient.close()
    }

    override fun getAacDataRtp(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        rtspClient.sendAudio(aacBuffer, info)
    }

    override fun onSpsPpsVpsRtp(sps: ByteBuffer, pps: ByteBuffer, vps: ByteBuffer?) {
        rtspClient.setVideoInfo(sps, pps, vps)
    }

    override fun getH264DataRtp(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        rtspClient.sendVideo(h264Buffer, info)
    }
}
