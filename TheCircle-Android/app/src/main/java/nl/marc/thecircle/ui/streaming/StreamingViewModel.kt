package nl.marc.thecircle.ui.streaming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.marc.thecircle.data.ChatRepository
import nl.marc.thecircle.data.StreamRepository
import nl.marc.thecircle.data.UserRepository
import nl.marc.thecircle.domain.Message
import nl.marc.thecircle.domain.Stream
import nl.marc.thecircle.domain.User
import nl.marc_apps.streamer.rtsp.Protocol
import nl.marc_apps.streamer.rtsp.RtspClient
import nl.marc_apps.streamer.rtsp.RtspLoggingLevel

class StreamingViewModel(
    private val userRepository: UserRepository,
    private val streamRepository: StreamRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {
    private var stream: Stream? = null

    private val mutableRtspClient = MutableStateFlow<RtspClient?>(null)

    val rtspClient: StateFlow<RtspClient?>
        get() = mutableRtspClient

    val messageList: Flow<Map<Message, User?>> = chatRepository.messageFlow.map {
        val map = mutableMapOf<Message, User?>()
        for (message in it) {
            map[message] = userRepository.getUserById(message.senderId)
        }
        map
    }

    private val mutableCurrentUserId = MutableStateFlow<String?>(null)

    val currentUserId: Flow<String?>
        get() = mutableCurrentUserId

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

    fun loadChat() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableCurrentUserId.value = chatRepository.getCurrentUserId()
        }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.sendMessage(message)
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
