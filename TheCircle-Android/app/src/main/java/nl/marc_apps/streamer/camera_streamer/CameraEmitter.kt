package nl.marc_apps.streamer.camera_streamer

interface CameraEmitter {
    fun isStreaming(): Boolean

    fun stopStream()

    fun startPreview()
}
