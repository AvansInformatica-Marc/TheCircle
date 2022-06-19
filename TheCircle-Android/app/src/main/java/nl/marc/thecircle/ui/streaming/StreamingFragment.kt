package nl.marc.thecircle.ui.streaming

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.marc.thecircle.R
import nl.marc.thecircle.databinding.FragmentStreamingBinding
import nl.marc.thecircle.utils.observe
import nl.marc_apps.streamer.camera_streamer.Camera1Emitter
import nl.marc_apps.streamer.camera_streamer.Camera2Emitter
import nl.marc_apps.streamer.camera_streamer.CameraEmitter
import nl.marc_apps.streamer.rtsp.RtspClient
import nl.marc_apps.streamer.rtsp.VideoCodec
import org.koin.androidx.navigation.koinNavGraphViewModel

class StreamingFragment : Fragment(), SurfaceHolder.Callback {
    private val viewModel by koinNavGraphViewModel<StreamingViewModel>(R.id.streaming_fragment)

    private lateinit var binding: FragmentStreamingBinding

    private var camera: CameraEmitter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStreamingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.videoView.holder.addCallback(this)

        binding.videoView.setOnClickListener {
            camera?.switchCamera()
        }

        binding.actionSend.setOnClickListener {
            binding.inputMessage.editText?.text?.toString()?.let {
                if (it.isNotBlank()) {
                    viewModel.sendMessage(it)
                }
            }
            binding.inputMessage.editText?.setText("")
        }

        viewModel.currentUserId.observe(viewLifecycleOwner) {
            if (it != null) {
                val chatAdapter = ChatAdapter(it)
                binding.listMessages.adapter = chatAdapter
            }
        }

        viewModel.messageList.observe(viewLifecycleOwner) {
            val adapter = binding.listMessages.adapter as? ChatAdapter
            adapter?.submitList(it.toList())
        }

        viewModel.rtspClient.observe(viewLifecycleOwner) {
            setupStream()
        }

        viewModel.registerStream()
        viewModel.loadChat()
    }

    override fun onDestroyView() {
        lifecycleScope.launch {
            camera?.stopStream()
        }
        viewModel.close()

        super.onDestroyView()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}

    private val streamingMutex = Mutex()

    private var surfaceAvailable = false

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        camera?.startPreview()
        surfaceAvailable = true
        viewLifecycleOwner.lifecycleScope.launch {
            setupStream()
        }
    }

    private suspend fun setupStream() {
        if (surfaceAvailable) {
            streamingMutex.withLock {
                val rtspClient = viewModel.rtspClient.value
                if (camera?.isStreaming() != true && rtspClient != null) {
                    if (!setupCamera2(rtspClient)) {
                        this@StreamingFragment.camera?.stopStream()
                        setupCamera1(rtspClient)
                    }

                    viewModel.connect()
                }
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        surfaceAvailable = false

        lifecycleScope.launch {
            camera?.stopStream()
        }
        viewModel.close()
    }

    private fun setupCamera2(rtspClient: RtspClient): Boolean {
        try {
            val camera = Camera2Emitter(binding.videoView, rtspClient)
            this@StreamingFragment.camera = camera
            camera.setVideoCodec(VideoCodec.H264)
            camera.switchCamera()
            if (camera.prepareAudio() && camera.prepareVideo()) {
                camera.startStream()
                return true
            }
        } catch (error: Throwable) {
            error.printStackTrace()
        }

        return false
    }

    private fun setupCamera1(rtspClient: RtspClient): Boolean {
        try {
            val camera = Camera1Emitter(binding.videoView, rtspClient)
            this@StreamingFragment.camera = camera
            camera.setVideoCodec(VideoCodec.H264)
            camera.switchCamera()
            if (camera.prepareAudio() && camera.prepareVideo()) {
                camera.startStream("")
                return true
            }
        } catch (error: Throwable) {
            error.printStackTrace()
        }

        return false
    }
}
