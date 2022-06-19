package nl.marc.thecircle.ui.setup

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import nl.marc.thecircle.R
import nl.marc.thecircle.databinding.FragmentPermissionsBinding
import nl.marc.thecircle.utils.observe
import org.koin.androidx.navigation.koinNavGraphViewModel

class PermissionsFragment : Fragment() {
    private lateinit var binding: FragmentPermissionsBinding

    private val viewModel by koinNavGraphViewModel<PermissionsViewModel>(R.id.permissions_fragment)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (it.all { (_, isGranted) -> isGranted }) {
            onPermissionsGranted()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPermissionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            view.context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val hasMicrophonePermission = ContextCompat.checkSelfPermission(
            view.context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCameraPermission && hasMicrophonePermission) {
            onPermissionsGranted()
        } else {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.INTERNET,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ))
        }
    }

    private fun onPermissionsGranted() {
        viewModel.hasSignedUp.observe(viewLifecycleOwner) {
            when (it) {
                true -> findNavController().navigate(PermissionsFragmentDirections.fragmentPermissionsToStreaming())
                false -> findNavController().navigate(PermissionsFragmentDirections.fragmentPermissionsToSignup())
                null -> {}
            }
        }
    }
}
