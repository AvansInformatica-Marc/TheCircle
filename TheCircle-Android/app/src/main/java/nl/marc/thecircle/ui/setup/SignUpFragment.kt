package nl.marc.thecircle.ui.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import nl.marc.thecircle.R
import nl.marc.thecircle.databinding.FragmentSignupBinding
import org.koin.androidx.navigation.koinNavGraphViewModel

class SignUpFragment : Fragment() {
    lateinit var binding: FragmentSignupBinding

    private val viewModel by koinNavGraphViewModel<SignUpViewModel>(R.id.signup_fragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.hasSignedUp.collect {
                    if (it) {
                        findNavController().navigate(
                            SignUpFragmentDirections.fragmentSignupToStreaming()
                        )
                    }
                }
            }
        }

        binding.actionSignup.setOnClickListener {
            binding.inputUsername.editText?.text?.toString()?.let {
                viewModel.signup(it)
            }
        }
    }
}
