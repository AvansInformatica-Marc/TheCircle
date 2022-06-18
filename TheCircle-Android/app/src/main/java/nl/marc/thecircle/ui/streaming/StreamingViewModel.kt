package nl.marc.thecircle.ui.streaming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import nl.marc.thecircle.BuildConfig
import nl.marc.thecircle.data.StreamRepository
import nl.marc.thecircle.data.UserRepository
import nl.marc.thecircle.data.api.Stream
import nl.marc_apps.streamer.rtsp.Protocol
import nl.marc_apps.streamer.rtsp.RtspClient
import nl.marc_apps.streamer.rtsp.RtspLoggingLevel

class StreamingViewModel(
    private val userRepository: UserRepository,
    private val streamRepository: StreamRepository
) : ViewModel() {
    private var stream: Stream? = null

    private val mutableRtspClient = MutableStateFlow<RtspClient?>(null)

    val rtspClient: StateFlow<RtspClient?>
        get() = mutableRtspClient

    fun registerStream() {
        viewModelScope.launch(Dispatchers.IO) {
            if(!userRepository.isRegistered()) {
                userRepository.register("TestUser")
            }

            val stream = streamRepository.registerStream()
            this@StreamingViewModel.stream = stream

            mutableRtspClient.value = RtspClient.create(stream.rtspUrl) {
                protocol = Protocol.TCP
                streamType = RtspClient.StreamType.BOTH
                loggingLevel = RtspLoggingLevel.HEADERS
            }
        }
    }

    fun connect() {
        viewModelScope.launch(Dispatchers.IO) {
            rtspClient.value?.connect()
        }
    }

    fun close() {
        viewModelScope.launch(Dispatchers.IO) {
            stream?.let {
                streamRepository.deleteStream(it)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            rtspClient.value?.closeAwait()
        }
    }

    override fun onCleared() {
        close()
        super.onCleared()
    }
}
