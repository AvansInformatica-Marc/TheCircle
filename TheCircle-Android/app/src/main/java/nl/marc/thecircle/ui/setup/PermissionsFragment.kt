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
import androidx.navigation.fragment.findNavController
import nl.marc.thecircle.databinding.FragmentPermissionsBinding

class PermissionsFragment : Fragment() {
    private lateinit var binding: FragmentPermissionsBinding

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
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ))
        }
    }

    private fun onPermissionsGranted() {
        findNavController().navigate(PermissionsFragmentDirections.fragmentPermissionsToStreaming())
    }
}
